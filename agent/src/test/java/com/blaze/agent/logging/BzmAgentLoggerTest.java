/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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