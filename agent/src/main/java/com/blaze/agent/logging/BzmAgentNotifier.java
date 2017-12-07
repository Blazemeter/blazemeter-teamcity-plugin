package com.blaze.agent.logging;

import com.blazemeter.api.logging.UserNotifier;
import jetbrains.buildServer.agent.BuildProgressLogger;

/**
 * Implementation of com.blazemeter.api.logging.UserNotifier
 */
public class BzmAgentNotifier implements UserNotifier {

    private final BuildProgressLogger logger;

    public BzmAgentNotifier(BuildProgressLogger logger) {
        this.logger = logger;
    }

    @Override
    public void notifyInfo(String info) {
        logger.message(info);
    }

    @Override
    public void notifyWarning(String warning) {
        logger.warning(warning);
    }

    @Override
    public void notifyError(String error) {
        logger.error(error);
    }
}
