package com.blaze.api.urlmanager;

import com.blaze.runner.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Created by dzmitrykashlach on 5/12/14.
 */
public class BmUrlManagerV3Impl implements BmUrlManager{

    private String SERVER_URL = Constants.DEFAULT_BZM_SERVER;
    private static String CLIENT_IDENTIFICATION = "_clientId=CI_TEAMCITY&_clientVersion=SNAPSHOT-201408281729&​";

    static{
        try{
            CLIENT_IDENTIFICATION= URLEncoder.encode(CLIENT_IDENTIFICATION, "UTF-8");
            CLIENT_IDENTIFICATION=CLIENT_IDENTIFICATION.substring(0,59);
            CLIENT_IDENTIFICATION= URLDecoder.decode(CLIENT_IDENTIFICATION, "UTF-8");

        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public BmUrlManagerV3Impl(String blazeMeterUrl) {
        SERVER_URL = blazeMeterUrl;
    }

    @Override
    public String getServerUrl() {
        return SERVER_URL;
    }

    @Override
    public String getTests(String appKey, String userKey){
        String getTests=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTests=SERVER_URL+"/api/latest/tests?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTests;
    }

    @Override
    public String testStatus(String appKey, String userKey, String sessionId) {
        String testStatus=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            sessionId = URLEncoder.encode(sessionId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStatus=SERVER_URL+"/api/latest/sessions/"+sessionId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStatus;
    }


    @Override
    public String scriptUpload(String appKey, String userKey, String testId, String fileName) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String fileUpload(String appKey, String userKey, String testId, String fileName) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String testStart(String appKey, String userKey, String testId) {
        String testStart=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStart=SERVER_URL+"/api/latest/tests/"
                +testId+"/start?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStart;
    }

    @Override
    public String testStop(String appKey, String userKey, String testId) {
        String testStop=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            testId = URLEncoder.encode(testId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testStop=SERVER_URL+"/api/latest/tests/"
                +testId+"/stop?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;
        return testStop;
    }

    @Override
    public String testReport(String appKey, String userKey, String reportId) {
        String testAggregateReport=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
            reportId = URLEncoder.encode(reportId, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        testAggregateReport=SERVER_URL+"/api/latest/sessions/"
                +reportId+"/reports/main/summary?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return testAggregateReport;
    }

    @Override
    public String getUrlForTestList(String appKey, String userKey) {
        return Constants.NOT_IMPLEMENTED;
    }

    @Override
    public String getTestInfo(String appKey, String userKey, String testId){
        String getTestInfo=null;
        try {
            appKey = URLEncoder.encode(appKey, "UTF-8");
            userKey = URLEncoder.encode(userKey, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        getTestInfo=SERVER_URL+"/api/latest/tests/"+testId+"?api_key="+userKey+"&app_key="+appKey+ CLIENT_IDENTIFICATION;

        return getTestInfo;
    }
}