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

package com.blaze.api;

import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.MediaType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface Api {

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    MediaType TEXT = MediaType.parse("text/plain; charset=ISO-8859-1");
    String AUTHORIZATION = "Authorization";
    String ACCEPT = "Accept";
    String CONTENT_TYPE = "Content-type";
    String APP_JSON = "application/json";
    String APP_JSON_UTF_8 = "application/json; charset=UTF-8";

    String APP_KEY = "jnk100x987c06f4e10c4";

    TestStatus masterStatus(String id);

    String getTestLabel(String testId);

    int getTestMasterStatusCode(String id);

    HashMap<String, String> startTest(String testId, boolean collection) throws IOException;

    JSONObject stopTest(String testId) throws IOException;

    void terminateTest(String testId) throws IOException;

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> testsMultiMap();

    LinkedHashMultimap<String, String> collectionsMultiMap(int workspaceId);

    JSONObject getUser() throws IOException;

    JSONObject getCIStatus(String sessionId) throws IOException;

    boolean active(String testId);

    String retrieveJUNITXML(String sessionId) throws IOException;

    JSONObject retrieveJtlZip(String sessionId) throws IOException;

    List<String> getListOfSessionIds(String masterId) throws IOException;

    JSONObject generatePublicToken(String sessionId) throws IOException;

    String getServerUrl();

    void setServerUrl(String serverUrl);

    boolean notes(String note, String masterId);

    boolean properties(JSONArray properties, String sessionId);

    String getApiKeyID();

    void setApiKeyID(String apiKeyID);

    void setApiKeySecret(String apiKeySecret);

    boolean verifyCredentials();

    HashMap<Integer, String> accounts();

    HashMap<Integer, String> workspaces();

    Map<String, Collection<String>> getTestsMultiMap();

    boolean collection(String testId);
}

