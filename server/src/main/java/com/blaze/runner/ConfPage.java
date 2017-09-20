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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.web.openapi.PagePlaces;

public class ConfPage extends AdminPage {
    private AdminSettings mainSettings;

    public ConfPage(PagePlaces pagePlaces, String tabId, String includeUrl, String title) {
        super(pagePlaces, tabId, includeUrl, title);
    }

    public AdminSettings getMainSettings() {
        return mainSettings;
    }

    public void setMainSettings(AdminSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    @Override
    public String getGroup() {
        return Constants.RUNNER_TYPE;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void fillModel(Map<String, Object> model, HttpServletRequest request) {
        super.fillModel(model, request);
        if (mainSettings != null) {
            model.put("user_key", mainSettings.getUserKey());
            model.put("blazeMeterUrl", mainSettings.getBlazeMeterUrl());
            mainSettings.saveProperties();
        }
    }

    @Override
    public String getPluginName() {
        return Constants.RUNNER_TYPE;
    }

}
