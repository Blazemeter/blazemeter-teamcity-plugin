package com.blaze;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.api.TestType;
import com.blaze.runner.CIStatus;
import com.blaze.runner.Constants;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.google.common.collect.LinkedHashMultimap;
import jetbrains.buildServer.agent.BuildProgressLogger;

import jetbrains.buildServer.agent.BuildRunnerContext;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marcel Milea
 */
public class BzmServiceManager {
    private static BzmServiceManager bzmServiceManager=null;

    private String userKey;
    private String blazeMeterUrl;
    private String blazeMeterApiVersion;
    private BuildProgressLogger logger;
    private BlazemeterApi blazemeterAPI;
    private String masterId;
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
        String masterId=null;
        try {
            TestType testType=TestType.http;
            if(!this.blazeMeterApiVersion.equals("v2")){
                testType=this.getTestType(testId);
            }
            masterId = this.blazemeterAPI.startTest(testId,testType);
            this.masterId=masterId;
        } catch (JSONException e) {
            logger.error("Exception while starting BlazeMeter Test: " + e.getMessage());
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return masterId;
    }

    public void retrieveJUNITXML(String masterId, BuildRunnerContext buildRunnerContext) {
        String junitReport = "";
        logger.message("Requesting JUNIT report from server, masterId=" + masterId);
        try {
                junitReport = this.blazemeterAPI.retrieveJUNITXML(masterId);
                logger.message("Received Junit report from server.... Saving it to the disc...");
                String reportFilePath = buildRunnerContext.getWorkingDirectory() + "/" + masterId + ".xml";
                Utils.saveReport(junitReport, reportFilePath, logger);
        } catch (Exception e) {
            logger.message("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
        }
    }



    public boolean active(String testId, BuildProgressLogger logger){
        return blazemeterAPI.active(testId);
    }

    public boolean stopMaster(String masterId, BuildProgressLogger logger) {
        boolean terminate = false;
        try {
            int statusCode = blazemeterAPI.getTestMasterStatusCode(masterId);
            if (statusCode < 100 & statusCode != 0) {
                blazemeterAPI.terminateTest(masterId);
                terminate = true;
            }
            if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                blazemeterAPI.stopTest(masterId);
                terminate = false;
            }
        } catch (Exception e) {
            logger.warning("Error while trying to stop test with testId=" + masterId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }
/*

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
*/

    /**
     * Get report results.
     *
     * @param logger
     * @return -1 fail, 0 success, 1 unstable
     */
    public TestResult getReport(BuildProgressLogger logger) {
        TestResult testResult = null;
        try {
            this.aggregate=this.blazemeterAPI.testReport(this.masterId);
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


    public TestStatus getTestSessionStatus(String testId) {
        TestStatus status=null;
        try {
            status=this.blazemeterAPI.getTestStatus(testId);
        } catch (JSONException e) {
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return status;
    }



    public CIStatus validateCIStatus(String masterId,BuildProgressLogger logger){
        CIStatus ciStatus= CIStatus.success;
        JSONObject jo;
        JSONArray failures=new JSONArray();
        JSONArray errors=new JSONArray();
        try {
            jo=blazemeterAPI.getCIStatus(masterId);
            logger.message("Test status object = " + jo.toString());
            failures=jo.getJSONArray(JsonConstants.FAILURES);
            errors=jo.getJSONArray(JsonConstants.ERRORS);
        } catch (JSONException je) {
            logger.message("No thresholds on server: setting 'success' for CIStatus ");
        } catch (Exception e) {
            logger.message("No thresholds on server: setting 'success' for CIStatus ");
        }finally {
            if(errors.length()>0){
                logger.message("Having errors while test status validation...");
                logger.message("Errors: " + errors.toString());
                ciStatus=CIStatus.errors;
                logger.message("Setting CIStatus="+CIStatus.errors.name());
                return ciStatus;
            }
            if(failures.length()>0){
                logger.message("Having failures while test status validation...");
                logger.message("Failures: " + failures.toString());
                ciStatus=CIStatus.failures;
                logger.message("Setting CIStatus="+CIStatus.failures.name());
                return ciStatus;
            }
            logger.message("No errors/failures while validating CIStatus: setting "+CIStatus.success.name());
        }
        return ciStatus;
    }

    public String getReportUrl(String masterId) {
        JSONObject jo=null;
        String publicToken="";
        String reportUrl=null;
        try {
            jo = this.blazemeterAPI.generatePublicToken(masterId);
            if(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
                publicToken=result.getString("publicToken");
                reportUrl=this.blazemeterAPI.getBlazeMeterURL()+"/app/?public-token="+publicToken+"#masters/"+masterId+"/summary";
            }else{
                logger.warning("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                reportUrl=this.blazemeterAPI.getBlazeMeterURL()+"/app/#masters/"+masterId+"/summary";
            }

        } catch (Exception e){
            logger.warning("Problems with generating public-token for report URL");
        }finally {
            return reportUrl;
        }}

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

    public String masterId() {
        return masterId;
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
