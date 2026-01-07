package com.dua3.utility.samples.log4j;

import com.dua3.utility.logging.DefaultLogEntryFilter;
import com.dua3.utility.logging.LogFilter;
import com.dua3.utility.logging.LogEntryFilter;
import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.LogUtil;
import com.dua3.utility.samples.SwingComponentsSampleLogBase;

/**
 * SwingComponentsSampleLog4j class extends the SwingComponentsSampleLogBase.
 *
 * <p>This implementation uses the Log4J logging framework.
 */
public final class SwingComponentsSampleCommonLogger extends SwingComponentsSampleLogBase {
    static {
        LogUtil.initUnifiedLogging();
        LogEntryFilter filter = new DefaultLogEntryFilter(
                LogLevel.TRACE,
                (name, level) -> name.contains("SwingComponentsSampleCommonLogger"),
                (text, level) -> true
        );
        LogUtil.getGlobalDispatcher().setFilter(filter);
    }

    private SwingComponentsSampleCommonLogger() { /* nothing to do */ }

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        start(SwingComponentsSampleCommonLogger::new);
    }
}
