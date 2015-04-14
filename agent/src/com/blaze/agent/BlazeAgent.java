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
	
	public BlazeAgent(BuildAgent buildAgent, final ArtifactsWatcher artifactsWatcher){
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
