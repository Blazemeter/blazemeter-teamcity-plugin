package com.blaze.api;

import com.blaze.api.urlmanager.BmUrlManagerV3Impl;
import com.blaze.runner.JsonConstants;
import com.blaze.runner.TestStatus;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.text.StringUtil;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author
 */
public class BlazemeterApiV3Impl implements BlazemeterApi {
    private Logger logger = (Logger) LoggerFactory.getLogger("com.blazemeter");

    private String userKey;

    public static final String APP_KEY = "tmcbzms4sbnsgb1z0hry";
    BzmHttpWrapper bzHttp;
    BmUrlManagerV3Impl urlManager;

    public BlazemeterApiV3Impl(String userKey, String bzmUrl) {
        this.userKey = userKey;
        urlManager = new BmUrlManagerV3Impl(bzmUrl);
        try {
            bzHttp = new BzmHttpWrapper();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public TestStatus masterStatus(String id) {
        TestStatus testStatus = null;

        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(id)) {
            testStatus = TestStatus.NotFound;
            return testStatus;
        }

        try {
            String url = this.urlManager.masterStatus(APP_KEY, userKey, id);
            JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            if (result.has(JsonConstants.DATA_URL) && result.get(JsonConstants.DATA_URL) == null) {
                testStatus = TestStatus.NotFound;
            } else {
                if (result.has("status") && !result.getString("status").equals("ENDED")) {
                    testStatus = TestStatus.Running;
                } else {
                    logger.info("Test is not running on server");
                    if (result.has("errors") && !result.get("errors").equals(JSONObject.NULL)) {
                        logger.debug("Error received from server: " + result.get("errors").toString());
                        testStatus = TestStatus.Error;
                    } else {
                        testStatus = TestStatus.NotRunning;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
            testStatus = TestStatus.Error;
        }
        return testStatus;
    }

    @Override
    public synchronized String startTest(String testId, TestType testType) throws JSONException {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(testId)) return null;
        String url = "";
        switch (testType) {
            case multi:
                url = this.urlManager.collectionStart(APP_KEY, userKey, testId);
                break;
            default:
                url = this.urlManager.testStart(APP_KEY, userKey, testId);
        }
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);

        if (jo==null) {
            if (logger.isDebugEnabled())
                logger.debug("Received NULL from server while start operation: will do 5 retries");
            boolean isActive=this.active(testId);
            if(!isActive){
                int retries = 1;
                while (retries < 6) {
                    try {
                        if (logger.isDebugEnabled())
                            logger.debug("Trying to repeat start request: " + retries + " retry.");
                        logger.debug("Pausing thread for " + 10*retries + " seconds before doing "+retries+" retry.");
                        Thread.sleep(10000*retries);
                        jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);
                        if (jo!=null) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                        if (logger.isDebugEnabled())
                            logger.debug("Start operation was interrupted at pause during " + retries + " request retry.");
                    } catch (Exception ex) {
                        if (logger.isDebugEnabled())
                            logger.debug("Received bad response from server while starting test: " + retries + " retry.");
                    }
                    finally {
                        retries++;
                    }
                }


            }
        }
        JSONObject result=null;
        try{
            result = (JSONObject) jo.get(JsonConstants.RESULT);
        }catch (Exception e){
            if (logger.isDebugEnabled())
                logger.debug("Error while starting test: ",e);
            throw new JSONException("Faild to get 'result' node "+e.getMessage());

        }
        return String.valueOf(result.getInt(JsonConstants.ID));
    }

    /**
     * @param testId - test id
     *               //     * @throws IOException
     *               //     * @throws ClientProtocolException
     */
    @Override
    public boolean stopTest(String testId) throws Exception {
        if (StringUtil.isEmptyOrSpaces(userKey) & StringUtil.isEmptyOrSpaces(testId)) return false;

        String url = this.urlManager.testStop(APP_KEY, userKey, testId);
        JSONObject stopJSON = this.bzHttp.response(url, null, BzmHttpWrapper.Method.POST, JSONObject.class);

        if (stopJSON.getJSONArray(JsonConstants.RESULT).length() > 0) {
            return true;
        } else {
            String error = stopJSON.get(JsonConstants.RESULT).toString();
            throw new Exception("Error stopping test with ID=" + testId + ". Reported error is: " + error.toString());
        }

    }

    /**
     * @param reportId - report Id same as Session Id, can be obtained from start stop status.
     *                 //     * @throws IOException
     *                 //     * @throws ClientProtocolException
     */
    @Override
    public JSONObject testReport(String reportId) throws JSONException {
        if (StringUtil.isEmptyOrSpaces(userKey) & StringUtil.isEmptyOrSpaces(reportId)) return null;

        String url = this.urlManager.testReport(APP_KEY, userKey, reportId);
        JSONObject summary = (JSONObject)
                this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class).getJSONObject(JsonConstants.RESULT)
                .getJSONArray("summary")
                .get(0);
        return summary;
    }

