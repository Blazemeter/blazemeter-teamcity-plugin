/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blaze.agent;

import com.blaze.api.HttpLogger;
import com.blaze.runner.Constants;
import com.blaze.runner.TestStatus;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.intellij.openapi.util.text.StringUtil;

import java.io.File;
import java.util.Map;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildInterruptReason;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;

public class BzmBuildProcess implements BuildProcess {

    private static final int CHECK_INTERVAL = 60000;
    private static final int INIT_TEST_TIMEOUT = 180000;
    private com.blaze.agent.BzmBuild bzmBuild;
    private AgentRunningBuild agentRunningBuild;
    private BuildRunnerContext buildRunCtxt;
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
    private HttpLogger httpl;

    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext) {
        this.agentRunningBuild = agentRunningBuild;
        this.buildRunCtxt = buildRunnerContext;
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
        junit = Boolean.valueOf(params.get(Constants.SETTINGS_JUNIT));
        jtl = Boolean.valueOf(params.get(Constants.SETTINGS_JTL));
        junitPath = params.get(Constants.SETTINGS_JUNIT_PATH);
        jtlPath = params.get(Constants.SETTINGS_JTL_PATH);
        notes = params.get(Constants.SETTINGS_NOTES);
        jmProps = params.get(Constants.SETTINGS_JMETER_PROPERTIES);
        Map<String, String> buildParams = agentRunningBuild.getSharedConfigParameters();
        File ald = agent.getConfiguration().getAgentLogsDirectory();
        String pn = agentRunningBuild.getProjectName();
        String bn = agentRunningBuild.getBuildNumber();
        File httpld;
        File httplf;
        try {
            httpld = new File(ald, pn + "/" + bn);
            FileUtils.forceMkdir(httpld);
            httplf = new File(httpld, Constants.HTTP_LOG);
            FileUtils.touch(httplf);
            httplf.setWritable(true);
            httpl = new HttpLogger(httplf.getAbsolutePath());
        } catch (Exception e) {
            throw new RunBuildException(e.getMessage());
        }

        bzmBuild = new BzmBuild(
                buildParams.get(Constants.API_KEY_ID),
                buildParams.get(Constants.API_KEY_SECRET),
                buildParams.get(Constants.BLAZEMETER_URL),
                testId, httpl, logger);

        try { // TODO: refactor !!!!!
            if (!this.bzmBuild.validateInput()) {
                httpl.close();
                throw new RunBuildException("Failed to validate build parameters");
            }
        } catch (Exception e) {
            throw new RunBuildException(e.getMessage());
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {


        String masterId = null;
        try {
            masterId = bzmBuild.startTest(testId);
        } catch (Exception e) {
            logger.error("Failed to start test: testId = " + this.testId + "->" + e.getMessage());
        }


        if (masterId == null || masterId.isEmpty()) {
            httpl.close();
            return BuildFinishedStatus.FINISHED_FAILED;
        } else {
            logger.message("Test initialization is started");
            logger.message("Waiting for [DATA_RECEIVED] status");
            String reportUrl = bzmBuild.getReportUrl(masterId);
            logger.message("Test report will be available at " + reportUrl);

            if (StringUtil.isNotEmpty(this.notes)) {
                bzmBuild.notes(masterId, notes);
            }

            if (StringUtil.isNotEmpty(this.jmProps)) {
                try {
                    JSONArray pr = bzmBuild.prepareSessionProperties(this.jmProps);
                    bzmBuild.properties(pr, masterId);
                } catch (JSONException e) {
                    logger.warning("Failed to submit session properties to test: " + e.getMessage());
                }
            }
        }

        logger.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        TestStatus status;
        long testInitStart = System.currentTimeMillis();
        boolean initTimeOutPassed;
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
            boolean terminate = bzmBuild.stopMaster(masterId, logger);
            if (!terminate) {
                downloadJUnitReport(masterId);
                downloadJTLReport(masterId);
            }
            httpl.close();
            return BuildFinishedStatus.INTERRUPTED;
        }

        if (initTimeOutPassed & !status.equals(TestStatus.Running)) {
            logger.warning("Failed to initialize test " + testId);
            logger.warning("Build will be aborted");
            httpl.close();
            return bzmBuild.validateCIStatus(masterId, logger);
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
            boolean terminate = bzmBuild.stopMaster(masterId, logger);
            if (!terminate) {
                bzmBuild.waitNotActive(this.testId);
                downloadJUnitReport(masterId);
                downloadJTLReport(masterId);
            }
            httpl.close();
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

        downloadJUnitReport(masterId);
        downloadJTLReport(masterId);

        httpl.close();
        return bzmBuild.validateCIStatus(masterId, logger);
    }

    private void downloadJUnitReport(String masterId) {
        if (junit) {
            File junitDir = Utils.mkReportDir(getDefaultReportDir(), this.junitPath);
            bzmBuild.junitXml(masterId, junitDir);
        }
    }

    private void downloadJTLReport(String masterId) {
        if (jtl) {
            try {
                File jtlDir = Utils.mkReportDir(getDefaultReportDir(), this.jtlPath);
                bzmBuild.jtlReports(masterId, jtlDir);
            } catch (Exception je) {
                logger.error("Failed to download jtl-report: " + je.getMessage());
            }
        }
    }

    private String getDefaultReportDir() {
        return this.agent.getConfiguration().getAgentLogsDirectory().getAbsolutePath() + "/"
                + agentRunningBuild.getProjectName() + "/"
                + agentRunningBuild.getBuildNumber();
    }
}
