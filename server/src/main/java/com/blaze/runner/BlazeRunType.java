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

package com.blaze.runner;

import java.util.HashMap;
import java.util.Map;

import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class for configure Build Step Params
 */
public class BlazeRunType extends RunType {

    @NotNull
    private final PluginDescriptor pluginDescriptor;
    @NotNull
    private final String editParamsPageName = "editBlazeRunnerParams.jsp";
    @NotNull
    private final String viewParamsPageName = "viewBlazeRunnerParams.jsp";

    private AdminSettings pluginSettings;

    public BlazeRunType(final RunTypeRegistry runTypeRegistry,
                        @NotNull final PluginDescriptor pluginDescriptor,
                        @NotNull final AdminSettings pluginSettings) {
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
        final Map<String, String> defaultProps = new HashMap<String, String>();
        defaultProps.put(Constants.API_KEY_ID, pluginSettings.getApiKeyID());
        defaultProps.put(Constants.API_KEY_SECRET, pluginSettings.getApiKeySecret());
        defaultProps.put(Constants.BLAZEMETER_URL, pluginSettings.getBlazeMeterUrl());
        return defaultProps;
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

    public AdminSettings getPluginSettings() {
        return pluginSettings;
    }

    public void setPluginSettings(AdminSettings pluginSettings) {
        this.pluginSettings = pluginSettings;
    }
    @Nullable
    public Map<String, String> getDefaultParameters() {
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put(Constants.SETTING_NOTIFICATION_TYPE, "slack");
        return map;
    }

}