    @Override
    public LinkedHashMultimap<String, String> getTestList() throws IOException, JSONException {

        LinkedHashMultimap<String, String> testListOrdered = null;

        if (userKey == null || userKey.trim().isEmpty()) {
        } else {
            String url = this.urlManager.tests(APP_KEY, userKey);
            System.out.println("BlazemeterApiV3Impl: UserKey=" + this.userKey.substring(0, 5));
            System.out.println("BlazemeterApiV3Impl: Url=" + url);
            JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
            System.out.println("BlazemeterApiV3Impl: JSONObject=" + jo.toString());
            JSONArray result = (JSONArray) jo.get(JsonConstants.RESULT);
            System.out.println("BlazemeterApiV3Impl: JSONResult=" + result.toString());
            if (result != null && result.length() > 0) {
                testListOrdered = LinkedHashMultimap.create(result.length(), result.length());
                for (int i = 0; i < result.length(); i++) {
                    JSONObject en = null;
                    en = result.getJSONObject(i);
                    String id;
                    String name;
                    if (en != null) {
                        id = String.valueOf(en.getInt("id"));
                        name = en.get(JsonConstants.NAME).equals(JSONObject.NULL)? "<name is not defined>" : en.getString(JsonConstants.NAME).replaceAll("&", "&amp;");
                        String testType = en.has(JsonConstants.TYPE) ? en.getString(JsonConstants.TYPE) : TestType.http.name();
                        testListOrdered.put(name + "(" + testType + ")", id);

                    }
                }
                return testListOrdered;
            }
        }
        return testListOrdered;
    }

    @Override
    public JSONObject createTest(JSONObject data) {
        if (StringUtil.isEmptyOrSpaces(userKey)) return null;
        String url = this.urlManager.createTest(APP_KEY, userKey);
        JSONObject jo = this.bzHttp.response(url, data, BzmHttpWrapper.Method.POST,JSONObject.class);
        return jo;
    }




