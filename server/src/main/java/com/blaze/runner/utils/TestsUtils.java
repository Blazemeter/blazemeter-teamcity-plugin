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

package com.blaze.runner.utils;

import com.blaze.plugins.PluginInfo;
import com.blaze.utils.Utils;
import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestsUtils {

    private BzmServerUtils utils;

    public TestsUtils() {
    }

    public TestsUtils(BzmServerUtils utils) {
        this.utils = utils;
    }

    public BzmServerUtils getUtils() {
        return utils;
    }

    public void setUtils(BzmServerUtils utils) {
        this.utils = utils;
    }

    /**
     * Check if this plugin has updates
     */
    public boolean hasUpdates() {
        PluginInfo info = new PluginInfo(utils);
        return info.hasUpdates();
    }

    /**
     * Used in viewBlazeRunnerParams.jsp
     *
     * @param testId - testId in the following format 'id.type'
     * @return test label
     */
    public String getTestLabel(String testId) {
        Logger logger = utils.getLogger();
        String numberTestId = Utils.getTestId(testId);
        logger.info("Get test label for testId=" + numberTestId);
        try {
            AbstractTest test = TestDetector.detectTest(utils, numberTestId);
            return test.getName() + "(" + testId + ")";
        } catch (Throwable e) {
            logger.warn("Failed to get Test Label", e);
            return "Failed to get Test Label. " + e.getMessage();
        }
    }


    /**
     * Used in editBlazeRunnerParams.jsp
     *
     * @return Map where key is Workspace, value - lists of Tests in this Workspace
     */
    public Map<Workspace, List<AbstractTest>> getTests() {
        final Map<Workspace, List<AbstractTest>> result = new HashMap<>();
        try {
            User user = new User(utils);
            List<Account> accounts = user.getAccounts();
            for (Account account : accounts) {
                addTestsForAccount(account, result);
            }
        } catch (Throwable ex) {
            utils.getLogger().error("Failed to get tests. Reason is: " + ex.getMessage(), ex);
            if (result.isEmpty()) {
                result.put(new Workspace(utils, ex.getMessage(), "Failed to get tests. "), Collections.<AbstractTest>emptyList());
                return result;
            }
        }
        return checkForEmptyTests(result);
    }

    private Map<Workspace, List<AbstractTest>> checkForEmptyTests(Map<Workspace, List<AbstractTest>>  tests) {
        if (tests.isEmpty()) {
            return generateEmptyTestMap();
        }
        if (tests.size() == 1) {
            Set<Workspace> workspaces = tests.keySet();
            List<AbstractTest> list = tests.get(workspaces.toArray(new Workspace[1])[0]);
            if (list.isEmpty()) {
                return generateEmptyTestMap();
            }
        }
        return tests;
    }

    private Map<Workspace, List<AbstractTest>> generateEmptyTestMap() {
        final Map<Workspace, List<AbstractTest>> res = new HashMap<>();
        Workspace workspace = new Workspace(utils, "No tests for this account", "");
        res.put(workspace, Collections.<AbstractTest>emptyList());
        return res;
    }

    private void addTestsForAccount(Account account, Map<Workspace, List<AbstractTest>> result) {
        try {
            List<Workspace> workspaces = account.getWorkspaces();
            for (Workspace workspace : workspaces) {
                addTestsForWorkspace(result, workspace);
            }
        } catch (IOException e) {
            utils.getLogger().error("Failed to get workspaces for account id =" + account.getId() + ". Reason is: " + e.getMessage(), e);
        }
    }

    private void addTestsForWorkspace(Map<Workspace, List<AbstractTest>> result, Workspace workspace) {
        final List<AbstractTest> tests = new ArrayList<>();
        boolean hasError = false;
        try {
            tests.addAll(workspace.getSingleTests());
        } catch (Exception e) {
            utils.getLogger().error("Failed to get single tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
            hasError = true;
        }

        try {
            tests.addAll(workspace.getMultiTests());
        } catch (Exception e) {
            utils.getLogger().error("Failed to get multi tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
            hasError = true;
        }

        if (tests.isEmpty() && !hasError) {
            workspace.setId(workspace.getId() + ")(No tests for this workspace");
        } else if (hasError) {
            workspace.setId(workspace.getId() + ")(Failed to get tests");
        }

        Collections.sort(tests, new Comparator<AbstractTest>() {
            @Override
            public int compare(AbstractTest o1, AbstractTest o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        result.put(workspace, tests);
    }
}
