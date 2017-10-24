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
import com.blaze.api.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class TestApiImpl {
    private ApiImpl blazemeterApi = null;

    @BeforeClass
    public static void setUp() throws IOException {
        MockedAPI.startAPI();
        MockedAPI.getMasterStatus();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.accountId();
        MockedAPI.workspaces();
    }

    @AfterClass
    public static void tearDown() {
        MockedAPI.stopAPI();
    }


    @Test
    public void getTestStatus_Running() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_100);
        assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_140);
        assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        TestStatus testStatus = blazemeterApi.masterStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        assertEquals(testStatus, TestStatus.Error);
    }


    @Test
    public void startTest_multi() throws JSONException, IOException {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        assertEquals(blazemeterApi.startTest(TestConstants.TEST_MASTER_ID, true).get(JsonConstants.ID),
                "15105877");
    }

    @Test
    public void getTestReport() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        JSONObject testReport = blazemeterApi.testReport(TestConstants.TEST_MASTER_ID);
        assertTrue(testReport.length() == 33);
    }

    @Test
    public void getTestSessionStatusCode_25() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        assertTrue(status == 25);
    }


    @Test
    public void getTestSessionStatusCode_70() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        assertTrue(status == 70);
    }


    @Test
    public void getTestSessionStatusCode_140() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        assertTrue(status == 140);
    }


    @Test
    public void getTestSessionStatusCode_100() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        assertTrue(status == 100);
    }


    @Test
    public void getTestSessionStatusCode_0() {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_EXCEPTION, TestConstants.TEST_API_SECRET_EXCEPTION, TestConstants.mockedApiUrl);
        int status = blazemeterApi.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        assertTrue(status == 0);
    }

    @Test
    public void accountId() throws IOException {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        assertTrue(blazemeterApi.accounts().size() == 3);
    }

    @Test
    public void workspaces() throws IOException {
        blazemeterApi = new ApiImpl(TestConstants.TEST_API_ID_VALID, TestConstants.TEST_API_SECRET_VALID, TestConstants.mockedApiUrl);
        HashMap<Integer, String> ws = blazemeterApi.workspaces();
        assertEquals(1, ws.size());
        assertTrue("DWorkspace".equals(ws.get(32563)));
    }

}
