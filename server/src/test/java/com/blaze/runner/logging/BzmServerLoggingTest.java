package com.blaze.runner.logging;

import org.junit.Test;

import static org.junit.Assert.*;

public class BzmServerLoggingTest {

    @Test
    public void testFlow() throws Exception {
        BzmServerLogging logger = new BzmServerLogging();
        logger.info("info");
        logger.info("info", new RuntimeException("info"));

        logger.warn("warn");
        logger.warn("warn", new RuntimeException("warn"));

        logger.debug("debug");
        logger.debug("debug", new RuntimeException("debug"));

        logger.error("error");
        logger.error("error", new RuntimeException("error"));

        assertNotNull(logger);
    }
}