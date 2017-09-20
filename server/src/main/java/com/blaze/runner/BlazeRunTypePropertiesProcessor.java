/**
 * Copyright 2017 BlazeMeter Inc.
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
        List<InvalidProperty> result = new Vector<>();

        final String test = properties.get(Constants.SETTINGS_ALL_TESTS_ID);

        if (PropertiesUtil.isEmptyOrNull(test)) {
            result.add(new InvalidProperty(Constants.SETTINGS_ALL_TESTS_ID, "A test must be selected."));
        }

        return result;
    }

}
