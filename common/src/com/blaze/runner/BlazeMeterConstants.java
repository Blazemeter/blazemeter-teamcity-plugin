package com.blaze.runner;

public interface BlazeMeterConstants {
	
    public class TestStatus {
        public static final String Running = "Running";
        public static final String NotRunning = "Not Running";
        public static final String NotFound = "NotFound";
        public static final String Error = "error";
    }
	
    //runner properties
	public final static String RUNNER_DESCRIPTION = "Blaze Meter";
	public final static String RUNNER_DISPLAY_NAME = "Blaze Meter";
	public final static String RUNNER_TYPE = "BlazeMeter";
	public final static String BLAZE_METER_STATISTICS_NAME = "BlazeMeterStatistics";
	public final static String USER_KEY = "USER_KEY";
	
	public final static String PROXY_SERVER_NAME = "SERVER_NAME";
	public final static String PROXY_SERVER_PORT = "SERVER_PORT";
	public final static String PROXY_USERNAME = "USERNAME";
	public final static String PROXY_PASSWORD = "PASSWORD";
	
	//settings properties
	public final static String SETTINGS_ALL_TESTS_ID = "all_tests";
	public final static String SETTINGS_ERROR_THRESHOLD_UNSTABLE = "thr_unstable";
	public final static String SETTINGS_ERROR_THRESHOLD_FAIL = "thr_fail";
	public final static String SETTINGS_RESPONSE_TIME_UNSTABLE = "resp_unstable";
	public final static String SETTINGS_RESPONSE_TIME_FAIL = "resp_fail";
	public final static String SETTINGS_TEST_DURATION = "test_duration";
	public final static String SETTINGS_DATA_FOLDER = "data_folder";
	public final static String SETTINGS_MAIN_JMX = "main_jmx";
	
	//Default properties
	public final static String DEFAULT_SETTINGS_DATA_FOLDER = "DataFolder";
	
}
