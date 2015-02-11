package com.blaze.api.urlmanager;

import com.blaze.utils.Utils;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BmUrlManager {
    String CLIENT_IDENTIFICATION = "_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.getVersion()+"&​";

    public String getServerUrl();

    public String getTests(String appKey, String userKey);

    public String testStatus(String appKey, String userKey, String testId);

    public String scriptUpload(String appKey, String userKey, String testId, String fileName);

    public String fileUpload(String appKey, String userKey, String testId, String fileName);

    public String testStart(String appKey, String userKey, String testId);

    public String testStop(String appKey, String userKey, String testId);

    public String testReport(String appKey, String userKey, String reportId);

    public String getTestInfo(String appKey, String userKey, String testId);

    public String getTresholds(String appKey, String userKey, String sessionId);

    public String postJsonConfig(String appKey, String userKey, String testId);

    public String createTest(String appKey, String userKey);

    public String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    public String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    public String getUser(String appKey, String userKey);

}