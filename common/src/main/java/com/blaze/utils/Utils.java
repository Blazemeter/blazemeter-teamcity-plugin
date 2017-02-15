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

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.*;
import java.util.Properties;

public class Utils {

    private static Logger logger = LoggerFactory.getLogger("com.blazemeter");

    private Utils(){}


    public static String version() {
        Properties props = new Properties();
        try {
            props.load(Utils.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty("version", "N/A");
        }
        return props.getProperty("version");
    }


    public static String getTestId(String testId){
        try{
            return testId.substring(testId.lastIndexOf("(")+1,testId.lastIndexOf("."));
        }catch (Exception e){
            return testId;
        }
    }

    public static File mkReportDir(BuildRunnerContext context, String reportDir) {
        File reportFile = null;
        if (StringUtil.isNotEmpty(reportDir) && (reportDir.startsWith("/") | reportDir.matches("(^[a-zA-Z][:][\\\\].+)"))) {
            reportFile = new File(FilenameUtils.normalize(reportDir));
        } else {
            reportFile = new File(FilenameUtils.normalize(context.getWorkingDirectory()
                + "/" + (reportDir == null ? "" : reportDir)));
        }
        try {
            if (!reportFile.exists()) {
                FileUtils.forceMkdir(reportFile);
            }
        } catch (FileNotFoundException fnfe) {
            reportFile = new File(context.getWorkingDirectory(), reportDir);
        } catch (IOException e) {
            reportFile = new File(context.getWorkingDirectory(), reportDir);
        } finally {
            return reportFile;
        }
    }

    public static void saveJunit(String report,
                                 File junitDir,
                                 String masterId,
                                 BuildProgressLogger logger
    ) {

        File junitFile = new File(junitDir, masterId + ".xml");
        try {
            if (!junitFile.exists()) {
                FileUtils.forceMkdir(junitFile.getParentFile());
                junitFile.createNewFile();
            }

            BufferedWriter out = new BufferedWriter(new FileWriter(junitFile));
            out.write(report);
            out.close();
            logger.message("Report was saved to " + junitFile.getAbsolutePath());
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
