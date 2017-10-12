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

package com.blaze;

import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import com.blaze.api.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

public class TestApiImpl {
    private ApiImpl blazemeterApi = null;


    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestConfig();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.active();
        MockedAPI.ping();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        MockedAPI.stopAPI();
    }


    @Test
    public void getTestStatus_Running() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound() {
        blazemeterApi = new ApiImpl("", "", TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus("");
        Assert.assertEquals(testStatus, TestStatus.NotFound);
    }


    @Test
    public void startTest_http() throws JSONException, IOException {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApi.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConstants.ID)
                , "15102806");
    }

    @Test
    public void startTest_jmeter() throws JSONException, IOException {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApi.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConstants.ID),
                "15102806");
    }

    @Test
    public void startTest_followme() throws JSONException, IOException {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApi.startTest(TestConstants.TEST_MASTER_ID, false).get(JsonConstants.ID),
                "15102806");
    }

    @Test
    public void startTest_multi() throws JSONException, IOException {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID,
                TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApi.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConstants.ID),
                "15105877");
    }


    @Test
    public void getTestRunStatus_notFound() {
        blazemeterApi = new ApiImpl(null, null, TestConstants.mockedApiUrl);
        Assert.assertEquals(blazemeterApi.masterStatus(null), TestStatus.NotFound);
    }

    @Test
    public void getTestList_4_5() throws IOException, JSONException, ServletException, MessagingException {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_5_TESTS, TestConstants.mockedApiUrl);
        LinkedHashMultimap<String, String> testList = blazemeterApi.testsMultiMap();
        Assert.assertTrue(testList.asMap().size() == 4);
        Assert.assertTrue(testList.size() == 5);

    }

    @Test
    public void getTestReport() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApi.testReport(TestConstants.TEST_MASTER_ID);
        Assert.assertTrue(testReport.length() == 33);


    }

    @Test
    public void getTestSessionStatusCode_25() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status == 25);
    }

    @Test
    public void getTestSessionStatusCode_70() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status == 70);
    }

    @Test
    public void getTestSessionStatusCode_140() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status == 140);
    }

    @Test
    public void getTestSessionStatusCode_100() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status == 100);
    }

    @Test
    public void getTestSessionStatusCode_0() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_EXCEPTION, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status == 0);
    }

    @Test
    public void active() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApi.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    public void activeNot() {
        blazemeterApi = new ApiImpl(TestConstants.MOCKED_USER_KEY_ID_VALID, TestConstants.MOCKED_USER_KEY__SECRET_VALID, TestConstants.mockedApiUrl);
        boolean active = blazemeterApi.active("51338483");
        Assert.assertFalse(active);
    }
}
