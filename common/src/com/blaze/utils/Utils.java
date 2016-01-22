package com.blaze.utils;

import com.blaze.APIFactory;
import com.blaze.BzmServiceManager;
import com.blaze.api.BlazemeterApi;
import com.blaze.runner.JsonConstants;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.*;
import java.util.Properties;

/**
 * Created by dzmitrykashlach on 9/12/14.
 */
public class Utils {

    private static Logger logger = LoggerFactory.getLogger("com.blazemeter");

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

    public static BzmServiceManager.ApiVersion autoDetectApiVersion(String userKey,
                                                                    String blazeMeterUrl) {
        BlazemeterApi api = null;
        BzmServiceManager.ApiVersion detectedApiVersion = null;
        api = APIFactory.getAPI(userKey,
                                blazeMeterUrl,
                               "v3");
        boolean isV3 = false;
        try {
            isV3 = api.getUser().getJSONObject("features").getBoolean("v3");
            if (isV3) {
                detectedApiVersion=BzmServiceManager.ApiVersion.v3;
            } else {
                detectedApiVersion=BzmServiceManager.ApiVersion.v2;
            }
        } catch (JSONException je) {
            Utils.logger.error("Error occuired while auto-detecting API version",je);
        } catch (NullPointerException npe) {
            Utils.logger.error("Error occuired while auto-detecting API version",npe);
            return BzmServiceManager.ApiVersion.v3;
        }
        return detectedApiVersion;
    }
}
