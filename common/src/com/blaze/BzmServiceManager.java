package com.blaze;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV2Impl;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.entities.TestInfo;
import com.blaze.runner.Constants;
import com.blaze.testresult.TestResult;
import com.blaze.testresult.TestResultFactory;
import com.blaze.utils.Utils;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;

import org.jetbrains.annotations.NotNull;
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
        this.blazemeterAPI = getAPI();
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
        }
        return bzmServiceManager;
    }

    @NotNull
    public String getDebugKey() {
        return "Debug Key";
    }

    private BlazemeterApi getAPI() {
        if (blazemeterAPI == null||
                (this.blazeMeterApiVersion.equals("v3")&this.blazemeterAPI instanceof BlazemeterApiV2Impl)||
                        this.blazeMeterApiVersion.equals("v2")&this.blazemeterAPI instanceof BlazemeterApiV3Impl) {
            int serverPortInt = 0;
            if (serverPort != null && !serverPort.isEmpty()) {
                serverPortInt = Integer.parseInt(serverPort);
            }
            switch (ApiVersion.valueOf(this.blazeMeterApiVersion)) {
                case autoDetect:
                    blazemeterAPI = new BlazemeterApiV3Impl(serverName, serverPortInt, username, password, this.blazeMeterUrl);
                    break;
                case v3:
                    blazemeterAPI = new BlazemeterApiV3Impl(serverName, serverPortInt, username, password, this.blazeMeterUrl);
                    break;
                case v2:
                    blazemeterAPI = new BlazemeterApiV2Impl(serverName, serverPortInt, username, password, this.blazeMeterUrl);
                    break;
            }
        }

        return blazemeterAPI;
    }

    public HashMap<String, String> getTests() {
        HashMap<String,String>tests=new HashMap<>();
        try {
            tests= getAPI().getTestList(userKey);
        } catch (IOException e) {
            logger.exception(e);
        } catch (JSONException e) {
            logger.exception(e);
        }
        return tests;
    }

    public String startTest(String testId, int attempts, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;
        String session=null;
        try {
        do {
            json = getAPI().startTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > attempts) {
                logger.error("Could not start BlazeMeter Test with "+attempts+" attempts");
                session="";
                return session;
            }
        } while (json == null);

            if (this.blazeMeterApiVersion.equals(ApiVersion.v2.name())) {
                if (!json.get("response_code").equals(200)) {
                    if (json.get("response_code").equals(500) && json.get("error").toString().startsWith("Test already running")) {
                        logger.error("Test already running, please stop it first");
                        session="";
                        return session;
                    }
                }
                session = json.get("session_id").toString();
            }else{
                session = json.getJSONObject("result").getJSONArray("sessionsId").getString(0);
            }
            this.session=session;
        } catch (JSONException e) {
            logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            logger.exception(e);
        }
        return session;
    }

    public void updateTest(String testId, int testDuration, BuildProgressLogger logger) {
        Utils.updateTest(userKey, getAPI(), testId, testDuration, logger);
    }



    /**
     * Get report results.
     *
     * @param logger
     * @return -1 fail, 0 success, 1 unstable
     */
    public TestResult getReport(BuildProgressLogger logger) {
        TestResultFactory testResultFactory = TestResultFactory.getTestResultFactory();
        testResultFactory.setVersion(ApiVersion.valueOf(blazeMeterApiVersion));
        TestResult testResult = null;
        try {
            this.aggregate=getAPI().testReport(this.userKey,this.session);
            testResult = testResultFactory.getTestResult(this.aggregate);

        } catch (JSONException e) {
            logger.exception(e);
        } catch (IOException e) {
            logger.exception(e);
        }finally {
            return testResult;
        }
    }

    public boolean uploadJMX(String testId, String filename, String pathname) {
        boolean uploadJMX=false;
        try {
            uploadJMX=getAPI().uploadJmx(userKey, testId, filename, pathname);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (IOException ioe) {
            logger.exception(ioe);
            logger.error("Could not upload file " + filename + " " + ioe.getMessage());
        }
        return uploadJMX;
    }

    public void uploadFile(String testId, String dataFolder, String fileName, BuildProgressLogger logger) {
        try {
            org.json.JSONObject json = getAPI().uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
            if (!json.get("response_code").equals(new Integer(200))) {
                logger.error("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.exception(e);
            logger.error("Could not upload file " + fileName + " " + e.getMessage());
        } catch (IOException ioe) {
            logger.exception(ioe);
            logger.error("Could not upload file " + fileName + " " + ioe.getMessage());
        }
    }

    public boolean stopTest(String testId, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;

        try {
            json = getAPI().stopTest(userKey, testId);
            logger.message("Attempt to stop test with result: " + json.toString());
            if (this.blazeMeterApiVersion.equals(ApiVersion.v2.name())) {


                if (json.get("response_code").equals(200)) {
                    logger.message("Test stopped succesfully." + json.toString());
                } else {
                    String error = json.get("error").toString();
                    logger.error("Error stopping test. Reported error is: " + error + " " + json.toString());
                    logger.error("Please use BlazeMeter website to manually stop the test with ID: " + testId);
                }
            }else{
                if (json.getJSONArray("result").length()>0) {
                    logger.message("Test stopped succesfully." + json.toString());
                    return true;
                } else {
                    String error = json.get("result").toString();
                    logger.error("Error stopping test. Reported error is: " + error + " " + json.toString());
                    return false;
                }

            }
        } catch (JSONException e) {
            logger.error("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]");
            logger.exception(e);
            return false;
         }
        return true;
    }

    public TestInfo getTestStatus(String testId) {
        TestInfo ti=null;
        try {
            ti=getAPI().getTestRunStatus(userKey, testId);
        } catch (JSONException e) {
            logger.exception(e);
        }
        return ti;
    }

    public boolean postJsonConfig(String testId,JSONObject jsonConfig){
        try {
            getAPI().postJsonConfig(userKey, testId, jsonConfig);
        } catch (Exception e) {
            logger.warning("Problems with posting jsonConfiguration to server. Check URL and json configuration.");
            return false;
        }
        return true;
    }

    public BuildFinishedStatus validateServerTresholds() {
        JSONObject jo = null;
        boolean tresholdsValid=true;
        JSONObject result=null;
        try {
            jo=this.getAPI().getTresholds(userKey,session);
            result=jo.getJSONObject("result");
            tresholdsValid=result.getJSONObject("data").getBoolean("success");
        } catch (JSONException je) {
            logger.warning("Failed to get tresholds for  session=" + session);
        }finally {
            return tresholdsValid? BuildFinishedStatus.FINISHED_SUCCESS:BuildFinishedStatus.FINISHED_FAILED;
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
}
