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

package com.blaze.api;

import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public interface BlazemeterApi {

    TestStatus masterStatus(String testId) throws JSONException;

    String startTest(String testId,TestType testType) throws JSONException;

    boolean stopTest(String testId) throws Exception;

    JSONObject testReport(String reportId) throws JSONException;

    LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException;

    JSONObject getUser();

    boolean active(String testId);

    int getTestMasterStatusCode(String id);

    JSONObject getCIStatus(String sessionId) throws JSONException;

    JSONObject postJsonConfig(String testId, JSONObject data);

    JSONObject createTest(JSONObject data);

    String retrieveJUNITXML(String sessionId);

    JSONObject retrieveJTLZIP(String sessionId);

    JSONObject generatePublicToken(String sessionId);

    JSONObject terminateTest(String testId);

    JSONObject getTestsJSON();

    String getBlazeMeterURL();

    BzmHttpWrapper getBzmHttpWr();
    void setBzmHttpWr(BzmHttpWrapper bzmHttpWr);

    List<String> getListOfSessionIds(String masterId);
}
