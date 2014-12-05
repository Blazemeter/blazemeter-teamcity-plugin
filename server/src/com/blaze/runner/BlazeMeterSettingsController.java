package com.blaze.runner;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that helps to load the user key in the edit runner settings page.
 * @author Marcel Milea
 * Controller for administration page
 */
public class BlazeMeterSettingsController extends BaseController {
    private final WebControllerManager myWebManager;
    private final PluginDescriptor myPluginDescriptor;
    private final BlazeMeterServerSettings mySettings;
    
	public BlazeMeterSettingsController(final SBuildServer server,
                                     final WebControllerManager webManager,
                                     final PluginDescriptor pluginDescriptor,
                                     @NotNull final BlazeMeterServerSettings pluginSettings){
		super(server);
		this.myWebManager = webManager;
	    this.myPluginDescriptor = pluginDescriptor;
		this.mySettings = pluginSettings;
	}
	
	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.getSession().setAttribute("userKey", mySettings.getUserKey());
        request.getSession().setAttribute("blazeMeterUrl", mySettings.getBlazeMeterUrl());
        request.getSession().setAttribute("serverName", mySettings.getServerName());
        request.getSession().setAttribute("serverPort", mySettings.getServerPort());
        request.getSession().setAttribute("username", mySettings.getUsername());
        request.getSession().setAttribute("password", mySettings.getPassword());
        Map<String,Object> params = new HashMap<String,Object>();
        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editSettings.jsp"), params);
	}

	/**
	 * Register controller
	 */
	public void register(){
        myWebManager.registerController("/blazeRunnerController.html", this);
      }
}
