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

import com.blaze.agent.logging.BzmAgentLogger;
import com.blaze.agent.logging.BzmAgentNotifier;
import com.blaze.runner.Constants;
import com.blaze.utils.TCBzmUtils;
import com.blaze.utils.Utils;
import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.BuildResult;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BzmBuildProcess implements BuildProcess {

    private BuildAgent agent;
    private AgentRunningBuild agentRunningBuild;

    private boolean finished = false;
    private boolean interrupted = false;
    private final BlazeMeterUtils utils;
    private final CiBuild build;
    private final BuildProgressLogger logger;

    private Master master;

    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext) throws RunBuildException {
        this.agentRunningBuild = agentRunningBuild;
        this.agent = buildAgent;
        this.logger = agentRunningBuild.getBuildLogger();
        this.finished = false;
        this.utils = createBzmUtils(agentRunningBuild.getSharedConfigParameters());
        this.build = createCiBuild(buildRunnerContext.getRunnerParameters());
    }

    private BlazeMeterUtils createBzmUtils(Map<String, String> buildParams) throws RunBuildException {
        String apiKeyId = buildParams.get(Constants.API_KEY_ID);
        String apiKeySecret = buildParams.get(Constants.API_KEY_SECRET);
        String address = buildParams.get(Constants.BLAZEMETER_URL);

        return new TCBzmUtils(apiKeyId, apiKeySecret, address, new BzmAgentNotifier(logger), new BzmAgentLogger(createLogFile()));
    }

    private String createLogFile() throws RunBuildException {
        try {
            File logFile = new File(createArtifactDirectory(), "bzm-log.log");
            FileUtils.touch(logFile);
            logFile.setWritable(true);
            return logFile.getAbsolutePath();
        } catch (IOException ex) {
            throw new RunBuildException("Cannot create artifact log file", ex);
        }
    }

    private File createArtifactDirectory() throws RunBuildException {
        File agentLogsDirectory = agent.getConfiguration().getAgentLogsDirectory();
        String projectName = agentRunningBuild.getProjectName();
        String buildNumber = agentRunningBuild.getBuildNumber();

        try {
            File logDirectory = new File(agentLogsDirectory, projectName + "/" + buildNumber);
            FileUtils.forceMkdir(logDirectory);
            return logDirectory;
        } catch (IOException ex) {
            throw new RunBuildException("Cannot create artifact directory", ex);
        }
    }

    private CiBuild createCiBuild(Map<String, String> params) {
        String testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
        String properties = params.get(Constants.SETTINGS_JMETER_PROPERTIES);
        String notes = params.get(Constants.SETTINGS_NOTES);

        return new CiBuild(utils, Utils.getTestId(testId), properties, notes, createCiPostProcess(params));
    }

    private CiPostProcess createCiPostProcess(Map<String, String> params) {
        boolean isDownloadJtl = Boolean.valueOf(params.get(Constants.SETTINGS_JTL));
        boolean isDownloadJunit = Boolean.valueOf(params.get(Constants.SETTINGS_JUNIT));
        String junitPath = params.get(Constants.SETTINGS_JUNIT_PATH);
        String jtlPath = params.get(Constants.SETTINGS_JTL_PATH);

        String workspace = getDefaultReportDir();
        logger.error("WSP: " + workspace);
        return new CiPostProcess(isDownloadJtl, isDownloadJunit, junitPath, jtlPath, workspace, utils.getNotifier(), utils.getLogger());
    }


    @Override
    public void interrupt() {
        if (build != null && master != null) {
            build.doPostProcess(master);
        }
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
        try {
            master = build.start();
        } catch (IOException e) {
            closeLogger();
            throw new RunBuildException("Failed to start build", e);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        try {
            build.waitForFinish(master);
        } catch (InterruptedException e) {
            interrupted = true;
            utils.getLogger().warn("Wait for finish has been interrupted", e);
            logger.message("Build has been interrupted");
            build.doPostProcess(master);
            return BuildFinishedStatus.INTERRUPTED;
        } catch (IOException e) {
            utils.getLogger().warn("Caught exception while waiting for build", e);
            logger.message("Build has been interrupted");
            return BuildFinishedStatus.FINISHED_FAILED;
        }

        finished = true;
        return mappedBuildResult(build.doPostProcess(master));
    }

    private BuildFinishedStatus mappedBuildResult(BuildResult buildResult) {
        switch (buildResult) {
            case SUCCESS:
                return BuildFinishedStatus.FINISHED_SUCCESS;
            case ABORTED:
                return BuildFinishedStatus.INTERRUPTED;
            case ERROR:
                return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            case FAILED:
                return BuildFinishedStatus.FINISHED_FAILED;
            default:
                return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
    }

    private void closeLogger() {
        if (utils != null) {
            Logger logger = utils.getLogger();
            if (logger != null && (logger instanceof BzmAgentLogger)) {
                ((BzmAgentLogger) logger).close();
            }
        }
    }

    private String getDefaultReportDir() {
        return this.agent.getConfiguration().getAgentLogsDirectory().getAbsolutePath() + "/"
                + agentRunningBuild.getProjectName() + "/"
                + agentRunningBuild.getBuildNumber();
    }
}
