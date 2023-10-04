/**
 * Copyright 2018 BlazeMeter Inc.
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
import com.blaze.agent.utils.BzmProcess;
import com.blaze.plugins.PluginInfo;
import com.blaze.runner.Constants;
import com.blaze.utils.TCBzmUtils;
import com.blaze.utils.Utils;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.utils.BlazeMeterUtils;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BzmBuildProcess implements BuildProcess {

    private BuildAgent agent;
    private AgentRunningBuild agentRunningBuild;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private BzmProcess bzmProcess;
    private Future<BuildFinishedStatus> processFuture;

    private final BlazeMeterUtils utils;
    private final BuildProgressLogger logger;

    public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild,
                           BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher,BuildRunnerContext context) throws RunBuildException {
        this.agentRunningBuild = agentRunningBuild;
        this.agent = buildAgent;
        this.logger = agentRunningBuild.getBuildLogger();
        this.utils = createBzmUtils(agentRunningBuild.getSharedConfigParameters());
        this.bzmProcess = new BzmProcess(buildAgent, agentRunningBuild, buildRunnerContext, artifactsWatcher, utils,context);
    }


    @Override
    public void interrupt() {
        logger.message("Interrupt BlazeMeter build step");
        if (processFuture != null) {
            processFuture.cancel(true);
        }
    }

    @Override
    public boolean isFinished() {
        return processFuture.isDone();
    }

    @Override
    public boolean isInterrupted() {
        return processFuture.isCancelled() && isFinished();
    }


    @Override
    public void start() throws RunBuildException {
        logger.message("BlazeMeter agent started: version = " + Utils.version());
        checkUpdates();
        processFuture = executor.submit(bzmProcess);
    }

    private void checkUpdates() {
        PluginInfo info = new PluginInfo(utils);
        if (info.hasUpdates()) {
            logger.message("A new version of BlazeMeter's TeamCity plugin is available. Please got to plugin's page to download a new version");
            logger.message("https://plugins.jetbrains.com/plugin/9020-blazemeter");
        }
    }


    @SuppressWarnings("static-access")
    @Override
    public BuildFinishedStatus waitFor() throws RunBuildException {
        try {
            return processFuture.get();
        } catch (final InterruptedException | CancellationException e) {
            utils.getLogger().warn("Wait for finish has been interrupted", e);
            return BuildFinishedStatus.INTERRUPTED;
        } catch (final ExecutionException e) {
            utils.getLogger().warn("Caught exception while waiting for build", e);
            return BuildFinishedStatus.FINISHED_FAILED;
        } finally {
            closeLogger();
            executor.shutdown();
        }
    }

    private BlazeMeterUtils createBzmUtils(Map<String, String> buildParams) throws RunBuildException {
        String apiKeyId = buildParams.get(Constants.API_KEY_ID);
        String apiKeySecret = buildParams.get(Constants.API_KEY_SECRET);
        String address = buildParams.get(Constants.BLAZEMETER_URL);

        return new TCBzmUtils(apiKeyId, apiKeySecret, address, new BzmAgentNotifier(logger), new BzmAgentLogger(createLogFile()));
    }

    private String createLogFile() throws RunBuildException {
        try {
            File logFile = new File(createArtifactDirectory(), "bzm-log-" + System.currentTimeMillis());
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
            File logDirectory = new File(agentLogsDirectory, projectName + File.separator + buildNumber);
            FileUtils.forceMkdir(logDirectory);
            return logDirectory;
        } catch (IOException ex) {
            throw new RunBuildException("Cannot create artifact directory", ex);
        }
    }

    private void closeLogger() {
        if (utils != null) {
            Logger logger = utils.getLogger();
            if (logger instanceof BzmAgentLogger) {
                ((BzmAgentLogger) logger).close();
            }
        }
    }
}
