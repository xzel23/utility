package com.dua3.utility.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Provides utility methods for performing text-related assertions.
 * This class is a final utility class and cannot be instantiated.
 */
public final class TextAssertions {
    private TextAssertions() {
        // utility class
    }

    /**
     * Compares two strings for equality and provides detailed failure messages
     * with escaped representations of whitespace characters if the assertion fails.
     *
     * @param expected the expected string value in the comparison
     * @param actual the actual string value in the comparison
     * @param name the name of the value being compared, used to identify the source of the values in the error message
     */
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
