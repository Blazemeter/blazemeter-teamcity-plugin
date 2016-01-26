package com.blaze.api;

import com.blaze.api.urlmanager.BmUrlManager;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BlazemeterApi {

    TestStatus getTestStatus(String testId) throws JSONException;

    String startTest(String testId,TestType testType) throws JSONException;

    boolean stopTest(String testId) throws Exception;

    JSONObject testReport(String reportId) throws JSONException;

    LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException;

    JSONObject putTestInfo(String testId, JSONObject data,BuildProgressLogger logger);

//    JSONObject getTestStatus(String testId, BuildProgressLogger logger);

    JSONObject getUser();

    boolean active(String testId);

    int getTestMasterStatusCode(String id);

    JSONObject getCIStatus(String sessionId) throws JSONException;

    JSONObject postJsonConfig(String testId, JSONObject data);

    JSONObject createTest(JSONObject data);

    String retrieveJUNITXML(String sessionId);

    JSONObject retrieveJTLZIP(String sessionId);

    JSONObject generatePublicToken(String sessionId);

    int getTestSessionStatusCode(String id) throws Exception;

    JSONObject terminateTest(String testId);

    JSONObject getTestsJSON();

    String getBlazeMeterURL();

    BzmHttpWrapper getBzmHttpWr();
    void setBzmHttpWr(BzmHttpWrapper bzmHttpWr);
}
