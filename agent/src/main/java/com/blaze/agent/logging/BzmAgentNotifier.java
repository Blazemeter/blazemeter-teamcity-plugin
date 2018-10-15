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

package com.blaze.agent.logging;

import com.blazemeter.api.logging.UserNotifier;
import jetbrains.buildServer.agent.BuildProgressLogger;

/**
 * Implementation of com.blazemeter.api.logging.UserNotifier
 */
public class BzmAgentNotifier implements UserNotifier {

    private final BuildProgressLogger logger;

    public BzmAgentNotifier(BuildProgressLogger logger) {
        this.logger = logger;
    }

    @Override
    public void notifyInfo(String info) {
        logger.message(info);
    }

    @Override
    public void notifyWarning(String warning) {
        logger.warning(warning);
    }

    @Override
    public void notifyError(String error) {
        logger.error(error);
    }
}
