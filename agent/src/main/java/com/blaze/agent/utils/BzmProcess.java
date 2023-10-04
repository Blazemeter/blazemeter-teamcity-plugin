/**
 * Copyright 2018 BlazeMeter Inc.
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

package com.blaze.agent.utils;

import com.blaze.runner.Constants;
import com.blaze.utils.Utils;
import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.BuildResult;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class BzmProcess implements Callable<BuildFinishedStatus> {
    private final BuildAgent agent;
    private final AgentRunningBuild agentRunningBuild;

    private final BlazeMeterUtils utils;
    private final CiBuild build;
    private final BuildProgressLogger logger;
    private ArtifactsWatcher artifactsWatcher;
    private BuildRunnerContext buildRunnerContext;

    private Master master;
    private String SLACK ="slack";
    private String TEAMS ="teams";
    public BzmProcess(BuildAgent buildAgent, AgentRunningBuild agentRunningBuild,
                      BuildRunnerContext buildRunnerContext, ArtifactsWatcher artifactsWatcher, BlazeMeterUtils utils,BuildRunnerContext context) {
        this.agent = buildAgent;
        this.agentRunningBuild = agentRunningBuild;

        this.logger = agentRunningBuild.getBuildLogger();
        this.utils = utils;
        this.build = createCiBuild(buildRunnerContext.getRunnerParameters());
        this.artifactsWatcher = artifactsWatcher;
        this.buildRunnerContext = context;
    }


    @Override
    public BuildFinishedStatus call() {
        try {
            master = build.start();
            if (master != null) {
                publishArtifacts(build);
                build.waitForFinish(master);
                String selectedNotificationType = buildRunnerContext.getRunnerParameters().get(Constants.SETTING_NOTIFICATION_TYPE);
                String webHookURL= buildRunnerContext.getRunnerParameters().get(Constants.SETING_WEBHOOK_URL);
                 if(SLACK.equals(selectedNotificationType)){
                     if (isNotNullOrEmpty(webHookURL)){
                         sendWebhookNotificationSlack(master.getId(), build.getCurrentTest().getName(), build.getPublicReport(), webHookURL);
                     }
                 } else if (TEAMS.equals(selectedNotificationType)) {
                     if (isNotNullOrEmpty(webHookURL)){
                         sendWebhookNotificationTeams(master.getId(), build.getCurrentTest().getName(), build.getPublicReport(), webHookURL);
                     }
                 }


            } else {
                utils.getLogger().error("Failed to start build ");
                logger.error("Failed to start test");
                return BuildFinishedStatus.FINISHED_FAILED;
            }
        } catch (InterruptedException e) {
            utils.getLogger().warn("Wait for finish has been interrupted", e);
            interrupt(build, master);
            return BuildFinishedStatus.INTERRUPTED;
        } catch (Exception e) {
            if (master == null) {
                utils.getLogger().warn("Failed to start BlazeMeter test", e);
                logger.error("Failed to start BlazeMeter test: " + e.getMessage());
            } else {
                utils.getLogger().warn("Caught exception while waiting for build", e);
                logger.error("Caught exception: " + e.getMessage());
            }
            return BuildFinishedStatus.FINISHED_FAILED;
        }

        BuildResult buildResult = build.doPostProcess(master);
        return mappedBuildResult(buildResult);
    }

    public void interrupt(CiBuild build, Master master) {
        if (build != null && master != null) {
            try {
                boolean hasReport = build.interrupt(master);
                if (hasReport) {
                    logger.message("Get reports after interrupt");
                    build.doPostProcess(master);
                }
            } catch (IOException e) {
                logger.message("Failed to interrupt build " + e.getMessage());
                utils.getLogger().warn("Failed to interrupt build ", e);
            }
        }
    }

    private void publishArtifacts(CiBuild build) {
        File buildDirectory = new File(agentRunningBuild.getBuildTempDirectory() + "/" + agentRunningBuild.getProjectName() + "/" + agentRunningBuild.getBuildTypeName() + "/" + agentRunningBuild.getBuildNumber() + "/BlazeMeter");
        File file = new File(buildDirectory, Constants.BZM_REPORTS_FILE);
        try {
            FileUtils.touch(file);
            appendStringToFile(file, "BlazeMeter report: " + build.getCurrentTest().getName() + "\r\n");
            appendStringToFile(file, build.getPublicReport() + "\r\n");
        } catch (IOException e) {
            logger.warning("Failed to generate BlazeMeter report: " + e.getMessage());
            if (utils.getLogger() != null) {
                utils.getLogger().error("Failed to generate BlazeMeter report", e);
            }
            return;
        }
        artifactsWatcher.addNewArtifactsPath(file + "=>" + Constants.RUNNER_DISPLAY_NAME);
    }

    private void appendStringToFile(File file, String content) throws IOException {
        Files.write(Paths.get(file.toURI()), content.getBytes(), StandardOpenOption.APPEND);
    }


    private CiBuild createCiBuild(Map<String, String> params) {
        String testId = params.get(Constants.SETTINGS_ALL_TESTS_ID);
        String properties = params.get(Constants.SETTINGS_JMETER_PROPERTIES);
        String notes = params.get(Constants.SETTINGS_NOTES);

        CiBuild build = new CiBuild(utils, Utils.getTestId(testId), properties, notes, createCiPostProcess(params));
        build.setWorkspaceId(params.get(Constants.SETTINGS_ALL_WORKSPACES));
        build.setReportName(params.get(Constants.SETTINGS_REPORT_NAME));
        return build;
    }

    private CiPostProcess createCiPostProcess(Map<String, String> params) {
        boolean isDownloadJtl = Boolean.valueOf(params.get(Constants.SETTINGS_JTL));
        boolean isDownloadJunit = Boolean.valueOf(params.get(Constants.SETTINGS_JUNIT));
        String junitPath = params.get(Constants.SETTINGS_JUNIT_PATH);
        String jtlPath = params.get(Constants.SETTINGS_JTL_PATH);

        return new CiPostProcess(isDownloadJtl, isDownloadJunit, jtlPath, junitPath, getDefaultReportDir(), utils);
    }

    private String getDefaultReportDir() {
        return this.agent.getConfiguration().getAgentLogsDirectory().getAbsolutePath() + File.separator
                + agentRunningBuild.getProjectName() + File.separator
                + agentRunningBuild.getBuildNumber();
    }

    private BuildFinishedStatus mappedBuildResult(BuildResult buildResult) {
        switch (buildResult) {
            case SUCCESS:
                return BuildFinishedStatus.FINISHED_SUCCESS;
            case ABORTED:
                return BuildFinishedStatus.INTERRUPTED;
            case ERROR:
                return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
            case FAILED:
                return BuildFinishedStatus.FINISHED_FAILED;
            default:
                return BuildFinishedStatus.FINISHED_WITH_PROBLEMS;
        }
    }
    private boolean isNotNullOrEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    private String getColor(BuildResult buildResult) {
        return (buildResult == BuildResult.FAILED) ? "#FF0000" : "#008000";
    }
    public void sendWebhookNotificationTeams(String masterId, String testName, String publicReportUrl, String webhookUrl){
        BuildResult buildResult = build.doPostProcess(master);
        String color = getColor(buildResult);
        String json = "{\n"
                + "    \"@type\": \"MessageCard\",\n"
                + "    \"@context\": \"http://schema.org/extensions\",\n"
                + "    \"themeColor\": \"" + color + "\",\n"
                + "    \"summary\": \"Blazemeter Test Execution\",\n"
                + "    \"sections\": [\n"
                + "        {\n"
                + "            \"activityTitle\": \"Blazemeter Test Execution\",\n"
                + "            \"facts\": [\n"
                + "                {\n"
                + "                    \"name\": \"Test Name : \",\n"
                + "                    \"value\": \"" + testName + "\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"name\": \"Master ID : \",\n"
                + "                    \"value\": \"" + masterId + "\"\n"
                + "                },\n"
                + "                {\n"
                + "                    \"name\": \"Test Status : \",\n"
                + "                    \"value\": \"" + buildResult + "\"\n"
                + "                }\n"
                + "            ],\n"
                + "            \"markdown\": true\n"
                + "        }\n"
                + "    ],\n"
                + "    \"potentialAction\": [\n"
                + "        {\n"
                + "            \"@type\": \"OpenUri\",\n"
                + "            \"name\": \"View Report\",\n"
                + "            \"targets\": [\n"
                + "                {\n"
                + "                    \"os\": \"default\",\n"
                + "                    \"uri\": \"" + publicReportUrl + "\"\n"
                + "                }\n"
                + "            ]\n"
                + "        }\n"
                + "    ]\n"
                + "}\n";
        sendWebhookNotification(webhookUrl, json);
    }
    public void sendWebhookNotificationSlack(String masterId,String testName,String publicReportUrl,String webhookUrl){
        BuildResult buildResult = build.doPostProcess(master);
        String color = getColor(buildResult);
        String jsonPayload = String.format(
                "{" +
                        "\"attachments\":[" +
                        "{" +
                        "\"title\":\"Blazemeter Test Execution\"," +
                        "\"pretext\":\"Blazemeter Test Execution\"," +
                        "\"fallback\":\"Blazemeter Test Execution\"," +
                        "\"color\":\"%s\"," +
                        "\"fields\":[" +
                        "{" +
                        "\"title\":\"Test Name\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"Master ID\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"Test Status\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}," +
                        "{" +
                        "\"title\":\"View Report\"," +
                        "\"value\":\"%s\"," +
                        "\"short\":true" +
                        "}" +
                        "]" +
                        "}" +
                        "]" +
                        "}",
                color, testName, masterId, buildResult, publicReportUrl);
        sendWebhookNotification(webhookUrl, jsonPayload);

    }
    private void sendWebhookNotification(String webhookUrl, String jsonPayload) {
        try{
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            byte[] postData = jsonPayload.getBytes(StandardCharsets.UTF_8);

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(postData);


            int responseCode = connection.getResponseCode();
            logger.message("Response Code: " + responseCode);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}