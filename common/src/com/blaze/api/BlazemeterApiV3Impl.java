package com.blaze.api;

import com.blaze.api.urlmanager.BmUrlManager;
import com.blaze.api.urlmanager.BmUrlManagerV3Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV3Impl implements BlazemeterApi {

    private String userKey;
    private String serverName;
	private int serverPort;
	private String username;
	private String password;

    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpClient bzmHttpClient;
    BmUrlManagerV3Impl urlManager;

    public BlazemeterApiV3Impl(String userKey, String serverName, int serverPort, String username, String password, String bzmUrl) {
        this.userKey = userKey;
        this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;
        urlManager = new BmUrlManagerV3Impl(bzmUrl);
        try {
            bzmHttpClient = new BzmHttpClient(this.serverName,this.username,this.password,this.serverPort);
            bzmHttpClient.configureProxy();
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
            throws JSONException, IOException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return false;
        boolean upLoadJMX=false;
        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = FileUtil.readText(new File(pathName));
        jmxData.put(JsonConstants.DATA, fileCon);
        upLoadJMX=this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST)!=null;
        return upLoadJMX;
    }

    @Override
    public synchronized JSONObject uploadFile(String testId, String fileName, String pathName)
            throws JSONException, IOException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;
        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = null;
        fileCon = FileUtil.readText(new File(pathName));
        jmxData.put(JsonConstants.DATA, fileCon);
        return this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST);
    }

    @Override
    public TestInfo getTestInfo(String testId) throws JSONException{
        TestInfo ti = new TestInfo();

        if(StringUtils.isEmpty(this.userKey)&StringUtils.isEmpty(testId))
        {
            ti.setStatus(TestStatus.NotFound);
            return ti;
        }

        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, this.userKey, testId);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL)&&result.get(JsonConstants.DATA_URL) == null) {
                ti.setStatus(TestStatus.NotFound);
            } else {
                if(this.urlManager.getTestType().equals(TestType.multi)){
                    ti.setId(String.valueOf(result.getInt("collectionId")));
                }else{
                    ti.setId(String.valueOf(result.getInt("testId")));
                }ti.setName(result.getString(JsonConstants.NAME));
                if (!result.has("ended")||String.valueOf(result.getInt("ended")).equals(JSONObject.NULL)||String.valueOf(result.getInt("ended")).isEmpty()) {
                    ti.setStatus(TestStatus.Running);
                } else {
                    if(result.has("errors")&&!result.get("errors").equals(JSONObject.NULL)){
                        ti.setStatus(TestStatus.Error);
                    }else {
                        ti.setStatus(TestStatus.NotRunning);
                    }
                }
            }
        } catch (Exception e) {
            ti.setStatus(TestStatus.Error);
        }
        return ti;
    }

    @Override
    public synchronized String startTest(String testId) throws JSONException{
        if(StringUtils.isEmpty(userKey)&StringUtils.isEmpty(testId)) return null;
        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        JSONObject jo=this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
        JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
        if(!this.urlManager.getTestType().equals(TestType.multi)){
            return  ((JSONArray) result.get("sessionsId")).get(0).toString();
        }else{
            return  String.valueOf(result.getInt(JsonConstants.ID));
        }
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
        JSONObject jo=this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);

        if (jo.getJSONArray(JsonConstants.RESULT).length()>0) {
            return true;
        } else {
            String error = jo.get(JsonConstants.RESULT).toString();
            throw new Exception("Error stopping test with ID="+testId+". Reported error is: " + error.toString());
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
        JSONObject summary = (JSONObject) this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET).getJSONObject(JsonConstants.RESULT)
                    .getJSONArray("summary")
                    .get(0);
        return summary;
    }

    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException{

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            System.out.println("BlazemeterApiV3Impl: UserKey" + this.userKey.substring(0,5));
            System.out.println("BlazemeterApiV3Impl: Url" + url);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
            System.out.println("BlazemeterApiV3Impl: JSONObject" + jo.toString());
            JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
            System.out.println("BlazemeterApiV3Impl: JSONResult" + result.toString());
            if (result != null && result.length() > 0) {
                testListOrdered = LinkedHashMultimap.create(result.length(), result.length());
                for (int i = 0; i < result.length(); i++) {
                    JSONObject en = null;
                    en = result.getJSONObject(i);
                    String id;
                    String name;
                    if (en != null) {
                        id = String.valueOf(en.getInt("id"));
                        name = en.getString(JsonConstants.NAME).replaceAll("&", "&amp;");
                        String testType = en.has(JsonConstants.TYPE) ? en.getString(JsonConstants.TYPE) : TestType.http.name();
                        testListOrdered.put(name + "(" + testType + ")", id);

                    }
                    }
                    return testListOrdered;
                }
        }
        return testListOrdered;
    }

    @Override
    public JSONObject getTestInfo(String testId,BuildProgressLogger logger){
        if (userKey == null || userKey.trim().isEmpty()) {
            logger.message("ERROR: User apiKey is empty");
            return null;
        }
        String url = this.urlManager.getTestInfo(APP_KEY, userKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }

    @Override
    public JSONObject putTestInfo(String testId, JSONObject data,BuildProgressLogger logger){
        if (userKey == null || userKey.trim().isEmpty()) {
            logger.message("ERROR: User apiKey is empty");
            return null;
        }
        String url = this.urlManager.getTestInfo(APP_KEY, userKey,testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, BzmHttpClient.Method.PUT);
        return jo;
    }


    @Override
    public JSONObject createTest(JSONObject data) {
        if(StringUtil.isEmptyOrSpaces(userKey)) return null;
        String url = this.urlManager.createTest(APP_KEY, userKey);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, BzmHttpClient.Method.POST);
        return jo;
    }


    public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


    @Override
    public JSONObject getTresholds(String sessionId){
        if (userKey == null || userKey.trim().isEmpty()) {
            return null;
        }
        String url = this.urlManager.getTresholds(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data){
        if(StringUtils.isEmpty(userKey)&StringUtils.isEmpty(testId)) return null;

        String url = this.urlManager.postJsonConfig(APP_KEY, userKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, BzmHttpClient.Method.POST);
        return jo;
    }

    @Override
    public String retrieveJUNITXML(String sessionId) {
        if(StringUtil.isEmptyOrSpaces((userKey))&StringUtil.isEmptyOrSpaces(sessionId)) return null;
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, userKey, sessionId);
        String xmlJunit = this.bzmHttpClient.getResponseAsString(url, null, BzmHttpClient.Method.GET);
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        if(StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces((sessionId))) return null;
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, userKey, sessionId);
        JSONObject jtlzip = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jtlzip;
    }

    @Override
    public JSONObject getUser() {
        if(StringUtils.isEmpty(userKey)) return null;
        String url = this.urlManager.getUser(APP_KEY, userKey);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }


    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if(StringUtils.isEmpty(userKey)&StringUtils.isEmpty(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
        return jo;
    }

    @Override
    public int getTestSessionStatusCode(String id) throws Exception{
        int statusCode=0;
        if(StringUtils.isEmpty(this.userKey)&StringUtils.isEmpty(id))
        {
            return statusCode;
        }
        try {
            String url = this.urlManager.testSessionStatus(APP_KEY, this.userKey, id);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode=result.getInt("statusCode");
        } catch (Exception e) {
            throw e;
        }finally {
            {
                return statusCode;
            }
        }
    }
    @Override
    public JSONObject getTestsJSON() {
        String url = this.urlManager.getTests(APP_KEY, userKey);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        if(StringUtils.isEmpty(this.userKey)&StringUtils.isEmpty(testId)) return null;

        String url = this.urlManager.testTerminate(APP_KEY, this.userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);

    }

    @Override
    public BmUrlManager getUrlManager() {
        return this.urlManager;
    }
}
