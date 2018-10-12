package com.blaze.plugins;

import com.blaze.utils.Utils;
import com.blazemeter.api.http.HttpUtils;
import com.blazemeter.api.logging.Logger;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PluginInfo {

    private static final String URL = System.getProperty("bzm.plugin.url", "https://plugins.jetbrains.com/plugin/updates?pluginId=9020");
    private final HttpUtils utils;
    private final Logger logger;

    public PluginInfo(HttpUtils utils) {
        this.utils = utils;
        this.logger = utils.getLogger();
    }

    public boolean hasUpdates() {
        List<String> versions = getVersions();
        if (versions.isEmpty()) {
            return false;
        }
        Collections.sort(versions, versionComparator);
        String maxVersion = versions.get(versions.size() - 1);
        String currentVersion = Utils.version();
        return !maxVersion.equals(currentVersion);
    }

    public List<String> getVersions() {
        JSONArray pluginUpdates = getPluginUpdates();
        if (pluginUpdates == null) {
            return Collections.emptyList();
        }
        List<String> versions = new ArrayList<>();
        for (Object obj : pluginUpdates) {
            if (obj instanceof JSONObject) {
                JSONObject releaseInfo = (JSONObject) obj;
                versions.add(releaseInfo.getString("version"));
            }
        }
        logger.debug("Found " + versions.size() + " versions");
        return versions;
    }

    private JSONArray getPluginUpdates() {
        try {
            JSONObject response = utils.execute(utils.createGet(URL));
            if (response != null && response.containsKey("updates")) {
                return response.getJSONArray("updates");
            } else {
                logger.info("Did not found BlazeMeter plugin updates info");
            }
        } catch (Exception ex) {
            logger.warn("Cannot get BlazeMeter plugin updates info", ex);
        }
        return null;
    }

    public final Comparator<String> versionComparator = new Comparator<String>() {
        @Override
        public int compare(String version1, String version2) {
            int code = 0;

            final String[] versions1 = version1.split("[.]");
            final String[] versions2 = version2.split("[.]");
            int length = (versions1.length > versions2.length) ? versions2.length : versions1.length;

            for (int i = 0; i < length; i++) {
                try {
                    code = Integer.compare(Integer.parseInt(versions1[i]), Integer.parseInt(versions2[i]));
                } catch (NumberFormatException e) {
                    logger.debug("Cannot parse library version", e);
                    code = versions1[i].compareTo(versions2[i]);
                }
                if (code != 0) {
                    break;
                }
            }

            // if version1: 9.9 and version2: 9.9.1
            if (code == 0 && versions1.length != versions2.length) {
                code = Integer.compare(versions1.length, versions2.length);
            }

            return code;
        }
    };
}
