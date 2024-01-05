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

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SwingComponentsSampleSlf4j.class);

    public static void main(String[] args) {
        LOG.info("starting up");

        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComponentsSampleSlf4j instance = new SwingComponentsSampleSlf4j();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }
}
