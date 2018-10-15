/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blaze.utils;

import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class TCBzmUtils extends BlazeMeterUtils {

    private static final String APP_KEY = "app_key=jnk100x987c06f4e10c4";

    private static final String CLIENT_IDENTIFICATION = "&_clientId=CI_TEAMCITY&_clientVersion="
            + Utils.version();

    public static String TEAM_CITY_PLUGIN_INFO = APP_KEY + CLIENT_IDENTIFICATION;

    public TCBzmUtils(String apiKeyId, String apiKeySecret, String address, UserNotifier notifier, Logger logger) {
        super(apiKeyId, apiKeySecret, address, "data_address", notifier, logger);
    }

    @Override
    protected String modifyRequestUrl(String url) {
        return url.contains("?") ?
                (url + '&' + TEAM_CITY_PLUGIN_INFO) :
                (url + '?' + TEAM_CITY_PLUGIN_INFO);
    }

}
