package com.blaze.agent.logging;


import com.blaze.runner.Constants;
import jetbrains.buildServer.RunBuildException;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

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
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(false);
    }

    public void close() {
        fileHandler.close();
    }


    @Override
    public void debug(String s) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public void info(String s) {

    }

    @Override
    public void info(String s, Throwable throwable) {

    }

    @Override
    public void warn(String s) {

    }

    @Override
    public void warn(String s, Throwable throwable) {

    }

    @Override
    public void error(String s) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }
}
