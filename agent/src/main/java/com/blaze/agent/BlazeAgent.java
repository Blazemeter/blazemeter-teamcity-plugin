/**
 * Copyright 2016 BlazeMeter Inc.
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

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;

import com.blaze.runner.Constants;

public class BlazeAgent implements AgentBuildRunner {
    private AgentBuildRunnerInfo runnerInfo;
    private BzmBuildProcess buildProcess;
    private BuildAgent buildAgent;
    private ArtifactsWatcher artifactsWatcher;

    public BlazeAgent(BuildAgent buildAgent, final ArtifactsWatcher artifactsWatcher) {
        this.buildAgent = buildAgent;
        this.artifactsWatcher = artifactsWatcher;
    }

    @Override
    public BuildProcess createBuildProcess(AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext)
            throws RunBuildException {

        buildProcess = new BzmBuildProcess(buildAgent, agentRunningBuild, buildRunnerContext, artifactsWatcher);

        return buildProcess;
    }

    @Override
    public AgentBuildRunnerInfo getRunnerInfo() {
        runnerInfo = new AgentBuildRunnerInfo() {

            @Override
            public String getType() {
                return Constants.RUNNER_TYPE;
            }

            @Override
            public boolean canRun(BuildAgentConfiguration buildAgentConfig) {
                return true;
            }
        };

        return runnerInfo;
    }

}
