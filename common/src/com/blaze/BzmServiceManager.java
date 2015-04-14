package com.blaze;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.Constants;
import com.blaze.runner.JsonConstants;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.google.common.collect.LinkedHashMultimap;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;

import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.util.PropertiesUtil;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marcel Milea
 */
public class BzmServiceManager {
    public enum ApiVersion {
        v3, v2,autoDetect
    }
    private static BzmServiceManager bzmServiceManager=null;
    //Default properties
    public final static String DEFAULT_SETTINGS_DATA_FOLDER = "/DataFolder";

    private String userKey;
    private String blazeMeterUrl;
    private String blazeMeterApiVersion;
    private String serverName;
    private String serverPort;
    private String username;
    private String password;
    private BuildProgressLogger logger;
    private BlazemeterApi blazemeterAPI;
    private String session;
    private JSONObject aggregate;

    public BzmServiceManager() {
    }

    private BzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {

        this.userKey = buildSharedMap.get(Constants.USER_KEY);
        this.blazeMeterUrl = buildSharedMap.get(Constants.BLAZEMETER_URL);
        this.serverName = buildSharedMap.get(Constants.PROXY_SERVER_NAME);
        this.serverPort = buildSharedMap.get(Constants.PROXY_SERVER_PORT);
        this.username =buildSharedMap.get(Constants.PROXY_USERNAME);
        this.password = buildSharedMap.get(Constants.PROXY_PASSWORD);
        this.blazeMeterApiVersion = buildSharedMap.get(Constants.BLAZEMETER_API_VERSION);
        this.blazemeterAPI = APIFactory.getAPI(userKey, serverName, serverPort, username, password, blazeMeterUrl, blazeMeterApiVersion, logger);
        this.blazeMeterApiVersion=(this.blazemeterAPI instanceof BlazemeterApiV3Impl)?"v3":"v2";
        this.logger = logger;
    }

