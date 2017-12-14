package com.blaze.runner.logging;

import com.blazemeter.api.logging.UserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of `com.blazemeter.api.logging.UserNotifier`
 */
public class BzmServerNotifier implements UserNotifier {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    @Override
    public void notifyInfo(String info) {
        logger.info("User notification: " + info);
    }

    @Override
    public void notifyWarning(String warn) {
        logger.warn("User notification: " + warn);
    }

    @Override
    public void notifyError(String error) {
        logger.error("User notification: " + error);
    }
}
