package com.blaze.runner;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;

import org.jetbrains.annotations.NotNull;

public class BlazeBuildStartContextProcessor implements BuildStartContextProcessor{
	private BlazeMeterServerSettings pluginSettings;
	private ExtensionHolder extensionHolder;
	
	public BlazeBuildStartContextProcessor(@NotNull final BlazeMeterServerSettings pluginSettings, @NotNull ExtensionHolder extensionHolder){
		this.pluginSettings = pluginSettings;
		this.extensionHolder = extensionHolder;
	}
	
	@Override
	public void updateParameters(@NotNull BuildStartContext buildStartContext) {
		buildStartContext.addSharedParameter(BlazeMeterConstants.USER_KEY, pluginSettings.getUserKey());
		buildStartContext.addSharedParameter(BlazeMeterConstants.PROXY_SERVER_NAME, pluginSettings.getServerName());
		buildStartContext.addSharedParameter(BlazeMeterConstants.PROXY_SERVER_PORT, pluginSettings.getServerPort());
		buildStartContext.addSharedParameter(BlazeMeterConstants.PROXY_USERNAME, pluginSettings.getUsername());
		buildStartContext.addSharedParameter(BlazeMeterConstants.PROXY_PASSWORD, pluginSettings.getPassword());
	}

	public void register(){
		 extensionHolder.registerExtension(BuildStartContextProcessor.class, this.getClass().getName(), this);
	}
}