    public static BzmServiceManager getBzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager(buildSharedMap,logger);
        }else{
            bzmServiceManager.setUserKey(buildSharedMap.get(Constants.USER_KEY));
            bzmServiceManager.setBlazeMeterUrl(buildSharedMap.get(Constants.BLAZEMETER_URL));
            bzmServiceManager.setServerName(buildSharedMap.get(Constants.PROXY_SERVER_NAME));
            bzmServiceManager.setServerPort(buildSharedMap.get(Constants.PROXY_SERVER_PORT));
            bzmServiceManager.setUsername(buildSharedMap.get(Constants.PROXY_USERNAME));
            bzmServiceManager.setPassword(buildSharedMap.get(Constants.PROXY_PASSWORD));
            bzmServiceManager.blazeMeterApiVersion = buildSharedMap.get(Constants.BLAZEMETER_API_VERSION);
            bzmServiceManager.setLogger(logger);
            BlazemeterApi blazemeterAPI = APIFactory.getAPI(buildSharedMap.get(Constants.USER_KEY),
                    buildSharedMap.get(Constants.PROXY_SERVER_NAME),
                    buildSharedMap.get(Constants.PROXY_SERVER_PORT),
                    buildSharedMap.get(Constants.PROXY_USERNAME),
                    buildSharedMap.get(Constants.PROXY_PASSWORD),
                    buildSharedMap.get(Constants.BLAZEMETER_URL),
                    buildSharedMap.get(Constants.BLAZEMETER_API_VERSION),
                    logger);
            bzmServiceManager.setBlazemeterAPI(blazemeterAPI);
        }
        return bzmServiceManager;
    }

    @NotNull
    public String getDebugKey() {
        return "Debug Key";
    }

    public String prepareTest(String testId,String jsonConfiguration,String testDuration){
        JSONObject jsonConf=null;
        if(jsonConfiguration!=null&&!jsonConfiguration.isEmpty()){
            try{
                File jsonF=new File(jsonConfiguration);
                String jsonStr = new String(FileUtils.readFileToString((jsonF)));
                jsonConf=new JSONObject(jsonStr);
            }catch (Exception e){
                logger.warning("Failed to read JSON Configuration from "+jsonConfiguration);
            }
            if(testId.contains(Constants.NEW_TEST)){
                testId=this.createTest(jsonConf);
            }else{
                this.postJsonConfig(testId, jsonConf);
            }
        }else{
            if (!PropertiesUtil.isEmptyOrNull(testDuration)) {
                try{
                    logger.message("Attempting to update testDuration for test with id:"+testId);
                    bzmServiceManager.updateTestDuration(testId, testDuration, logger);
                } catch (NumberFormatException nfe){
                    logger.exception(nfe);
                    logger.warning("Test duration is not a number.");
                    return "Test duration is not a number.";
                }
            }
        }

        return testId;
    }




    public LinkedHashMultimap<String, String> getTests() {
        if(this.blazemeterAPI==null){
            blazemeterAPI=APIFactory.getAPI(this.userKey,this.serverName,this.serverPort,
                                            this.username,this.password,this.blazeMeterUrl,this.blazeMeterApiVersion,logger);
        }

        LinkedHashMultimap tests=LinkedHashMultimap.create();
        tests.put(Constants.CREATE_FROM_JSON,Constants.NEW_TEST);
        try {
            tests.putAll(this.blazemeterAPI.getTestList());
        } catch (IOException e) {
            logger.exception(e);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }finally {
            return tests;
        }
    }

    public Map<String, Collection<String>> getTestsAsMap(){
        return getTests().asMap();
    }

    public String startTest(String testId, int attempts, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;
        String session=null;
        try {
        do {
            json = this.blazemeterAPI.startTest(testId);
            countStartRequests++;
            if (countStartRequests > attempts) {
                logger.error("Could not start BlazeMeter Test with "+attempts+" attempts");
                session="";
                return session;
            }
        } while (json == null);

            if (this.blazeMeterApiVersion.equals(ApiVersion.v2.name())) {
                if (!json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
                    if (json.get(JsonConstants.RESPONSE_CODE).equals(500) && json.get(JsonConstants.ERROR).toString().startsWith("Test already running")) {
                        logger.error("Test already running, please stop it first");
                        session="";
                        return session;
                    }
                }
                session = json.get("session_id").toString();
            }else{
                session = json.getJSONObject(JsonConstants.RESULT).getJSONArray("sessionsId").getString(0);
            }
            this.session=session;
        } catch (JSONException e) {
            logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return session;
    }

    public void updateTestDuration(String testId, String testDuration, BuildProgressLogger logger) {
        if(this.blazemeterAPI instanceof BlazemeterApiV3Impl){
            Utils.updateTestDuration(this.blazemeterAPI, testId, testDuration, logger);
        }else{
            logger.message("Updating test duration is not implemented for D6");
        }
    }

    public void retrieveJUNITXML(String session,BuildRunnerContext buildRunnerContext){
       String junitReport = this.blazemeterAPI.retrieveJUNITXML(session);
        logger.message("Received Junit report from server.... Saving it to the disc...");
        String reportFilePath=buildRunnerContext.getWorkingDirectory()+"/"+session+".xml";
        Utils.saveReport(junitReport, reportFilePath,logger);
}

    public void retrieveJTL(String session,BuildRunnerContext buildRunnerContext){
        BlazemeterApi api=this.blazemeterAPI;
        JSONObject jo=api.retrieveJTLZIP(session);
        String dataUrl=null;
        try {
            JSONArray data=jo.getJSONObject(JsonConstants.RESULT).getJSONArray(JsonConstants.DATA);
            for(int i=0;i<data.length();i++){
                String title=data.getJSONObject(i).getString("title");
                if(title.equals("Zip")){
                    dataUrl=data.getJSONObject(i).getString(JsonConstants.DATA_URL);
                    break;
                }
            }
            String jtlFilePath=buildRunnerContext.getWorkingDirectory()+"/"+session+".zip";

            File jtlZip=new File(jtlFilePath);
            URL url=new URL(dataUrl+"?api_key="+userKey);
            FileUtils.copyURLToFile(url,jtlZip);
            logger.message("Downloading JTLZIP from " + url + "to " + jtlZip.getCanonicalPath());
//            unzip(jtlZip.getAbsolutePath(), jtlZip.getParent(), jenBuildLog);
        } catch (JSONException e) {
            logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
        } catch (MalformedURLException e) {
            logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
        } catch (IOException e) {
            logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
        } catch (NullPointerException e){
            logger.exception(e);
        }

    }

    /**
     * Get report results.
     *
     * @param logger
     * @return -1 fail, 0 success, 1 unstable
     */
    public TestResult getReport(BuildProgressLogger logger) {
        TestResult testResult = null;
        try {
            this.aggregate=this.blazemeterAPI.testReport(this.session);
            testResult = new TestResult(this.aggregate);

        } catch (JSONException e) {
            logger.exception(e);
        } catch (IOException e) {
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        finally {
            return testResult;
        }
    }

    public boolean uploadJMX(String testId, String filename, String pathname) {
        boolean uploadJMX=false;
        try {
            uploadJMX=this.blazemeterAPI.uploadJmx(testId, filename, pathname);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (IOException ioe) {
            logger.exception(ioe);
            logger.error("Could not upload file " + filename + " " + ioe.getMessage());
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return uploadJMX;
    }

    public void uploadFile(String testId, String dataFolder, String fileName, BuildProgressLogger logger) {
        try {
            org.json.JSONObject json = this.blazemeterAPI.uploadFile(testId, fileName, dataFolder + File.separator + fileName);
            if (!json.get(JsonConstants.RESPONSE_CODE).equals(new Integer(200))) {
                logger.error("Could not upload file " + fileName + " " + json.get(JsonConstants.ERROR).toString());
            }
        } catch (JSONException e) {
            logger.exception(e);
            logger.error("Could not upload file " + fileName + " " + e.getMessage());
        } catch (IOException ioe) {
            logger.exception(ioe);
            logger.error("Could not upload file " + fileName + " " + ioe.getMessage());
        } catch (NullPointerException e){
            logger.exception(e);
        }
    }

    public boolean stopTest(String testId, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;

        try {
            json = this.blazemeterAPI.stopTest(testId);
            logger.message("Attempt to stop test with result: " + json.toString());
            if (this.blazeMeterApiVersion.equals(ApiVersion.v2.name())) {


                if (json.get(JsonConstants.RESPONSE_CODE).equals(200)) {
                    logger.message("Test stopped succesfully." + json.toString());
                } else {
                    String error = json.get(JsonConstants.ERROR).toString();
                    logger.error("Error stopping test. Reported error is: " + error + " " + json.toString());
                    logger.error("Please use BlazeMeter website to manually stop the test with ID: " + testId);
                }
            }else{
                if (json.getJSONArray(JsonConstants.RESULT).length()>0) {
                    logger.message("Test stopped succesfully." + json.toString());
                    return true;
                } else {
                    String error = json.get(JsonConstants.RESULT).toString();
                    logger.error("Error stopping test. Reported error is: " + error + " " + json.toString());
                    return false;
                }

            }
        } catch (JSONException e) {
            logger.error("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]");
            logger.exception(e);
            return false;
         } catch (NullPointerException e){
            logger.exception(e);
        }
        return true;
    }

    public TestInfo getTestStatus(String testId) {
        TestInfo ti=null;
        try {
            ti=this.blazemeterAPI.getTestRunStatus(testId);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return ti;
    }

    public boolean postJsonConfig(String testId,JSONObject jsonConfig){
        try {
            this.blazemeterAPI.postJsonConfig(testId, jsonConfig);
        } catch (NullPointerException e){
            logger.exception(e);
        } catch (Exception e) {
            logger.warning("Problems with posting jsonConfiguration to server. Check URL and json configuration.");
            return false;
        }
        return true;
    }

    public String createTest(JSONObject jsonConfig){
        String testId=null;
        try {
            JSONObject jo=this.blazemeterAPI.createTest(jsonConfig);
            if(jo.has(JsonConstants.ERROR)&&!jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                logger.warning("Failed to create test: " + jo.getString(JsonConstants.ERROR));
                testId="";
            }else{
                testId = String.valueOf(jo.getJSONObject(JsonConstants.RESULT).getInt("id"));
            }
        } catch (NullPointerException e){
            logger.exception(e);
        }catch (Exception e) {
            logger.warning("Problems with creating test on server. Check URL and json configuration.");
        }finally {
            return testId;
        }
    }

    public BuildFinishedStatus validateServerTresholds() {
        JSONObject jo = null;
        boolean tresholdsValid=true;
        JSONObject result=null;
        logger.message("Going to validate server tresholds...");
        try {
            jo=this.blazemeterAPI.getTresholds(session);
            result=jo.getJSONObject(JsonConstants.RESULT);
            tresholdsValid=result.getJSONObject(JsonConstants.DATA).getBoolean("success");
        } catch (NullPointerException e){
            logger.message("Server tresholds validation was not executed");
            logger.exception(e);
        }catch (JSONException je) {
            logger.message("Server tresholds validation was not executed");
            logger.warning("Failed to get tresholds for  session=" + session);
        }finally {
            logger.message("Server tresholds validation "+
                    (tresholdsValid?"passed. Marking build as PASSED":"failed. Marking build as FAILED"));
            return tresholdsValid? BuildFinishedStatus.FINISHED_SUCCESS:BuildFinishedStatus.FINISHED_FAILED;
        }
    }

    public String getReportUrl(String sessionId) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            jo = this.blazemeterAPI.generatePublicToken(sessionId);
            if(jo.get("error").equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject("result");
                publicToken=result.getString("publicToken");
                reportUrl=this.blazeMeterUrl+"/app/?public-token="+publicToken+"#reports/"+sessionId+"/summary";
            }else{
                logger.error("Problems with generating public-token for report URL: " + jo.get("error").toString());
                reportUrl=this.blazeMeterUrl+"/app/#reports/"+sessionId+"/summary";
            }
        } catch (Exception e){
            logger.error("Problems with generating public-token for report URL: "+e);
        }finally {
            return reportUrl;
        }
    }


    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
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

    public String getSession() {
        return session;
    }

    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }

    public String getBlazeMeterApiVersion() {
        return blazeMeterApiVersion;
    }

    public void setBlazeMeterApiVersion(String blazeMeterApiVersion) {
        this.blazeMeterApiVersion = blazeMeterApiVersion;
    }

    public BuildProgressLogger getLogger() {
        return logger;
    }

    public void setLogger(BuildProgressLogger logger) {
        this.logger = logger;
    }

    public BlazemeterApi getBlazemeterAPI() {
        return blazemeterAPI;
    }

    public void setBlazemeterAPI(BlazemeterApi blazemeterAPI) {
        this.blazemeterAPI = blazemeterAPI;
    }
}
