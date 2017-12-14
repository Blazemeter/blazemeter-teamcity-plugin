package com.blaze.runner.utils;

import com.blaze.runner.logging.BzmServerLogging;
import com.blaze.runner.logging.BzmServerNotifier;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestsUtilsTest {

    @Test
    public void testFlow() throws Exception {
        BzmServerUtils utils = new BzmServerUtils();
        assertNotNull(utils);
        utils = new BzmServerUtils("id", "secret", "addrr");
        assertNotNull(utils);
        assertTrue(utils.getLogger() instanceof BzmServerLogging);
        assertTrue(utils.getNotifier() instanceof BzmServerNotifier);
    }
}