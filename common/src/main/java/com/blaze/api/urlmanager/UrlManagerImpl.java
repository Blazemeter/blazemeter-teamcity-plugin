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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class UrlManagerImpl implements UrlManager {

    private String serverUrl = "";

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
    public String masterStatus(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + UrlManager.MASTERS + "/" + masterId + "/status?events=false&api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String tests(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + "/api/web/tests?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStart(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/tests/"
                + testId + "/start?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String collectionStart(String appKey, String userKey, String collectionId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            collectionId = URLEncoder.encode(collectionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/collections/"
                + collectionId + "/start?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testStop(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + LATEST + UrlManager.MASTERS + "/"
                + masterId + "/stop?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testTerminate(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + UrlManager.MASTERS + "/"
                + masterId + "/terminate?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testReport(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            masterId = URLEncoder.encode(masterId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + UrlManager.MASTERS + "/"
                + masterId + "/reports/main/summary?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String getUser(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/user?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String testConfig(String appKey, String userKey, String testId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/tests/" + testId + "?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String getCIStatus(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + MASTERS + "/" + masterId + UrlManager.CI_STATUS + "?api_key="
                + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String retrieveJUNITXML(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + MASTERS + "/" + masterId +
                "/reports/thresholds?format=junit&api_key="
                + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }


/*

    @Override
    public String postJsonConfig(String appKey, String userKey, String testId) {
        String getTestInfo=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTestInfo= serverUrl +LATEST+TESTS+"/"+testId+"/custom?custom_test_type=yahoo&api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTestInfo;
    }
*/

    @Override
    public String retrieveJTLZIP(String appKey, String userKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/sessions/" + sessionId +
                "/reports/logs?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String generatePublicToken(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + LATEST + MASTERS + "/" + masterId +
                "/publicToken?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }


    @Override
    public String listOfSessionIds(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + MASTERS + "/" + masterId +
                "/sessions?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String activeTests(String appKey, String userKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + WEB + "/active?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String version(String appKey) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return serverUrl + LATEST + WEB + "/version?app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String masterId(String appKey, String userKey, String masterId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + UrlManager.MASTERS + "/" + masterId + "?api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }

    @Override
    public String properties(String appKey, String userKey, String sessionId) {
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return serverUrl + LATEST + "/sessions/" + sessionId + "/properties?target=all&api_key=" + userKey + "&app_key=" + appKey + CLIENT_IDENTIFICATION;
    }
}

