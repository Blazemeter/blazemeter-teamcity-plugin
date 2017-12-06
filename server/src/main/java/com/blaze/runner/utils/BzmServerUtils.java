package com.blaze.runner.utils;

import com.blaze.runner.logging.BzmServerLogging;
import com.blaze.runner.logging.BzmServerNotifier;
import com.blaze.utils.TCBzmUtils;

public class BzmServerUtils extends TCBzmUtils {

    public BzmServerUtils(String apiKeyId, String apiKeySecret, String address) {
        super(apiKeyId, apiKeySecret, address, new BzmServerNotifier(), new BzmServerLogging());
    }

    public BzmServerUtils() {
        super("", "", "", new BzmServerNotifier(), new BzmServerLogging());
    }
}
