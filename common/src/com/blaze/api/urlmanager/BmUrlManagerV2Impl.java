package com.blaze.api.urlmanager;

import com.blaze.runner.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 5/12/14.
 */
public class BmUrlManagerV2Impl implements BmUrlManager{

    private String SERVER_URL = Constants.DEFAULT_BZM_SERVER;

    public BmUrlManagerV2Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public String getTests(String appKey, String userKey){
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all",
                appKey, userKey)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStatus(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&",
                appKey, userKey, testId)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
                appKey, userKey, testId, fileName)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
                appKey, userKey, testId, fileName)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s&",
                appKey, userKey, testId)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStop(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s&",
                appKey, userKey, testId)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String userKey, String reportId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            reportId = URLEncoder.encode(reportId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return SERVER_URL+String.format("/api/rest/blazemeter/testGetReport.json/?app_key=%s&user_key=%s&report_id=%s&get_aggregate=true&",
                appKey, userKey, reportId)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String getTestInfo(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String getTresholds(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String postJsonConfig(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String createTest(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String retrieveJTLZIP(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String getUser(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String generatePublicToken(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }
}
