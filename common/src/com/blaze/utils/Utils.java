package com.blaze.utils;

import com.blaze.APIFactory;
import com.blaze.BzmServiceManager;
import com.blaze.api.BlazemeterApi;
import com.blaze.runner.JsonConstants;
import com.blaze.testresult.TestResult;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Properties;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public class Utils {

    private Utils(){}


    public static int getTestDuration(String apiKey,BlazemeterApi api, String testId, BuildProgressLogger logger){
        int testDuration=-1;
        try {
            JSONObject jo = api.getTestInfo(testId,logger);
            JSONObject result = jo.getJSONObject(JsonConstants.RESULT);
            JSONObject configuration = result.getJSONObject(JsonConstants.CONFIGURATION);
            JSONObject plugins = configuration.getJSONObject(JsonConstants.PLUGINS);
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject(JsonConstants.OVERRIDE);
            testDuration=override.getInt(JsonConstants.DURATION);
        } catch (JSONException je) {
            logger.message("Failed to get testDuration from server: "+ je);
            logger.exception(je);
        } catch (Exception e) {
            logger.message("Failed to get testDuration from server: "+ e);
            logger.exception(e);
        }
        return testDuration;
    }

    public static void updateTestDuration(BlazemeterApi api, String testId, String updDuration, BuildProgressLogger logger) {
        try {
            JSONObject jo = api.getTestInfo(testId,logger);
            JSONObject result = jo.getJSONObject(JsonConstants.RESULT);
            JSONObject configuration = result.getJSONObject(JsonConstants.CONFIGURATION);
            JSONObject plugins = configuration.getJSONObject(JsonConstants.PLUGINS);
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject(JsonConstants.OVERRIDE);
            override.put(JsonConstants.DURATION, Integer.parseInt(updDuration));
            api.putTestInfo(testId, result,logger);

        } catch (JSONException je) {
            logger.message("Received JSONException while saving testDuration: "+ je);
        } catch (Exception e) {
            logger.message("Received JSONException while saving testDuration: "+ e);
        }
    }

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }


    public static void saveReport(String report,
                                  String filePath,
                                  BuildProgressLogger logger
    ) {
        File reportFile = new File(filePath);
        try {
            if (!reportFile.exists()) {
                reportFile.createNewFile();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(reportFile));
            out.write(report);
            out.close();
        logger.message("Report was saved to "+reportFile.getAbsolutePath());
        } catch (FileNotFoundException fnfe) {
            logger.message("ERROR: Failed to save XML report to workspace " + fnfe.getMessage());
            logger.message("Unable to save XML report to workspace - check that test is finished on server or turn to support ");
            logger.exception(fnfe);
        } catch (IOException e) {
            logger.message("ERROR: Failed to save XML report to workspace " + e.getMessage());
            logger.message("Unable to save XML report to workspace - check that test is finished on server or turn to support ");
            logger.exception(e);
        }
    }


    public static void sleep(int sleepPeriod,BuildProgressLogger logger){
        try {
            Thread.currentThread().sleep(sleepPeriod);
        } catch (InterruptedException e) {
            logger.exception(e);
            logger.warning("Test was interrupted during sleeping");
        }
    }

    public static BzmServiceManager.ApiVersion autoDetectApiVersion(String userKey,String serverName,
                                              String serverPort,String username,
                                              String password,String blazeMeterUrl,
                                              BuildProgressLogger logger) {
        BlazemeterApi api = null;
        BzmServiceManager.ApiVersion detectedApiVersion = null;
        api = APIFactory.getAPI(userKey,serverName,
                                serverPort,username,
                                password,blazeMeterUrl,
                               "v3",logger);
        boolean isV3 = false;
        try {
            isV3 = api.getUser().getJSONObject("features").getBoolean("v3");
            if (isV3) {
                detectedApiVersion=BzmServiceManager.ApiVersion.v3;
            } else {
                detectedApiVersion=BzmServiceManager.ApiVersion.v2;
            }
        } catch (JSONException je) {
            logger.exception(je);
        } catch (NullPointerException npe) {
            logger.exception(npe);
            return BzmServiceManager.ApiVersion.v3;
        }
        return detectedApiVersion;
    }


    public static BuildFinishedStatus validateLocalTresholds(TestResult testResult,
                                                String errorUnstableThreshold,
                                                String errorFailedThreshold,
                                                String responseTimeUnstableThreshold,
                                                String responseTimeFailedThreshold,
                                                BuildProgressLogger logger){

        BuildFinishedStatus buildStatus=null;
        logger.message("Going to validate local tresholds...");
        try{

            int responseTimeUnstable = Integer.valueOf(responseTimeUnstableThreshold==null||
                    responseTimeUnstableThreshold.isEmpty()
                    ?"-1":responseTimeUnstableThreshold);
            int responseTimeFailed = Integer.valueOf(responseTimeFailedThreshold==null||
                    responseTimeFailedThreshold.isEmpty()
                    ?"-1":responseTimeFailedThreshold);
            int errorUnstable = Integer.valueOf(errorUnstableThreshold==null||
                    errorUnstableThreshold.isEmpty()
                    ?"-1":errorUnstableThreshold);
            int errorFailed = Integer.valueOf(errorFailedThreshold==null||
                    errorFailedThreshold.isEmpty()
                    ?"-1":errorFailedThreshold);


            if (errorUnstable < 0) {
                logger.message("ErrorUnstable percentage validation will be skipped: value was not set in configuration");
            }

            if (errorFailed < 0) {
                logger.message("ErrorFailed percentage validation will be skipped: value was not set in configuration");
            }

            if (responseTimeUnstable < 0) {
                logger.message("ResponseTimeUnstable validation will be skipped: value was not set in configuration");
            }

            if (responseTimeFailed < 0) {
                logger.message("ResponseTimeFailed validation will be skipped: value was not set in configuration");
            }

            if(responseTimeFailed < 0&responseTimeUnstable < 0&errorFailed < 0&errorUnstable < 0){
                buildStatus=null;
                return buildStatus;
            }

            if (responseTimeUnstable >= 0 & testResult.getErrorPercentage() > responseTimeUnstable) {
                logger.message("Validating reponseTimeUnstable...\n");
                logger.message("Average response time="+testResult.getAverage()+" is higher than responseTimeUnstable treshold="
                        +responseTimeUnstable+"\n");
                logger.message("Marking build as FINISHED_WITH_PROBLEMS");
                buildStatus=BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            }

            if (errorUnstable >= 0 & testResult.getErrorPercentage() > errorUnstable) {
                logger.message("Validating errorPercentageUnstable...\n");
                logger.message("Error percentage="+testResult.getErrorPercentage()+" is higher than errorPercentageUnstable treshold="+
                        errorUnstable+"\n");
                logger.message("Marking build as FINISHED_WITH_PROBLEMS");
                buildStatus=BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            }


            if (responseTimeFailed >= 0 & testResult.getAverage() >= responseTimeFailed) {
                logger.message("Validating reponseTimeFailed...\n");
                logger.message("Average response time"+testResult.getAverage()+" is higher than responseTimeFailure treshold"+responseTimeFailed+"\n");
                logger.message("Marking build as FINISHED_FAILED");
                buildStatus=BuildFinishedStatus.FINISHED_FAILED;
                return buildStatus;
            }

            if (errorFailed >= 0 & testResult.getErrorPercentage() >= errorFailed) {
                logger.message("Validating errorPercentageFailed...\n");
                logger.message("Error percentage"+testResult.getErrorPercentage()+" is higher than errorPercentageFailed treshold"+errorFailed+"\n");
                logger.message("Marking build as FINISHED_FAILED");
                buildStatus=BuildFinishedStatus.FINISHED_FAILED;
                return buildStatus;
            }


        }catch (Exception e){
            logger.message("Unexpected error occured while validating local tresholds");
            logger.exception(e);
        }finally {
            return buildStatus;
        }
    }
}
