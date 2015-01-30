package com.blaze.api;

import com.blaze.entities.TestInfo;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BlazemeterApi {

    public boolean uploadJmx(String userKey, String testId, String fileName, String pathName)
            throws JSONException,IOException;

    public JSONObject uploadFile(String userKey, String testId, String fileName, String pathName)
            throws JSONException,IOException;

    public TestInfo getTestRunStatus(String userKey, String testId) throws JSONException;

    public JSONObject startTest(String userKey, String testId) throws JSONException;

    public JSONObject stopTest(String userKey, String testId) throws JSONException;

    public JSONObject testReport(String userKey, String reportId) throws JSONException;

    public HashMap<String, String> getTestList(String userKey) throws IOException, JSONException;

    public JSONObject putTestInfo(String apiKey,String testId, JSONObject data,BuildProgressLogger logger);

    public JSONObject getTestInfo(String apiKey,String testId, BuildProgressLogger logger);

    public JSONObject getTresholds(String userKey, String sessionId);

    public JSONObject postJsonConfig(String userKey,String testId, JSONObject data);

}
