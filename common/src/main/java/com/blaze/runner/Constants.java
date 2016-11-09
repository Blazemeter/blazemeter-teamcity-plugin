/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blaze.runner;

public interface Constants {
	

    //runner properties
	String RUNNER_DISPLAY_NAME = "BlazeMeter";
	String RUNNER_TYPE = "BlazeMeter";
	String USER_KEY = "USER_KEY";
	String BLAZEMETER_URL = "BLAZEMETER_URL";
    String DEFAULT_BZM_SERVER="https://a.blazemeter.com";
	String UNKNOWN_TYPE="unknown_type";

	//settings properties
	String SETTINGS_ALL_TESTS_ID = "all_tests";
	String SETTINGS_JUNIT = "blazeMeterPlugin.request.junit";
	String SETTINGS_JUNIT_PATH = "blazeMeterPlugin.request.junit.path";
	String SETTINGS_JTL = "blazeMeterPlugin.request.jtl";
	String SETTINGS_JTL_PATH = "blazeMeterPlugin.request.jtl.path";
	String SETTINGS_NOTES = "blazeMeterPlugin.notes";
	String SETTINGS_JMETER_PROPERTIES = "blazeMeterPlugin.jmeter.properties";
	String BZM_PROPERTIES_FILE="/userKeyFile.properties";
    String NOT_IMPLEMENTED="This call is not implemented.";

	String NO_TEST_WAS_DEFINED="No test was defined in the configuration page.";
	String REPORT_URL="reportUrl_";
    String PROBLEM_WITH_VALIDATING="Problem with validating testId, serverUrl, userKey";
	String PROXY_HOST="http.proxyHost";
	String PROXY_PORT="http.proxyPort";
	String PROXY_USER="http.proxyUser";
	String PROXY_PASS="http.proxyPass";
	String HTTP_LOG="http-log";
	String THREE_DOTS ="...";

}
