package com.blaze.agent;

import java.io.File;
import java.util.Map;

import com.blaze.BzmServiceManager;
import com.blaze.testresult.TestResult;
import com.blaze.utils.Utils;
import com.google.common.collect.LinkedHashMultimap;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
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
public class BzmBuildProcess implements BuildProcess{
	private static final int CHECK_INTERVAL = 60000;
	private static final int INIT_TEST_TIMEOUT = 900000;
    private static final String TC_AGENT_WORK_DIR="teamcity.agent.work.dir";
	private BzmServiceManager bzmServiceManager;
	private AgentRunningBuild agentRunningBuild;
	private BuildRunnerContext buildRunnerContext;
	private ArtifactsWatcher artifactsWatcher;
	
	private String validationError;
	private String testId;
    private String jsonConfiguration;
	private String testDuration;
	private String errorUnstableThreshold;
    private String errorFailedThreshold;
    private String responseTimeUnstableThreshold;
    private String responseTimeFailedThreshold;
	private String dataFolder;
	private String mainJMX;
	
	final BuildProgressLogger logger;
	boolean finished;
	boolean needTestUpload;
	boolean interrupted;
	
	
	public BzmBuildProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild, BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher) {
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
        bzmServiceManager = BzmServiceManager.getBzmServiceManager(buildSharedMap,logger);
	}

	private String validateParams(Map<String, String> params) {

		testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
		if (StringUtil.isEmptyOrSpaces(testId)) {
            logger.warning("No test was defined in the configuration page.");
			return "No test was defined in the configuration page.";
		} else {
			//verify if the test still exists on BlazeMeter server
            LinkedHashMultimap<String, String> tests = bzmServiceManager.getTests();
			if (tests != null){
				if (!testId.equals(Constants.NEW_TEST)&&!tests.values().contains(testId)) {
                    logger.warning("Test was not found at BlazeMeter server "+bzmServiceManager.getBlazeMeterUrl());
					return "Test was not found at BlazeMeter server "+bzmServiceManager.getBlazeMeterUrl();
				}
			}
		}

        String jsonConf = params.get(Constants.JSON_CONFIGURATION);
        if(jsonConf!=null&&!jsonConf.isEmpty()){
            File jsonF = new File(agentRunningBuild.getCheckoutDirectory() + "/" + jsonConf);
            logger.message("Trying to find JSON configuration in build.checkout.directory=" + agentRunningBuild.getCheckoutDirectory());
            if (jsonF.exists()) {
                jsonConfiguration = jsonF.getAbsolutePath();
                logger.message("File with JSON configuration was found. Actual path=" + jsonF.getAbsolutePath());
            } else {
                String agentWorkDir = agentRunningBuild.getSharedConfigParameters().get(TC_AGENT_WORK_DIR);
                logger.message("Trying to find JSON configuration in teamcity.agent.work.dir=" + agentRunningBuild.getCheckoutDirectory());
                jsonF = new File(agentWorkDir + "/" + jsonConf);
                if (jsonF.exists()) {
                    jsonConfiguration = jsonF.getAbsolutePath();
                    logger.message("File with JSON configuration was found. Actual path=" + jsonF.getAbsolutePath());
                } else {
                    logger.warning("File with JSON configuration was not found.");
                    return "File with JSON configuration was not found.";

                }

            }
        }

        testDuration = params.get(Constants.SETTINGS_TEST_DURATION);
        errorUnstableThreshold = params.get(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
        errorFailedThreshold = params.get(Constants.SETTINGS_ERROR_THRESHOLD_FAIL);
        responseTimeUnstableThreshold = params.get(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE);
        responseTimeFailedThreshold = params.get(Constants.SETTINGS_RESPONSE_TIME_FAIL);

		dataFolder = params.get(Constants.SETTINGS_DATA_FOLDER);
		if (PropertiesUtil.isEmptyOrNull(dataFolder)){
			dataFolder = "";
		}
		
		dataFolder = dataFolder.trim();
		mainJMX = params.get(Constants.SETTINGS_MAIN_JMX);
		
		logger.warning("File separator should be " + File.separator);

		if (StringUtil.isEmptyOrSpaces(mainJMX)) {
			needTestUpload = false;
		} else {
			String agentCheckoutDir = agentRunningBuild.getCheckoutDirectory().getAbsolutePath();
			if (!((agentCheckoutDir.endsWith("/") || agentCheckoutDir.endsWith("\\")))){//if the path doesn't have folder separator
				agentCheckoutDir += File.separator;//make sure that the path ends with '/'
			}
									
	        if (!FileUtil.isAbsolute(dataFolder)){//full path
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
            try{
                testId=bzmServiceManager.prepareTest(testId,jsonConfiguration,testDuration);
            }catch (Exception e){
                logger.warning("Failed to prepare/create test with JSON Configuration from "+jsonConfiguration);
            }

        logger.message("Attempting to start test with id:"+testId);
        String session = bzmServiceManager.startTest(testId, 5, logger);
		BuildFinishedStatus result=null;
		if (session.isEmpty()){
			return BuildFinishedStatus.FINISHED_FAILED;
		} else {
			logger.message("Test initialization is started... Waiting for DATA_RECEIVED status");
			String reportUrl=bzmServiceManager.getReportUrl(session);
            logger.message("Test report will be available at "+reportUrl);
		}
		

		logger.activityStarted("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
		TestInfo testInfo;
        String apiVersion=bzmServiceManager.getBlazeMeterApiVersion();
        long testInitStart=System.currentTimeMillis();
		boolean initTimeOutPassed=false;
        do{
            Utils.sleep(CHECK_INTERVAL, logger);
            testInfo = bzmServiceManager.getTestStatus(apiVersion.equals(Constants.V2)?testId:bzmServiceManager.getSession());
            logger.message("Check if the test is initialized...");
        initTimeOutPassed=System.currentTimeMillis()>testInitStart+INIT_TEST_TIMEOUT;
        }while (!(testInfo.getStatus().equals(Constants.TestStatus.Running)|initTimeOutPassed));
        if(initTimeOutPassed&!testInfo.getStatus().equals(Constants.TestStatus.Running)){
            logger.warning("Failed to initialize test "+testId);
            logger.warning("Build will be aborted");
            return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
        long testRunStart=System.currentTimeMillis();

        do{
            Utils.sleep(CHECK_INTERVAL, logger);
			logger.message("Check if the test is still running. Time passed since start: "+((System.currentTimeMillis()-testRunStart) / 1000 / 60) + " minutes.");
            testInfo = bzmServiceManager.getTestStatus(apiVersion.equals(Constants.V2)?testId:bzmServiceManager.getSession());
			logger.message("TestInfo="+testInfo.toString());
        }while (!testInfo.getStatus().equals(Constants.TestStatus.NotRunning));

        logger.message("Test finished. Checking for test report...");
        logger.message("Actual test duration was: " + ((System.currentTimeMillis()-testRunStart) / 1000 / 60) + " minutes.");
        logger.activityFinished("Check", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        Utils.sleep(180000,logger);
        TestResult testResult = bzmServiceManager.getReport(logger);

        if(testResult==null){
            logger.warning("Failed to get report from server...");
        }else{
            logger.message("Test report is received...");
            logger.message(testResult.toString());
        }
        bzmServiceManager.retrieveJUNITXML(session,buildRunnerContext);
        bzmServiceManager.retrieveJTL(session,buildRunnerContext);
        BuildFinishedStatus serverTrRes = bzmServiceManager.validateServerTresholds();
        BuildFinishedStatus localTrRes = Utils.validateLocalTresholds(testResult, errorUnstableThreshold,
                errorFailedThreshold,
                responseTimeUnstableThreshold,
                responseTimeFailedThreshold, logger);

        result=localTrRes==null?serverTrRes:localTrRes;
        return result;
    }
       
	
	/**
	 * Upload main JMX file and all the files from the data folder
	 */
    private void uploadDataFolderFiles() {
    	logger.activityStarted("Uploading data files", DefaultMessagesInfo.BLOCK_TYPE_BUILD_STEP);
        if (StringUtil.isEmptyOrSpaces(dataFolder)){
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