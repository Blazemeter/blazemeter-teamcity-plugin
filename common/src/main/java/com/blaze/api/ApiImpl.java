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

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.mail.MessagingException;
import javax.servlet.ServletException;

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


public class ApiImpl implements Api {

    private Logger logger = (Logger) LoggerFactory.getLogger("com.blazemeter");

    private String proxyHost = null;
    private int proxyPort = 0;
    private String proxyUser = null;
    private String proxyPass = null;

    private Proxy proxy = Proxy.NO_PROXY;
    private Authenticator auth = Authenticator.NONE;
    private String apiKey;
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

    public ApiImpl(String apiKey, String blazeMeterUrl) {
        this();
        this.apiKey = apiKey;
        this.serverUrl = blazeMeterUrl;
        this.urlManager = new UrlManagerImpl(this.serverUrl);
    }

    public ApiImpl(String apiKey, String blazeMeterUrl, HttpLogger httpl) {
        this();
        this.apiKey = apiKey;
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
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(id)) {
            return statusCode;
        }

        try {
            String url = this.urlManager.masterStatus(APP_KEY, apiKey, id);
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
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
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(id)) {
            return TestStatus.NotFound;
        }

        TestStatus testStatus;
        try {
            String url = this.urlManager.masterStatus(APP_KEY, apiKey, id);
            Request r = new Request.Builder().url(url).get()
                    .addHeader(ACCEPT, APP_JSON).
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
    public synchronized HashMap<String, String> startTest(String testId, boolean collection) throws JSONException,
            IOException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) {
            return null;
        }

