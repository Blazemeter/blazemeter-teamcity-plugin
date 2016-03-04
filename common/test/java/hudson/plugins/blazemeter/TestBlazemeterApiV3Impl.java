package hudson.plugins.blazemeter;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.api.BzmHttpWrapper;
import com.blaze.api.TestType;
import com.blaze.runner.TestStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
public class TestBlazemeterApiV3Impl {
    private BlazemeterApiV3Impl blazemeterApiV3 =null;


    @BeforeClass
    public static void setUp()throws IOException{
        MockedAPI.startAPI();
        MockedAPI.userProfile();
        MockedAPI.getMasterStatus();
        MockedAPI.getTests();
        MockedAPI.getTestReport();
        MockedAPI.startTest();
        MockedAPI.active();
    }

    @AfterClass
    public static void tearDown()throws IOException{
        MockedAPI.stopAPI();
    }


    @Test
    public void createTest_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl) APIFactory.getAPI(null, TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertEquals(blazemeterApiV3.createTest(null), null);
    }

    @Test
    public void retrieveJUNITXML_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertEquals(blazemeterApiV3.retrieveJUNITXML(null), null);
    }


    @Test
    public void updateTestInfo_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertEquals(blazemeterApiV3.postJsonConfig(null, null), null);
    }

    @Test
    public void testStatus_NotFound(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertEquals(blazemeterApiV3.masterStatus(null), TestStatus.NotFound);
    }

    @Test
    public void getTestStatus_Running(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,TestConstants.mockedApiUrl,
                ApiVersion.v3.name());
        TestStatus testStatus=blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_100);
        Assert.assertEquals(testStatus, TestStatus.Running);
    }

    @Test
    public void getTestInfo_NotRunning(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        TestStatus testStatus=blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_140);
        Assert.assertEquals(testStatus, TestStatus.NotRunning);
    }


    @Test
    public void getTestInfo_Error(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        TestStatus testStatus=blazemeterApiV3.masterStatus(TestConstants.TEST_MASTER_NOT_FOUND);
        Assert.assertEquals(testStatus, TestStatus.Error);
    }

    @Test
    public void getTestInfo_NotFound(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI("",TestConstants.mockedApiUrl,ApiVersion.v3.name());
        TestStatus testStatus=blazemeterApiV3.masterStatus("");
        Assert.assertEquals(testStatus, TestStatus.NotFound);
    }



    @Test
    public void getUser_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
        Assert.assertEquals(blazemeterApiV3.getUser(), null);
    }

    @Test
    public void testReport_null(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
        try {
            Assert.assertEquals(blazemeterApiV3.testReport(null), null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   @Test
    public void stopTest_false(){
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       try {
           Assert.assertEquals(blazemeterApiV3.stopTest(null), false);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   @Test
    public void startTest_null() throws JSONException{
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,null,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.startTest(null,null), null);
    }

    @Test
    public void startTest_http() throws JSONException{
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.http), "15102806");
    }

    @Test
    public void startTest_jmeter() throws JSONException{
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.jmeter), "15102806");
    }

    @Test
    public void startTest_followme() throws JSONException{
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID, TestType.followme), "15102806");
    }

    @Test
    public void startTest_multi() throws JSONException{
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.startTest(TestConstants.TEST_MASTER_ID,TestType.multi), "15105877");
    }
    @Ignore
    @Test
    public void startTest_Retries() throws JSONException {
        blazemeterApiV3 = (BlazemeterApiV3Impl) APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_RETRIES,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        BlazemeterApi spyApi = Mockito.spy(blazemeterApiV3);
        BzmHttpWrapper spyWrapper=Mockito.spy(blazemeterApiV3.getBzmHttpWr());
        spyApi.setBzmHttpWr(spyWrapper);
        try {
            spyApi.startTest(TestConstants.TEST_MASTER_ID, TestType.http);
        } catch (JSONException je) {
            Mockito.verify(spyApi, Mockito.times(1)).active(TestConstants.TEST_MASTER_ID);
            String url="http://127.0.0.1:1234/api/latest/tests/testMasterId/start?" +
                    "api_key=mockedAPIKeyRetries&app_key=jnk100x987c06f4e10c4_clientId=CI_JENKINS&_clientVersion=2.2.-SNAPSHOT&​";
            Mockito.verify(spyWrapper, Mockito.times(6)).response(url,null, BzmHttpWrapper.Method.POST, JSONObject.class);

        }
    }


   @Test
    public void getTestRunStatus_notFound(){
       blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(null,TestConstants.mockedApiUrl,ApiVersion.v3.name());
       Assert.assertEquals(blazemeterApiV3.masterStatus(null), TestStatus.NotFound);
    }


    @Test
    public void getTestReport(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        JSONObject testReport= null;
        try {
            testReport = blazemeterApiV3.testReport(TestConstants.TEST_MASTER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(testReport.length()==33);


    }

    @Test
    public void getTestSessionStatusCode_25(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        int status=blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_25);
        Assert.assertTrue(status==25);
    }

    @Test
    public void getTestSessionStatusCode_70(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        int status=blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_70);
        Assert.assertTrue(status==70);
    }

    @Test
    public void getTestSessionStatusCode_140(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        int status=blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_140);
        Assert.assertTrue(status==140);
    }

    @Test
    public void getTestSessionStatusCode_100(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        int status=blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_100);
        Assert.assertTrue(status==100);
    }

    @Test
    public void getTestSessionStatusCode_0(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_EXCEPTION,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        int status=blazemeterApiV3.getTestMasterStatusCode(TestConstants.TEST_MASTER_0);
        Assert.assertTrue(status==0);
    }

   @Test
    public void active(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        boolean active=blazemeterApiV3.active("5133848");
        Assert.assertTrue(active);
    }

    @Test
    public void activeNot(){
        blazemeterApiV3=(BlazemeterApiV3Impl)APIFactory.getAPI(TestConstants.MOCKED_USER_KEY_VALID,
                TestConstants.mockedApiUrl,ApiVersion.v3.name());
        boolean active=blazemeterApiV3.active("51338483");
        Assert.assertFalse(active);
    }

}
