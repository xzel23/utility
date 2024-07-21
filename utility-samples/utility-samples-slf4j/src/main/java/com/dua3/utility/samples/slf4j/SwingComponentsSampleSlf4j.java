package com.dua3.utility.samples.slf4j;

import com.dua3.utility.samples.SwingComponentsSampleLogBase;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class SwingComponentsSampleSlf4j extends SwingComponentsSampleLogBase {
    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) {
        start(SwingComponentsSampleSlf4j::new, args);
    }
}
