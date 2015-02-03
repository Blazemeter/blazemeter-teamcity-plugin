package com.blaze.utils;

import com.blaze.api.BlazemeterApi;
import com.blaze.runner.JsonConstants;
import com.blaze.testresult.TestResult;
import com.intellij.openapi.vfs.FilePath;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
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
            JSONObject jo = api.getTestInfo(apiKey,testId,logger);
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

    public static void updateTestDuration(String apiKey, BlazemeterApi api, String testId, String updDuration, BuildProgressLogger logger) {
        try {
            JSONObject jo = api.getTestInfo(apiKey,testId,logger);
            JSONObject result = jo.getJSONObject(JsonConstants.RESULT);
            JSONObject configuration = result.getJSONObject(JsonConstants.CONFIGURATION);
            JSONObject plugins = configuration.getJSONObject(JsonConstants.PLUGINS);
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject(JsonConstants.OVERRIDE);
            override.put(JsonConstants.DURATION, Integer.parseInt(updDuration));
            api.putTestInfo(apiKey,testId, result,logger);

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


    public static void saveReport(String filename,
                                  String report,
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

        } catch (FileNotFoundException fnfe) {
            logger.message("ERROR: Failed to save XML report to workspace " + fnfe.getMessage());
            logger.message("Unable to save XML report to workspace - check that test is finished on server or turn to support ");
        } catch (IOException e) {
            logger.message("ERROR: Failed to save XML report to workspace " + e.getMessage());
            logger.message("Unable to save XML report to workspace - check that test is finished on server or turn to support ");
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

    public static BuildFinishedStatus validateLocalTresholds(TestResult testResult,
                                                String errorUnstableThreshold,
                                                String errorFailedThreshold,
                                                String responseTimeUnstableThreshold,
                                                String responseTimeFailedThreshold,
                                                BuildProgressLogger logger){

        BuildFinishedStatus buildStatus = BuildFinishedStatus.FINISHED_SUCCESS;
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
                    ?"-1":errorUnstableThreshold);

            if (responseTimeUnstable >= 0 & testResult.getAverage() > responseTimeUnstable &
                    testResult.getAverage() < responseTimeFailed) {
                logger.message("Validating reponseTimeUnstable...\n");
                logger.message("Average response time is higher than responseTimeUnstable treshold\n");
                logger.message("Marking build as FINISHED_WITH_PROBLEMS");
                buildStatus=BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            }

            if (errorUnstable >= 0 & testResult.getErrorPercentage() > errorUnstable &
                    testResult.getAverage() < errorFailed) {
                logger.message("Validating errorPercentageUnstable...\n");
                logger.message("Error percentage is higher than errorPercentageUnstable treshold\n");
                logger.message("Marking build as FINISHED_WITH_PROBLEMS");
                buildStatus=BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            }


            if (responseTimeFailed >= 0 & testResult.getAverage() >= responseTimeFailed) {
                logger.message("Validating reponseTimeFailed...\n");
                logger.message("Average response time is higher than responseTimeFailure treshold\n");
                logger.message("Marking build as failed");
                buildStatus=BuildFinishedStatus.FINISHED_FAILED;
            }

            if (errorFailed >= 0 & testResult.getAverage() >= errorFailed) {
                logger.message("Validating errorPercentageUnstable...\n");
                logger.message("Error percentage is higher than errorPercentageUnstable treshold\n");
                logger.message("Marking build as failed");
                buildStatus=BuildFinishedStatus.FINISHED_FAILED;
            }

            if (errorUnstable < 0) {
                logger.message("ErrorUnstable percentage validation was skipped: value was not set in configuration");
            }

            if (errorFailed < 0) {
                logger.message("ErrorFailed percentage validation was skipped: value was not set in configuration");
            }

            if (responseTimeUnstable < 0) {
                logger.message("ResponseTimeUnstable validation was skipped: value was not set in configuration");
            }

            if (responseTimeFailed < 0) {
                logger.message("ResponseTimeFailed validation was skipped: value was not set in configuration");
            }

            if(responseTimeFailed < 0&responseTimeUnstable < 0&errorFailed < 0&errorUnstable < 0){
                buildStatus=null;
            }

        }catch (Exception e){
            logger.message("Unexpected error occured while validating local tresholds. Check that test was finished correctly or turn to customer support");
        }finally {
            return buildStatus;
        }
    }
}
