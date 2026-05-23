package com.shiptrack.util;

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
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);  // Disable console output for logging (optional)
        } catch (IOException | SecurityException e) {
            System.out.println("Logger setup failed: " + e.getMessage());
        }
    }

    // Method to write log messages of type INFO
    public static void writeToLog(String msg) {
        LOGGER.log(Level.INFO, msg);
    }

    // Method to write log messages of type WARNING along with exception details
    public static void writeToLog(String msg, Exception e) {
        LOGGER.log(Level.WARNING, msg, e);
    }
}