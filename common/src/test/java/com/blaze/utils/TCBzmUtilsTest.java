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

public class TCBzmUtilsTest {

    @Test
    public void testFlow() throws Exception {
        TCBzmUtils utils = new TCBzmUtils("id", "secret", "address", null, null);

        String url = utils.modifyRequestUrl("http://blazedemo.com/");
        assertTrue(url, url.contains("?app_key=jnk100x987c06f4e10c4&_clientId=CI_TEAMCITY&_clientVersion="));

        url = utils.modifyRequestUrl("http://blazedemo.com/?param=val");
        assertTrue(url, url.contains("&app_key=jnk100x987c06f4e10c4&_clientId=CI_TEAMCITY&_clientVersion="));
    }
}