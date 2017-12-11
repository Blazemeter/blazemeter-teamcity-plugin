package com.blaze.agent.logging;


import jetbrains.buildServer.RunBuildException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Implementation of `com.blazemeter.api.logging.Logger`
 */
public class BzmAgentLogger implements com.blazemeter.api.logging.Logger {

    private Logger logger = Logger.getLogger("bzm-log");
    private FileHandler fileHandler;

    public BzmAgentLogger(String logFile) throws RunBuildException {
        try {
            fileHandler = new FileHandler(logFile);
        } catch (IOException ex) {
            throw new RunBuildException("Cannot create file handler for log file", ex);
        }
        fileHandler.setFormatter(new SimpleFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINE);
    }

    public void close() {
        fileHandler.close();
    }


    @Override
    public void debug(String s) {
        logger.log(Level.FINE, s);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        logger.log(Level.FINE, s, throwable);
    }

    @Override
    public void info(String s) {
        logger.log(Level.INFO, s);
    }

    @Override
    public void info(String s, Throwable throwable) {
        logger.log(Level.INFO, s, throwable);
    }

    @Override
    public void warn(String s) {
        logger.log(Level.WARNING, s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        logger.log(Level.WARNING, s, throwable);
    }

    @Override
    public void error(String s) {
        logger.log(Level.SEVERE, s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        logger.log(Level.SEVERE, s, throwable);
    }
}
