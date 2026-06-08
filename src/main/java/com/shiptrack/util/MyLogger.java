package com.shiptrack.util;
// Handles logging.
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class MyLogger {

    private MyLogger() {
    }
    // Using java.util.logging.Logger for logging
    private final static Logger LOGGER = Logger.getLogger(MyLogger.class.getName());

    static {
        try {
            // Creating a file handler to write logs to logfile.log
            FileHandler fileHandler = new FileHandler("logfile.log", true);  // Append to the log file
            fileHandler.setFormatter(new SimpleFormatter()); // Set a simple text formatter for the log messages
            LOGGER.addHandler(fileHandler); // Add the file handler to the logger
            LOGGER.setUseParentHandlers(false);  // Disable console output for logging to avoid duplicate logs in the console
        } catch (IOException | SecurityException e) { // Handle exceptions that may occur during logger setup, such as issues with file access permissions or IO errors
            System.out.println("Logger setup failed: " + e.getMessage());
        }
    }

    // Method to write log messages of type INFO
    // This method can be called from anywhere in the application to log informational messages, such as successful operations or important events that are not errors.
    public static void writeToLog(String msg) {
        LOGGER.log(Level.INFO, msg);
    }

    // Method to write log messages of type WARNING along with exception details
    // This method can be called from anywhere in the application to log warning messages, such as potential issues or conditions that may lead to errors.
    public static void writeToLog(String msg, Exception e) {
        LOGGER.log(Level.WARNING, msg, e);
    }
}