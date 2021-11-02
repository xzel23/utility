package com.dua3.utility.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LogUtilTest {

    private static String[] splitArgs(String s) {
        return s.split(" ");    
    }
    
    @Test
    void testHandleLoggingCmdArgs() {
        // passing no arguments
        String[] args1 = {};
        String[] result1 = LogUtil.handleLoggingCmdArgs(args1);
        Assertions.assertArrayEquals(args1, result1);

        // none are logging arguments, retain all
        String[] args2 = splitArgs("a b s -log-level INFO");
        String[] result2 = LogUtil.handleLoggingCmdArgs(args2);
        Assertions.assertArrayEquals(args2, result2);

        // the root level should be extracted
        String[] args3 = splitArgs("a b s --log-level-root INFO SEVERE");
        String[] result3 = LogUtil.handleLoggingCmdArgs(args3);
        String[] expected3 = splitArgs("a b s SEVERE");
        Assertions.assertArrayEquals(expected3, result3);

        // testing level parsing
        String[] args4 = splitArgs("--log-level-root INFO --log-level com.dua3.utility.logging.LogUtilTest FINE");
        String[] result4 = LogUtil.handleLoggingCmdArgs(args4);
        String[] expected4 = {};
        Assertions.assertArrayEquals(expected4, result4);
    }
    
}
