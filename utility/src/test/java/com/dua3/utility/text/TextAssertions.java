package com.dua3.utility.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class TextAssertions {
    public static void assertEqualsWithEscapedOutput(String expected, String actual, String name) {
        if (!expected.equals(actual)) {
            // Escape the whitespace characters
            String escapedExpected = TextUtil.escape(expected);
            String escapedActual = TextUtil.escape(actual);
            // Fail the test with detailed message
            assertEquals(escapedExpected, escapedActual, "\"" + name + "\"\nexpected: \"" + escapedExpected + "\"\nbut was:  \"" + escapedActual + "\"\n");
        }
    }
}
