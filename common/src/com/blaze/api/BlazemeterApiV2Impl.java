package com.blaze.api;

import java.io.File;
import java.io.IOException;

import com.blaze.api.urlmanager.BmUrlManager;
import com.blaze.api.urlmanager.BmUrlManagerV2Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blaze.runner.Constants;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV2Impl implements BlazemeterApi {

    private String userKey;
    private final JSONObject not_implemented;

    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpWrapper bzmHttpWrapper;
    BmUrlManagerV2Impl urlManager;

    public BlazemeterApiV2Impl(String userKey, String bzmUrl) {
    	this.userKey = userKey;
        urlManager = new BmUrlManagerV2Impl(bzmUrl);
            bzmHttpWrapper = new BzmHttpWrapper();
        not_implemented=new JSONObject();
        try {
            not_implemented.put(Constants.NOT_IMPLEMENTED,Constants.NOT_IMPLEMENTED);
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }




    /**
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    @Override
    public synchronized boolean uploadJmx(String testId, String fileName, String pathName)
            throws JSONException, IOException {
        boolean upLoadJMX=false;
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return false;
        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = FileUtil.readText(new File(pathName));
        jmxData.put(JsonConstants.DATA, fileCon);
        upLoadJMX=this.bzmHttpWrapper.response(url, jmxData, BzmHttpWrapper.Method.POST,JSONObject.class)!=null;
        return upLoadJMX;
    }

    @Override
    public synchronized JSONObject uploadFile(String testId, String fileName, String pathName)
            throws JSONException, IOException {
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;
        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = FileUtil.readText(new File(pathName));
        jmxData.put(JsonConstants.DATA, fileCon);
        return this.bzmHttpWrapper.response(url, jmxData, BzmHttpWrapper.Method.POST,JSONObject.class);
    }

    @Override
    public TestInfo getTestInfo(String testId) throws JSONException {
        TestInfo ti = new TestInfo();

        if (StringUtils.isEmpty(this.userKey)&StringUtils.isEmpty(testId)) {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, this.userKey, testId);
            JSONObject jo = this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);

            if ("Test not found".equals(jo.get(JsonConstants.ERROR))) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                ti.setId(jo.getString(JsonConstants.TEST_ID));
                ti.setName(jo.getString("test_name"));
                ti.setStatus(TestStatus.valueOf(jo.getString(JsonConstants.STATUS).equals("Not Running") ? "NotRunning" : jo.getString(JsonConstants.STATUS)));
            }
        } catch (Exception e) {
            ti.setStatus(TestStatus.Error);
        }
        return ti;
    }

    @Override
    public synchronized String startTest(String testId) throws JSONException{
        if (StringUtils.isEmpty(userKey)&StringUtils.isEmpty(testId)) {
            return null;
        }
        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject jo=this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
        if (jo.get(JsonConstants.RESPONSE_CODE).equals(500) && jo.get(JsonConstants.ERROR).toString()
                .startsWith("Test already running")) {
            return "";
        }

        String session = jo.get("session_id").toString();
        return session;
    }


    /**
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public boolean stopTest(String testId) throws Exception{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return false;

            String url = this.urlManager.testStop(APP_KEY, userKey, testId);
            JSONObject jo= this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.POST,JSONObject.class);
        if (jo.get(JsonConstants.RESPONSE_CODE).equals(200)) {
            return true;
        } else {
            String error = jo.get(JsonConstants.ERROR).toString();
            throw new Exception("Error stopping test with ID="+testId+". Reported error is: "+error.toString());
        }
    }

    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) throws JSONException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject)
                this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class).getJSONObject(JsonConstants.RESULT)
                .getJSONArray("summary")
                .get(0);
        return summary;
    }

    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException {
        LinkedHashMultimap<String, String> testListOrdered = null;
        if (userKey == null || userKey.trim().isEmpty()) {
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            JSONObject jo = this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.POST,JSONObject.class);
                String r = jo.get(JsonConstants.RESPONSE_CODE).toString();
                if (r.equals("200")) {
                    JSONArray arr = (JSONArray) jo.get("tests");
                    testListOrdered = LinkedHashMultimap.create(arr.length(),arr.length());
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject en = null;
                            en = arr.getJSONObject(i);
                        String id;
                        String name;
                            if (en != null) {
                                id = en.getString("test_id");
                                name = en.getString("test_name").replaceAll("&", "&amp;");
                                testListOrdered.put(name, id);

                            }
                    }
                }
        }

        return testListOrdered;
    }
    
    

    @Override
    public JSONObject putTestInfo(String testId, JSONObject data,BuildProgressLogger logger) {
        return null;
    }

    @Override
    public JSONObject getTestInfo(String testId, BuildProgressLogger logger) {
        return not_implemented;
    }

    @Override
    public JSONObject getTresholds(String sessionId) {
        return not_implemented;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data) {
        return not_implemented;
    }

    @Override
    public JSONObject createTest(JSONObject data) {
        return not_implemented;
    }

    @Override
    public String retrieveJUNITXML(String sessionId) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        return not_implemented;
    }

    @Override
    public JSONObject getUser() {
        return not_implemented;
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if(StringUtils.isEmpty(userKey)&StringUtils.isEmpty(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.POST,JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject getTestsJSON() {
        String url = this.urlManager.getTests(APP_KEY, userKey);
        JSONObject jo = this.bzmHttpWrapper.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
        return jo;
    }

    @Override
    public int getTestSessionStatusCode(String id) throws Exception {
        return -1;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        return not_implemented;
    }


    @Override
    public BmUrlManager getUrlManager() {
        return this.urlManager;
    }
}
