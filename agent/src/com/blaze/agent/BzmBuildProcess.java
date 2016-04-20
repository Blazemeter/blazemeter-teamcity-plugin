package com.blaze.agent;

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
    private BzmServiceManager bzmServMan;
    private AgentRunningBuild agentRunningBuild;
    private BuildRunnerContext buildRunCtxt;
    private ArtifactsWatcher artifactsWatcher;

    private String validationError;
    private String testId;
    private BuildAgent agent;

    final BuildProgressLogger log;
    boolean finished;
    boolean interrupted;


    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher) {
        this.agentRunningBuild = agentRunningBuild;
        this.buildRunCtxt = buildRunnerContext;
        this.artifactsWatcher = artifactsWatcher;
        this.agent = buildAgent;
        this.finished = false;

        log = agentRunningBuild.getBuildLogger();
        Map<String, String> buildSharedMap = agentRunningBuild.getSharedConfigParameters();
        bzmServMan = BzmServiceManager.getBzmServiceManager(buildSharedMap, log);
    }

    private String validateParams(Map<String, String> params) {

        testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
        if (StringUtil.isEmptyOrSpaces(testId)) {
            log.warning(Constants.NO_TEST_WAS_DEFINED);
            return Constants.NO_TEST_WAS_DEFINED;
        } else {
            //verify if the test still exists on BlazeMeter server
            LinkedHashMultimap<String, String> tests = bzmServMan.getTests();
            Collection<String> values = tests.values();
            log.message("The following tests were found on server:");
            for (String s : values) {
                log.message(s);
            }
            if (tests != null) {
                if (!values.contains(testId)) {
                    log.warning(Constants.PROBLEM_WITH_VALIDATING);
                    log.warning("Server url="+bzmServMan.getBlazeMeterUrl());
                    log.warning("UserKey="+bzmServMan.getUserKey().substring(0,4)+"...");
                    log.warning("Check the following settings: serverUrl, userKey, proxy settings at buildAgent");
                    return Constants.PROBLEM_WITH_VALIDATING;
                }
            }
        }
        return null;
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
        log.message("BlazeMeter agent started: version=" + Utils.getVersion());

        log.activityStarted("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        Map<String, String> runnerParams = buildRunCtxt.getRunnerParameters();
        validationError = validateParams(runnerParams);
        if (validationError != null) {
            log.error(validationError);
        } else {
            log.message("Validation passed.");
        }
        log.activityFinished("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);

        if (validationError != null) {
            throw new RunBuildException(validationError);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        log.message("Attempting to start test with id:" + testId);
        String masterId = bzmServMan.startTest(testId, log);
        BuildFinishedStatus result = null;
        if (masterId.isEmpty()) {
            return BuildFinishedStatus.FINISHED_FAILED;
        } else {
            log.message("Test initialization is started... Waiting for DATA_RECEIVED status");
            String reportUrl = bzmServMan.getReportUrl(masterId);
            log.message("Test report will be available at " + reportUrl);
            if (StringUtil.isNotEmpty(reportUrl)) {
                this.agent.getConfiguration().addEnvironmentVariable(Constants.REPORT_URL + this.agentRunningBuild.getBuildNumber(), reportUrl);
            }
        }


        log.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        TestStatus status;
        long testInitStart = System.currentTimeMillis();
        boolean initTimeOutPassed = false;
        BuildInterruptReason buildInterruptReason;
        do {
            Utils.sleep(CHECK_INTERVAL, log);
            status = bzmServMan.masterStatus(bzmServMan.masterId());
            log.message("Check if the test is initialized...");
            initTimeOutPassed = System.currentTimeMillis() > testInitStart + INIT_TEST_TIMEOUT;
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        }
        while (buildInterruptReason == null && (!(status.equals(TestStatus.Running) | initTimeOutPassed)));
        if (buildInterruptReason != null) {
            log.warning("Build was aborted by user");
            boolean terminate=bzmServMan.stopMaster(masterId,log);
            if(!terminate){
                bzmServMan.junitXml(masterId, buildRunCtxt);
                bzmServMan.jtlReports(masterId, buildRunCtxt);
            }
            return BuildFinishedStatus.INTERRUPTED;
        }
        if (initTimeOutPassed & !status.equals(TestStatus.Running)) {
            log.warning("Failed to initialize test " + testId);
            log.warning("Build will be aborted");
            return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
        long testRunStart = System.currentTimeMillis();

        do {
            Utils.sleep(CHECK_INTERVAL, log);
            log.message("Check if the test is still running. Time passed since start: " + ((System.currentTimeMillis() - testRunStart) / 1000 / 60) + " minutes.");
            status = bzmServMan.masterStatus(bzmServMan.masterId());
            log.message("TestInfo=" + status.toString());
            buildInterruptReason = agentRunningBuild.getInterruptReason();
        } while (buildInterruptReason == null && !status.equals(TestStatus.NotRunning));
        if (buildInterruptReason != null) {
            log.warning("Build was aborted by user");
            boolean terminate=bzmServMan.stopMaster(masterId,log);
            if(!terminate){
                Utils.sleep(180000, log);
                bzmServMan.junitXml(masterId, buildRunCtxt);
                bzmServMan.jtlReports(masterId, buildRunCtxt);
            }
            return BuildFinishedStatus.INTERRUPTED;
        }
        log.message("Test finished. Checking for test report...");
        log.message("Actual test duration was: " + ((System.currentTimeMillis() - testRunStart) / 1000 / 60) + " minutes.");
        log.activityFinished("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        Utils.sleep(180000, log);
        TestResult testResult = bzmServMan.getReport(log);
        if (testResult == null) {
            log.warning("Failed to get report from server...");
        } else {
            log.message("Test report is received...");
            log.message(testResult.toString());
        }
        bzmServMan.junitXml(masterId, buildRunCtxt);
        bzmServMan.jtlReports(masterId, buildRunCtxt);
        CIStatus ciStatus = bzmServMan.validateCIStatus(masterId, log);
        result = ciStatus.equals(CIStatus.failures) ? BuildFinishedStatus.FINISHED_FAILED : BuildFinishedStatus.FINISHED_SUCCESS;
        return result;
    }

}