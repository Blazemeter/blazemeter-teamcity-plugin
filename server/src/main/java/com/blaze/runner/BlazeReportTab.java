package com.blaze.runner;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Present BlazeMeter report tab in Build results
 */
public class BlazeReportTab extends ViewLogTab {

    /**
     * Creates and registers tab for Build Results pages
     *
     * @param pagePlaces used to register the tab
     * @param server     server object
     */
    public BlazeReportTab(@NotNull PagePlaces pagePlaces, @NotNull SBuildServer server) {
        super("BlazeMeter Report", "bzm", pagePlaces, server);
//        setIncludeUrl(pluginDescriptor.getPluginResourcesPath("reportTab.jsp"));
    }

    @Override
    protected void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request, @NotNull SBuild build) {
        System.out.println();
        model.put("url", "http://blazedemo.com");
    }

    @Override
    protected boolean isAvailable(@NotNull HttpServletRequest request, @NotNull SBuild build) {
        return true;
//        return super.isAvailable(request, build);
    }
}
