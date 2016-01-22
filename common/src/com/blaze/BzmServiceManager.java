package com.blaze;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.api.TestType;
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
    private BuildProgressLogger logger;
    private BlazemeterApi blazemeterAPI;
    private String session;
    private JSONObject aggregate;

    public BzmServiceManager() {
    }

    private BzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {

        this.userKey = buildSharedMap.get(Constants.USER_KEY);
        this.blazeMeterUrl = buildSharedMap.get(Constants.BLAZEMETER_URL);
        this.blazeMeterApiVersion = buildSharedMap.get(Constants.BLAZEMETER_API_VERSION);
        this.blazemeterAPI = APIFactory.getAPI(userKey, blazeMeterUrl, blazeMeterApiVersion);
        this.blazeMeterApiVersion=(this.blazemeterAPI instanceof BlazemeterApiV3Impl)?"v3":"v2";
        this.logger = logger;
    }

    public static BzmServiceManager getBzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager(buildSharedMap,logger);
        }else{
            bzmServiceManager.setUserKey(buildSharedMap.get(Constants.USER_KEY));
            bzmServiceManager.setBlazeMeterUrl(buildSharedMap.get(Constants.BLAZEMETER_URL));
            bzmServiceManager.blazeMeterApiVersion = buildSharedMap.get(Constants.BLAZEMETER_API_VERSION);
            bzmServiceManager.setLogger(logger);
            if(bzmServiceManager.blazemeterAPI==null){
                BlazemeterApi blazemeterAPI = APIFactory.getAPI(buildSharedMap.get(Constants.USER_KEY),
                    buildSharedMap.get(Constants.BLAZEMETER_URL),
                    buildSharedMap.get(Constants.BLAZEMETER_API_VERSION));
            bzmServiceManager.setBlazemeterAPI(blazemeterAPI);
            }
        }
        return bzmServiceManager;
    }

    @NotNull
    public String getDebugKey() {
        return "Debug Key";
    }

    public String prepareTest(String testId,String jsonConfiguration,String testDuration){
        JSONObject jsonConf = null;
        TestType testType=TestType.http;
        if(!this.blazeMeterApiVersion.equals("v2")){
            testType=this.getTestType(testId);
        }
        this.blazemeterAPI.getUrlManager().testType(testType);
        if (!testType.equals(TestType.multi)) {
            if (jsonConfiguration != null && !jsonConfiguration.isEmpty()) {
                try {
                    File jsonF = new File(jsonConfiguration);
                    String jsonStr = new String(FileUtils.readFileToString((jsonF)));
                    jsonConf = new JSONObject(jsonStr);
                } catch (Exception e) {
                    logger.warning("Failed to read JSON Configuration from " + jsonConfiguration);
                }
            } else {
                if (!PropertiesUtil.isEmptyOrNull(testDuration)) {
                    try {
                        logger.message("Attempting to update testDuration for test with id:" + testId);
                        bzmServiceManager.updateTestDuration(testId, testDuration, logger);
                    } catch (NumberFormatException nfe) {
                        logger.exception(nfe);
                        logger.warning("Test duration is not a number.");
                        return "Test duration is not a number.";
                    }
                }
            }
        }else{
            logger.warning("Updating test-configuration will be skipped: test-type is 'multi'");
        }
        return testId;
    }




    public LinkedHashMultimap<String, String> getTests() {
        if(this.blazemeterAPI==null){
            blazemeterAPI=APIFactory.getAPI(this.userKey,this.blazeMeterUrl,this.blazeMeterApiVersion);
        }
        // added on Jacob's request for issue investigation
        System.out.println("TeamCity plugin: Trying to get tests with userKey=" + this.userKey.substring(0,4) + " and server=" + this.blazeMeterUrl);

        LinkedHashMultimap tests=LinkedHashMultimap.create();
        try {
            // added on Jacob's request for issue investigation
            System.out.println("TeamCity plugin: Requesting tests from server " + this.blazeMeterUrl);
            tests.putAll(this.blazemeterAPI.getTestList());
            // added on Jacob's request for issue investigation
            System.out.println("TeamCity plugin: Received " + tests.entries().size() + " tests");
            for(Object key : tests.keySet()){
                System.out.println("TeamCity plugin: Key "+key + ", value"+tests.get(key));
            }
        } catch (IOException e) {
            // added on Jacob's request for issue investigation
            System.out.println(e);
            System.out.println("TeamCity plugin:IOException: Failed to get tests from server="
                    + this.blazeMeterUrl + " and userKey=" + this.userKey+": "+e.getMessage());
        } catch (JSONException e) {
            System.out.println(e);
            System.out.println("TeamCity plugin:JSONException: Failed to get tests from server="
                    + this.blazeMeterUrl + " and userKey=" + this.userKey+": "+e.getMessage());
            // added on Jacob's request for issue investigation
        } catch (NullPointerException e){
            // added on Jacob's request for issue investigation
            System.out.println(e);
            System.out.println("TeamCity plugin:JSONException: Failed to get tests from server="
                    + this.blazeMeterUrl + " and userKey=" + this.userKey+": "+e.getMessage());
        }finally {
            return tests;
        }
    }

    public Map<String, Collection<String>> getTestsAsMap(){
        return getTests().asMap();
    }

    public String startTest(String testId, BuildProgressLogger logger) {
        String session=null;
        try {
            TestType testType=TestType.http;
            if(!this.blazeMeterApiVersion.equals("v2")){
                testType=this.getTestType(testId);
            }
            session = this.blazemeterAPI.startTest(testId,testType);
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
        if(this.blazemeterAPI instanceof BlazemeterApiV3Impl) {
            Utils.updateTestDuration(this.blazemeterAPI, testId, testDuration, logger);
        } else {
            logger.message("Updating test duration is not implemented for D6");
        }
    }

    public void retrieveJUNITXML(String session, BuildRunnerContext buildRunnerContext) {
        TestType testType = this.blazemeterAPI.getUrlManager().testType();
        if (!testType.equals(TestType.multi)) {
            String junitReport = this.blazemeterAPI.retrieveJUNITXML(session);
            logger.message("Received Junit report from server.... Saving it to the disc...");
            String reportFilePath = buildRunnerContext.getWorkingDirectory() + "/" + session + ".xml";
            Utils.saveReport(junitReport, reportFilePath, logger);

        } else {
            logger.warning("JUNIT report will not be downloaded: test-type is 'multi' ");
        }
    }



    public boolean stopTestSession(String testId, BuildProgressLogger logger) {
        boolean terminate = false;
        try {
            TestType testType = this.blazemeterAPI.getUrlManager().testType();
            if (testType != TestType.multi) {
                int statusCode = this.blazemeterAPI.getTestSessionStatusCode(session);
                if (statusCode < 100 & statusCode != 0) {
                    this.blazemeterAPI.terminateTest(testId);
                    terminate = true;
                }
                if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                    this.blazemeterAPI.stopTest(testId);
                    terminate = false;
                }
            } else {
                this.blazemeterAPI.stopTest(testId);
                terminate = false;
            }

        } catch (Exception e) {
            logger.warning("Error while trying to stop test with testId=" + testId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }

    public void retrieveJTL(String session,BuildRunnerContext buildRunnerContext){
        TestType testType = this.blazemeterAPI.getUrlManager().testType();
        if (!testType.equals(TestType.multi)) {

            JSONObject jo = this.blazemeterAPI.retrieveJTLZIP(session);
            String dataUrl = null;
            try {
                JSONArray data = jo.getJSONObject(JsonConstants.RESULT).getJSONArray(JsonConstants.DATA);
                for (int i = 0; i < data.length(); i++) {
                    String title = data.getJSONObject(i).getString("title");
                    if (title.equals("Zip")) {
                        dataUrl = data.getJSONObject(i).getString(JsonConstants.DATA_URL);
                        break;
                    }
                }
                String jtlFilePath = buildRunnerContext.getWorkingDirectory() + "/" + session + ".zip";

                File jtlZip = new File(jtlFilePath);
                URL url = new URL(dataUrl + "?api_key=" + userKey);
                FileUtils.copyURLToFile(url, jtlZip);
                logger.message("Downloading JTLZIP from " + url + "to " + jtlZip.getCanonicalPath());
            } catch (JSONException e) {
                logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
            } catch (MalformedURLException e) {
                logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
            } catch (IOException e) {
                logger.warning("Unable to get  JTLZIP: check test status, try to download manually");
            } catch (NullPointerException e) {
                logger.exception(e);
            }
        }else {
            logger.warning("JTLZIP will not be downloaded: test-type is 'multi' ");
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
            logger.warning("Failed to get aggregate report from server, local thresholds won't be validated");
        } catch (IOException e) {
            logger.warning("Failed to get aggregate report from server, local thresholds won't be validated");
        } catch (NullPointerException e) {
            logger.warning("Failed to get aggregate report from server, local thresholds won't be validated");
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

    public TestInfo getTestSessionStatus(String testId) {
        TestInfo ti=null;
        try {
            ti=this.blazemeterAPI.getTestInfo(testId);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return ti;
    }



    public BuildFinishedStatus validateServerTresholds() {
        TestType testType = this.blazemeterAPI.getUrlManager().testType();
        if (!testType.equals(TestType.multi)) {
            JSONObject jo = null;
            boolean thresholdsValid = true;
            JSONObject result = null;
            logger.message("Going to validate server thresholds...");
            try {
                jo = this.blazemeterAPI.getTresholds(session);
                result = jo.getJSONObject(JsonConstants.RESULT);
                thresholdsValid = result.getJSONObject(JsonConstants.DATA).getBoolean("success");
            } catch (NullPointerException e) {
                logger.message("Server thresholds validation was not executed due to NullPointerException");
                logger.exception(e);
                thresholdsValid=false;
            } catch (JSONException je) {
                logger.message("Server thresholds validation was not executed:failed to get thresholds for  session=" + session);
                thresholdsValid=false;
            } finally {
                logger.message("Server thresholds validation " +
                        (thresholdsValid ? "passed. Marking build as PASSED" : "failed. Marking build as FAILED"));
                return thresholdsValid ? BuildFinishedStatus.FINISHED_SUCCESS : BuildFinishedStatus.FINISHED_FAILED;
            }
        }else{
            logger.warning("Server thresholds won't be validated: test-type is 'multi' ");
            return  BuildFinishedStatus.FINISHED_SUCCESS;
        }
    }

    public String getReportUrl(String sessionId) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            TestType testType=this.blazemeterAPI.getUrlManager().testType();
            String reportType=(testType!=null&&testType.equals(TestType.multi))?"masters":"reports";
            jo = this.blazemeterAPI.generatePublicToken(sessionId);
            if(jo.get("error").equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject("result");
                publicToken=result.getString("publicToken");
                reportUrl=this.blazeMeterUrl+"/app/?public-token="+publicToken+"#"+reportType+"/"+sessionId+"/summary";
            }else{
                logger.error("Problems with generating public-token for report URL: " + jo.get("error").toString());
                reportUrl=this.blazeMeterUrl+"/app/#reports/"+sessionId+"/summary";
            }
        } catch (Exception e){
            logger.error("Problems with generating public-token for report URL: " + e);
        }finally {
            return reportUrl;
        }
    }

    private TestType getTestType(String testId){
        TestType testType=TestType.http;
        logger.message("Detecting testType....");
        try{
            JSONArray result=this.blazemeterAPI.getTestsJSON().getJSONArray(JsonConstants.RESULT);
            int resultLength=result.length();
            for (int i=0;i<resultLength;i++){
                JSONObject jo=result.getJSONObject(i);
                if(String.valueOf(jo.getInt(JsonConstants.ID)).equals(testId)){
                    testType= TestType.valueOf(jo.getString(JsonConstants.TYPE));
                    logger.message("Received testType=" + testType.toString() + " for testId=" + testId);
                }
            }
        } catch (Exception e) {
            logger.message("Error while detecting type of test:" + e);
        }finally {
            return testType;
        }
    }



    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
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
