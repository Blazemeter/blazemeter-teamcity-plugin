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

package com.blaze.utils;

import jetbrains.buildServer.agent.BuildProgressLogger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.*;
import java.util.Properties;

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
