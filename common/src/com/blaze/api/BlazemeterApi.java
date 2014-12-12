package com.blaze.api;

import com.blaze.entities.TestInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BlazemeterApi {

    public boolean uploadJmx(String userKey, String testId, String fileName, String pathName) throws JSONException;

    public JSONObject uploadFile(String userKey, String testId, String fileName, String pathName) throws JSONException;

    public TestInfo getTestRunStatus(String userKey, String testId) throws JSONException;

    public JSONObject startTest(String userKey, String testId) throws JSONException;

    public JSONObject stopTest(String userKey, String testId) throws JSONException;

    public JSONObject aggregateReport(String userKey, String reportId) throws JSONException;

    public HashMap<String, String> getTestList(String userKey) throws IOException, JSONException;
}
