package com.blaze.agent;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import com.blaze.BzmServiceManager;
import com.blaze.runner.CIStatus;
import com.blaze.runner.TestStatus;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.DefaultMessagesInfo;

import com.blaze.runner.Constants;

/**
 * @author Marcel Milea
 */
public class BzmBuildProcess implements BuildProcess {
    private static final int CHECK_INTERVAL = 60000;
    private static final int INIT_TEST_TIMEOUT = 900000;
    private static final String TC_AGENT_WORK_DIR = "teamcity.agent.work.dir";
    private BzmServiceManager bzmServiceManager;
    private AgentRunningBuild agentRunningBuild;
    private BuildRunnerContext buildRunnerContext;
    private ArtifactsWatcher artifactsWatcher;

    private String validationError;
    private String testId;
    private BuildAgent agent;

    final BuildProgressLogger logger;
    boolean finished;
    boolean interrupted;


    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher) {
        this.agentRunningBuild = agentRunningBuild;
        this.buildRunnerContext = buildRunnerContext;
        this.artifactsWatcher = artifactsWatcher;
        this.agent = buildAgent;
        this.finished = false;

        logger = agentRunningBuild.getBuildLogger();
        Map<String, String> buildSharedMap = agentRunningBuild.getSharedConfigParameters();
        String proxyPortStr = buildSharedMap.get(Constants.PROXY_SERVER_PORT);
        int proxyPortInt = 0;
        if (proxyPortStr != null && !proxyPortStr.isEmpty()) {
            proxyPortInt = Integer.parseInt(proxyPortStr);
        }
        bzmServiceManager = BzmServiceManager.getBzmServiceManager(buildSharedMap, logger);
    }

    private String validateParams(Map<String, String> params) {

        testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
        if (StringUtil.isEmptyOrSpaces(testId)) {
            logger.warning("No test was defined in the configuration page.");
            return "No test was defined in the configuration page.";
        } else {
            //verify if the test still exists on BlazeMeter server
            LinkedHashMultimap<String, String> tests = bzmServiceManager.getTests();
            Collection<String> values = tests.values();
            logger.message("The following tests were found on server:");
            for (String s : values) {
                logger.message(s);
            }
            if (tests != null) {
                if (!values.contains(testId)) {
                    logger.warning("Test was not found at BlazeMeter server " + bzmServiceManager.getBlazeMeterUrl());
                    return "Test was not found at BlazeMeter server " + bzmServiceManager.getBlazeMeterUrl();
                }
            }
        }
        return null;
    }

    @Override
    public void interrupt() {
        logger.message("BlazeMeter agent interrupted.");
        bzmServiceManager.stopTestSession(testId, logger);
        interrupted = true;
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
        logger.message("BlazeMeter agent started: version=" + Utils.getVersion());

        logger.activityStarted("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        Map<String, String> runnerParams = buildRunnerContext.getRunnerParameters();
        validationError = validateParams(runnerParams);
        if (validationError != null) {
            logger.error(validationError);
        } else {
            logger.message("Validation passed.");
        }
        logger.activityFinished("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);

        if (validationError != null) {
            throw new RunBuildException(validationError);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        logger.message("Attempting to start test with id:" + testId);
        String masterId = bzmServiceManager.startTest(testId, logger);
        BuildFinishedStatus result = null;
        if (masterId.isEmpty()) {
            return BuildFinishedStatus.FINISHED_FAILED;
        } else {
            logger.message("Test initialization is started... Waiting for DATA_RECEIVED status");
            String reportUrl = bzmServiceManager.getReportUrl(masterId);
            logger.message("Test report will be available at " + reportUrl);
            if (StringUtil.isNotEmpty(reportUrl)) {
                this.agent.getConfiguration().addEnvironmentVariable(Constants.REPORT_URL + this.agentRunningBuild.getBuildNumber(), reportUrl);
            }
        }


        logger.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        TestStatus status;
        String apiVersion = bzmServiceManager.getBlazeMeterApiVersion();
        long testInitStart = System.currentTimeMillis();
        boolean initTimeOutPassed = false;
        BuildInterruptReason buildInterruptReason;
        do {
            Utils.sleep(CHECK_INTERVAL, logger);
            status = bzmServiceManager.getTestSessionStatus(apiVersion.equals(Constants.V2) ? testId : bzmServiceManager.getSession());
            logger.message("Check if the test is initialized...");
            initTimeOutPassed = System.currentTimeMillis() > testInitStart + INIT_TEST_TIMEOUT;
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        }
        while (buildInterruptReason == null && (!(status.equals(TestStatus.Running) | initTimeOutPassed)));
        if (buildInterruptReason != null) {
            logger.warning("Build was be aborted by user");
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
            status = bzmServiceManager.getTestSessionStatus(apiVersion.equals(Constants.V2) ? testId : bzmServiceManager.getSession());
            logger.message("TestInfo=" + status.toString());
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        } while (buildInterruptReason == null && !status.equals(TestStatus.NotRunning));
        if (buildInterruptReason != null) {
            logger.warning("Build was be aborted by user");
            return BuildFinishedStatus.INTERRUPTED;
        }
        logger.message("Test finished. Checking for test report...");
        logger.message("Actual test duration was: " + ((System.currentTimeMillis() - testRunStart) / 1000 / 60) + " minutes.");
        logger.activityFinished("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        Utils.sleep(180000, logger);
        TestResult testResult = bzmServiceManager.getReport(logger);
        BuildFinishedStatus localTrRes = null;
        if (testResult == null) {
            logger.warning("Failed to get report from server...");
        } else {
            logger.message("Test report is received...");
            logger.message(testResult.toString());
        }
        bzmServiceManager.retrieveJUNITXML(masterId, buildRunnerContext);
//        bzmServiceManager.retrieveJTL(masterId, buildRunnerContext);
        CIStatus ciStatus = bzmServiceManager.validateCIStatus(masterId, logger);
        result = ciStatus.equals(CIStatus.failures) ? BuildFinishedStatus.FINISHED_FAILED : BuildFinishedStatus.FINISHED_SUCCESS;
        return result;
    }

}