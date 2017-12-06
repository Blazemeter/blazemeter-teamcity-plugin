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
    public void notifyAbout(String s) {
        logger.info("User notification: " + s);
    }
}
