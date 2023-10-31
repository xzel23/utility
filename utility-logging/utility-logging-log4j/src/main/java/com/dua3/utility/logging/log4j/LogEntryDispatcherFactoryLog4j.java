package com.dua3.utility.logging.log4j;

import com.dua3.utility.logging.LogEntryDispatcher;
import com.dua3.utility.logging.ILogEntryDispatcherFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

public class LogEntryDispatcherFactoryLog4j implements ILogEntryDispatcherFactory {
    @Override
    public LogEntryDispatcher getGlobalDispatcher() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        return LogUtilLog4J.createAppenderAndAttachAllLoggers(context, "LogEntryDispatcherLog4J");
    }
}
