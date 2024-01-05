package com.dua3.utility.samples.log4j;

import com.dua3.utility.samples.SwingComponentsSampleLogBase;
import com.dua3.utility.swing.SwingUtil;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class SwingComponentsSampleLog4j extends SwingComponentsSampleLogBase {

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SwingComponentsSampleLog4j.class);

    public static void main(String[] args) {
        LOG.info("starting up");

        SwingUtil.setNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            SwingComponentsSampleLog4j instance = new SwingComponentsSampleLog4j();
            instance.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            instance.setVisible(true);
        });
    }
}
