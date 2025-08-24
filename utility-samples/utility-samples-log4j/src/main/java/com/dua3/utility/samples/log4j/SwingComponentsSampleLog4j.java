package com.dua3.utility.samples.log4j;

import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.log4j.LogUtilLog4J;
import com.dua3.utility.samples.SwingComponentsSampleLogBase;

/**
 * SwingComponentsSampleLog4j class extends the SwingComponentsSampleLogBase.
 *
 * <p>This implementation uses the Log4J logging framework.
 */
public final class SwingComponentsSampleLog4j extends SwingComponentsSampleLogBase {
    static {
        LogUtilLog4J.init(LogLevel.TRACE);
    }

    private SwingComponentsSampleLog4j() { /* nothing to do */ }

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        start(SwingComponentsSampleLog4j::new);
    }
}
