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

import com.blaze.runner.Constants;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.jetbrains.annotations.NotNull;

public class BlazeAgent implements AgentBuildRunner {

    private AgentBuildRunnerInfo runnerInfo;
    private BzmBuildProcess buildProcess;
    private BuildAgent buildAgent;
    private ArtifactsWatcher artifactsWatcher;

    public BlazeAgent(BuildAgent buildAgent, @NotNull final ArtifactsWatcher artifactsWatcher) {
        this.buildAgent = buildAgent;
        this.artifactsWatcher = artifactsWatcher;
    }

    @Override
    @NotNull
    public BuildProcess createBuildProcess(@NotNull AgentRunningBuild agentRunningBuild, @NotNull BuildRunnerContext buildRunnerContext)
            throws RunBuildException {

        buildProcess = new BzmBuildProcess(buildAgent, agentRunningBuild, buildRunnerContext, artifactsWatcher);

        return buildProcess;
    }

    @Override
    @NotNull
    public AgentBuildRunnerInfo getRunnerInfo() {
        runnerInfo = new AgentBuildRunnerInfo() {

            @Override
            @NotNull
            public String getType() {
                return Constants.RUNNER_TYPE;
            }

            @Override
            public boolean canRun(@NotNull BuildAgentConfiguration buildAgentConfig) {
                return true;
            }
        };

        return runnerInfo;
    }

}
