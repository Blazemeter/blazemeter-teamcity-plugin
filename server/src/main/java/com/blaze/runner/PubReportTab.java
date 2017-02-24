/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.blaze.runner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

public class PubReportTab extends ViewLogTab {
	private static String TAB_TITLE = "BlazeMeter Report Tab";
	private static String TAB_CODE = "BlazeMeterReportTab";

	private String session_id = "";

	SBuildServer server;

	public PubReportTab(final PagePlaces pagePlaces,
						final SBuildServer server) {
		super(TAB_TITLE, TAB_CODE, pagePlaces, server);
		this.server = server;
	}


	@Override
	protected void fillModel(@NotNull Map<String, Object> model,
			@NotNull HttpServletRequest request, @NotNull SBuild response) {
		SBuild sbuild = this.getBuild(request);

		session_id = "";
        String lf=sbuild.getAgent().getAvailableParameters().get("system.agent.home.dir")+"/logs";
		String pn=sbuild.getBuildOwnParameters().get("system.teamcity.projectName");
		String bn=sbuild.getBuildOwnParameters().get("build.number");
		File ruf=new File(lf+"/"+pn+"/"+bn+"/"+Constants.REPORT_URL_F);
		String reportUrl = null;
		try {
			reportUrl = FileUtils.readFileToString(ruf);
		} catch (IOException ioe) {
			System.out.println("Failed to read reportUrl from " + ruf.getAbsolutePath());
		}
		model.put("session_id", session_id);
		request.setAttribute("reportUrl", reportUrl);
	}

	@Override
	protected boolean isAvailable(@NotNull HttpServletRequest request,
			@NotNull SBuild build) {
		SBuild sbuild = this.getBuild(request);
        boolean hasBZMstep=hasBZMstep(sbuild);
		return super.isAvailable(request, build) && hasBZMstep;
	}

    private boolean hasBZMstep(@NotNull SBuild sbuild){
        Iterator<SBuildRunnerDescriptor> descriptorIterator = sbuild.getBuildType().getBuildRunners().iterator();
        boolean hasBZMstep=false;
        while(descriptorIterator.hasNext()){
            SBuildRunnerDescriptor descriptor=descriptorIterator.next();
            if(descriptor.getType().equals("BlazeMeter")){
                hasBZMstep=true;
            }
        }
        return hasBZMstep;
    }


}
