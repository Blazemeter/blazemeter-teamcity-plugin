/**
 * Copyright 2017 BlazeMeter Inc.
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

package com.blaze.runner;

public interface Constants {

    String RUNNER_DISPLAY_NAME = "BlazeMeter";
    String RUNNER_TYPE = "BlazeMeter";
    String API_KEY_ID = "API_KEY_ID";
    String API_KEY_SECRET = "API_KEY_SECRET";
    String BLAZEMETER_URL = "BLAZEMETER_URL";

    String SETTINGS_ALL_TESTS_ID = "all_tests";
    String SETTINGS_JUNIT = "blazeMeterPlugin.request.junit";
    String SETTINGS_JUNIT_PATH = "blazeMeterPlugin.request.junit.path";
    String SETTINGS_JTL = "blazeMeterPlugin.request.jtl";
    String SETTINGS_JTL_PATH = "blazeMeterPlugin.request.jtl.path";
    String SETTINGS_NOTES = "blazeMeterPlugin.notes";
    String SETTINGS_JMETER_PROPERTIES = "blazeMeterPlugin.jmeter.properties";
    String BZM_PROPERTIES_FILE = "/userKeyFile.properties";
}
