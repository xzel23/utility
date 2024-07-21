package com.dua3.utility.samples.log4j;

import com.dua3.utility.logging.LogLevel;
import com.dua3.utility.logging.log4j.LogUtilLog4J;
import com.dua3.utility.samples.SwingComponentsSampleLogBase;

public class SwingComponentsSampleLog4j extends SwingComponentsSampleLogBase {
    static {
        LogUtilLog4J.init(LogLevel.TRACE);
    }

    public static void main(String[] args) {
        start(SwingComponentsSampleLog4j::new, args);
    }
}
