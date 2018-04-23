package com.blaze.runner;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import jetbrains.buildServer.web.reportTabs.ReportTabUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Present BlazeMeter report tab in Build results
 */
public class BlazeReportTab extends ViewLogTab {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    /**
     * Creates and registers tab for Build Results pages
     *
     * @param pagePlaces used to register the tab
     * @param server     server object
     */
    public BlazeReportTab(@NotNull PagePlaces pagePlaces, @NotNull SBuildServer server, @NotNull final PluginDescriptor pluginDescriptor) {
        super("BlazeMeter Report", "bzm", pagePlaces, server);
        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("reportTab.jsp"));
    }

    @Override
    protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
        BuildArtifact artifact = ReportTabUtil.getArtifact(build, Constants.RUNNER_DISPLAY_NAME + "/" + Constants.BZM_REPORTS_FILE);
        if(artifact != null) {
            try {
                final Map<String, String> links = getReports(artifact.getInputStream());
                if (!links.isEmpty()) {
                    model.put("bzmReports", links);
                } else {
                    model.put("bzmMsg", "There is no report for this build");
                }

            } catch (IOException e) {
                logger.error("Failed to get the report: ", e);
                model.put("bzmMsg", "Failed to get the report: " + e.getMessage());
            }
        } else {
            logger.info("No BlazeMeter artifacts for this build");
            model.put("bzmMsg", "No BlazeMeter artifacts for this build");
        }
    }

    private Map<String, String> getReports(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        final Map<String, String> links = new HashMap<>();

        String line;
        while ((line = reader.readLine()) != null) {
            String key = line;
            logger.debug("Get key: " + key);
            if ((line = reader.readLine()) != null) {
                logger.debug("Get address: " + line);
                links.put(key, line);
            } else {
                logger.warn("Key " + key + " has not url");
                links.put(key, "");
            }
        }
        return links;
    }

    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        return super.isAvailable(request, build) && ReportTabUtil.isAvailable(build, Constants.RUNNER_DISPLAY_NAME + "/" + Constants.BZM_REPORTS_FILE);
    }
}
