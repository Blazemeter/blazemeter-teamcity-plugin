package com.blaze.runner.utils;

import com.blaze.runner.logging.BzmServerLogging;
import com.blaze.runner.logging.BzmUserNotifier;
import com.blaze.utils.TCBzmUtils;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;

public class BzmServerUtils extends TCBzmUtils {

    public BzmServerUtils(String apiKeyId, String apiKeySecret, String address, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, notifier, logger);
    }

    public BzmServerUtils() {
        super("", "", "", new BzmUserNotifier(), new BzmServerLogging());
    }
}
