package com.dua3.utility.lang;

public class LangUtil {

    private LangUtil() {
        // nop
    }

    /**
     * Check that condition is fulfilled.
     * @param condition condition to test
     * @param fmt message format (@see {@link String#format(String, Object...)})
     * @param args format arguments
     * @throws IllegalStateException if condition is not {@code true}
     */
    public static void check(boolean condition, String fmt, Object... args) {
        if (!condition) {
            String message = String.format(fmt, args);
            throw new IllegalStateException(message);
        }
    }

    /**
     * Check that condition is fulfilled.
     * @param condition condition to test
     * @throws IllegalStateException if condition is not {@code true}
     */
    public static void check(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

}
