package com.blaze.api;

import com.blaze.api.urlmanager.BmUrlManagerV3Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.Constants;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV3Impl implements BlazemeterApi {

	private String serverName;
	private int serverPort;
	private String username;
	private String password;

    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpClient bzmHttpClient;
    BmUrlManagerV3Impl urlManager;

    public BlazemeterApiV3Impl(String serverName, int serverPort, String username, String password, String bzmUrl) {
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
     * @param userKey  - user key
     * @param testId   - test id
     * @param fileName - test name
     * @param pathName - jmx file path
     *                 //     * @return test id
     *                 //     * @throws java.io.IOException
     *                 //     * @throws org.json.JSONException
     */
    @Override
    public synchronized boolean uploadJmx(String userKey, String testId, String fileName, String pathName)
            throws JSONException, IOException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return false;
        boolean upLoadJMX=false;
        String url = this.urlManager.scriptUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = FileUtil.readText(new File(pathName));
        jmxData.put("data", fileCon);
        upLoadJMX=this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST)!=null;
        return upLoadJMX;
    }

    @Override
    public synchronized JSONObject uploadFile(String userKey, String testId, String fileName, String pathName)
            throws JSONException, IOException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;
        String url = this.urlManager.fileUpload(APP_KEY, userKey, testId, fileName);
        JSONObject jmxData = new JSONObject();
        String fileCon = null;
        fileCon = FileUtil.readText(new File(pathName));
        jmxData.put("data", fileCon);
        return this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST);
    }

    @Override
    public TestInfo getTestRunStatus(String userKey, String testId) throws JSONException{
        TestInfo ti = new TestInfo();
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) {
            ti.setStatus(Constants.TestStatus.NotFound);
            return ti;
        }
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
            JSONObject result = (JSONObject) jo.get("result");
            if (result.get("dataUrl") == null) {
                ti.setStatus(Constants.TestStatus.NotFound);
            } else {
                ti.setId(String.valueOf(result.getInt("testId")));
                ti.setName(result.getString("name"));
                if (result.getString("status").equals("DATA_RECIEVED")) {
                    ti.setStatus(Constants.TestStatus.Running);
                } else if (result.getString("status").equals("ENDED")) {
                    ti.setStatus(Constants.TestStatus.NotRunning);
                } else {
                    ti.setStatus(Constants.TestStatus.NotRunning);
                }
            }
        return ti;
    }

    @Override
    public synchronized JSONObject startTest(String userKey, String testId) throws JSONException{

        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
    }

    /**
     * @param userKey - user key
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject stopTest(String userKey, String testId) throws JSONException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
    }

    /**
     * @param userKey  - user key
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String userKey, String reportId) throws JSONException{
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject) this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET).getJSONObject("result")
                    .getJSONArray("summary")
                    .get(0);
        return summary;
    }

    @Override
    public HashMap<String, String> getTestList(String userKey) throws IOException, JSONException{

        LinkedHashMap testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
                JSONArray result = (JSONArray) jo.get("result");
                if (result != null && result.length() > 0) {
                    testListOrdered = new LinkedHashMap<String, String>(result.length());
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject en = null;
                            en = result.getJSONObject(i);
                        String id;
                        String name;
                            if (en != null) {
                                id = String.valueOf(en.getInt("id"));
                                name = en.getString("name").replaceAll("&", "&amp;");
                                testListOrdered.put(name, id);
                            }
                    }
                    return testListOrdered;
                }
        }
        return testListOrdered;
    }

    @Override
    public JSONObject getTestInfo(String apiKey,String testId,BuildProgressLogger logger){
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.message("ERROR: User apiKey is empty");
            return null;
        }
        String url = this.urlManager.getTestInfo(APP_KEY, apiKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }

    @Override
    public JSONObject putTestInfo(String apiKey,String testId, JSONObject data,BuildProgressLogger logger){
        if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.message("ERROR: User apiKey is empty");
            return null;
        }
        String url = this.urlManager.getTestInfo(APP_KEY, apiKey,testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, BzmHttpClient.Method.PUT);
        return jo;
    }


    @Override
    public JSONObject createTest(String apiKey,JSONObject data) {
        if(StringUtil.isEmptyOrSpaces(apiKey)) return null;
        String url = this.urlManager.createTest(APP_KEY, apiKey);
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
    public JSONObject getTresholds(String userKey, String sessionId){
        if (userKey == null || userKey.trim().isEmpty()) {
            return null;
        }
        String url = this.urlManager.getTresholds(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jo;
    }

    @Override
    public JSONObject postJsonConfig(String userKey,String testId, JSONObject data){
        if(StringUtils.isEmpty(userKey)&StringUtils.isEmpty(testId)) return null;

        String url = this.urlManager.postJsonConfig(APP_KEY, userKey, testId);
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, data, BzmHttpClient.Method.POST);
        return jo;
    }

    @Override
    public String retrieveJUNITXML(String userKey,String sessionId) {
        if(StringUtil.isEmptyOrSpaces((userKey))&StringUtil.isEmptyOrSpaces(sessionId)) return null;
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, userKey, sessionId);
        String xmlJunit = this.bzmHttpClient.getResponseAsString(url, null, BzmHttpClient.Method.GET);
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJTLZIP(String userKey,String sessionId) {
        if(StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces((sessionId))) return null;
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, userKey, sessionId);
        JSONObject jtlzip = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.GET);
        return jtlzip;
    }

}
