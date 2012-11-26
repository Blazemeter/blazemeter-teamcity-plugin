package com.blaze.runner;

import jetbrains.buildServer.web.openapi.PagePlace;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;

public class BlazePagePlace implements PagePlaces{
	WebControllerManager wcm;
	
	public BlazePagePlace(@NotNull final WebControllerManager wcm) {
		this.wcm = wcm;
	}
	
	@Override
	public PagePlace getPlaceById(PlaceId placeId) {
		return wcm.getPlaceById(PlaceId.ADMIN_SERVER_CONFIGURATION);
	}

}
