package com.blaze.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.blaze.api.BzmServiceManager;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.messages.DefaultMessagesInfo;
import jetbrains.buildServer.util.PropertiesUtil;

import com.blaze.entities.TestInfo;
import com.blaze.runner.Constants;

/**
 * @author Marcel Milea
 *
 */
public class BlazeAgentProcessor implements BuildProcess{
	private static final int CHECK_INTERVAL = 60000;
	private BzmServiceManager bzmServiceManager;
	private AgentRunningBuild agentRunningBuild;
	private BuildRunnerContext buildRunnerContext;
	private ArtifactsWatcher artifactsWatcher;
	
	private String validationError;
	private String testId;
	private int testDuration;
	int errorUnstableThreshold;
	int errorFailedThreshold;
	int responseTimeUnstableThreshold;
	int responseTimeFailedThreshold;
	String dataFolder;
	String mainJMX;
	
	final BuildProgressLogger logger;
	boolean finished;
	boolean needTestUpload;
	boolean interrupted;
	
	
	public BlazeAgentProcessor(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher) {
		this.agentRunningBuild = agentRunningBuild;
		this.buildRunnerContext = buildRunnerContext;
		this.artifactsWatcher = artifactsWatcher;

		this.finished = false;
		
		logger = agentRunningBuild.getBuildLogger();
		Map<String, String> buildSharedMap = agentRunningBuild.getSharedConfigParameters();
		String proxyPortStr=buildSharedMap.get(Constants.PROXY_SERVER_PORT);
        int proxyPortInt=0;
        if(proxyPortStr!=null&&!proxyPortStr.isEmpty()){
            proxyPortInt=Integer.parseInt(proxyPortStr);
        }
        bzmServiceManager = new BzmServiceManager(
				buildSharedMap.get(Constants.USER_KEY),
                buildSharedMap.get(Constants.BLAZEMETER_URL),
                buildSharedMap.get(Constants.PROXY_SERVER_NAME),
				proxyPortInt,
                buildSharedMap.get(Constants.PROXY_USERNAME),
                buildSharedMap.get(Constants.PROXY_PASSWORD),
                logger);
	}

