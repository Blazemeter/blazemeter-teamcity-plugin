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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

public class BlazeMeterSettingsController extends BaseController {

    private final WebControllerManager myWebManager;
    private final PluginDescriptor myPluginDescriptor;
    private final AdminSettings mySettings;

    public BlazeMeterSettingsController(final SBuildServer server,
                                        final WebControllerManager webManager,
                                        final PluginDescriptor pluginDescriptor,
                                        @NotNull final AdminSettings pluginSettings) {
        super(server);
        this.myWebManager = webManager;
        this.myPluginDescriptor = pluginDescriptor;
        this.mySettings = pluginSettings;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().setAttribute("userKey", mySettings.getUserKey());
        request.getSession().setAttribute("blazeMeterUrl", mySettings.getBlazeMeterUrl());
        Map<String, Object> params = new HashMap<>();
        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("viewBlazeRunnerParams.jsp"), params);
    }

    /**
     * Register controller
     */
    public void register() {
        myWebManager.registerController("/blazeRunnerController.html", this);
    }
}
