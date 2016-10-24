/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blaze;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
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
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BzmServiceManager {
    private static BzmServiceManager bzmServiceManager=null;
    private static final int CHECK_INTERVAL = 60000;

    private String userKey;
    private String blazeMeterUrl;
    private BuildProgressLogger logger;
    private BlazemeterApi api;
    private String masterId;
    private JSONObject aggregate;

    public BzmServiceManager() {
    }

    private BzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {

        this.userKey = buildSharedMap.get(Constants.USER_KEY);
        this.blazeMeterUrl = buildSharedMap.get(Constants.BLAZEMETER_URL);
        this.api = new BlazemeterApiV3Impl(userKey, blazeMeterUrl);
        this.logger = logger;
    }

    public static BzmServiceManager getBzmServiceManager(Map<String, String> buildSharedMap,BuildProgressLogger logger) {
        if(bzmServiceManager==null){
            bzmServiceManager=new BzmServiceManager(buildSharedMap,logger);
        }else{
            bzmServiceManager.setUserKey(buildSharedMap.get(Constants.USER_KEY));
            bzmServiceManager.setBlazeMeterUrl(buildSharedMap.get(Constants.BLAZEMETER_URL));
            bzmServiceManager.setLogger(logger);
            if(bzmServiceManager.api ==null){
                BlazemeterApi blazemeterAPI = new BlazemeterApiV3Impl(buildSharedMap.get(Constants.USER_KEY),
                    buildSharedMap.get(Constants.BLAZEMETER_URL));
            bzmServiceManager.setApi(blazemeterAPI);
            }
        }
        return bzmServiceManager;
    }

    @NotNull
    public String getDebugKey() {
        return "Debug Key";
    }

    public LinkedHashMultimap<String, String> getTests() {
        api =new BlazemeterApiV3Impl(this.userKey,this.blazeMeterUrl);
        // added on Jacob's request for issue investigation
        System.out.println("TeamCity plugin: Trying to get tests with userKey=" + this.userKey.substring(0,4) + " and server=" + this.blazeMeterUrl);

        LinkedHashMultimap tests=LinkedHashMultimap.create();
        try {
            // added on Jacob's request for issue investigation
            System.out.println("TeamCity plugin: Requesting tests from server " + this.blazeMeterUrl);
            tests.putAll(this.api.getTestList());
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
            TestType testType=this.getTestType(testId);
            masterId = this.api.startTest(testId,testType);
            this.masterId=masterId;
        } catch (JSONException e) {
            logger.error("Exception while starting BlazeMeter Test: " + e.getMessage());
            logger.exception(e);
        } catch (NullPointerException e){
            logger.exception(e);
        }
        return masterId;
    }

    public void junitXml(String masterId, BuildRunnerContext buildRunnerContext) {
        String junitReport = "";
        logger.message("Requesting JUNIT report from server, masterId=" + masterId);
        try {
            junitReport = this.api.retrieveJUNITXML(masterId);
            logger.message("Received Junit report from server");
            String reportFilePath = buildRunnerContext.getWorkingDirectory() + "/" + masterId + ".xml";
            logger.message("Saving junit report to " +reportFilePath);
            Utils.saveReport(junitReport, reportFilePath, logger);
        } catch (Exception e) {
            logger.message("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
        }
    }



    public boolean active(String testId, BuildProgressLogger logger){
        return api.active(testId);
    }

    public boolean stopMaster(String masterId, BuildProgressLogger logger) {
        boolean terminate = false;
        try {
            int statusCode = api.getTestMasterStatusCode(masterId);
            if (statusCode < 100 & statusCode != 0) {
                api.terminateTest(masterId);
                terminate = true;
            }
            if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                api.stopTest(masterId);
                terminate = false;
            }
        } catch (Exception e) {
            logger.warning("Error while trying to stop test with testId=" + masterId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }

    public void jtlReports(String masterId, BuildRunnerContext ctxt) {
        List<String> sessionsIds = this.api.getListOfSessionIds(masterId);
        for (String s : sessionsIds) {
            this.retrieveJtlForSession(s,ctxt);
        }
    }


    public void retrieveJtlForSession(String sessionId, BuildRunnerContext ctxt){
            JSONObject jo = this.api.retrieveJTLZIP(sessionId);
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
                String jtlFilePath = ctxt.getWorkingDirectory() + "/" + sessionId + ".zip";

                File jtlZip = new File(jtlFilePath);
                URL url = new URL(dataUrl);
                FileUtils.copyURLToFile(url, jtlZip);
                logger.message("Downloading JTLZIP from " + url);
                logger.message("JTL zip location: " + jtlZip.getCanonicalPath());
            } catch (JSONException e) {
                logger.warning("Unable to get  JTLZIP: "+e.getMessage());
            } catch (MalformedURLException e) {
                logger.warning("Unable to get  JTLZIP: "+e.getMessage());
            } catch (IOException e) {
                logger.warning("Unable to get  JTLZIP: "+e.getMessage());
            } catch (NullPointerException e) {
                logger.warning("Unable to get  JTLZIP: "+e.getMessage());
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
            this.aggregate=this.api.testReport(this.masterId);
            testResult = new TestResult(this.aggregate);
        } catch (JSONException e) {
            logger.warning("Failed to get aggregate report from server "+e);
        } catch (IOException e) {
            logger.warning("Failed to get aggregate report from server"+e);
        } catch (NullPointerException e) {
            logger.warning("Failed to get aggregate report from server"+e);
        }
        finally {
            return testResult;
        }
    }


    public TestStatus masterStatus(String testId) {
        TestStatus status=null;
        try {
            status=this.api.masterStatus(testId);
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
            jo= api.getCIStatus(masterId);
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
            jo = this.api.generatePublicToken(masterId);
            if(jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)){
                JSONObject result=jo.getJSONObject(JsonConstants.RESULT);
                publicToken=result.getString("publicToken");
                reportUrl=this.api.getBlazeMeterURL()+"/app/?public-token="+publicToken+"#masters/"+masterId+"/summary";
            }else{
                logger.warning("Problems with generating public-token for report URL: "+jo.get(JsonConstants.ERROR).toString());
                reportUrl=this.api.getBlazeMeterURL()+"/app/#masters/"+masterId+"/summary";
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
            JSONArray result=this.api.getTestsJSON().getJSONArray(JsonConstants.RESULT);
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

    public void waitNotActive(String testId){

        boolean active=true;
        int activeCheck=1;
        while(active&&activeCheck<11){
            try {
                Thread.currentThread().sleep(CHECK_INTERVAL);
            } catch (InterruptedException e) {
                logger.warning("Thread was interrupted during sleep()");
                logger.warning("Received interrupted Exception: " + e.getMessage());
                break;
            }
            logger.message("Checking, if test is active, testId="+testId+", retry # "+activeCheck);
            active=this.api.active(testId);
            activeCheck++;
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

    public BuildProgressLogger getLogger() {
        return logger;
    }

    public void setLogger(BuildProgressLogger logger) {
        this.logger = logger;
    }

    public BlazemeterApi getApi() {
        return api;
    }

    public void setApi(BlazemeterApi api) {
        this.api = api;
    }
}
