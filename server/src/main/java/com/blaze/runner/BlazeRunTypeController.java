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

import com.blaze.runner.utils.BzmServerUtils;
import com.blazemeter.api.explorer.User;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Controller for configure admin properties
 */
public class BlazeRunTypeController extends BaseController {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");
    private final WebControllerManager myManager;
    private AdminSettings mainSettings;


    /**
     * @param manager
     */
    public BlazeRunTypeController(@NotNull AdminSettings mainSettings, final WebControllerManager manager) {
        this.mainSettings = mainSettings;
        this.myManager = manager;
    }

    public AdminSettings getMainSettings() {
        return mainSettings;
    }

    public void setMainSettings(AdminSettings mainSettings) {
        this.mainSettings = mainSettings;
    }

    public void register() {
        myManager.registerController("/admin/saveUserKeys/", this);
    }

    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            @Override
            public void handleRequest(@NotNull final HttpServletRequest request, final @NotNull HttpServletResponse response,
                                      @NotNull final Element xmlResponse) {
                try {
                    doAction(request, xmlResponse);
                } catch (Exception e) {
                    logger.error("Cannot save BlazeMeter configuration", e);
                    addResultElement(xmlResponse, "blazeErrorMessage", "Cannot save BlazeMeter configuration " + getMessageWithNested(e));
                }
            }
        });

        return null;
    }

    private void doAction(final HttpServletRequest request, Element xmlResponse) throws IOException {
        String apiKeyID = request.getParameter("apiKeyID");
        String apiKeySecret = request.getParameter("apiKeySecret");
        String blazeMeterUrl = request.getParameter("blazeMeterUrl");
        String validationWarnings = getValidationWarnings(apiKeyID, apiKeySecret, blazeMeterUrl);
        if (!validationWarnings.isEmpty()) {
            addResultElement(xmlResponse, "blazeWarningMessage", validationWarnings);
        } else {
            mainSettings.setApiKeyID(apiKeyID);
            mainSettings.setApiKeySecret(apiKeySecret);
            mainSettings.setBlazeMeterUrl(blazeMeterUrl);
            mainSettings.saveProperties();
            addResultElement(xmlResponse, "blazeSuccessMessage", "Configuration saved successfully!");
        }
    }

    private String getValidationWarnings(String apiKeyID, String apiKeySecret, String blazeMeterUrl) {
        String result = "";

        if (apiKeyID == null || apiKeyID.isEmpty() ||
                apiKeySecret == null || apiKeySecret.isEmpty() ||
                blazeMeterUrl == null || blazeMeterUrl.isEmpty()) {
            result += "Please, fill all fields with valid data before saving";
        }

        if (result.isEmpty()) {
            BzmServerUtils utils = new BzmServerUtils(apiKeyID, apiKeySecret, blazeMeterUrl);
            try {
                User user = User.getUser(utils);
                assert user.getId() != null;
            } catch (Exception e) {
                logger.info("Invalid user credentials or/and server url, please check it: " + e.getMessage(), e);
                result += "Invalid user credentials or/and server url, please check it ("  + e.getMessage() + ")";
            }
        }

        return result;
    }

    public void addResultElement(Element xmlResponse, String resultId, String resultContent) {
        Element resultElement = new Element("result");
        resultElement.setAttribute("id", resultId);
        resultElement.addContent(resultContent);
        xmlResponse.addContent(resultElement);
    }

    static private String getMessageWithNested(Throwable e) {
        String result = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null) {
            result += " Caused by: " + getMessageWithNested(cause);
        }
        return result;
    }
}
