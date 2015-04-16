package com.blaze.runner;

public interface Constants {
	

    //runner properties
	String RUNNER_DESCRIPTION = "BlazeMeter";
	String RUNNER_DISPLAY_NAME = "BlazeMeter";
	String RUNNER_TYPE = "BlazeMeter";
	String BLAZE_METER_STATISTICS_NAME = "BlazeMeterStatistics";
	String USER_KEY = "USER_KEY";
	String BLAZEMETER_URL = "BLAZEMETER_URL";
	String BLAZEMETER_API_VERSION = "BLAZEMETER_API_VERSION";
    String DEFAULT_BZM_SERVER="https://a.blazemeter.com";

	String PROXY_SERVER_NAME = "SERVER_NAME";
	String PROXY_SERVER_PORT = "SERVER_PORT";
	String PROXY_USERNAME = "USERNAME";
	String PROXY_PASSWORD = "PASSWORD";
	
	//settings properties
	String SETTINGS_ALL_TESTS_ID = "all_tests";
	String SETTINGS_ERROR_THRESHOLD_UNSTABLE = "thr_unstable";
	String SETTINGS_ERROR_THRESHOLD_FAIL = "thr_fail";
	String SETTINGS_RESPONSE_TIME_UNSTABLE = "resp_unstable";
	String SETTINGS_RESPONSE_TIME_FAIL = "resp_fail";
	String SETTINGS_TEST_DURATION = "test_duration";
	String SETTINGS_DATA_FOLDER = "data_folder";
	String SETTINGS_MAIN_JMX = "main_jmx";
	String JSON_CONFIGURATION = "json_config";

	//Default properties
	String DEFAULT_SETTINGS_DATA_FOLDER = "DataFolder";
	String BZM_PROPERTIES_FILE="/userKeyFile.properties";
    String NOT_IMPLEMENTED="This call is not implemented.";
    String CREATE_FROM_JSON="create from JSON";
    String NEW_TEST="New test";

    String V2="v2";
	String REPORT_URL="reportUrl_";
}
