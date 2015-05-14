package com.blaze.agent;

import java.util.Map;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildInterruptReason;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

import com.blaze.BzmServiceManager;
import com.blaze.runner.Constants;

public class AgentListener extends AgentLifeCycleAdapter{
	
	private BzmServiceManager bzmServiceManager;
	
	public AgentListener(@NotNull final EventDispatcher<AgentLifeCycleListener> dispatcher){
		dispatcher.addListener(this);
	}
	
	@Override
	public void beforeBuildInterrupted(@NotNull AgentRunningBuild runningBuild, @NotNull BuildInterruptReason reason) {
		super.beforeBuildInterrupted(runningBuild, reason);
		stopTest(runningBuild);
	}

	@Override
	public void buildFinished(@NotNull AgentRunningBuild runningBuild, @NotNull BuildFinishedStatus buildStatus) {
		super.buildFinished(runningBuild, buildStatus);
		stopTest(runningBuild);
	}

	private void stopTest(AgentRunningBuild runningBuild) {
		Map<String, String> buildSharedMap = runningBuild
				.getSharedConfigParameters();

		bzmServiceManager = BzmServiceManager.getBzmServiceManager(buildSharedMap,runningBuild.getBuildLogger());
		Map<String, String> runnerParams = runningBuild.getRunnerParameters();
		String testId = runnerParams.get(Constants.SETTINGS_ALL_TESTS_ID);
		bzmServiceManager.stopTestSession(testId, runningBuild.getBuildLogger());
	}
}
