package com.blaze.utils;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;

public class TCBlazeMeterUtils extends BlazeMeterUtils {

    private static final String APP_KEY = "app_key=jnk100x987c06f4e10c4";

    private static final String CLIENT_IDENTIFICATION = "&_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.version();

    public static String TEAM_CITY_PLUGIN_INFO = APP_KEY + CLIENT_IDENTIFICATION;

    public TCBlazeMeterUtils(String apiKeyId, String apiKeySecret, String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, dataAddress, notifier, logger);
    }

    public TCBlazeMeterUtils(String address, String dataAddress, UserNotifier notifier, Logger logger) {
        super(address, dataAddress, notifier, logger);
    }

    @Override
    protected String modifyRequestUrl(String url) {
        return url.contains("?") ?
                (url + '&' + TEAM_CITY_PLUGIN_INFO) :
                (url + '?' + TEAM_CITY_PLUGIN_INFO);
    }
}
