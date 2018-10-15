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