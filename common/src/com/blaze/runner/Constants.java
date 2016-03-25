package com.blaze.runner;

public interface Constants {
	

    //runner properties
	String RUNNER_DISPLAY_NAME = "BlazeMeter";
	String RUNNER_TYPE = "BlazeMeter";
	String USER_KEY = "USER_KEY";
	String BLAZEMETER_URL = "BLAZEMETER_URL";
    String DEFAULT_BZM_SERVER="https://a.blazemeter.com";

	//settings properties
	String SETTINGS_ALL_TESTS_ID = "all_tests";
	String BZM_PROPERTIES_FILE="/userKeyFile.properties";
    String NOT_IMPLEMENTED="This call is not implemented.";

	String REPORT_URL="reportUrl_";

	String PROXY_HOST="http.proxyHost";
	String PROXY_PORT="http.proxyPort";
	String PROXY_USER="http.proxyUser";
	String PROXY_PASS="http.proxyPassword";
	String USE_PROXY="http.useProxy";
}
