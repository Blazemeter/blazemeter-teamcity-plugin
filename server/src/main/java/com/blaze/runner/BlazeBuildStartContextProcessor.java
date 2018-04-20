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

package com.blaze.runner;

import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.serverSide.BuildStartContext;
import jetbrains.buildServer.serverSide.BuildStartContextProcessor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for update BuildStartContext before it is started on a build agent.
 * Add some admin properties for send on Agent
 */
public class BlazeBuildStartContextProcessor implements BuildStartContextProcessor {
    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    private AdminSettings pluginSettings;
    private ExtensionHolder extensionHolder;

    public BlazeBuildStartContextProcessor(@NotNull final AdminSettings pluginSettings, @NotNull ExtensionHolder extensionHolder) {
        this.pluginSettings = pluginSettings;
        this.extensionHolder = extensionHolder;
    }

    @Override
    public void updateParameters(@NotNull BuildStartContext buildStartContext) {
        logger.info("Add Shared Parameter user creds to build context");
        buildStartContext.addSharedParameter(Constants.API_KEY_ID, validateValue(pluginSettings.getApiKeyID()));
        buildStartContext.addSharedParameter(Constants.API_KEY_SECRET, validateValue(pluginSettings.getApiKeySecret()));
        buildStartContext.addSharedParameter(Constants.BLAZEMETER_URL, validateValue(pluginSettings.getBlazeMeterUrl()));
    }

    private String validateValue(String value) {
        if (value == null) {
            logger.warn("User credential contains NULL value, check your credentials in 'Administration -> BlazeMeter' page");
            return "";
        }
        return value;
    }

    public void register() {
        extensionHolder.registerExtension(BuildStartContextProcessor.class, this.getClass().getName(), this);
    }
}
