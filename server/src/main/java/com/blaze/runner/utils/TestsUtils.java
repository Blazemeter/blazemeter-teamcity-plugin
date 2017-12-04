package com.blaze.runner.utils;

import com.blaze.utils.Utils;
import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            return test.getName();
        } catch (IOException e) {
            logger.warn("Failed to get Test Label", e);
            return testId;
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
        } catch (IOException ex) {
            utils.getNotifier().notifyAbout("Failed to get accounts. Reason is: " + ex.getMessage());
            utils.getLogger().error("Failed to get accounts. Reason is: " + ex.getMessage(), ex);
        }
        return result;
    }

    private void addTestsForAccount(Account account, Map<Workspace, List<AbstractTest>> result) {
        try {
            List<Workspace> workspaces = account.getWorkspaces();
            for (Workspace workspace : workspaces) {
                final List<AbstractTest> tests = new ArrayList<>();
                tests.addAll(getSingleTestsForWorkspace(workspace));
                tests.addAll(getMultiTestsForWorkspace(workspace));
                result.put(workspace, tests);
            }
        } catch (IOException e) {
            utils.getLogger().error("Failed to get workspaces for account id =" + account.getId() + ". Reason is: " + e.getMessage(), e);
        }
    }

    private List<SingleTest> getSingleTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getSingleTests();
        } catch (IOException e) {
            utils.getLogger().error("Failed to get single tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    private List<MultiTest> getMultiTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getMultiTests();
        } catch (IOException e) {
            utils.getLogger().error("Failed to get multi tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage(), e);
        }
        return Collections.emptyList();
    }
}