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

package com.blaze.api.urlmanager;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class UrlManagerImpl implements UrlManager {

    public static final Logger logger = LoggerFactory.getLogger("com.blazemeter");

    private String serverUrl = "";

    /**
     * Use this constructor from 'editBlazeRunnerParams.jsp`
     */
    public UrlManagerImpl() {
    }

    public UrlManagerImpl(String blazeMeterUrl) {
        this.serverUrl = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public String masterStatus(String appKey, String masterId) {
        return serverUrl + V4 + UrlManager.MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/status?events=false&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String tests(String appKey, int workspaceId) {
        return serverUrl + V4 + TESTS + "?limit=10000&workspaceId=" + workspaceId
                + "&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String multiTests(String appKey, int workspaceId) {
        return serverUrl + V4 + "/multi-tests?limit=10000&workspaceId=" + workspaceId
                + "&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String testId) {
        return serverUrl + V4 + TESTS + "/" + encode(testId, "testId", UrlManager.UTF_8)
                + "/start?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String collectionStart(String appKey, String collectionId) {
        return serverUrl + V4 + "/collections/" + encode(collectionId, "collectionId", UrlManager.UTF_8)
                + "/start?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStop(String appKey, String masterId) {
        return serverUrl + V4 + UrlManager.MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/stop?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testTerminate(String appKey, String masterId) {
        return serverUrl + V4 + UrlManager.MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/terminate?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String masterId) {
        return serverUrl + V4 + UrlManager.MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/reports/main/summary?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String getUser(String appKey) {
        return serverUrl + V4 + "/user?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String getCIStatus(String appKey, String masterId) {
        return serverUrl + V4 + MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + UrlManager.CI_STATUS + "?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String masterId) {
        return serverUrl + V4 + MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/reports/thresholds?format=junit&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String retrieveJTLZIP(String appKey, String sessionId) {
        return serverUrl + V4 + "/sessions/" + encode(sessionId, "sessionId", UrlManager.UTF_8)
                + "/reports/logs?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String generatePublicToken(String appKey, String masterId) {
        return serverUrl + V4 + MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/public-token?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String listOfSessionIds(String appKey, String masterId) {
        return serverUrl + V4 + MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "/sessions?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String activeTests(String appKey, int workspaceId) {
        return serverUrl + V4 + UrlManager.MASTERS + "?workspaceId=" + workspaceId
                + "&active=true&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey, String masterId) {
        return serverUrl + V4 + UrlManager.MASTERS + "/" + encode(masterId, "masterId", UrlManager.UTF_8)
                + "?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String properties(String appKey, String sessionId) {
        return serverUrl + V4 + "/sessions/" + encode(sessionId, "sessionId", UrlManager.UTF_8)
                + "/properties?target=all&app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String workspaces(String appKey, int accountId) {
        return serverUrl + V4 + "/workspaces?limit=1000&enabled=true&app_key="
                + encode(appKey, "appKey", UrlManager.UTF_8) + "&accountId=" + accountId
                + CLIENT_IDENTIFICATION;
    }

    @Override
    public String accounts(String appKey) {
        return serverUrl + V4 + "/accounts?app_key=" + encode(appKey, "appKey", UrlManager.UTF_8)
                + CLIENT_IDENTIFICATION;
    }

    private String encode(String text, String paramName, String encoding) {
        try {
            return URLEncoder.encode(text, encoding);
        } catch (UnsupportedEncodingException e) {
            logger.warn("Cannot encode " + paramName + " to '" + encoding + "' encoding", e);
            return text;
        }
    }
}

