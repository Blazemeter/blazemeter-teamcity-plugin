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

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    private static final Comparator<Map.Entry<String, String>> comparator = new Comparator<Map.Entry<String, String>>() {
        @Override
        public int compare(Map.Entry<String, String> e1, Map.Entry<String, String> e2) {
            return e1.getValue().compareToIgnoreCase(e2.getValue());
        }
    };

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
    private UrlManager urlManager;
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
                    .addInterceptor(new RetryInterceptor(logger))
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .proxy(this.proxy)
                    .proxyAuthenticator(this.auth).build();
        } catch (Exception ex) {
            logger.warn("ERROR Instantiating HTTPClient. Exception received: ", ex);
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
                .addInterceptor(new RetryInterceptor(logger))
                .addInterceptor(httpLog)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .proxyAuthenticator(this.auth).build();
    }

    private JSONObject executeGetRequest(String url) throws IOException {
        return execute(createRequestBuilder(url).get().build());
    }

    private JSONObject executePostRequest(String url, RequestBody requestBody) throws IOException {
        return execute(createRequestBuilder(url).post(requestBody).build());
    }

    private JSONObject execute (Request request) throws IOException {
        JSONObject response = new JSONObject(okhttp.newCall(request).execute().body().string());
        logger.info("Received json: " + response.toString());
        return response;
    }

    private Request.Builder createRequestBuilder(String url) {
        return new Request.Builder().url(url).
                addHeader(ACCEPT, APP_JSON).
                addHeader(AUTHORIZATION, getCredentials()).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8);
    }

    @Override
    public int getTestMasterStatusCode(String id) {
        try {
            String url = urlManager.masterStatus(APP_KEY, id);
            JSONObject result = (JSONObject) executeGetRequest(url).get(JsonConstants.RESULT);
            return result.getInt("progress");
        } catch (Exception e) {
            logger.warn("Error getting master status code: ", e);
        }

        return 0;
    }

    @Override
    public TestStatus masterStatus(String masterId) {
        try {
            String url = urlManager.masterStatus(APP_KEY, masterId);
            JSONObject result = (JSONObject) executeGetRequest(url).get(JsonConstants.RESULT);

            return (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) ?
                    TestStatus.NotFound :
                    getMasterStatusFromJSON(result, masterId);
        } catch (Exception e) {
            logger.warn("Error while getting master status ", e);
            return TestStatus.Error;
        }
    }

    private TestStatus getMasterStatusFromJSON(JSONObject result, String masterId) {
        return (result.has(JsonConstants.STATUS) && !result.getString(JsonConstants.STATUS).equals("ENDED")) ?
                TestStatus.Running :
                getMasterStatusErrorsFromJSON(result, masterId);
    }

    private TestStatus getMasterStatusErrorsFromJSON(JSONObject result, String masterId) {
        if (result.has(JsonConstants.ERRORS) && !result.get(JsonConstants.ERRORS).equals(JSONObject.NULL)) {
            logger.debug("Error while getting master status: " + result.get(JsonConstants.ERRORS).toString());
            return TestStatus.Error;
        } else {
            logger.info("Master with id = " + masterId + " has status = " + TestStatus.NotRunning.name());
            return TestStatus.NotRunning;
        }
    }


    @Override
    public synchronized HashMap<String, String> startTest(String testId, boolean isCollection) throws IOException {
        HashMap<String, String> startResp = new HashMap<>();

        String url = isCollection ?
                urlManager.collectionStart(APP_KEY, testId) :
                urlManager.testStart(APP_KEY, testId);


        JSONObject response = executePostRequest(url, RequestBody.create(null, new byte[0]));

        try {
            JSONObject result = (JSONObject) response.get(JsonConstants.RESULT);
            startResp.put(JsonConstants.ID, String.valueOf(result.get(JsonConstants.ID)));
            startResp.put(JsonConstants.TEST_ID, isCollection ? String.valueOf(result.get(JsonConstants.TEST_COLLECTION_ID)) :
                    String.valueOf(result.get(JsonConstants.TEST_ID)));
            startResp.put(JsonConstants.NAME, result.getString(JsonConstants.NAME));
        } catch (Exception e) {
            startResp.put(JsonConstants.ERROR, response.get(JsonConstants.ERROR).toString());
        }

        return startResp;
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException {
        String url = urlManager.testStop(APP_KEY, testId);
        return executePostRequest(url, RequestBody.create(null, new byte[0]));
    }

    @Override
    public void terminateTest(String testId) throws IOException {
        String url = urlManager.testTerminate(APP_KEY, testId);
        executePostRequest(url, RequestBody.create(null, new byte[0]));
    }


    @Override
    public JSONObject testReport(String reportId) {
        String url = urlManager.testReport(APP_KEY, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            result = executeGetRequest(url).getJSONObject(JsonConstants.RESULT);
            summary = (JSONObject) result.getJSONArray("summary").get(0);
        } catch (Exception e) {
            logger.warn("Aggregate report(result object): " + result);
            logger.warn("Error while parsing aggregate report summary: check common jenkins log and make sure that aggregate report" +
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


        HashMap<Integer, String> ws = this.workspaces();
        logger.info("Getting tests...");
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String wsn = ws.get(k);
            String url = urlManager.tests(APP_KEY, k);
            try {

                JSONObject response = executeGetRequest(url);
                JSONArray result = null;
                if (response.has(JsonConstants.ERROR) && (response.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                        (((JSONObject) response.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                    return testListOrdered;
                }

                if (response.has(JsonConstants.RESULT) && (!response.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) response.get(JsonConstants.RESULT);
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
                            logger.warn("JSONException while getting tests: " + e);
                        }
                        String id;
                        String name;
                        try {
                            if (entry != null) {
                                id = String.valueOf(entry.get(JsonConstants.ID));
                                name = entry.has(JsonConstants.NAME) ? entry.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                                String testType;
                                try {
                                    testType = entry.getJSONObject(JsonConstants.CONFIGURATION).getString(JsonConstants.TYPE);
                                } catch (Exception e) {
                                    testType = Constants.UNKNOWN_TYPE;
                                }
                                wst.put(id + "." + testType, name + "(" + id + "." + testType + ")");
                            }
                        } catch (JSONException ie) {
                            logger.warn("JSONException while getting tests: " + ie);
                        }
                    }
                }

                List<Map.Entry<String, String>> list = new ArrayList<>(wst.entries());
                Collections.sort(list, comparator);
                for (Map.Entry<String, String> entry : list) {
                    testListOrdered.put(entry.getKey(), entry.getValue());
                }

            } catch (Exception e) {
                logger.warn("Exception while getting tests: ", e);
                logger.warn("Check connection/proxy settings");
                testListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
            }
        }

        return testListOrdered;
    }

    @Override
    public LinkedHashMultimap<String, String> collectionsMultiMap(int workspaceId) {
        LinkedHashMultimap<String, String> collectionsListOrdered = LinkedHashMultimap.create();
        logger.info("Getting collections...");
        String url = urlManager.multiTests(APP_KEY, workspaceId);
        try {
            JSONObject response = executeGetRequest(url);
            JSONArray result = null;
            if (response.has(JsonConstants.ERROR) && (response.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                    (((JSONObject) response.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                return collectionsListOrdered;
            }

            if (response.has(JsonConstants.RESULT) && (!response.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                result = (JSONArray) response.get(JsonConstants.RESULT);
            }

            if (result != null && result.length() > 0) {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject entry = null;
                    try {
                        entry = result.getJSONObject(i);
                    } catch (JSONException e) {
                        logger.warn("JSONException while getting tests: " + e);
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
                        logger.warn("JSONException while getting tests: " + ie);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Exception while getting tests: ", e);
            logger.warn("Check connection/proxy settings");
            collectionsListOrdered.put(Constants.CHECK_SETTINGS, Constants.CHECK_SETTINGS);
        }
        return collectionsListOrdered;
    }

    @Override
    public HashMap<Integer, String> accounts() {
        String url = urlManager.accounts(APP_KEY);
        HashMap<Integer, String> acs = new HashMap<>();
        try {
            JSONArray result = executeGetRequest(url).getJSONArray(JsonConstants.RESULT);
            for (int i = 0; i < result.length(); i++) {
                JSONObject a = result.getJSONObject(i);
                acs.put(a.getInt(JsonConstants.ID), a.getString(JsonConstants.NAME));
            }
        } catch (Exception e) {
            logger.error("Failed to get accounts: ", e);
        }
        return acs;
    }

    @Override
    public HashMap<Integer, String> workspaces() {
        HashMap<Integer, String> acs = this.accounts();
        HashMap<Integer, String> ws = new HashMap<>();

        Set<Integer> keys = acs.keySet();
        for (Integer key : keys) {
            String url = urlManager.workspaces(APP_KEY, key);

            try {
                JSONArray result = executeGetRequest(url).getJSONArray(JsonConstants.RESULT);
                for (int i = 0; i < result.length(); i++) {
                    JSONObject s = result.getJSONObject(i);
                    ws.put(s.getInt(JsonConstants.ID), s.getString(JsonConstants.NAME));
                }
            } catch (Exception e) {
                logger.error("Failed to get workspaces: " + e);
            }
        }
        return ws;
    }

    @Override
    public JSONObject getUser() throws IOException {
        String url = urlManager.getUser(APP_KEY);
        return executeGetRequest(url);
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws IOException {
        logger.info("Trying to get jtl url for the sessionId = " + sessionId);
        String url = urlManager.getCIStatus(APP_KEY, sessionId);
        return executeGetRequest(url).getJSONObject(JsonConstants.RESULT);
    }


    @Override
    public String retrieveJUNITXML(String sessionId) throws IOException {
        String url = urlManager.retrieveJUNITXML(APP_KEY, sessionId);
        return executeGetRequest(url).toString();
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException {
        logger.info("Trying to get jtl url for the sessionId=" + sessionId);
        String url = urlManager.retrieveJTLZIP(APP_KEY, sessionId);
        logger.info("Trying to retrieve jtl json for the sessionId = " + sessionId);
        return executeGetRequest(url);
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException {
        String url = urlManager.generatePublicToken(APP_KEY, sessionId);
        return executePostRequest(url, RequestBody.create(null, new byte[0]));
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException {
        List<String> sessionsIds = new ArrayList<>();
        String url = urlManager.listOfSessionIds(APP_KEY, masterId);
        JSONObject jo = executeGetRequest(url);

        try {
            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
            }
        } catch (Exception e) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        }

        return sessionsIds;
    }

    @Override
    public boolean active(String testId) {
        HashMap<Integer, String> ws = this.workspaces();
        Set<Integer> wsk = ws.keySet();
        for (Integer k : wsk) {
            String url = urlManager.activeTests(APP_KEY, k);
            try {
                JSONObject response = executeGetRequest(url);
                if (response.has(JsonConstants.RESULT) && (!response.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    JSONObject result = (JSONObject) response.get(JsonConstants.RESULT);
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
                logger.info("Failed to check if test=" + testId + " is active", e);
                return false; // TODO: Why here return false?? If at the next workspace will be active test we skip it!!!
            }
        }
        return false;
    }

    @Override
    public boolean verifyCredentials() {
        logger.info("Verifying userKey...");
        String url = this.urlManager.getUser(APP_KEY);
        try {
            JSONObject response = executeGetRequest(url);
            return response.get(JsonConstants.ERROR).equals(JSONObject.NULL);
        } catch (Exception e) {
            logger.error("Got an exception while verifying credentials: ", e);
            return false;
        }
    }

    @Override
    public boolean notes(String note, String masterId) {
        String url = urlManager.masterId(APP_KEY, masterId);
        try {
            JSONObject response = executePostRequest(url,
                    RequestBody.create(TEXT, StringEscapeUtils.escapeJson("{'" + JsonConstants.NOTE + "':'" + note + "'}")));

            if (response.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit report notes to masterId = " + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) {
        String url = urlManager.properties(APP_KEY, sessionId);
        try {
            JSONObject response = executePostRequest(url, RequestBody.create(JSON, properties.toString()));
            if (response.get(JsonConstants.RESULT).equals(JSONObject.NULL)) {
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

        LinkedHashMultimap<String, String> tests = this.testsMultiMap();
        Set<Map.Entry<String, String>> entries = tests.entries();
        for (Map.Entry<String, String> e : entries) {
            int point = e.getKey().indexOf(".");
            if (point > 0 && testId.contains( e.getKey().substring(0, point))) {
                collection = (e.getKey().substring(point + 1)).contains("multi");
                if (e.getKey().substring(point + 1).contains("workspace")) {
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
