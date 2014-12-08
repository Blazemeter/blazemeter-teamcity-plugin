package com.blaze.runner;

import java.util.HashMap;
import java.util.Map;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BlazeRunType extends RunType {
	@NotNull
	private final PluginDescriptor pluginDescriptor;
	@NotNull
	private final String editParamsPageName = "editBlazeRunnerParams.jsp";
	@NotNull
	private final String viewParamsPageName = "viewBlazeRunnerParams.jsp";

	Map<String, String> defaultProperties = null;

	private BlazeMeterServerSettings pluginSettings;

	public BlazeRunType(final RunTypeRegistry runTypeRegistry, 
			@NotNull final PluginDescriptor pluginDescriptor,
			@NotNull final BlazeMeterServerSettings pluginSettings) {
		this.pluginDescriptor = pluginDescriptor;
		this.pluginSettings = pluginSettings;
		
		runTypeRegistry.registerRunType(this);
	}

	@Override
	public String getDescription() {
		return Constants.RUNNER_DISPLAY_NAME;
	}

	@Override
	public String getDisplayName() {
		return Constants.RUNNER_DISPLAY_NAME;
	}

	@Override
	public String getType() {
		return Constants.RUNNER_TYPE;
	}

	@Nullable
	public Map<String, String> getDefaultRunnerProperties() {
		if (defaultProperties == null){
			defaultProperties = new HashMap<String, String>();
		}		
		setupDefaultProperties(defaultProperties);
		return defaultProperties;
	}

	private void setupDefaultProperties(Map<String, String> params) {
		params.put(Constants.SETTINGS_DATA_FOLDER, Constants.DEFAULT_SETTINGS_DATA_FOLDER);

		if (pluginSettings != null) {
			params.remove(Constants.USER_KEY);
			params.remove(Constants.BLAZEMETER_URL);
			params.remove(Constants.PROXY_SERVER_NAME);
			params.remove(Constants.PROXY_SERVER_PORT);
			params.remove(Constants.PROXY_USERNAME);
			params.remove(Constants.PROXY_PASSWORD);
			params.put(Constants.USER_KEY, pluginSettings.getUserKey());
			params.put(Constants.BLAZEMETER_URL, pluginSettings.getBlazeMeterUrl());
			params.put(Constants.PROXY_SERVER_NAME, pluginSettings.getServerName());
			params.put(Constants.PROXY_SERVER_PORT, pluginSettings.getServerPort());
			params.put(Constants.PROXY_USERNAME, pluginSettings.getUsername());
			params.put(Constants.PROXY_PASSWORD, pluginSettings.getPassword());
		}
	}

	@Nullable
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return new BlazeRunTypePropertiesProcessor();
	}

	@Nullable
	public String getEditRunnerParamsJspFilePath() {
		return pluginDescriptor.getPluginResourcesPath(editParamsPageName);
	}

	@Override
	public String getViewRunnerParamsJspFilePath() {
		return pluginDescriptor.getPluginResourcesPath(viewParamsPageName);
	}

	public BlazeMeterServerSettings getPluginSettings() {
		return pluginSettings;
	}

	public void setPluginSettings(BlazeMeterServerSettings pluginSettings) {
		this.pluginSettings = pluginSettings;
	}

}
