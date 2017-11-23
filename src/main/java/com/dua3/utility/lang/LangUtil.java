package com.dua3.utility.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.dua3.utility.Pair;

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
     * Check that index is valid.
     *
     * @param idx
     *            index to test
     * @param size
     *            collection size
     * @throws IndexOutOfBoundsException
     *             if index is out of range
     */
    public static void checkIndex(int idx, int size) {
        if (idx<0 || idx>=size) {
            throw new IndexOutOfBoundsException("index: "+idx);
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
     * @param <T> argument type
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

	public static <E extends Enum<E>>
	Optional<E> enumConstant(Class<E> clazz, Predicate<E> condition) {
		for (E ec: clazz.getEnumConstants()) {
			if (condition.test(ec)) {
				return Optional.of(ec);
			}
		}
		return Optional.empty();
	}

	public static <E extends Enum<E>>
	Optional<E> enumConstant(Class<E> clazz, String value) {
		return enumConstant(clazz, ec -> ec.toString().equals(value));
	}

    private LangUtil() {
        // nop
    }

    /** The byte order mark in UTF files */
    public static final char UTF_BYTE_ORDER_MARK = 0xfeff;

    /**
     * Test if character is the byte order mark.
     * @param c the character to test
     * @return true if c is the byte order mark
     */
    public boolean isByteOrderMark(char c) {
        return c==UTF_BYTE_ORDER_MARK;
    }

    /**
     * Trim string, remove prepending byte order mark.
     * @param s the string to trim
     * @return the trimmed string
     */
    public static String trimWithByteOrderMark(String s) {
        if (s.isEmpty()) {
            return s;
        }

        if (s.charAt(0)==0xfeff) {
            s = s.substring(1);
        }

        return s.trim();
    }

    /**
     * Insert key-value pairs into map.
     * @param items key-value pairs
     */
    @SafeVarargs
    public static <K,V> void putAllIfAbsent(Map<K,V> map, Pair<K,V>... items) {
        Arrays.stream(items).forEach(item -> map.putIfAbsent(item.first, item.second));
    }

    /**
     * Create an unmodifiable map from key-value pairs.
     * @param items key-value pairs
     * @return unmodifiable map
     */
    @SafeVarargs
    public static <K,V> Map<K,V> map(Pair<K,V>... items) {
        HashMap<K,V> map = new HashMap<>();
        putAllIfAbsent(map, items);
        return Collections.unmodifiableMap(map);
    }
}
