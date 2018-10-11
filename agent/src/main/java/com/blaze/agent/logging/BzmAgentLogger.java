package com.blaze.agent.logging;


import com.blazemeter.api.logging.impl.FileLogger;


public class BzmAgentLogger extends FileLogger {

    public BzmAgentLogger(String logFile) {
        super(logFile);
    }
}
