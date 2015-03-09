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

    public boolean uploadJmx(String testId, String fileName, String pathName)
            throws JSONException,IOException;

    public JSONObject uploadFile(String testId, String fileName, String pathName)
            throws JSONException,IOException;

    public TestInfo getTestRunStatus(String testId) throws JSONException;

    public JSONObject startTest(String testId) throws JSONException;

    public JSONObject stopTest(String testId) throws JSONException;

    public JSONObject testReport(String reportId) throws JSONException;

    public HashMap<String, String> getTestList() throws IOException, JSONException;

    public JSONObject putTestInfo(String testId, JSONObject data,BuildProgressLogger logger);

    public JSONObject getTestInfo(String testId, BuildProgressLogger logger);

    public JSONObject getUser();

    public JSONObject getTresholds(String sessionId);

    public JSONObject postJsonConfig(String testId, JSONObject data);

    public JSONObject createTest(JSONObject data);

    public String retrieveJUNITXML(String sessionId);

    public JSONObject retrieveJTLZIP(String sessionId);

    public JSONObject generatePublicToken(String sessionId);

}
