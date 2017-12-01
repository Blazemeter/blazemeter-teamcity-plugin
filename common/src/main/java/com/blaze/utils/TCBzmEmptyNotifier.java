package com.blaze.utils;

import com.blazemeter.api.logging.UserNotifier;

public class TCBzmEmptyNotifier implements UserNotifier {
    @Override
    public void notifyAbout(String s) {
        // NOOP
    }
}
