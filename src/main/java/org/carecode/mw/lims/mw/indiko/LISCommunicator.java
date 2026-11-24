package org.carecode.mw.lims.mw.indiko;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.carecode.lims.libraries.DataBundle;
import org.carecode.lims.libraries.QueryRecord;
import org.carecode.lims.libraries.ResultsRecord;

public class LISCommunicator {

    private static final Gson gson = new Gson();
    private static final Logger logger = LogManager.getLogger(IndikoServer.class);

    public static DataBundle pullTestOrdersForSampleRequests(QueryRecord queryRecord) {
        logger.info("Pulling Test Orders from LIS for " + queryRecord);
        try {
            String postSampleDataEndpoint = SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl();
            URL url = new URL(postSampleDataEndpoint + "/test_orders_for_sample_requests");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            DataBundle databundle = new DataBundle();
            databundle.setMiddlewareSettings(SettingsLoader.getSettings());
            databundle.getQueryRecords().add(queryRecord);
            String jsonInputString = gson.toJson(databundle);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = conn.getResponseCode();
            logger.info("responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("response.toString() = " + response.toString());
                // Convert the response to a PatientDataBundle object
                DataBundle patientDataBundle = gson.fromJson(response.toString(), DataBundle.class);
                System.out.println("patientDataBundle = " + patientDataBundle);
                return patientDataBundle;
            } else {
                System.out.println("POST request failed. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void pushResults(DataBundle dataBundle) {
        System.out.println("pushResults = ");
        try {
            System.out.println("SettingsLoader.getSettings() = " + SettingsLoader.getSettings());
            System.out.println("SettingsLoader.getSettings().getLimsSettings() = " + SettingsLoader.getSettings().getLimsSettings());
            System.out.println("SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl() = " + SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl());
            String pushResultsEndpoint = SettingsLoader.getSettings().getLimsSettings().getLimsServerBaseUrl() + "/test_results";

            for (ResultsRecord rr : dataBundle.getResultsRecords()) {
                System.out.println("rr value  = " + rr.getResultValue() + "");
                System.out.println("rr value string = " + rr.getResultValueString());
            }

            URL url = new URL(pushResultsEndpoint);
            System.out.println("url = " + url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            // Serialize PatientDataBundle to JSON
            dataBundle.setMiddlewareSettings(SettingsLoader.getSettings());
            String jsonInputString = gson.toJson(dataBundle);
            System.out.println("jsonInputString = " + jsonInputString);
            // Send the JSON in the request body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode = " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("ok");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                

                JsonObject responseObject = JsonParser.parseString(response.toString()).getAsJsonObject();


                String status = responseObject.get("status").getAsString();
                logger.info("Status: " + status);

                Gson gson = new Gson();
                JsonArray detailsArray = responseObject.getAsJsonArray("details");

                List<ResultsRecord> resultsRecords = new ArrayList<>();
                for (JsonElement element : detailsArray) {
                    ResultsRecord record = gson.fromJson(element, ResultsRecord.class);
                    resultsRecords.add(record);
                }

                for (ResultsRecord record : resultsRecords) {
                    logger.info("Sample ID: " + record.getSampleId()
                            + ", Test: " + record.getTestCode()
                            + ", Status: " + record.getStatus());
                }

            } else {
                logger.error("Failed : " + dataBundle);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
