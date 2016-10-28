/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blaze.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
public class BlazeRunTypeController extends BaseController {

    private final WebControllerManager myManager;
    private final String actualUrl;
    private final String actualJsp;
    private AdminSettings mainSettings;
    private final ServerPaths serverPaths;

    /**
     *
     * @param actualUrl
     * @param actualJsp
     * @param manager
     * @param serverPaths
     */
    public BlazeRunTypeController(@NotNull final String actualUrl, @NotNull final String actualJsp,
            final WebControllerManager manager, final ServerPaths serverPaths) {
        this.actualJsp = actualJsp;
        this.actualUrl = actualUrl;
        this.myManager = manager;
        this.serverPaths = serverPaths;
    }

    /**
     * Save user key to local file
     *
     * @param keyFile
     */
    private void saveSettings(final File keyFile) {
        try {
            Properties prop = new Properties();
            FileReader inFile = new FileReader(keyFile);
            FileOutputStream fos = new FileOutputStream(keyFile);
            prop.load(inFile);
            prop.setProperty("user_key", mainSettings.getUserKey());
            prop.setProperty("blazeMeterUrl", mainSettings.getBlazeMeterUrl());
            prop.store(fos, "");

            fos.close();
            inFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for mainSettings
     *
     * @return
     */
    public AdminSettings getMainSettings() {
        return mainSettings;
    }

    /**
     *
     * @param mainSettings
     */
    public void setMainSettings(AdminSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    /**
     * Register controller
     */
    public void register() {
        myManager.registerController(actualUrl, this);
    }

    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            @Override
            public void handleRequest(@NotNull final HttpServletRequest request, final @NotNull HttpServletResponse response,
                    @NotNull final Element xmlResponse) {
                try {
                    doAction(request);
                } catch (Exception e) {
                    Loggers.SERVER.warn(e);
                    ActionErrors errors = new ActionErrors();
                    errors.addError("blazeMessage", getMessageWithNested(e));
                    errors.serialize(xmlResponse);
                }
            }
        });

        return null;
    }

    /**
     *
     * @param request
     * @throws Exception
     */
    private void doAction(final HttpServletRequest request) throws Exception {
        String user_key = request.getParameter("user_key");
        String blazeMeterUrl = request.getParameter("blazeMeterUrl");
        String blazeMeterApiVersion = request.getParameter("blazeMeterApiVersion");
        mainSettings.setUserKey(user_key);
        if (blazeMeterUrl != null) {
            mainSettings.setBlazeMeterUrl(blazeMeterUrl);
        }
}

    /**
     *
     * @param e
     * @return
     */
    static private String getMessageWithNested(Throwable e) {
        String result = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null) {
            result += " Caused by: " + getMessageWithNested(cause);
        }
        return result;
    }
}