        String url = "";
        HashMap<String, String> startResp = new HashMap<>();
        if (collection) {
            url = this.urlManager.collectionStart(APP_KEY, apiKey, testId);
        } else {
            url = this.urlManager.testStart(APP_KEY, apiKey, testId);
        }

        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
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
    public int getTestCount() throws JSONException, IOException, ServletException {
        if (StringUtils.isBlank(apiKey)) {
            return 0;
        }

        String url = this.urlManager.tests(APP_KEY, apiKey);

        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                    addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
            JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
            return result.length();
        } catch (RuntimeException e) {
            this.logger.warn("Error getting response from server: ", e);
            return -1;
        }
    }

    @Override
    public JSONObject stopTest(String testId) throws IOException, JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) {
            return null;
        }

        String url = this.urlManager.testStop(APP_KEY, apiKey, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public void terminateTest(String testId) throws IOException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) {
            return;
        }

        String url = this.urlManager.testTerminate(APP_KEY, apiKey, testId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
        okhttp.newCall(r).execute();
    }


    @Override
    public JSONObject testReport(String reportId) {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(reportId)) {
            return null;
        }

        String url = this.urlManager.testReport(APP_KEY, apiKey, reportId);
        JSONObject summary = null;
        JSONObject result = null;
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
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
    public Map<String, Collection<String>> getTestsMultiMap() throws IOException, MessagingException {
        return this.testsMultiMap().asMap();
    }

    @Override
    public LinkedHashMultimap<String, String> testsMultiMap() throws IOException, MessagingException {
        LinkedHashMultimap<String, String> testListOrdered = null;
        if (StringUtils.isBlank(apiKey)) {
            testListOrdered = LinkedHashMultimap.create(1, 1);
            testListOrdered.put(Constants.FILL_API_KEY, Constants.EMPTY_API_KEY);
            return testListOrdered;
        } else {
            String url = this.urlManager.tests(APP_KEY, apiKey);
            this.logger.info("Getting tests: " + url.substring(0, url.indexOf("?") + 14));

            try {
                Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                        addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();
                JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
                this.logger.info("Received json: " + jo.toString());
                JSONArray result = null;

                if (jo.has(JsonConstants.ERROR) && (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) &&
                        (((JSONObject) jo.get(JsonConstants.ERROR)).getInt(JsonConstants.CODE) == 401)) {
                    testListOrdered = LinkedHashMultimap.create(1, 1);
                    testListOrdered.put(Constants.INCORRECT_KEY, Constants.CHECK_ACCOUNT);
                    return testListOrdered;
                }

                if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                    result = (JSONArray) jo.get(JsonConstants.RESULT);
                }

                if (result != null) {
                    if (result.length() > 0) {
                        testListOrdered = LinkedHashMultimap.create(result.length(), result.length());

                        for (int i = 0; i < result.length(); i++) {
                            JSONObject en = null;
                            try {
                                en = result.getJSONObject(i);
                            } catch (JSONException e) {
                                this.logger.warn("JSONException while getting tests: " + e);
                            }
                            String id;
                            String name;
                            try {
                                if (en != null) {
                                    id = String.valueOf(en.get(JsonConstants.ID));
                                    name = en.has(JsonConstants.NAME) ? en.getString(JsonConstants.NAME).replaceAll("&", "&amp;") : "";
                                    String testType = en.has(JsonConstants.TYPE) ? en.getString(JsonConstants.TYPE) : Constants.UNKNOWN_TYPE;
                                    testListOrdered.put(name, id + "." + testType);
                                }
                            } catch (JSONException ie) {
                                this.logger.warn("JSONException while getting tests: " + ie);
                            }
                        }
                    } else {
                        testListOrdered = LinkedHashMultimap.create(1, 1);
                        testListOrdered.put(Constants.NO_TESTS, Constants.CHECK_ACCOUNT);
                    }
                }
            } catch (NullPointerException npe) {
                this.logger.warn("Exception while getting tests - check connection/proxy settings: ", npe);
                testListOrdered = LinkedHashMultimap.create(1, 1);
            } catch (ConnectException e) {
                this.logger.warn("Failed to connect while getting tests: " + e.getMessage());
                testListOrdered = LinkedHashMultimap.create(1, 1);
            } catch (UnknownHostException e) {
                this.logger.warn("Failed to resolve host while getting tests: " + e.getMessage());
                testListOrdered = LinkedHashMultimap.create(1, 1);
            } catch (Exception e) {
                this.logger.warn("Exception while getting tests: ", e);
                testListOrdered = LinkedHashMultimap.create(1, 1);
            }

            return testListOrdered;
        }
    }

    @Override
    public JSONObject getUser() throws IOException, JSONException {
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }

        String url = this.urlManager.getUser(APP_KEY, apiKey);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException, IOException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) {
            return null;
        }

        this.logger.info("Trying to get jtl url for the sessionId = " + sessionId);
        String url = this.urlManager.getCIStatus(APP_KEY, apiKey, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string()).getJSONObject(JsonConstants.RESULT);
    }


    @Override
    public String retrieveJUNITXML(String sessionId) throws IOException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) {
            return null;
        }

        String url = this.urlManager.retrieveJUNITXML(APP_KEY, apiKey, sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return okhttp.newCall(r).execute().body().string();
    }

    @Override
    public JSONObject retrieveJtlZip(String sessionId) throws IOException, JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) {
            return null;
        }

        this.logger.info("Trying to get jtl url for the sessionId=" + sessionId);
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, apiKey, sessionId);
        this.logger.info("Trying to retrieve jtl json for the sessionId = " + sessionId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public JSONObject generatePublicToken(String sessionId) throws IOException, JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) {
            return null;
        }

        String url = this.urlManager.generatePublicToken(APP_KEY, apiKey, sessionId);
        RequestBody emptyBody = RequestBody.create(null, new byte[0]);
        Request r = new Request.Builder().url(url).post(emptyBody).addHeader(ACCEPT, APP_JSON).
                addHeader(CONTENT_TYPE, APP_JSON_UTF_8).build();

        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) throws IOException, JSONException {
        List<String> sessionsIds = new ArrayList<>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, apiKey, masterId);
        Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
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
        String url = this.urlManager.activeTests(APP_KEY, apiKey);
        JSONObject jo = null;
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).
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

            return false;
        } catch (Exception e) {
            this.logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
            return false;
        }
    }

    @Override
    public boolean ping() throws Exception {
        String url = this.urlManager.version(APP_KEY);
        JSONObject jo = null;
        try {
            Request r = new Request.Builder().url(url).get().addHeader(ACCEPT, APP_JSON).build();
            jo = new JSONObject(okhttp.newCall(r).execute().body().string());
            return jo.isNull(JsonConstants.ERROR);
        } catch (Exception e) {
            this.logger.info("Failed to ping server: " + jo, e);
            throw e;
        }
    }

    @Override
    public boolean notes(String note, String masterId) throws Exception {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(masterId)) {
            return false;
        }

        String noteEsc = StringEscapeUtils.escapeJson("{'" + JsonConstants.NOTE + "':'" + note + "'}");
        String url = this.urlManager.masterId(APP_KEY, apiKey, masterId);
        JSONObject noteJson = new JSONObject(noteEsc);
        RequestBody body = RequestBody.create(TEXT, noteJson.toString());
        Request r = new Request.Builder().url(url).patch(body).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            if (jo.get(JsonConstants.ERROR).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report notes to masterId = " + masterId, e);
        }
        return true;
    }

    @Override
    public boolean properties(JSONArray properties, String sessionId) throws Exception {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(sessionId)) {
            return false;
        }

        String url = this.urlManager.properties(APP_KEY, apiKey, sessionId);
        RequestBody body = RequestBody.create(JSON, properties.toString());
        Request r = new Request.Builder().url(url).post(body).build();
        JSONObject jo = new JSONObject(okhttp.newCall(r).execute().body().string());
        try {
            if (jo.get(JsonConstants.RESULT).equals(JSONObject.NULL)) {
                return false;
            }
        } catch (Exception e) {
            throw new Exception("Failed to submit report properties to sessionId = " + sessionId, e);
        }
        return true;
    }

    @Override
    public JSONObject testConfig(String testId) throws IOException, JSONException {
        if (StringUtils.isBlank(apiKey) & StringUtils.isBlank(testId)) {
            return null;
        }

        String url = this.urlManager.testConfig(APP_KEY, apiKey, testId);
        Request r = new Request.Builder().url(url).get().build();
        return new JSONObject(okhttp.newCall(r).execute().body().string());
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }


    @Override
    public String getServerUrl() {
        return this.serverUrl;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setUrlManager(UrlManager urlManager) {
        this.urlManager = urlManager;
    }
}
