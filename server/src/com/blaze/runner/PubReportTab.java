package com.blaze.runner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.ViewLogTab;

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
		
		BuildArtifacts arts = sbuild.getArtifacts(BuildArtifactsViewMode.VIEW_ALL);
		BuildArtifact ba = arts.getArtifact("blaze_session.id")	;
		if (ba!=null) {
			//Founded artifact BlazeMeter session.
			try {
				InputStream fis = ba.getInputStream();
				byte[] buffer = new byte[64];
				int readLen = 0;
				
				while ((readLen = fis.read(buffer, 0, 64)) != -1){
					session_id += new String(buffer, 0, readLen);
				}
				System.out.println("session_id:"+session_id);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Artifact BlazeMeter session not Founded!");
		}
		String reportUrl=sbuild.getAgent().getAvailableParameters().get("env."+Constants.REPORT_URL+sbuild.getBuildNumber());

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