    @Override
    public JSONObject getCIStatus(String sessionId) throws JSONException, NullPointerException {
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(sessionId)) return null;
        String url = this.urlManager.ciStatus(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class).getJSONObject(JsonConstants.RESULT);
        return jo;
    }

    @Override
    public JSONObject postJsonConfig(String testId, JSONObject data) {
        if (StringUtils.isEmpty(userKey) & StringUtils.isEmpty(testId)) return null;

        String url = this.urlManager.postJsonConfig(APP_KEY, userKey, testId);
        JSONObject jo = this.bzHttp.response(url, data, BzmHttpWrapper.Method.POST,JSONObject.class);
        return jo;
    }

    @Override
    public String retrieveJUNITXML(String sessionId) {
        if (StringUtil.isEmptyOrSpaces((userKey)) & StringUtil.isEmptyOrSpaces(sessionId)) return null;
        String url = this.urlManager.retrieveJUNITXML(APP_KEY, userKey, sessionId);
        String xmlJunit = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,String.class);
        return xmlJunit;
    }

    @Override
    public JSONObject retrieveJTLZIP(String sessionId) {
        if (StringUtil.isEmptyOrSpaces(userKey) & StringUtil.isEmptyOrSpaces((sessionId))) return null;
        String url = this.urlManager.retrieveJTLZIP(APP_KEY, userKey, sessionId);
        JSONObject jtlzip = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
        return jtlzip;
    }

    @Override
    public JSONObject getUser() {
        if (StringUtils.isEmpty(userKey)) return null;
        String url = this.urlManager.getUser(APP_KEY, userKey);
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
        return jo;
    }


    @Override
    public JSONObject generatePublicToken(String sessionId) {
        if (StringUtils.isEmpty(userKey) & StringUtils.isEmpty(sessionId)) return null;

        String url = this.urlManager.generatePublicToken(APP_KEY, userKey, sessionId);
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.POST,JSONObject.class);
        return jo;
    }


    @Override
    public JSONObject getTestsJSON() {
        String url = this.urlManager.tests(APP_KEY, userKey);
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);
        return jo;
    }

    @Override
    public JSONObject terminateTest(String testId) {
        if (StringUtils.isEmpty(this.userKey) & StringUtils.isEmpty(testId)) return null;

        String url = this.urlManager.testTerminate(APP_KEY, this.userKey, testId);
        return this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET,JSONObject.class);

    }


    @Override
    public boolean active(String testId) {
        boolean isActive=false;
        String url = this.urlManager.activeTests(APP_KEY, userKey);
        JSONObject jo = null;
        try {
            jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = null;
            if (jo.has(JsonConstants.RESULT) && (!jo.get(JsonConstants.RESULT).equals(JSONObject.NULL))) {
                result = (JSONObject) jo.get(JsonConstants.RESULT);
                JSONArray tests = (JSONArray) result.get(JsonConstants.TESTS);
                for(int i=0;i<tests.length();i++){
                    if(String.valueOf(tests.getInt(i)).equals(testId)){
                        isActive=true;
                        return isActive;
                    }
                }
                JSONArray collections = (JSONArray) result.get(JsonConstants.COLLECTIONS);
                for(int i=0;i<collections.length();i++){
                    if(String.valueOf(collections.getInt(i)).equals(testId)){
                        isActive=true;
                        return isActive;
                    }
                }
            }
            return isActive;
        } catch (JSONException je) {
            logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, je);
            return false;
        } catch (Exception e) {
            logger.info("Failed to check if test=" + testId + " is active: received JSON = " + jo, e);
            return false;
        }
    }

    @Override
    public String getBlazeMeterURL() {
        return this.urlManager.getServerUrl();
    }



    @Override
    public int getTestMasterStatusCode(String id) {
        int statusCode = 0;
        if (StringUtils.isBlank(userKey) & StringUtils.isBlank(id)) {
            return statusCode;
        }
        try {
            String url = this.urlManager.masterStatus(APP_KEY, userKey, id);
            JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
            JSONObject result = (JSONObject) jo.get(JsonConstants.RESULT);
            statusCode = result.getInt("progress");
        } catch (Exception e) {
            logger.warn("Error getting status ", e);
        } finally {
            {
                return statusCode;
            }
        }
    }

    @Override
    public List<String> getListOfSessionIds(String masterId) {
        List<String> sessionsIds = new ArrayList<String>();
        String url = this.urlManager.listOfSessionIds(APP_KEY, userKey, masterId);
        JSONObject jo = this.bzHttp.response(url, null, BzmHttpWrapper.Method.GET, JSONObject.class);
        try {
            JSONArray sessions = jo.getJSONObject(JsonConstants.RESULT).getJSONArray("sessions");
            int sessionsLength = sessions.length();
            for (int i = 0; i < sessionsLength; i++) {
                sessionsIds.add(sessions.getJSONObject(i).getString(JsonConstants.ID));
            }
        } catch (JSONException je) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, je);
        } catch (Exception e) {
            logger.info("Failed to get list of sessions from JSONObject " + jo, e);
        } finally {
            return sessionsIds;
        }

    }

    @Override
    public BzmHttpWrapper getBzmHttpWr() {
        return this.bzHttp;
    }

    @Override
    public void setBzmHttpWr(BzmHttpWrapper bzmHttpWr) {
        this.bzHttp =bzmHttpWr;
    }
}
