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

package com.blaze.runner;

import java.io.*;
import java.util.Properties;

import jetbrains.buildServer.serverSide.ServerPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity for store admin properties:
 * - apiKeyID and apiKeySecret  - BlazeMeter user API key and API secret
 * - blazeMeterUrl - BlazeMeter URL
 */
public class AdminSettings {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    public ServerPaths serverPaths;
    private String apiKeyID = "";
    private String apiKeySecret = "";
    private String blazeMeterUrl = "";

    public AdminSettings(ServerPaths serverPaths) {
        this.serverPaths = serverPaths;
    }

    public void init() {
        loadProperties();
    }

    public void saveProperties() throws IOException {
        File keyFile = propFile();
        if (keyFile == null) {
            throw new RuntimeException("Property file not found");
        }

        Properties prop = new Properties();
        FileWriter outFile = new FileWriter(keyFile);
        prop.put("apiKeyID", this.apiKeyID);
        prop.put("apiKeySecret", this.apiKeySecret);
        prop.put("blazeMeterUrl", this.blazeMeterUrl);
        prop.store(outFile, null);
        outFile.close();
    }

    public void loadProperties() {
        File keyFile = propFile();
        if (keyFile == null) {
            return;
        }

        FileReader inFile = null;
        try {
            inFile = new FileReader(keyFile);
            Properties prop = new Properties();
            prop.load(inFile);
            this.apiKeyID = prop.getProperty("apiKeyID");
            this.apiKeySecret = prop.getProperty("apiKeySecret");
            this.blazeMeterUrl = prop.getProperty("blazeMeterUrl");
            inFile.close();
        } catch (IOException e) {
            logger.error("Cannot load configuration: " + e.getMessage());
        } finally {
            try {
                if (inFile != null) {
                    inFile.close();
                }
            } catch (IOException e) {
                logger.error("Cannot close " + keyFile.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }

    private File propFile() {
        File keyFile = new File(serverPaths.getConfigDir() + Constants.BZM_PROPERTIES_FILE);
        if (!keyFile.exists()) {
            try {
                boolean created = keyFile.createNewFile();
                if (!created) {
                    logger.error("Cannot create configuration file, file already exists!");
                }
            } catch (IOException e) {
                logger.error("Cannot create configuration file! Error is: " + e.getMessage());
                return null;
            }
        }
        return keyFile;
    }


    public String getApiKeyID() {
        return apiKeyID;
    }

    public void setApiKeyID(String apiKeyID) {
        this.apiKeyID = apiKeyID;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public void setApiKeySecret(String apiKeySecret) {
        this.apiKeySecret = apiKeySecret;
    }

    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }
}
