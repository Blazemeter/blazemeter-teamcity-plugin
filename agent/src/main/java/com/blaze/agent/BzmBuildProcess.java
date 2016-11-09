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

package com.blaze.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.blaze.runner.CIStatus;
import com.blaze.runner.TestStatus;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.DefaultMessagesInfo;

import com.blaze.runner.Constants;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;

import javax.mail.MessagingException;

public class BzmBuildProcess implements BuildProcess {
    private static final int CHECK_INTERVAL = 60000;
    private static final int INIT_TEST_TIMEOUT = 900000;
    private BzmBuild bzmBuild;
    private AgentRunningBuild agentRunningBuild;
    private BuildRunnerContext buildRunCtxt;
    private ArtifactsWatcher artifactsWatcher;

    private String testId;
    private boolean junit;
    private boolean jtl;
    private String junitPath;
    private String jtlPath;
    private String notes;
    private String jmProps;
    private BuildAgent agent;

    final BuildProgressLogger logger;
    boolean finished;
    boolean interrupted;


    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher) {
        this.agentRunningBuild = agentRunningBuild;
        this.buildRunCtxt = buildRunnerContext;
        this.artifactsWatcher = artifactsWatcher;
        this.agent = buildAgent;
        this.finished = false;
        logger = agentRunningBuild.getBuildLogger();
    }


    @Override
    public void interrupt() {
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public boolean isInterrupted() {
        return interrupted;
    }

    @Override
    public void start() throws RunBuildException {
        logger.message("BlazeMeter agent started: version = " + Utils.version());
        Map<String, String> params = buildRunCtxt.getRunnerParameters();
        testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
        junit=Boolean.valueOf(params.get(Constants.SETTINGS_JUNIT));
        jtl=Boolean.valueOf(params.get(Constants.SETTINGS_JTL));
        junitPath=params.get(Constants.SETTINGS_JUNIT_PATH);
        jtlPath = params.get(Constants.SETTINGS_JTL_PATH);
        notes = params.get(Constants.SETTINGS_NOTES);
        jmProps = params.get(Constants.SETTINGS_JMETER_PROPERTIES);
        Map<String, String> buildParams = agentRunningBuild.getSharedConfigParameters();
        File ald=agent.getConfiguration().getAgentLogsDirectory();
        String pn=agentRunningBuild.getProjectName();
        String bn=agentRunningBuild.getBuildNumber();
        File httpld;
        File httplf;
        try {
            httpld = new File(ald, pn + "/" + bn);
            FileUtils.forceMkdir(httpld);
            httplf = new File(httpld, Constants.HTTP_LOG);
            FileUtils.touch(httplf);
            httplf.setWritable(true);
        } catch (Exception e) {
            throw new RunBuildException(e.getMessage());
        }

        bzmBuild = new BzmBuild(((String) buildParams.get(Constants.USER_KEY)),
                ((String) buildParams.get(Constants.BLAZEMETER_URL)), testId, httplf.getAbsolutePath(),logger);

        try {
            if (!this.bzmBuild.validateInput()) {
                throw new RunBuildException("Failed to validate build parameters");
            }
        } catch (IOException e) {
            throw new RunBuildException(e.getMessage());
        } catch (MessagingException e) {
            throw new RunBuildException(e.getMessage());
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        logger.message("Attempting to start test with id: " + testId);
        String masterId = null;
        try {
            masterId = bzmBuild.startTest(testId, logger);
        } catch (IOException e) {
            logger.error("Failed to start test: testId = "+this.testId+"->"+e.getMessage());
        } catch (JSONException e) {
            logger.error("Failed to start test: testId = "+this.testId+"->"+e.getMessage());
        }
        BuildFinishedStatus result = null;
        if (masterId.isEmpty()) {
            return BuildFinishedStatus.FINISHED_FAILED;
        } else {
            logger.message("Test initialization is started");
            logger.message("Waiting for [DATA_RECEIVED] status");
            String reportUrl = bzmBuild.getReportUrl(masterId);
            logger.message("Test report will be available at " + reportUrl);
            if (StringUtil.isNotEmpty(reportUrl)) {
                this.agent.getConfiguration().addEnvironmentVariable(Constants.REPORT_URL + this.agentRunningBuild.getBuildNumber(), reportUrl);
            }
            if(StringUtil.isNotEmpty(this.notes)){
                bzmBuild.notes(masterId,notes);
            }

            if (StringUtil.isNotEmpty(this.jmProps)) {
                JSONArray pr = null;
                try {
                    pr = bzmBuild.prepareSessionProperties(this.jmProps);
                    bzmBuild.properties(pr, masterId);
                } catch (JSONException e) {
                    logger.warning("Failed to submit session properties to test: " + e.getMessage());
                }
            }
        }

        File  junitDir=Utils.reportDir(this.buildRunCtxt,this.junitPath);
        File  jtlDir=Utils.reportDir(this.buildRunCtxt,this.jtlPath);
        logger.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        TestStatus status;
        long testInitStart = System.currentTimeMillis();
        boolean initTimeOutPassed = false;
        BuildInterruptReason buildInterruptReason;
        do {
            Utils.sleep(CHECK_INTERVAL, logger);
            status = bzmBuild.masterStatus(bzmBuild.masterId());
            logger.message("Check if the test is initialized...");
            initTimeOutPassed = System.currentTimeMillis() > testInitStart + INIT_TEST_TIMEOUT;
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        }
        while (buildInterruptReason == null && (!(status.equals(TestStatus.Running) | initTimeOutPassed)));
        if (buildInterruptReason != null) {
            logger.warning("Build was aborted by user");
            boolean terminate= bzmBuild.stopMaster(masterId, logger);
            if (!terminate) {
                if (junit) {
                    bzmBuild.junitXml(masterId, junitDir);
                }
                if (jtl) {
                    try{
                        bzmBuild.jtlReports(masterId, jtlDir);
                    }catch (IOException io){
                        logger.error("Failed to download jtl-report: "+io.getMessage());
                    }catch (JSONException je){
                        logger.error("Failed to download jtl-report: "+je.getMessage());
                    }                }
            }
            return BuildFinishedStatus.INTERRUPTED;
        }
        if (initTimeOutPassed & !status.equals(TestStatus.Running)) {
            logger.warning("Failed to initialize test " + testId);
            logger.warning("Build will be aborted");
            return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
        long testRunStart = System.currentTimeMillis();

        do {
            Utils.sleep(CHECK_INTERVAL, logger);
            logger.message("Check if the test is still running. Time passed since start: " + ((System.currentTimeMillis() - testRunStart) / 1000 / 60) + " minutes.");
            status = bzmBuild.masterStatus(bzmBuild.masterId());
            logger.message("TestInfo=" + status.toString());
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        } while (buildInterruptReason == null && !status.equals(TestStatus.NotRunning));
        if (buildInterruptReason != null) {
            logger.warning("Build was aborted by user");
            boolean terminate= bzmBuild.stopMaster(masterId, logger);
            if (!terminate) {
                bzmBuild.waitNotActive(this.testId);
                if (junit) {
                    bzmBuild.junitXml(masterId, junitDir);
                }
                if (jtl) {
                    try{
                        bzmBuild.jtlReports(masterId, jtlDir);
                    }catch (IOException io){
                         logger.error("Failed to download jtl-report: "+io.getMessage());
                    }catch (JSONException je){
                        logger.error("Failed to download jtl-report: "+je.getMessage());
                    }
                }
            }
            return BuildFinishedStatus.INTERRUPTED;
        }
        logger.message("Test finished. Checking for test report...");
        logger.message("Actual test duration was: " + ((System.currentTimeMillis() - testRunStart) / 1000 / 60) + " minutes.");
        logger.activityFinished("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        bzmBuild.waitNotActive(this.testId);
        TestResult testResult = bzmBuild.getReport(logger);
        if (testResult == null) {
            logger.warning("Failed to get report from server...");
        } else {
            logger.message("Test report is received...");
            logger.message(testResult.toString());
        }
        if (junit) {
            bzmBuild.junitXml(masterId, junitDir);
        }
        if (jtl) {
            try{
                bzmBuild.jtlReports(masterId, jtlDir);
            }catch (IOException io){
                logger.error("Failed to download jtl-report: "+io.getMessage());
            } catch (JSONException je) {
                logger.error("Failed to download jtl-report: " + je.getMessage());
            }
        }
        CIStatus ciStatus = bzmBuild.validateCIStatus(masterId, logger);
        result = ciStatus.equals(CIStatus.failures) ? BuildFinishedStatus.FINISHED_FAILED : BuildFinishedStatus.FINISHED_SUCCESS;
        return result;
    }



}