	private String validateParams(Map<String, String> params) {
		
		testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
		if (isNullorEmpty(testId)) {
			return "No test was defined in the configuration page.";
		} else {
			//verify if the test still exists on BlazeMeter server
			HashMap<String, String> tests = bzmServiceManager.getTests();
			if (tests != null){
				if (!tests.values().contains(testId)) {
					return "Test removed from BlazeMeter server.";
				}
			}
		}
		String testDrt = params.get(Constants.SETTINGS_TEST_DURATION);
		if (isNullorEmpty(testDrt)) {
			return "Test duration not set.";
		} else {
			try{
				testDuration = Integer.valueOf(testDrt);
			} catch (NumberFormatException nfe){
				return "Test duration not a numbers.";
			}
		}

		String errorUnstable = params.get(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		String errorFail = params.get(Constants.SETTINGS_ERROR_THRESHOLD_FAIL);
		String timeUnstable = params.get(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		String timeFail = params.get(Constants.SETTINGS_RESPONSE_TIME_FAIL);

		errorFailedThreshold = Integer.valueOf(errorFail);
		errorUnstableThreshold = Integer.valueOf(errorUnstable);
		responseTimeFailedThreshold = Integer.valueOf(timeFail);
		responseTimeUnstableThreshold = Integer.valueOf(timeUnstable);
		
		dataFolder = params.get(Constants.SETTINGS_DATA_FOLDER);
		if (PropertiesUtil.isEmptyOrNull(dataFolder)){
			dataFolder = "";
		}
		
		dataFolder = dataFolder.trim();
		mainJMX = params.get(Constants.SETTINGS_MAIN_JMX);
		
		logger.warning("File separator should be " + File.separator);

		if (isNullorEmpty(mainJMX)) {
			needTestUpload = false;
		} else {
			String agentCheckoutDir = agentRunningBuild.getCheckoutDirectory().getAbsolutePath();
			if (!((agentCheckoutDir.endsWith("/") || agentCheckoutDir.endsWith("\\")))){//if the path doesn't have folder separator
				agentCheckoutDir += File.separator;//make sure that the path ends with '/'
			}
									
	        if (!isFullPath(dataFolder)){//full path
	        	dataFolder = agentCheckoutDir + dataFolder;
	        } 
	        
	        File folder = new File(dataFolder);

	        if (!folder.exists() || !folder.isDirectory()){
	            return dataFolder + " could not be found on local file system, please check that the folder exists.";
	        }
			needTestUpload = true;
		}
		
		return null;
	}

	private boolean isFullPath(String path){
		if (path.startsWith("/")){
			return true;
		}
		
		if (path.length() < 3) {
			return false;
		}
		if (path.substring(0,1).matches("[a-zA-Z]")){//like D:/
			if (path.substring(1, 2).equals(":") && (path.substring(2, 3).equals("/") || path.substring(2, 3).equals("\\"))){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the value is null or empty
	 * @param value
	 * @return true if the value is null or empty, false otherwise
	 */
	private boolean isNullorEmpty(String value){
		if ((value == null) || ("".equals(value))){
			return true;
		}
		return false;
	}
	
	@Override
	public void interrupt() {
		logger.message("BlazeMeter agent interrupted.");
        bzmServiceManager.stopTest(testId, logger);
		interrupted = true;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public boolean isInterrupted() {
		return interrupted;
	}

	@Override
	public void start() throws RunBuildException {
		logger.message("BlazeMeter agent started.");
		
		logger.activityStarted("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
		Map<String, String> runnerParams = buildRunnerContext.getRunnerParameters();
		validationError = validateParams(runnerParams);
		if (validationError != null){
			logger.error(validationError);
		} else {
			logger.message("Validation passed.");
		}
		logger.activityFinished("Parameter validation", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
		
		if (needTestUpload){
			uploadDataFolderFiles();
		}
		
		if (validationError != null ){
			throw new RunBuildException(validationError);
		}
	}

	@SuppressWarnings("static-access")
	@Override
	public BuildFinishedStatus waitFor() throws RunBuildException {
		logger.message("Attempting to start test with id:"+testId);
		boolean started = bzmServiceManager.startTest(testId, logger);
		
		if (!started){
			return BuildFinishedStatus.FINISHED_FAILED;
		} else {
			try {
				//TODO nu cred ca e nevoie de file.separator
				File file = new File(agentRunningBuild.getAgentTempDirectory().getAbsolutePath()+File.separator+"blaze_session.id");
				if (!file.exists()){
					file.createNewFile();
				}
				FileWriter fw;
				fw = new FileWriter(file);
				fw.write(bzmServiceManager.getSession());
				fw.flush();
				fw.close();
				artifactsWatcher.addNewArtifactsPath(file.getAbsolutePath());
			} catch (IOException e) {
				logger.error("Failed to save test session id! Error is:"+e.getMessage());
				return BuildFinishedStatus.FINISHED_FAILED;
			}
			
			logger.message("Test started. Waiting " + testDuration + " minutes to finish!");
		}
		
		long totalWaitTime = (testDuration+2) * 60 * 1000;//the duration is in minutes so we multiply to get the value in ms
		long nrOfCheckInterval = totalWaitTime / CHECK_INTERVAL;//
		long currentCheck = 0;
		
		logger.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
		TestInfo testInfo;
		while (currentCheck++ < nrOfCheckInterval){
			try {
				Thread.currentThread().sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
			}
			
			logger.message("Check if the test is still running. Time passed since start:"+((currentCheck*CHECK_INTERVAL)/1000/60) + " minutes.");
			testInfo = bzmServiceManager.getTestStatus(testId);
			if (testInfo.getStatus().equals(Constants.TestStatus.NotRunning)){
				logger.warning("Test is finished earlier then estimated! Time passed since start:"+((currentCheck*CHECK_INTERVAL)/1000/60) + " minutes.");
                bzmServiceManager.waitForReport(logger);
                return BuildFinishedStatus.INTERRUPTED;
            }
			else
			if (testInfo.getStatus().equals(Constants.TestStatus.NotFound)){
				logger.error("Test not found!");
				return BuildFinishedStatus.FINISHED_FAILED;
			}
		}
		
		//BlazeMeter test stopped due to user test duration setup reached
		logger.message("Stopping test...");
		bzmServiceManager.stopTest(testId, logger);
		
		logger.activityFinished("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
		
		logger.message("Test finished. Checking for test report...");
		
        //get testGetArchive information
        boolean waitForReport = bzmServiceManager.waitForReport(logger);
        
        if (waitForReport){
        	int reportStatus = bzmServiceManager.getReport(errorFailedThreshold, errorUnstableThreshold, responseTimeFailedThreshold, responseTimeUnstableThreshold, logger);

        	if (reportStatus != -1){
            	bzmServiceManager.publishReportArtifact(agentRunningBuild.getAgentTempDirectory().getAbsolutePath() + File.separator + "teamcity-info.xml");
           		artifactsWatcher.addNewArtifactsPath(agentRunningBuild.getAgentTempDirectory().getAbsolutePath() + File.separator + "teamcity-info.xml");
        	}
        	
        	switch (reportStatus) {
        		case -1:
        			return BuildFinishedStatus.FINISHED_FAILED;
        		case 0:
        			return BuildFinishedStatus.FINISHED_SUCCESS;
        		case 1:
        			return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        		default: 
        			return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        	}
        } else {
        	return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
	}
	
	/**
	 * Upload main JMX file and all the files from the data folder
	 */
    private void uploadDataFolderFiles() {
    	logger.activityStarted("Uploading data files", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        if (dataFolder == null || dataFolder.isEmpty()){
        	logger.error("Empty data folder. Please enter the path to your data folder or '.' for main folder where the files are checked out.");
        	logger.activityFinished("Uploading data files", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
            return;
        }
        
        File folder = new File(dataFolder);
        if (!folder.exists() || !folder.isDirectory()){
            logger.error("dataFolder " + dataFolder + " could not be found on local file system, please check that the folder exists.");
            logger.activityFinished("Uploading data files", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
            return ;
        } else {
        	logger.message("DataFolder "+dataFolder+" exists.");
        }

        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            String file;
            if (listOfFiles[i].isFile()) {
                file = listOfFiles[i].getName();
                if (file.endsWith(mainJMX)){
                	logger.message("Uploading main JMX "+mainJMX);
                    bzmServiceManager.uploadJMX(testId, mainJMX, dataFolder + File.separator + mainJMX);
                }
                else {
                	logger.message("Uploading data files "+file);
                	bzmServiceManager.uploadFile(testId, dataFolder, file, logger);
                }
            }
        }
        logger.activityFinished("Uploading data files", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
    }
	
}