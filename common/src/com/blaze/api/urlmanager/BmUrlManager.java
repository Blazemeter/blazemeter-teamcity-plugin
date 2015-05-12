package com.blaze.api.urlmanager;

import com.blaze.api.TestType;
import com.blaze.utils.Utils;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BmUrlManager {
    String CLIENT_IDENTIFICATION = "_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.getVersion()+"&​";

    String getServerUrl();

    String getTests(String appKey, String userKey);

    String testSessionStatus(String appKey, String userKey, String testId);

    String scriptUpload(String appKey, String userKey, String testId, String fileName);

    String fileUpload(String appKey, String userKey, String testId, String fileName);

    String testStart(String appKey, String userKey, String testId);

    String testStop(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getTestInfo(String appKey, String userKey, String testId);

    String getTresholds(String appKey, String userKey, String sessionId);

    String postJsonConfig(String appKey, String userKey, String testId);

    String createTest(String appKey, String userKey);

    String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    String getUser(String appKey, String userKey);

    String generatePublicToken(String appKey, String userKey, String sessionId);

    String testTerminate(String appKey, String userKey, String testId);
    TestType getTestType();
    void setTestType(TestType testType);

}