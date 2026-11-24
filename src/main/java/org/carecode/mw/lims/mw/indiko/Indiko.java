package org.carecode.mw.lims.mw.indiko;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Indiko {

    private static final Logger logger = LogManager.getLogger(Indiko.class);
    private static JFrame frame;
    private static JLabel statusLabel;
    private static IndikoServer server;
    private static int port;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
        startMiddleware();
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Indiko Middleware Monitor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        statusLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        frame.add(statusLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(e -> restartServer());
        buttonPanel.add(restartButton);

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void startMiddleware() {
        try {
            logger.info("Loading settings...");
            SettingsLoader.loadSettings();
            logger.info("Settings loaded successfully.");
            port = SettingsLoader.getSettings().getAnalyzerDetails().getAnalyzerPort();
            server = new IndikoServer();
            server.start(port);
            statusLabel.setText("Server running on port: " + port);
        } catch (Exception e) {
            logger.error("Failed to start server.", e);
            statusLabel.setText("Failed to start server.");
            JOptionPane.showMessageDialog(frame, "Error starting server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void restartServer() {
        try {
            if (server != null) {
                server.stop(); // Assuming there's a method to stop the server correctly
                logger.info("Server stopped.");
            }
            server = new IndikoServer();
            server.start(port);
            statusLabel.setText("Server restarted on port: " + port);
            logger.info("Server restarted.");
        } catch (Exception e) {
            logger.error("Failed to restart server.", e);
            statusLabel.setText("Failed to restart server.");
            JOptionPane.showMessageDialog(frame, "Error restarting server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
