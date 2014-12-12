package com.blaze;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV2Impl;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.entities.AggregateTestResult;
import com.blaze.entities.TestInfo;
import com.blaze.runner.Constants;
import jetbrains.buildServer.agent.BuildProgressLogger;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

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
    private String aggregate;

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
        if (blazemeterAPI == null) {
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

    public boolean startTest(String testId, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;
        try {
        do {
            json = getAPI().startTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
                logger.error("Could not start BlazeMeter Test with 5 attempts");
                return false;
            }
        } while (json == null);

            if (this.blazeMeterApiVersion.equals(ApiVersion.v2.name())) {
                if (!json.get("response_code").equals(200)) {
                    if (json.get("response_code").equals(500) && json.get("error").toString().startsWith("Test already running")) {
                        logger.error("Test already running, please stop it first");
                        return false;
                    }
                }
                this.session = json.get("session_id").toString();
            }else{
                this.session = json.getJSONObject("result").getJSONArray("sessionsId").getString(0);
            }
        } catch (JSONException e) {
            logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            logger.exception(e);
        }
        return true;
    }


    @SuppressWarnings("static-access")
    public boolean waitForReport(BuildProgressLogger logger) {
        //get testGetArchive information
        org.json.JSONObject json = null;
        for (int i = 0; i < 200; i++) {
            try {
                json = getAPI().aggregateReport(userKey, session);
                if (json.get("response_code").equals(404))
                    json = getAPI().aggregateReport(userKey, session);
                else
                    break;
            } catch (JSONException e) {
                logger.exception(e);
            } finally {
                try {
                    Thread.currentThread().sleep(5 * 1000);
                } catch (InterruptedException e) {
                    logger.exception(e);
                }
            }
        }

        aggregate = null;

        for (int i = 0; i < 30; i++) {
            try {
                if (!json.get("response_code").equals(200)) {
                    logger.error("Error: Requesting aggregate report response code:" + json.get("response_code"));
                }
                aggregate = json.getJSONObject("report").get("aggregate").toString();
            } catch (JSONException e) {
                logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
                logger.exception(e);
            }

            if (!aggregate.equals("null"))
                break;

            try {
                Thread.sleep(2 * 1000);
                json = getAPI().aggregateReport(userKey, session);
            } catch (InterruptedException e) {
                logger.exception(e);
            } catch (JSONException e) {
                logger.exception(e);
            }
        }

        if (aggregate == null) {
            logger.error("Error: Requesting aggregate is not available");
            return false;
        }

        return true;
    }

    /**
     * Get report results.
     *
     * @param logger
     * @return -1 fail, 0 success, 1 unstable
     */
    public int getReport(int errorFailedThreshold, int errorUnstableThreshold, int responseTimeFailedThreshold, int responseTimeUnstableThreshold, BuildProgressLogger logger) {
        AggregateTestResult aggregateTestResult;
        try {
            aggregateTestResult = AggregateTestResult.generate(aggregate);

        } catch (IOException e) {
            logger.exception(e);
            logger.error("Error: Requesting aggregate Test Result is not available");
            return -1;
        }

        if (aggregateTestResult == null) {
            logger.error("Error: Requesting aggregate Test Result is not available");
            return -1;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
            logger.error("Test ended with failure on error percentage threshold");
            return -1;
        } else if (errorUnstableThreshold >= 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
            logger.error("Test ended with unstable on error percentage threshold");
            return 1;
        }

        if (responseTimeFailedThreshold >= 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
            logger.error("Test ended with failure on response time threshold");
            return -1;
        } else if (responseTimeUnstableThreshold >= 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
            logger.error("Test ended with unstable on response time threshold");
            return 1;
        }

        return 0;
    }

    public boolean uploadJMX(String testId, String filename, String pathname) {
        boolean uploadJMX=false;
        try {
            uploadJMX=getAPI().uploadJmx(userKey, testId, filename, pathname);
        } catch (JSONException e) {
            logger.exception(e);
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

    public boolean publishReportArtifact(String pathname) {
        AggregateTestResult aggregateTestResult;
        try {
            aggregateTestResult = AggregateTestResult.generate(aggregate);
            if (aggregateTestResult == null) {
                return false;
            }

            double errorPercent = aggregateTestResult.getErrorPercentage();
            double AverageResponseTime = aggregateTestResult.getAverage();

            File file = new File(pathname);
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write("<build>");
            fw.write("<statisticValue key=\"blazeAvgResponseTime\" value=\"" + AverageResponseTime + "\"/>");
            fw.write("<statisticValue key=\"blazeThresholdTime\" value=\"" + errorPercent + "\"/>");
            fw.write("</build>");
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            logger.exception(e);
        }
        return false;
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
}
