package com.dua3.utility.lang;

import java.util.Objects;

/**
 * A Utility class with general purpose methods.
 */
public class LangUtil {

    /**
     * Check that condition is fulfilled.
     *
     * @param condition
     *            condition to test
     * @throws IllegalStateException
     *             if condition is not {@code true}
     */
    public static void check(boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param condition
     *            condition to test
     * @param fmt
     *            message format (@see {@link String#format(String, Object...)})
     * @param args
     *            format arguments
     * @throws IllegalStateException
     *             if condition is not {@code true}
     */
    public static void check(boolean condition, String fmt, Object... args) {
        if (!condition) {
            String message = String.format(fmt, args);
            throw new IllegalStateException(message);
        }
    }

    /**
     * Do nothing.
     * <p>
     * This method does exactly nothing. It's purpose is to keep static code
     * analysis tools happy that complain about ignored return values of methods
     * like BufferedReader.readLine(). Use this method to explicitly ignore the
     * return value and avoid false positives from static code checkers.
     * </p>
     *
     * @param <T>
     *            the parameter type
     * @param arg
     *            the variable to ignore
     */
    public static <T> void ignore(T arg) {
        // nop
    }

    /**
     * Test if first argument is equal to one of the other arguments.
     * @param arg first argument
     * @param rest remaining arguments
     * @return true, if {@code rest} contains at least one item that is equal to {@code arg}
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T arg, T... rest) {
        for (T t:rest) {
            if (Objects.equals(arg, t)) {
                return true;
            }
        }
        return false;
    }

    private LangUtil() {
        // nop
    }

}
