package com.blaze.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import jetbrains.buildServer.agent.BuildProgressLogger;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

/**
 * 
 * @author Marcel Milea
 *
 */
public class BlazeBean {
    //Default properties
    public final static String DEFAULT_SETTINGS_DATA_FOLDER = "/DataFolder";

    private String userKey;
	private String blazeMeterUrl;
	private String serverName;
	private String serverPort;
	private String username;
	private String password;
    private BuildProgressLogger logger;
	private BlazemeterApi blazemeterApi;
	private String session;
	private String aggregate;

	public BlazeBean(){
		
	}
	
	public BlazeBean(String userKey, String blazeMeterUrl, String serverName,
                     int serverPort, String username,
                      String password, BuildProgressLogger logger) {
		this.blazemeterApi = new BlazemeterApi(serverName, serverPort, username, password);
		this.userKey = userKey;
        this.blazeMeterUrl = blazeMeterUrl;
        this.logger = logger;
	}
	
	@NotNull
	public String getDebugKey() {
		return "Debug Key";
	}
	
	private BlazemeterApi getAPI(){
		if (blazemeterApi == null){
            int serverPortInt=0;
            if(serverPort!=null&&!serverPort.isEmpty()){
                serverPortInt=Integer.parseInt(serverPort);
            }
			blazemeterApi = new BlazemeterApi(serverName, serverPortInt, username, password);
		}
		
		return blazemeterApi;
	}
	
