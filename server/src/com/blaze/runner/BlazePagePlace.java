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
