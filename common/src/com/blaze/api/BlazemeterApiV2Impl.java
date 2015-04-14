package com.blaze.api;

import java.io.File;
import java.io.IOException;

import com.blaze.api.urlmanager.BmUrlManagerV2Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.JsonConstants;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.util.FileUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blaze.runner.Constants;
import org.springframework.util.StringUtils;

/**
 * 
 * @author 
 *
 */
public class BlazemeterApiV2Impl implements BlazemeterApi {

    private String userKey;
	private String serverName;
	private int serverPort;
	private String username;
	private String password;
    private final JSONObject not_implemented;

    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpClient bzmHttpClient;
    BmUrlManagerV2Impl urlManager;

    public BlazemeterApiV2Impl(String userKey,String serverName, int serverPort, String username, String password, String bzmUrl) {
    	this.userKey = userKey;
        this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.username = username;
    	this.password = password;
        urlManager = new BmUrlManagerV2Impl(bzmUrl);
            bzmHttpClient = new BzmHttpClient(this.serverName,this.username,this.password,this.serverPort);
            bzmHttpClient.configureProxy();
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
        upLoadJMX=this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST)!=null;
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
        return this.bzmHttpClient.getResponseAsJson(url, jmxData, BzmHttpClient.Method.POST);
    }

    @Override
    public TestInfo getTestRunStatus(String testId) throws JSONException {
        TestInfo ti = new TestInfo();
        if ((StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId))) {
            ti.setStatus(Constants.TestStatus.NotFound);
            return ti;
        }
            String url = this.urlManager.testStatus(APP_KEY, userKey, testId);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);

            if (jo.get(JsonConstants.STATUS) == "Test not found")
                ti.setStatus(Constants.TestStatus.NotFound);
            else {
                ti.setId(jo.getString("test_id"));
                ti.setName( jo.getString("test_name"));
                ti.setStatus(jo.getString(JsonConstants.STATUS));
            }
        return ti;
    }

    @Override
    public synchronized JSONObject startTest(String testId) {

        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;

        String url = this.urlManager.testStart(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
    }


    /**
     * @param testId  - test id
     *                //     * @throws IOException
     *                //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject stopTest(String testId) {
        if (StringUtil.isEmptyOrSpaces(userKey)&StringUtil.isEmptyOrSpaces(testId)) return null;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        return this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
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
    public LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException {
        LinkedHashMultimap<String, String> testListOrdered = null;
        if (userKey == null || userKey.trim().isEmpty()) {
        } else {
            String url = this.urlManager.getTests(APP_KEY, userKey);
            JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
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
        JSONObject jo = this.bzmHttpClient.getResponseAsJson(url, null, BzmHttpClient.Method.POST);
        return jo;
    }
}
