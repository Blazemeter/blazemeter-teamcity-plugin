package com.blaze.agent.logging;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class BzmAgentLoggerTest {

    @Test
    public void testFlow() throws Exception {
        File file = File.createTempFile("tmp-log-file", ".log");

        BzmAgentLogger logger = new BzmAgentLogger(file.getAbsolutePath());
        logger.info("info");
        logger.info("info", new RuntimeException("info"));

        logger.warn("warn");
        logger.warn("warn", new RuntimeException("warn"));

        logger.debug("debug");
        logger.debug("debug", new RuntimeException("debug"));

        logger.error("error");
        logger.error("error", new RuntimeException("error"));

        logger.close();

        assertTrue(file.length() > 0);
    }
}