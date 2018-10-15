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

package com.blaze.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testVersion() throws Exception {
        String version = Utils.version();
        System.out.println(version);
        assertNotNull(version);
        assertFalse("N/A".equals(version));
    }

    @Test
    public void testGetTestId() throws Exception {
        assertEquals("123456", Utils.getTestId("123456.http"));
        assertEquals("123456http", Utils.getTestId("123456http"));
    }
}