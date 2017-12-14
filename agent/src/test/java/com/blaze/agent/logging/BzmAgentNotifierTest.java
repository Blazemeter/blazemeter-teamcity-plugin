package com.blaze.agent.logging;

import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.NullBuildProgressLogger;
import org.junit.Test;

import static org.junit.Assert.*;

public class BzmAgentNotifierTest {

    @Test
    public void testFlow() throws Exception {
        final StringBuilder buffer = new StringBuilder();
        BuildProgressLogger buildProgressLogger = new NullBuildProgressLogger() {

            @Override
            public void message(String message) {
                buffer.append(message);
            }

            @Override
            public void error(String message) {
                buffer.append(message);
            }

            @Override
            public void warning(String message) {
                buffer.append(message);
            }
        };
        BzmAgentNotifier notifier = new BzmAgentNotifier(buildProgressLogger);

        notifier.notifyInfo("info");
        notifier.notifyWarning("warn");
        notifier.notifyError("error");

        assertEquals(13, buffer.length());
    }
}