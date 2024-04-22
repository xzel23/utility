package com.dua3.utility.samples.log4j;

import com.dua3.utility.samples.SwingComponentsSampleLogBase;

public class SwingComponentsSampleLog4j extends SwingComponentsSampleLogBase {
    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    public static void main(String[] args) {
        SwingComponentsSampleLogBase.start(SwingComponentsSampleLog4j::new, args);
    }
}
