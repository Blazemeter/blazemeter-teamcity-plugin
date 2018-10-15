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