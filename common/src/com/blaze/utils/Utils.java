package com.blaze.utils;

import com.blaze.api.BlazemeterApi;
import com.blaze.testresult.TestResult;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public class Utils {

    private Utils(){}


    public static String getFileContents(String fn) {

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();
        File aFile = new File(fn);

        try {

            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            BufferedReader input = new BufferedReader(new FileReader(aFile));

            try {
                String line;    // not declared within while loop

                /*
                 *         readLine is a bit quirky : it returns the content of a line
                 *         MINUS the newline. it returns null only for the END of the
                 *         stream. it returns an empty String if two newlines appear in
                 *         a row.
                 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ignored) {
        }

        return contents.toString();
    }

    public static int getTestDuration(String apiKey,BlazemeterApi api, String testId, BuildProgressLogger logger){
        int testDuration=-1;
        try {
            JSONObject jo = api.getTestInfo(apiKey,testId,logger);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            testDuration=override.getInt("duration");
        } catch (JSONException je) {
            logger.message("Failed to get testDuration from server: "+ je);
            logger.exception(je);
        } catch (Exception e) {
            logger.message("Failed to get testDuration from server: "+ e);
            logger.exception(e);
        }
        return testDuration;
    }

    public static void updateTest(String apiKey,BlazemeterApi api, String testId, int updDuration, BuildProgressLogger logger) {
        try {
            JSONObject jo = api.getTestInfo(apiKey,testId,logger);
            JSONObject result = jo.getJSONObject("result");
            JSONObject configuration = result.getJSONObject("configuration");
            JSONObject plugins = configuration.getJSONObject("plugins");
            String type = configuration.getString("type");
            JSONObject options = plugins.getJSONObject(type);
            JSONObject override = options.getJSONObject("override");
            override.put("duration", String.valueOf(updDuration));
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

            int responseTimeUnstable = Integer.valueOf(responseTimeUnstableThreshold.isEmpty()
                    ?"-1":responseTimeUnstableThreshold);

            int responseTimeFailed = Integer.valueOf(responseTimeFailedThreshold.isEmpty()
                    ?"-1":responseTimeFailedThreshold);
            int errorUnstable = Integer.valueOf(errorUnstableThreshold.isEmpty()
                    ?"-1":errorUnstableThreshold);
            int errorFailed = Integer.valueOf(errorFailedThreshold.isEmpty()
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
