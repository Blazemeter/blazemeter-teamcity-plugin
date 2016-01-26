package hudson.plugins.blazemeter;

import com.blaze.api.urlmanager.BmUrlManager;
import com.blaze.api.urlmanager.BmUrlManagerV2Impl;
import com.blaze.runner.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by dzmitrykashlach on 9/01/15.
 */

public class TestBmUrlManagerV2 {
    private String userKey="881a84b35e97c4342bf11";
    private String appKey="jnk100x987c06f4e10c4";
    private String testId="123456789";
    private String sessionId="987654321";
    private String reportId="1212121212";
    private String fileName="111111111";
    private BmUrlManager bmUrlManager=new BmUrlManagerV2Impl(TestConstants.mockedApiUrl);

    @Test
    public void getServerUrl(){
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void setServerUrl(){
        bmUrlManager.setServerUrl(TestConstants.mockedApiUrl);
        Assert.assertTrue(bmUrlManager.getServerUrl().equals(TestConstants.mockedApiUrl));
    }

    @Test
    public void testStatus(){
        String expTestGetStatus=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testGetStatus.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestGetStatus=bmUrlManager.masterStatus(appKey, userKey, testId);
        Assert.assertEquals(expTestGetStatus, actTestGetStatus);
    }

    @Test
    public void getTests(){
    String expGetTestsUrl=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/getTests.json/?app_key="+appKey+
            "&user_key="+userKey+"&test_id=all"+BmUrlManager.CLIENT_IDENTIFICATION;
    String actGetTestsUrl=bmUrlManager.tests(appKey,userKey);
        Assert.assertEquals(expGetTestsUrl, actGetTestsUrl);
    }

    @Test
    public void testStart(){
        String expTestStart=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testStart.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestStart=bmUrlManager.testStart(appKey, userKey, testId);
        Assert.assertEquals(expTestStart,actTestStart);
    }

    @Test
    public void testStop(){
        String expTestStop=bmUrlManager.getServerUrl()+"/api/rest/blazemeter/testStop.json/?app_key="
                +appKey+"&user_key="+userKey+"&test_id="+testId+"&"+BmUrlManager.CLIENT_IDENTIFICATION;

        String actTestStop=bmUrlManager.testStop(appKey, userKey, testId);
        Assert.assertEquals(expTestStop,actTestStop);
    }


    @Test
    public void testReport(){
        String expTestReport=bmUrlManager.getServerUrl()+"/api/latest/sessions/"+reportId+"/reports/main/summary?api_key="
                +userKey+"&app_key="+appKey+BmUrlManager.CLIENT_IDENTIFICATION;
        String actTestReport=bmUrlManager.testReport(appKey, userKey, reportId);
        Assert.assertEquals(expTestReport,actTestReport);
    }

    @Test
    public void getUser(){
        String expGetUser=Constants.NOT_IMPLEMENTED;
        String actGetUser=bmUrlManager.getUser(appKey, userKey);
        Assert.assertEquals(expGetUser,actGetUser);
    }


    @Test
    public void ciStatus(){
        String expGetTresholds= Constants.NOT_IMPLEMENTED;
        String actGetTresholds=bmUrlManager.ciStatus(appKey, userKey, sessionId);
        Assert.assertEquals(expGetTresholds,actGetTresholds);
    }

    @Test
    public void getTestInfo(){
        String expGetTestInfo=Constants.NOT_IMPLEMENTED;
        String actGetTestInfo=bmUrlManager.testConfig(appKey, userKey, testId);
        Assert.assertEquals(expGetTestInfo,actGetTestInfo);
    }

    @Test
    public void putTestInfo(){
        String expPutTestInfo=Constants.NOT_IMPLEMENTED;
        String actPutTestInfo=bmUrlManager.postJsonConfig(appKey, userKey, testId);
        Assert.assertEquals(expPutTestInfo,actPutTestInfo);
    }

    @Test
    public void createTest(){
        String expCreateTest=Constants.NOT_IMPLEMENTED;
        String actCreateTest=bmUrlManager.createTest(appKey, userKey);
        Assert.assertEquals(expCreateTest,actCreateTest);
    }

    @Test
    public void retrieveJUNITXML(){
        String expRetrieveJUNITXML=Constants.NOT_IMPLEMENTED;
        String actRetrieveJUNITXML=bmUrlManager.retrieveJUNITXML(appKey, userKey, sessionId);
        Assert.assertEquals(expRetrieveJUNITXML,actRetrieveJUNITXML);
    }

    @Test
    public void activeTests(){
        String expActiveTests=Constants.NOT_IMPLEMENTED;
        String actActiveTests=bmUrlManager.activeTests(appKey, userKey);
        Assert.assertEquals(expActiveTests,actActiveTests);
    }

}
