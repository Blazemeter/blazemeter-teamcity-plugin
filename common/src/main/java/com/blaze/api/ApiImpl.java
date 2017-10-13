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

import com.blaze.api.urlmanager.UrlManager;
import com.blaze.api.urlmanager.UrlManagerImpl;
import com.blaze.runner.Constants;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class ApiImpl implements Api {

    private Logger logger = (Logger) LoggerFactory.getLogger("com.blazemeter");

    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPass = null;

    private Proxy proxy = Proxy.NO_PROXY;
    private Authenticator auth = Authenticator.NONE;
    private String apiKeyID;
    private String apiKeySecret;
    private String credentials;
    private String serverUrl;
    UrlManager urlManager;
    private OkHttpClient okhttp = null;
    private HttpLogger httpl = null;


    public ApiImpl() {
        try {
            proxyHost = System.getProperty(Constants.PROXY_HOST);

            if (!StringUtils.isBlank(this.proxyHost)) {
                logger.info("Using http.proxyHost = " + this.proxyHost);

                try {
                    this.proxyPort = Integer.parseInt(System.getProperty(Constants.PROXY_PORT));
                    logger.info("Using http.proxyPort = " + this.proxyPort);

                } catch (NumberFormatException nfe) {
                    logger.warn("Failed to read http.proxyPort: ", nfe);
                }

                this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxyHost, this.proxyPort));
                this.proxyUser = System.getProperty(Constants.PROXY_USER);
                logger.info("Using http.proxyUser = " + this.proxyUser);
                this.proxyPass = System.getProperty(Constants.PROXY_PASS);
                logger.info("Using http.proxyPass = " + StringUtils.left(this.proxyPass, 4));
            }


            if (!StringUtils.isBlank(this.proxyUser) && !StringUtils.isBlank(this.proxyPass)) {
                this.auth = new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(proxyUser, proxyPass);
                        if (response.request().header("Proxy-Authorization") != null) {
                            return null; // Give up, we've already attempted to authenticate.
                        }
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                };
            }

            okhttp = new OkHttpClient.Builder()
                    .addInterceptor(new RetryInterceptor(this.logger))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .proxy(this.proxy)
                    .proxyAuthenticator(this.auth).build();
        } catch (Exception ex) {
            this.logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
        }

    }

    public ApiImpl(String apiKeyID, String apiKeySecret, String blazeMeterUrl) {
        this();
        this.apiKeyID = apiKeyID;
        this.apiKeySecret = apiKeySecret;
        this.serverUrl = blazeMeterUrl;
        this.urlManager = new UrlManagerImpl(this.serverUrl);
    }

    public ApiImpl(String apiKeyID, String apiKeySecret, String blazeMeterUrl, HttpLogger httpl) {
        this();
        this.apiKeyID = apiKeyID;
        this.apiKeySecret = apiKeySecret;
        this.serverUrl = blazeMeterUrl;
        this.urlManager = new UrlManagerImpl(this.serverUrl);
        this.httpl = httpl;
        HttpLoggingInterceptor httpLog;
        httpLog = new HttpLoggingInterceptor(this.httpl);
        httpLog.setLevel(HttpLoggingInterceptor.Level.BODY);
        okhttp = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(this.logger))
                .addInterceptor(httpLog)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .proxyAuthenticator(this.auth).build();
    }


    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                    addHeader(AUTHORIZATION, getCredentials()).
                        addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            this.logger.warn("Error getting master status code: ", e);
        }

        return statusCode;
    }

    @Override
    public TestStatus masterStatus(String id) {
        TestStatus testStatus;
        try {
            String url = this.urlManager.masterStatus(APP_KEY, id);
            Request r = new Request.Builder().url(url).get()
                    .addHeader(ACCEPT, APP_JSON).addHeader(AUTHORIZATION, getCredentials()).
                            addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has(JsonConstants.STATUS) && !result.getString(JsonConstants.STATUS).equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    if (result.has(JsonConstants.ERRORS) && !result.get(JsonConstants.ERRORS).equals(JSONObject.NULL)) {
                        this.logger.debug("Error while getting master status: " + result.get(JsonConstants.ERRORS).toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                        this.logger.info("Master with id = " + id + " has status = " + TestStatus.NotRunning.name());
                    }
                }
            }
        } catch (Exception e) {
            this.logger.warn("Error while getting master status ", e);
            testStatus = TestStatus.Error;
        }

        return testStatus;
    }

    @Override
    public synchronized HashMap<String, String> startTest(String testId, boolean collection) throws IOException {

        String url = "";
        HashMap<String, String> startResp = new HashMap<>();
        if (collection) {
            url = this.urlManager.collectionStart(APP_KEY, testId);
        } else {
            url = this.urlManager.testStart(APP_KEY, testId);
        }

        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());

        try {
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            startResp.put(JsonConstants.ID, String.valueOf(result.get(JsonConstants.ID)));
            startResp.put(JsonConstants.TEST_ID, collection ? String.valueOf(result.get(JsonConstants.TEST_COLLECTION_ID)) :
                    String.valueOf(result.get(JsonConstants.TEST_ID)));
            startResp.put(JsonConstants.NAME, result.getString(JsonConstants.NAME));
        } catch (Exception e) {
            startResp.put(JsonConstants.ERROR, jo.get(JsonConstants.ERROR).toString());
        }

        return startResp;
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException {
        String url = this.urlManager.testStop(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public void terminateTest(String testId) throws IOException {
        String url = this.urlManager.testTerminate(APP_KEY, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        okhttp.newCall(r).execute();
    }


    @Override
    public JSONObject testReport(String reportId) {
        String url = this.urlManager.testReport(APP_KEY, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                    addHeader(AUTHORIZATION, getCredentials()).
                        addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            result = new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConstants.RESULT);
            summary = (JSONObject) result.getJSONArray("summary")
                    .get(0);
        } catch (Exception e) {
            this.logger.warn("Aggregate report(result object): " + result);
            this.logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
                    "is valid/not empty.", e);
        }

        return summary;
    }

    /**
     * Called from UI for display list of tests
     */
    public Map<String, Collection<String>> getTestsMultiMap() {
        return this.testsMultiMap().asMap();
    }

    @Override
    public LinkedHashMultimap<String, String> testsMultiMap() {
        LinkedHashMultimap<String, String> testListOrdered = LinkedHashMultimap.create();
        HashMap<Integer,String> ws = this.workspaces();
        logger.info("Getting tests...");
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String wsn=ws.get(k);
            String url = this.urlManager.tests(APP_KEY, k);
            try {
                Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                        .addHeader(AUTHORIZATION, getCredentials()).
                                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                JSONArray result = null;
                this.logger.info("Received json: " + jo.toString());
                if (jo.has(JsonConstants.ERROR) && (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                        (((JSONObject) jo.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                    return testListOrdered;
                }
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) jo.get(JsonConstants.RESULT);
                }
                LinkedHashMultimap<String, String> wst = LinkedHashMultimap.create();
                LinkedHashMultimap<String, String> wsc = this.collectionsMultiMap(k);
                wst.putAll(wsc);
                if (result != null && result.length() > 0) {
                    testListOrdered.put(String.valueOf(k) + "." + "workspace", "========" + wsn + "(" + k + ")========");
                    for (int i = 0; i < result.length(); i++) {
                        JSONObject entry = null;
                        try {
                            entry = result.getJSONObject(i);
                        } catch (JSONException e) {
                            this.logger.warn("JSONException while getting tests: " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (entry != null) {
                                id = String.valueOf(entry.get(JsonConstants.ID));
                                name = entry.has(JsonConstants.NAME) ? entry.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                                String testType = null;
                                try {
                                    testType = entry.getJSONObject(JsonConstants.CONFIGURATION).getString(JsonConstants.TYPE);
                                } catch (Exception e) {
                                    testType = Constants.UNKNOWN_TYPE;
                                }
                                wst.put(id + "." + testType, name + "(" + id + "." + testType + ")");
                            }
                        } catch (JSONException ie) {
                            this.logger.warn("JSONException while getting tests: " + ie);
                        }
                    }
                }
                Comparator<Map.Entry<String, String>> c = new Comparator<Map.Entry<String, String>>() {
                    @Override
                    public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
                        return e1.getValue().compareToIgnoreCase(e2.getValue());
                    }
                };

                List<Map.Entry<String, String>> list = new ArrayList<>(wst.entries());
                Collections.sort(list, c);
                for (Map.Entry<String, String> entry : list) {
                    testListOrdered.put(entry.getKey(), entry.getValue());
                }

            } catch (Exception e) {
                this.logger.warn("Exception while getting tests: ", e);
                this.logger.warn("Check connection/proxy settings");
                testListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
            }
        }

        return testListOrdered;
    }

    @Override
    public LinkedHashMultimap<String, String> collectionsMultiMap(int workspaceId) {
        LinkedHashMultimap<String, String> collectionsListOrdered = LinkedHashMultimap.create();
        logger.info("Getting collections...");
        String url = this.urlManager.multiTests(APP_KEY, workspaceId);
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                    .addHeader(AUTHORIZATION, getCredentials()).
                            addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONArray result = null;
            this.logger.info("Received json: " + jo.toString());
            if (jo.has(JsonConstants.ERROR) && (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                    (((JSONObject) jo.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                return collectionsListOrdered;
            }
            if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                result = (JSONArray) jo.get(JsonConstants.RESULT);
            }
            if (result != null && result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject entry = null;
                    try {
                        entry = result.getJSONObject(i);
                    } catch (JSONException e) {
                        this.logger.warn("JSONException while getting tests: " + e);
                    }
                    String id;
                    String name;
                    try {
                        if (entry != null) {
                            id = String.valueOf(entry.get(JsonConstants.ID));
                            name = entry.has(JsonConstants.NAME) ? entry.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                            String collectionsType = null;
                            try {
                                collectionsType = entry.getString(JsonConstants.COLLECTION_TYPE);
                            } catch (Exception e) {
                                collectionsType = Constants.UNKNOWN_TYPE;
                            }
                            collectionsListOrdered.put(id + "." + collectionsType, name + "(" + id + "." + collectionsType + ")");
                        }
                    } catch (JSONException ie) {
                        this.logger.warn("JSONException while getting tests: " + ie);
                    }
                }
            }
        } catch (Exception e) {
            this.logger.warn("Exception while getting tests: ", e);
            this.logger.warn("Check connection/proxy settings");
            collectionsListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
        }
        return collectionsListOrdered;
    }

    @Override
    public HashMap<Integer,String> accounts() {
        String url = this.urlManager.accounts(APP_KEY);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                .addHeader(AUTHORIZATION, getCredentials()).build();
        JSONObject jo = null;
        JSONArray result = null;
        JSONObject dp = null;
        HashMap<Integer, String> acs = new HashMap<>();
        try {
            jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        } catch (Exception ioe) {
            logger.error("Failed to get accounts: ", ioe);
            return acs;
        }
        try {
            result = jo.getJSONArray(JsonConstants.RESULT);
        } catch (Exception e) {
            logger.error("Failed to get accounts: ", e);
            return acs;
        }
        try {
            for (int i = 0; i < result.length(); i++) {
                JSONObject a = result.getJSONObject(i);
                acs.put(a.getInt(JsonConstants.ID),a.getString(JsonConstants.NAME));
            }
        } catch (Exception e) {
            logger.error("Failed to get accounts: ", e);
            return acs;
        }
        return acs;
    }

    @Override
    public HashMap<Integer, String> workspaces() {
        HashMap<Integer, String> acs = this.accounts();
        HashMap<Integer, String> ws = new HashMap<>();

        Set<Integer> keys = acs.keySet();
        for (Integer key : keys) {
            String url = this.urlManager.workspaces(APP_KEY, key);
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                    .addHeader(AUTHORIZATION, getCredentials()).build();
            JSONObject jo = null;
            JSONArray result = null;
            try {
                jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            } catch (Exception ioe) {
                logger.error("Failed to get workspaces: " + ioe);
                return ws;
            }
            try {
                result = jo.getJSONArray(JsonConstants.RESULT);
            } catch (Exception e) {
                logger.error("Failed to get workspaces: " + e);
                return ws;
            }
            try {

                for (int i = 0; i < result.length(); i++) {
                    JSONObject s = result.getJSONObject(i);
                    ws.put(s.getInt(JsonConstants.ID), s.getString(JsonConstants.NAME));
                }
            } catch (Exception e) {
                logger.error("Failed to get workspaces: " + e);
                return ws;
            }
        }
        return ws;
    }

    @Override
    public JSONObject getUser() throws IOException {
        String url = this.urlManager.getUser(APP_KEY);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws IOException {
        this.logger.info("Trying to get jtl url for the sessionId = " + sessionId);
        String url = this.urlManager.getCIStatus(APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConstants.RESULT);
    }


    @Override
    public String retrieveJUNITXML(String sessionId) throws IOException {
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return okhttp.newCall(r).execute().body().string();
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException {
        this.logger.info("Trying to get jtl url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, sessionId);
        this.logger.info("Trying to retrieve jtl json for the sessionId = " + sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException {
        String url = this.urlManager.generatePublicToken(APP_KEY, sessionId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException {
        List<String> sessionsIds = new ArrayList<>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, masterId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
            }
        } catch (Exception e) {
            this.logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        }

        return sessionsIds;
    }

    @Override
    public boolean active(String testId) {
        HashMap<Integer, String> ws = this.workspaces();
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String url = this.urlManager.activeTests(APP_KEY, k);
            JSONObject jo = null;
            try {
                Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON)
                        .addHeader(AUTHORIZATION, getCredentials()).
                                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                JSONObject result = null;
                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONObject) jo.get(JsonConstants.RESULT);
                    JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                    for (int i = 0; i < tests.length(); i++) {
                        if (String.valueOf(tests.getInt(i)).equals(testId)) {
                            return true;
                        }
                    }
                    JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
                    for (int i = 0; i < collections.length(); i++) {
                        if (String.valueOf(collections.getInt(i)).equals(testId)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                this.logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
                return false; // TODO: Why here return false?? If at the next workspace will be active test we skip it!!!
            }
        }
        return false;
    }


    @Override
    public boolean notes(String note, String masterId) {
        String noteEsc = StringEscapeUtils.escapeJson("{'" + JsonConstants.NOTE + "':'" + note + "'}");
        String url = this.urlManager.masterId(APP_KEY, masterId);
        JSONObject noteJson = new JSONObject(noteEsc);
        RequestBody body = RequestBody.create(TEXT, noteJson.toString());
        Request r = new Request.Builder().url(url).patch(body).addHeader(AUTHORIZATION, getCredentials()).build();
        try {
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            if (jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit report notes to masterId = " + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) {
        String url = this.urlManager.properties(APP_KEY, sessionId);
        RequestBody body = RequestBody.create(JSON, properties.toString());
        Request r = new Request.Builder().url(url).post(body).addHeader(AUTHORIZATION, getCredentials()).build();
        try {
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            if (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit report properties to sessionId = " + sessionId, e);
        }
        return true;
    }

    @Override
    public boolean collection(String testId) {
        boolean exists = false;
        boolean collection = false;

        LinkedHashMultimap tests = this.testsMultiMap();
        Set<Map.Entry> entries = tests.entries();
        for (Map.Entry e : entries) {
            int point = ((String) e.getKey()).indexOf(".");
            if (point > 0 && testId.contains(((String) e.getKey()).substring(0, point))) {
                collection = (((String) e.getKey()).substring(point + 1)).contains("multi");
                if (((String) e.getKey()).substring(point + 1).contains("workspace")) {
                    throw new RuntimeException("Please, select valid testId instead of workspace header");
                }
                exists = true;
            }
            if (collection) {
                break;
            }
        }
        if (!exists) {
            throw new RuntimeException("Test with test id = " + testId + " is not present on server");
        }
        return collection;
    }

    private String getCredentials() {
        if (credentials == null || credentials.isEmpty()) {
            credentials = Credentials.basic(apiKeyID, apiKeySecret);
        }

        return credentials;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    public String getServerUrl() {
        return this.serverUrl;
    }

    @Override
    public String getApiKeyID() {
        return apiKeyID;
    }

    /**
     * Use this setter from 'editBlazeRunnerParams.jsp`
     */
    public void setUrlManager(UrlManager urlManager) {
        this.urlManager = urlManager;
    }

    /**
     * Use this setter from 'editBlazeRunnerParams.jsp`
     */
    public void setApiKeyID(String apiKeyID) {
        this.apiKeyID = apiKeyID;
    }

    /**
     * Use this setter from 'editBlazeRunnerParams.jsp`
     */
    public void setApiKeySecret(String apiKeySecret) {
        this.apiKeySecret = apiKeySecret;
    }
}
