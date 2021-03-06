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

package com.blaze.runner.logging;

import com.blazemeter.api.logging.UserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of `com.blazemeter.api.logging.UserNotifier`
 */
public class BzmServerNotifier implements UserNotifier {

    private Logger logger = LoggerFactory.getLogger("com.blazemeter");

    @Override
    public void notifyInfo(String info) {
        logger.info("User notification: " + info);
    }

    @Override
    public void notifyWarning(String warn) {
        logger.warn("User notification: " + warn);
    }

    @Override
    public void notifyError(String error) {
        logger.error("User notification: " + error);
    }
}