	public HashMap<String, String> getTests() {
		try {
			return getAPI().getTestList(userKey);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		HashMap<String, String> temp = new HashMap<String, String>();
		
		return temp;
	}

	public boolean startTest(String testId, BuildProgressLogger logger) {
        org.json.JSONObject json;
        int countStartRequests = 0;
        do {
            json = getAPI().startTest(userKey, testId);
            countStartRequests++;
            if (countStartRequests > 5) {
            	logger.error("Could not start BlazeMeter Test");
                return false;
            }
        } while (json == null);
        
        try {
			if (!json.get("response_code").equals(200)) {
				if (json.get("response_code").equals(500) && json.get("error").toString().startsWith("Test already running")) {
					logger.error("Test already running, please stop it first");
					return false;
				}
                //Try again.
				json = getAPI().startTest(userKey, testId);
                if (!json.get("response_code").equals(200)) {
                	logger.error("Could not start BlazeMeter Test -" + json.get("error").toString());
                    return false;
                } 				
			}
			session = json.get("session_id").toString();
		} catch (JSONException e) {
			logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
		}
		return true;
	}

	public boolean isReportReady(){
        //get testGetArchive information
		org.json.JSONObject json = getAPI().aggregateReport(userKey, session);
        try {
            if (json.get("response_code").equals(404))
                return false;
            else
            	if (json.get("response_code").equals(200)){
            		return true;
            	}
        } catch (JSONException e) {
        } 
        return false;
    }
	
	@SuppressWarnings("static-access")
	public boolean waitForReport(BuildProgressLogger logger){
        //get testGetArchive information
		org.json.JSONObject json = getAPI().aggregateReport(userKey, session);
        for (int i = 0; i < 200; i++) {
            try {
                if (json.get("response_code").equals(404))
                    json = getAPI().aggregateReport(userKey, session);
                else
                    break;
            } catch (JSONException e) {
            } finally {
                try {
					Thread.currentThread().sleep(5 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
        }
        
        aggregate = null;

        for (int i = 0; i < 30; i++) {
            try {
                if (!json.get("response_code").equals(200)){
                	logger.error("Error: Requesting aggregate report response code:" + json.get("response_code"));
                }
                aggregate = json.getJSONObject("report").get("aggregate").toString();
            } catch (JSONException e) {
            	logger.error("Error: Exception while starting BlazeMeter Test [" + e.getMessage() + "]");
            }

            if (!aggregate.equals("null"))
                break;

            try {
				Thread.sleep(2 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            json = getAPI().aggregateReport(userKey, session);
        }

        if (aggregate == null) {
        	logger.error("Error: Requesting aggregate is not available");
            return false;
        }
     
        return true;
    }
	
	/**
	 * Get report results.
	 * @param logger 
	 * @return -1 fail, 0 success, 1 unstable
	 */
	public int getReport(int errorFailedThreshold, int errorUnstableThreshold, int responseTimeFailedThreshold, int responseTimeUnstableThreshold, BuildProgressLogger logger){
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);

		} catch (IOException e) {
			logger.error("Error: Requesting aggregate Test Result is not available");
			return -1;
		}
		
        if (aggregateTestResult == null) {
        	logger.error("Error: Requesting aggregate Test Result is not available");
            return -1;
        }

        double thresholdTolerance = 0.00005; //null hypothesis
        double errorPercent = aggregateTestResult.getErrorPercentage();
        double AverageResponseTime = aggregateTestResult.getAverage();

        if (errorFailedThreshold >= 0 && errorPercent - errorFailedThreshold > thresholdTolerance) {
        	logger.error("Test ended with failure on error percentage threshold");
            return -1;
        } else if (errorUnstableThreshold >= 0
                && errorPercent - errorUnstableThreshold > thresholdTolerance) {
        	logger.error("Test ended with unstable on error percentage threshold");
            return 1;
        }

        if (responseTimeFailedThreshold >= 0 && AverageResponseTime - responseTimeFailedThreshold > thresholdTolerance) {
        	logger.error("Test ended with failure on response time threshold");
            return -1;
        } else if (responseTimeUnstableThreshold >= 0
                && AverageResponseTime - responseTimeUnstableThreshold > thresholdTolerance) {
        	logger.error("Test ended with unstable on response time threshold");
            return 1;
        }

        return 0;   		
	}

	public boolean uploadJMX(String testId, String filename, String pathname){
		return getAPI().uploadJmx(userKey, testId, filename, pathname);
	}
	
    public void uploadFile(String testId, String dataFolder, String fileName, BuildProgressLogger logger) {
        org.json.JSONObject json = getAPI().uploadFile(userKey, testId, fileName, dataFolder + File.separator + fileName);
        try {
            if (!json.get("response_code").equals(new Integer(200))) {
                logger.error("Could not upload file " + fileName + " " + json.get("error").toString());
            }
        } catch (JSONException e) {
            logger.error("Could not upload file " + fileName + " " + e.getMessage());
        }
    } 	

    public void stopTest(String testId, BuildProgressLogger logger){
    	org.json.JSONObject json;

    	int countStartRequests = 0;
        do {
            json = getAPI().stopTest(userKey, testId);
            logger.message("Attempt to stop test with result: "+json.toString());
            countStartRequests++;
            if (countStartRequests > 5) {
            	logger.error("Could not stop BlazeMeter Test "+ testId);
            	return ;
            }
        } while (json == null);

        try {
			if (json.get("response_code").equals(200)) {
				logger.message("Test stopped succesfully."+json.toString());
			} else {
				String error = json.get("error").toString();
				logger.error("Error stopping test. Reported error is: "+error+" "+json.toString());
				logger.error("Please use BlazeMeter website to manually stop the test with ID: " + testId);
			}
		} catch (JSONException e) {
			logger.error("Error: Exception while stopping BlazeMeter Test [" + e.getMessage() + "]");
			logger.error("Please use BlazeMeter website to manually stop the test with ID: " + testId);
		}
    	
    }
    
    public TestInfo getTestStatus(String testId){
    	return getAPI().getTestRunStatus(userKey, testId);
    }
    
	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSession() {
		return session;
	}

	public boolean publishReportArtifact(String pathname) {
        AggregateTestResult aggregateTestResult;
		try {
			aggregateTestResult = AggregateTestResult.generate(aggregate);
			if (aggregateTestResult == null){
				return false;
			} 
			
	        double errorPercent = aggregateTestResult.getErrorPercentage();
	        double AverageResponseTime = aggregateTestResult.getAverage();

	        File file = new File(pathname);
	        file.createNewFile();
	        FileWriter fw = new FileWriter(file);
	        fw.write("<build>");
	        fw.write("<statisticValue key=\"blazeAvgResponseTime\" value=\""+AverageResponseTime+"\"/>");
	        fw.write("<statisticValue key=\"blazeThresholdTime\" value=\""+errorPercent+"\"/>");
	        fw.write("</build>");
	        fw.flush();
	        fw.close();
	        return true;
		} catch (IOException e) {
            e.printStackTrace();
		}
		return false;
	}
}
