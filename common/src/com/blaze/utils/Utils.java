package com.blaze.utils;

import com.blaze.api.BlazemeterApi;
import jetbrains.buildServer.agent.BuildProgressLogger;
import org.json.JSONException;
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
}
