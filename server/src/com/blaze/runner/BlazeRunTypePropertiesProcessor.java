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
        final String jsonConfig = (String) properties.get(Constants.JSON_CONFIGURATION);
		final String errThUnstable = (String) properties.get(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE);
		final String errThFail = (String) properties.get(Constants.SETTINGS_ERROR_THRESHOLD_FAIL);
		final String errRespTimeUnstable = (String) properties.get(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE);
		final String errRespTimeFail = (String) properties.get(Constants.SETTINGS_RESPONSE_TIME_FAIL);
		String dataFolder = (String) properties.get(Constants.SETTINGS_DATA_FOLDER);
		final String mainJMX = (String) properties.get(Constants.SETTINGS_MAIN_JMX);

        if (!PropertiesUtil.isEmptyOrNull(errThUnstable)) {
            try {
                Integer.parseInt(errThUnstable);
            } catch (NumberFormatException nfe) {
                result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
                        "Error threshold unstable is not a number."));

            }
        }

            if (!PropertiesUtil.isEmptyOrNull(errThFail)) {
                try {
				Integer.parseInt(errThFail);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_FAIL,
						"Error threshold failure is not a number."));

			}
		}

        if (!PropertiesUtil.isEmptyOrNull(errThUnstable)) {
            try {
                Integer.parseInt(errThUnstable);
            } catch (NumberFormatException nfe) {
                result.add(new InvalidProperty(Constants.SETTINGS_ERROR_THRESHOLD_UNSTABLE,
                        "Error threshold unstable is not a number."));

            }
        }


		if (!PropertiesUtil.isEmptyOrNull(errRespTimeUnstable)) {
			try {
				Integer.parseInt(errRespTimeUnstable);
			} catch (NumberFormatException nfe) {
				result.add(new InvalidProperty(Constants.SETTINGS_RESPONSE_TIME_UNSTABLE,
						"Error response time unstable is not a number."));

			}
		}

		if (!PropertiesUtil.isEmptyOrNull(errRespTimeFail)) {
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
