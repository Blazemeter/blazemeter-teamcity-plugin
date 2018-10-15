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


package com.blaze.agent.utils;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.TestDetector;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.CiBuild;
import com.blazemeter.ciworkflow.CiPostProcess;

import java.io.IOException;

public class TeamCityCiBuild extends CiBuild {

    protected AbstractTest currentTest;

    public TeamCityCiBuild(BlazeMeterUtils utils, String testId, String properties, String notes, CiPostProcess ciPostProcess) {
        super(utils, testId, properties, notes, ciPostProcess);
    }

    @Override
    public Master start() throws IOException, InterruptedException {
        this.notifier.notifyInfo("CiBuild is started.");
        AbstractTest test = TestDetector.detectTest(this.utils, this.testId);
        currentTest = test;
        if(test == null) {
            this.logger.error("Failed to detect test type. Test with id=" + this.testId + " not found.");
            this.notifier.notifyError("Failed to detect test type. Test with id = " + this.testId + " not found.");
            return null;
        } else {
            notifier.notifyInfo(String.format("Start test id : %s, name : %s", test.getId(), test.getName()));
            return this.startTest(test);
        }
    }

    public AbstractTest getCurrentTest() {
        return currentTest;
    }
}
