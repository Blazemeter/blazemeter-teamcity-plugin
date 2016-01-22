package com.blaze.api.urlmanager;

import com.blaze.api.TestType;
import com.blaze.runner.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 5/12/14.
 */
public class BmUrlManagerV2Impl implements BmUrlManager{

    private String serverUrl = Constants.DEFAULT_BZM_SERVER;
    private TestType testType=TestType.http;

    public BmUrlManagerV2Impl(String blazeMeterUrl) {
        serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String tests(String appKey, String userKey){
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl +String.format("/api/rest/blazemeter/getTests.json/?app_key=%s&user_key=%s&test_id=all",
                appKey, userKey)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterStatus(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl +String.format("/api/rest/blazemeter/testGetStatus.json/?app_key=%s&user_key=%s&test_id=%s&",
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
        return serverUrl +String.format("/api/rest/blazemeter/testScriptUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
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
        return serverUrl +String.format("/api/rest/blazemeter/testArtifactUpload.json/?app_key=%s&user_key=%s&test_id=%s&file_name=%s&",
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
        return serverUrl +String.format("/api/rest/blazemeter/testStart.json/?app_key=%s&user_key=%s&test_id=%s&",
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
        return serverUrl +String.format("/api/rest/blazemeter/testStop.json/?app_key=%s&user_key=%s&test_id=%s&",
                appKey, userKey, testId)+CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String userKey, String reportId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            reportId = URLEncoder.encode(reportId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testAggregateReport= serverUrl +"/api/latest/sessions/"
                +reportId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testAggregateReport;
    }

    @Override
    public String testInfo(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String thresholds(String appKey, String userKey, String sessionId) {
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
        String generatePublicToken=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        generatePublicToken= serverUrl +"/api/latest/sessions/"+sessionId+
                "/publicToken?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return generatePublicToken;
    }

    @Override
    public String testTerminate(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public TestType testType() {
        return this.testType;
    }

    @Override
    public void testType(TestType testType) {
        this.testType=testType;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl=serverUrl;
    }

    @Override
    public String activeTests(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String collectionStart(String appKey, String userKey, String collectionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String ciStatus(String appKey, String userKey, String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String testConfig(String appKey, String userKey, String testId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String listOfSessionIds(String appKey, String userKey, String masterId) {
        return Constants.NOT_IMPLEMENTED;
    }
}
