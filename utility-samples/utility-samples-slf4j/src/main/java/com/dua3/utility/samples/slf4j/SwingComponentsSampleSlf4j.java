package com.dua3.utility.samples.slf4j;

import com.dua3.utility.samples.SwingComponentsSampleLogBase;
import com.dua3.utility.swing.SwingUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class SwingComponentsSampleSlf4j extends SwingComponentsSampleLogBase {
    static {
        java.util.logging.LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) {
        SwingComponentsSampleLogBase.start(SwingComponentsSampleSlf4j::new, args);
    }
}
