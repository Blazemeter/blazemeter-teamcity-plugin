package com.blaze.utils;

import com.blazemeter.api.logging.Logger;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class TCBzmLogger implements Logger {

    private java.util.logging.Logger logger = java.util.logging.Logger.getLogger("bzm-log");
    private FileHandler fileHandler;

    public TCBzmLogger(String httpLogFile) throws IOException {
        fileHandler = new FileHandler(httpLogFile);
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
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
