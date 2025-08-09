package com.dua3.utility.samples.slf4j;

import com.dua3.utility.samples.SwingComponentsSampleLogBase;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * SwingComponentsSampleLog4j class extends the SwingComponentsSampleLogBase.
 *
 * <p>This implementation uses the SLF4J logging facade.
 */
public final class SwingComponentsSampleSlf4j extends SwingComponentsSampleLogBase {
    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    /**
     * The main entry point for the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        start(SwingComponentsSampleSlf4j::new);
    }

    private SwingComponentsSampleSlf4j() { /* nothing to do */ }
}
