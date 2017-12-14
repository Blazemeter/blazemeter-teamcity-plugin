package com.blaze.runner.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of `com.blazemeter.api.logging.Logger`
 */
public class BzmServerLogging implements com.blazemeter.api.logging.Logger {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    @Override
    public void debug(String s) {
        logger.debug(s);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        logger.debug(s, throwable);
    }

    @Override
    public void info(String s) {
        logger.info(s);
    }

    @Override
    public void info(String s, Throwable throwable) {
        logger.info(s, throwable);
    }

    @Override
    public void warn(String s) {
        logger.warn(s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        logger.warn(s, throwable);
    }

    @Override
    public void error(String s) {
        logger.error(s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        logger.error(s, throwable);
    }
}
