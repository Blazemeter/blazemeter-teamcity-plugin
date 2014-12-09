package com.blaze.api;

import com.blaze.entities.TestInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public interface BlazemeterApi {

    public boolean uploadJmx(String userKey, String testId, String fileName, String pathName);

    public JSONObject uploadFile(String userKey, String testId, String fileName, String pathName);

    public TestInfo getTestRunStatus(String userKey, String testId);

    public JSONObject startTest(String userKey, String testId);

    public JSONObject stopTest(String userKey, String testId);

    public JSONObject aggregateReport(String userKey, String reportId);

    public HashMap<String, String> getTestList(String userKey) throws IOException;
}
