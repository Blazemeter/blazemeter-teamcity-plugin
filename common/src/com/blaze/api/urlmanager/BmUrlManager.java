package com.blaze.api.urlmanager;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BmUrlManager {

    public String getServerUrl();

    public String getTests(String appKey, String userKey);

    public String testStatus(String appKey, String userKey, String testId);

    public String scriptUpload(String appKey, String userKey, String testId, String fileName);

    public String fileUpload(String appKey, String userKey, String testId, String fileName);

    public String testStart(String appKey, String userKey, String testId);

    public String testStop(String appKey, String userKey, String testId);

    public String testAggregateReport(String appKey, String userKey, String reportId);

    public String getUrlForTestList(String appKey, String userKey);
}