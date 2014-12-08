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
		buildStartContext.addSharedParameter(Constants.USER_KEY, pluginSettings.getUserKey());
		buildStartContext.addSharedParameter(Constants.BLAZEMETER_URL, pluginSettings.getBlazeMeterUrl());
		buildStartContext.addSharedParameter(Constants.PROXY_SERVER_NAME, pluginSettings.getServerName());
		buildStartContext.addSharedParameter(Constants.PROXY_SERVER_PORT, pluginSettings.getServerPort());
		buildStartContext.addSharedParameter(Constants.PROXY_USERNAME, pluginSettings.getUsername());
		buildStartContext.addSharedParameter(Constants.PROXY_PASSWORD, pluginSettings.getPassword());
	}

	public void register(){
		 extensionHolder.registerExtension(BuildStartContextProcessor.class, this.getClass().getName(), this);
	}
}
