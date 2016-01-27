package com.blaze.runner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.util.PropertiesUtil;

public class BlazeRunTypePropertiesProcessor implements PropertiesProcessor {

	@Override
	public Collection<InvalidProperty> process(Map<String, String> properties) {
		List<InvalidProperty> result = new Vector<InvalidProperty>();

		final String test = (String) properties.get(Constants.SETTINGS_ALL_TESTS_ID);

		if (PropertiesUtil.isEmptyOrNull(test)) {
			result.add(new InvalidProperty(Constants.SETTINGS_ALL_TESTS_ID, "A test must be selected."));
		}

		return result;
	}

}
