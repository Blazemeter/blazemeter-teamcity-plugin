package com.blaze.runner.logging;

import org.junit.Test;

import static org.junit.Assert.*;

public class BzmServerNotifierTest {

    @Test
    public void testFlow() throws Exception {
        BzmServerNotifier notifier = new BzmServerNotifier();

        notifier.notifyInfo("info");
        notifier.notifyWarning("warn");
        notifier.notifyError("error");
        assertNotNull(notifier);
    }
}