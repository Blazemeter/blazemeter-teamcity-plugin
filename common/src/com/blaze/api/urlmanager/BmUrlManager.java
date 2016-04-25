package com.blaze.api.urlmanager;

import com.blaze.api.TestType;
import com.blaze.utils.Utils;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BmUrlManager {
    String CLIENT_IDENTIFICATION = "&_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.getVersion();

    String getServerUrl();

    void setServerUrl(String serverUrl);

    String masterStatus(String appKey, String userKey, String testId);

    String tests(String appKey, String userKey);

    String activeTests(String appKey, String userKey);

    String testStart(String appKey, String userKey, String testId);

    String collectionStart(String appKey, String userKey, String collectionId);

    String testStop(String appKey, String userKey, String testId);

    String testTerminate(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getUser(String appKey, String userKey);

    String ciStatus(String appKey, String userKey, String sessionId);

    String testConfig(String appKey, String userKey, String testId);

    String postJsonConfig(String appKey, String userKey, String testId);

    String createTest(String appKey, String userKey);

    String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    String generatePublicToken(String appKey, String userKey, String sessionId);

    String listOfSessionIds(String appKey, String userKey, String masterId);

}