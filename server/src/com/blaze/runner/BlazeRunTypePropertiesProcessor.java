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
		final String errThUnstable = (String) properties.get(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		final String errThFail = (String) properties.get(Constants.SETTINGS_ERROR_THRESHOLD_FAIL);
		final String errRespTimeUnstable = (String) properties.get(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		final String errRespTimeFail = (String) properties.get(Constants.SETTINGS_RESPONSE_TIME_FAIL);
		final String testDuration = (String) properties.get(Constants.SETTINGS_TEST_DURATION);
		String dataFolder = (String) properties.get(Constants.SETTINGS_DATA_FOLDER);
		final String mainJMX = (String) properties.get(Constants.SETTINGS_MAIN_JMX);

		if (PropertiesUtil.isEmptyOrNull(errThUnstable)) {
			result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
					"Error threshold unstable empty."));
		} else {
			try {
				Integer.parseInt(errThUnstable);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
						"Error threshold unstable is not a number."));

			}
		}

		if (PropertiesUtil.isEmptyOrNull(errThFail)) {
			result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_FAIL, "Error threshold failure empty."));
		} else {
			try {
				Integer.parseInt(errThFail);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_FAIL,
						"Error threshold failure is not a number."));

			}
		}

		if (PropertiesUtil.isEmptyOrNull(errRespTimeUnstable)) {
			result.add(new InvalidProperty(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE,
					"Error response time unstable empty."));
		} else {
			try {
				Integer.parseInt(errRespTimeUnstable);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE,
						"Error response time unstable is not a number."));

			}
		}

		if (PropertiesUtil.isEmptyOrNull(errRespTimeFail)) {
			result.add(new InvalidProperty(Constants.SETTINGS_RESPONSE_TIME_FAIL, "Error response time failure empty."));
		} else {
			try {
				Integer.parseInt(errRespTimeFail);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_RESPONSE_TIME_FAIL,
						"Error response time failure is not a number."));

			}
		}

		if (PropertiesUtil.isEmptyOrNull(test)) {
			result.add(new InvalidProperty(Constants.SETTINGS_ALL_TESTS_ID, "A test must be selected."));
		}

		if (PropertiesUtil.isEmptyOrNull(testDuration)) {
			result.add(new InvalidProperty(Constants.SETTINGS_TEST_DURATION, "A test duration time must be selected."));
		}

		if (!PropertiesUtil.isEmptyOrNull(dataFolder)) {
			dataFolder = dataFolder.trim();
			if (dataFolder.startsWith("\\")) {
				result.add(new InvalidProperty(Constants.SETTINGS_DATA_FOLDER,
						"Data folder path cannot start with '\\' character."));
			}
			if (dataFolder.startsWith(".") || dataFolder.startsWith("..")) {
				result.add(new InvalidProperty(Constants.SETTINGS_DATA_FOLDER,
						"Data folder path cannot start with '.' or '..' characters."));
			}
		}

		return result;
	}

}
