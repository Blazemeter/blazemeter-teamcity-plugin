package com.blaze;

import com.blaze.api.BlazemeterApi;
import com.blaze.api.BlazemeterApiV2Impl;
import com.blaze.api.BlazemeterApiV3Impl;
import com.blaze.runner.Constants;
import com.blaze.utils.Utils;
import jetbrains.buildServer.agent.BuildProgressLogger;

/**
 * Created by zmicer on 11.2.15.
 */

public class APIFactory {
    private APIFactory(){}
    public static BlazemeterApi getAPI(String userKey,String serverName,
                                       String serverPort, String username,
                                       String password, String blazeMeterUrl,
                                       String blazeMeterApiVersion,
                                       BuildProgressLogger logger) {
            int serverPortInt = 0;
            if (serverPort != null && !serverPort.isEmpty()) {
                serverPortInt = Integer.parseInt(serverPort);
            }
        BlazemeterApi blazemeterAPI=null;
            switch (BzmServiceManager.ApiVersion.valueOf(blazeMeterApiVersion)) {
                case autoDetect:
                    BzmServiceManager.ApiVersion apiVersion= Utils.autoDetectApiVersion(userKey,serverName,
                            serverPort, username,password,blazeMeterUrl,logger);
                    blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverName, serverPortInt, username, password, blazeMeterUrl);
                    switch (apiVersion) {
                        case v3:
                            blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverName, serverPortInt, username, password, blazeMeterUrl);
                            break;
                        case v2:
                            blazemeterAPI = new BlazemeterApiV2Impl(userKey,serverName, serverPortInt, username, password, blazeMeterUrl);
                            break;
                    }
                    break;
                case v3:
                    blazemeterAPI = new BlazemeterApiV3Impl(userKey,serverName, serverPortInt, username, password, blazeMeterUrl);
                    break;
                case v2:
                    blazemeterAPI = new BlazemeterApiV2Impl(userKey,serverName, serverPortInt, username, password, blazeMeterUrl);
                    break;
            }
        return blazemeterAPI;
    }
}
