package com.blaze;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV2Impl;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.utils.Utils;

/**
 * Created by zmicer on 11.2.15.
 */

public class APIFactory {
    private APIFactory(){}
    public static BlazemeterApi getAPI(String userKey,
                                       String blazeMeterUrl,
                                       String blazeMeterApiVersion) {
        BlazemeterApi blazemeterAPI=null;
            switch (BzmServiceManager.ApiVersion.valueOf(blazeMeterApiVersion)) {
                case autoDetect:
                    BzmServiceManager.ApiVersion apiVersion= Utils.autoDetectApiVersion(userKey,blazeMeterUrl);
                    blazemeterAPI = new BlazemeterApiV3Impl(userKey, blazeMeterUrl);
                    switch (apiVersion) {
                        case v3:
                            blazemeterAPI = new BlazemeterApiV3Impl(userKey,blazeMeterUrl);
                            break;
                        case v2:
                            blazemeterAPI = new BlazemeterApiV2Impl(userKey,blazeMeterUrl);
                            break;
                    }
                    break;
                case v3:
                    blazemeterAPI = new BlazemeterApiV3Impl(userKey,blazeMeterUrl);
                    break;
                case v2:
                    blazemeterAPI = new BlazemeterApiV2Impl(userKey,blazeMeterUrl);
                    break;
            }
        return blazemeterAPI;
    }
}
