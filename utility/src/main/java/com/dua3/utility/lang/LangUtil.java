// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontData;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.io.IoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.lang.ref.Cleaner;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * A Utility class with general purpose methods.
 */
public final class LangUtil {
    private static final Logger LOG = LogManager.getLogger(LangUtil.class);
    private static final String INVALID_FORMATTING = "format String does not match arguments";

    /**
     * A holder class for a securely initialized instance of {@link SecureRandom}.
     * This class ensures a single instance of SecureRandom is lazily initialized
     * and safely published for use in cryptographic operations.
     */
    private static final class SecureRandomHolder {
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    }

    /**
     * Provides a thread-safe instance of SecureRandom.
     * <p>
     * The method accesses a lazily initialized SecureRandom instance
     * that ensures cryptographic security and should be preferred for
     * generating secure random values.
     *
     * @return a SecureRandom instance with cryptographically strong
     *         random number generation.
     */
    private static SecureRandom secureRandom() {
        return SecureRandomHolder.SECURE_RANDOM;
    }

    /**
     * The byte order mark in UTF files
     */
    public static final char UTF_BYTE_ORDER_MARK = 0xfeff;

    private static final Cleaner CLEANER = Cleaner.create();

    // private constructor for utility class
    private LangUtil() {
        // nop
    }

    /**
     * Validates the provided argument based on the specified condition. If the condition is not met,
     * an IllegalArgumentException is thrown with a descriptive message.
     *
     * @param <T> The type of the argument to validate.
     * @param argName The name of the argument being validated. Used for logging or debugging purposes.
     * @param condition A Predicate representing the condition that the argument must satisfy.
     * @param value The value of the argument to validate against the specified condition.
     * @throws IllegalArgumentException if the provided value does not satisfy the condition.
     */
    public static <T> void checkArg(String argName, Predicate<T> condition, T value) {
        if (!condition.test(value)) {
            throw new IllegalArgumentException("invalid argument '" + argName + "': " + value);
        }
    }

    /**
     * Validates the provided boolean condition and throws an IllegalArgumentException with the
     * specified message if the condition is false.
     *
     * @param condition the boolean condition to be checked
     * @param msg a supplier that provides the exception message if the condition is false
     * @throws IllegalArgumentException if the condition is false
     */
    public static void checkArg(boolean condition, Supplier<String> msg) throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(msg.get());
        }
    }

    /**
     * Checks a condition and throws an {@link IllegalArgumentException} with a formatted message
     * if the condition is false.
     *
     * @param condition the boolean condition to check; if false, an exception is thrown
     * @param fmt the format string used to construct the exception message
     * @param fmtArgs the arguments referenced by the format specifiers in the format string
     * @throws IllegalArgumentException if the specified condition is false
     */
    public static void checkArg(boolean condition, String fmt, Object... fmtArgs) throws IllegalArgumentException {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (!condition) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
    }

    /**
     * Validates whether the provided format string and arguments are compatible.
     * <p>
     * <strong>Note:</strong> This method is for checks during development only!
     *
     * @param fmt the format string to be validated
     * @param fmtArgs the arguments to be applied to the format string
     * @return true if the format string is valid with the provided arguments, false otherwise
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean isFormatValid(String fmt, Object[] fmtArgs) {
        try {
            String.format(fmt, fmtArgs);
            return true;
        } catch (IllegalFormatException e) {
            LOG.error("Invalid format string: {}", fmt, e);
            return false;
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param condition condition to test
     * @throws FailedCheckException if condition does not evaluate to {@code true}
     */
    public static void check(boolean condition) {
        if (!condition) {
            throw new FailedCheckException("condition failed");
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param condition         condition to test
     * @param exceptionSupplier the exception supplier
     * @param <E>               the exception type
     * @throws E if condition does not evaluate to {@code true}
     */
    public static <E extends Exception> void check(boolean condition, Supplier<E> exceptionSupplier) throws E {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param condition condition to test
     * @param fmt       message format (@see
     *                  {@link String#format(String, Object...)})
     * @param fmtArgs      format arguments
     * @throws FailedCheckException if condition does not evaluate to {@code true}
     */
    public static void check(boolean condition, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (!condition) {
            String message = String.format(Locale.ROOT, fmt, fmtArgs);
            throw new FailedCheckException(message);
        }
    }

    /**
     * Do nothing.
     * <p>
     * This method does exactly nothing. Its purpose is to keep static code
     * analysis tools happy that complain about ignored return values of methods
     * like BufferedReader.readLine(). Use this method to explicitly ignore the
     * return value and avoid false positives from static code checkers.
     * </p>
     *
     * @param <T> the parameter type
     * @param arg the variable to ignore
     */
    @SuppressWarnings("EmptyMethod")
    public static <T extends @Nullable Object> void ignore(T arg) {
        // nop
    }

    /**
     * Returns a fixed-size list backed by the specified array. The returned list is immutable and
     * any attempt to modify it will result in an UnsupportedOperationException.
     * <p>
     * In contrast to {@link List#of(Object[])}, this method supports null elements.
     *
     * @param <T> the class of the elements in the array
     * @param args the array by which the list will be backed
     * @return an immutable list containing the specified elements
     */
    @SafeVarargs
    public static <T extends @Nullable Object> List<T> asUnmodifiableList(T... args) {
        return new UnmodifiableArrayListWrapper<>(args);
    }

    /**
     * Test if first argument is equal to one of the other arguments.
     *
     * @param <T>  argument type
     * @param arg  first argument
     * @param rest remaining arguments
     * @return true, if {@code rest} contains at least one item that is equal
     * to {@code arg}
     */
    @SafeVarargs
    public static <T extends @Nullable Object> boolean isOneOf(T arg, T... rest) {
        return asUnmodifiableList(rest).contains(arg);
    }

    /**
     * Test if first argument is not equal to any of the other arguments.
     *
     * @param <T>  argument type
     * @param arg  first argument
     * @param rest remaining arguments
     * @return true, if {@code rest} does not contain any item that is equal
     * to {@code arg}
     */
    @SafeVarargs
    public static <T extends @Nullable Object> boolean isNoneOf(T arg, T... rest) {
        return !asUnmodifiableList(rest).contains(arg);
    }

    /**
     * Return first argument if non-null, second argument otherwise.
     *
     * @param a   the first argument
     * @param b   the second argument
     * @param <T> the type
     * @return a, if a != null, else b
     */
    public static <T extends @Nullable Object> T orElse(@Nullable T a, T b) {
        return a != null ? a : b;
    }

    /**
     * Return first argument if non-null, generate value otherwise.
     *
     * @param a   the first argument
     * @param b   the supplier in case a==null
     * @param <T> the type
     * @return a, if a != null, else b.get()
     */
    public static <T> @Nullable T orElseGet(@Nullable T a, Supplier<? extends @Nullable T> b) {
        return a != null ? a : b.get();
    }

    /**
     * Find enum by Predicate.
     *
     * @param clazz     the enum class
     * @param condition the predicate
     * @param <E>       the generic enum parameter
     * @return an Optional holding the enum constant or an empty Optional
     */
    public static <E extends Enum<E>> Optional<E> enumConstant(Class<? extends E> clazz, Predicate<? super E> condition) {
        for (E ec : clazz.getEnumConstants()) {
            if (condition.test(ec)) {
                return Optional.of(ec);
            }
        }
        return Optional.empty();
    }

    /**
     * Find enum by the result of its {@code toString()} method as opposed to {@link Enum#valueOf(Class, String)} which
     * compares by {@link Enum#name()}.
     *
     * @param clazz the enum class
     * @param value the value to look for
     * @param <E>   the generic enum parameter
     * @return an Optional holding the enum constant or an empty Optional
     */
    public static <E extends Enum<E>> Optional<E> enumConstant(Class<? extends E> clazz, String value) {
        return enumConstant(clazz, ec -> ec.toString().equals(value));
    }

    /**
     * Get the values for an enum class.
     *
     * @param clazz the enum class
     * @param <E>   the enum type
     * @return result of invoking enum class' values() method
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E[] enumValues(Class<? extends E> clazz) {
        try {
            return (E[]) clazz.getMethod("values").invoke(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access enum values() method - ensure enum class is public and accessible", e);
        } catch (InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("calling Enum.values() failed", e);
        }
    }

    /**
     * Test if character is the byte order mark.
     *
     * @param c the character to test
     * @return true if c is the byte order mark
     */
    public static boolean isByteOrderMark(char c) {
        return c == UTF_BYTE_ORDER_MARK;
    }

    /**
     * Re-throw checked exception as unchecked.
     *
     * @param e the exception
     * @return RuntimeException, UncheckedIOException, or WrappedException depending on the type of e
     */
    public static RuntimeException wrapException(Exception e) {
        return switch (e) {
            case RuntimeException re -> re;
            case IOException ioe -> new UncheckedIOException(ioe);
            default -> new WrappedException(e);
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param <T> the argument type
     * @param <E> the exception type
     * @param c   the consumer to call (instance of {@link ConsumerThrows})
     * @return instance of Consumer that invokes f and converts IOException to
     * UncheckedIOException, CheckedException to WrappedException, and lets UncheckedExceptions through
     * @throws RuntimeException     if {@link RuntimeException} is thrown during execution of the argument passed
     * @throws UncheckedIOException if {@link IOException} is thrown during execution of the argument passed
     * @throws WrappedException     if any other type of Exception is thrown during execution of the argument passed
     */
    public static <T extends @Nullable Object, E extends Exception> Consumer<T> uncheckedConsumer(ConsumerThrows<T, E> c) {
        return (T arg) -> {
            try {
                c.accept(arg);
            } catch (Exception e) {
                throw wrapException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param <T> the argument type
     * @param <E> the exception type
     * @param s   the supplier to call (instance of {@link SupplierThrows})
     * @return instance of {@link Supplier} that invokes f and converts IOException to
     * UncheckedIOException, CheckedException to WrappedException, and lets UncheckedExceptions through
     * @throws RuntimeException     if {@link RuntimeException} is thrown during execution of the argument passed
     * @throws UncheckedIOException if {@link IOException} is thrown during execution of the argument passed
     * @throws WrappedException     if any other type of Exception is thrown during execution of the argument passed
     */
    @SuppressWarnings({"OverlyBroadCatchBlock", "ProhibitedExceptionThrown"})
    public static <T extends @Nullable Object, E extends Exception> Supplier<T> uncheckedSupplier(SupplierThrows<? extends T, E> s) {
        return () -> {
            try {
                return s.get();
            } catch (Exception e) {
                throw wrapException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param <T> the argument type
     * @param <R> the result type
     * @param <E> the exception type
     * @param f   the function to call (instance of {@link FunctionThrows})
     * @return instance of Function that invokes f and converts IOException to
     * UncheckedIOException and other checked exceptions to {@link WrappedException}
     * @throws RuntimeException     if {@link RuntimeException} is thrown during execution of the argument passed
     * @throws UncheckedIOException if {@link IOException} is thrown during execution of the argument passed
     * @throws WrappedException     if any other type of Exception is thrown during execution of the argument passed
     */
    @SuppressWarnings({"OverlyBroadCatchBlock", "ProhibitedExceptionThrown"})
    public static <T extends @Nullable Object, R extends @Nullable Object, E extends Exception> Function<T, R> uncheckedFunction(FunctionThrows<T, R, E> f) {
        return (T arg) -> {
            try {
                return f.apply(arg);
            } catch (Exception e) {
                throw wrapException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException}.
     *
     * @param <E> exception type as declared by {@link RunnableThrows}
     * @param r   the Runnable to call (instance of {@link RunnableThrows})
     * @return instance of Function that invokes f and converts IOException to
     * UncheckedIOException
     * @throws RuntimeException     if {@link RuntimeException} is thrown during execution of the argument passed
     * @throws UncheckedIOException if {@link IOException} is thrown during execution of the argument passed
     * @throws WrappedException     if any other type of Exception is thrown during execution of the argument passed
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static <E extends Exception> Runnable uncheckedRunnable(RunnableThrows<E> r) {
        return () -> {
            try {
                r.run();
            } catch (Exception e) {
                throw wrapException(e);
            }
        };
    }

    /**
     * Trim string, remove prepending byte order mark.
     *
     * @param s the string to trim
     * @return the trimmed string
     */
    public static String trimWithByteOrderMark(String s) {
        if (s.isEmpty()) {
            return s;
        }

        if (s.charAt(0) == UTF_BYTE_ORDER_MARK) {
            s = s.substring(1);
        }

        return s.trim();
    }

    /**
     * Insert key-value pairs into map, <em>not</em> overwriting existing mappings.
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param map   the map to insert into
     * @param items the key-value pairs to put into the map
     */
    @SafeVarargs
    public static <K, V> void putAllIfAbsent(Map<? super K, ? super V> map, Map.Entry<K, V>... items) {
        for (var item : items) {
            map.putIfAbsent(item.getKey(), item.getValue());
        }
    }

    /**
     * Insert key-value pairs into map, <em>replacing</em> existing mappings.
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param map   the map to insert into
     * @param items the key-value pairs to put into the map
     */
    @SafeVarargs
    public static <K, V> void putAll(Map<? super K, ? super V> map, Map.Entry<K, V>... items) {
        for (Map.Entry<K, V> item : items) {
            map.put(item.getKey(), item.getValue());
        }
    }

    /**
     * Map {@link OptionalInt} to an {@link Optional}.
     *
     * @param opt the {@link OptionalInt}
     * @param f   the mapping function
     * @param <T> the result type
     * @return Optional holding the mapped value or Optional.empty()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> mapOptional(OptionalInt opt, IntFunction<? extends @Nullable T> f) {
        return opt.isEmpty() ? Optional.empty() : Optional.ofNullable(f.apply(opt.getAsInt()));
    }

    /**
     * Map {@link OptionalLong} to an {@link Optional}.
     *
     * @param opt the {@link OptionalLong}
     * @param f   the mapping function
     * @param <T> the result type
     * @return Optional holding the mapped value or Optional.empty()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> mapOptional(OptionalLong opt, LongFunction<? extends @Nullable T> f) {
        return opt.isEmpty() ? Optional.empty() : Optional.ofNullable(f.apply(opt.getAsLong()));
    }

    /**
     * Map {@link OptionalDouble} to an {@link Optional}.
     *
     * @param opt the {@link OptionalDouble}
     * @param f   the mapping function
     * @param <T> the result type
     * @return Optional holding the mapped value or Optional.empty()
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> mapOptional(OptionalDouble opt, DoubleFunction<? extends @Nullable T> f) {
        return opt.isEmpty() ? Optional.empty() : Optional.ofNullable(f.apply(opt.getAsDouble()));
    }

    /**
     * Applies a mapping function to the given input and returns the result.
     * If the input is null, this method will return null without applying the function.
     *
     * @param <T> the type of the input
     * @param <U> the type of the output
     * @param t the input value, which may be null
     * @param f the mapping function to apply to the input, must accept and produce possibly nullable values
     * @return the result of applying the mapping function to the input, or null if the input is null
     */
    public static <T, U extends @Nullable Object> U map(@Nullable T t, Function<? super @Nullable T, U> f) {
        return t == null ? null : f.apply(t);
    }

    /**
     * Test streams for equality.
     *
     * @param <T> the element type
     * @param s1  first stream
     * @param s2  second stream
     * @return true, if and only if both streams are equal elementwise
     */
    public static <T> boolean equals(@Nullable Stream<@Nullable T> s1, @Nullable Stream<@Nullable T> s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2;
        }

        Iterator<@Nullable T> iter1 = s1.iterator();
        Iterator<@Nullable T> iter2 = s2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return !iter1.hasNext() && !iter2.hasNext();
    }

    /**
     * Consume value if mapping exists.
     *
     * @param <K>      the key type
     * @param <V>      the value type
     * @param map      the map
     * @param k        the key to lookup
     * @param consumer the consumer to consume the mapped value
     */
    public static <K, V extends @Nullable Object> void consumeIfPresent(Map<K, V> map, K k, Consumer<? super V> consumer) {
        V v = map.get(k);
        if (v != null) {
            consumer.accept(v);
        }
    }

    /**
     * Consume value if mapping exists.
     *
     * @param <K>      the key type
     * @param <V>      the value type
     * @param map      the map
     * @param k        the key to lookup
     * @param consumer the consumer to consume the mapped value
     */
    public static <K, V extends @Nullable Object> void consumeIfPresent(Map<K, V> map, K k, BiConsumer<? super K, ? super V> consumer) {
        V v = map.get(k);
        if (v != null) {
            consumer.accept(k, v);
        }
    }

    /**
     * Create a lazy, caching Supplier. Upon first invocation of `get()`, `s.get()`
     * is called to create the object to be returned. Each subsequent call will
     * return the same object without invoking `s.get()` again.
     *
     * @param <T>      the result type
     * @param supplier the Supplier
     * @return caching Supplier
     */
    public static <T extends @Nullable Object> Supplier<T> cache(Supplier<T> supplier) {
        return new CachingSupplier<>(supplier, (T t) -> {});
    }

    /**
     * Create a lazy, caching, and auto-closable Supplier. Upon first invocation of
     * `get()`, `s.get()` is called to create the object to be returned. Each
     * subsequent call will return the same object without invoking `s.get()` again.
     * If the supplier is closed, it is reset to uninitialized state and can be
     * reused. A new object will be created when the supplier is reused.
     *
     * @param <T>      the result type
     * @param supplier the Supplier
     * @param cleaner  the cleanup operation to be executed on `close()`
     * @return caching Supplier
     */
    public static <T> AutoCloseableSupplier<T> cache(Supplier<? extends T> supplier, Consumer<? super T> cleaner) {
        return new CachingSupplier<>(supplier, cleaner);
    }

    /**
     * Get URL for a resource on the classpath.
     *
     * @param clazz    the Class that's used to load the resource.
     * @param resource path (relative to clazz) of resource to load
     * @return URL for the given resource
     * @throws MissingResourceException if the resource could not be found
     */
    public static URL getResourceURL(Class<?> clazz, String resource) {
        URL url = clazz.getResource(resource);
        if (url == null) {
            throw new MissingResourceException("Resource not found: " + resource, clazz.getName(), resource);
        }
        return url;
    }

    /**
     * Read the content of a resource on the classpath into a String.
     *
     * @param clazz    the Class that's used to load the resource.
     * @param resource path (relative to clazz) of resource to load
     * @return A String containing the resource's content
     * @throws IOException if the resource could not be loaded
     */
    public static String getResourceAsString(Class<?> clazz, String resource) throws IOException {
        return new String(getResource(clazz, resource), StandardCharsets.UTF_8);
    }

    /**
     * Read the content of a resource on the classpath.
     *
     * @param clazz    the Class that's used to load the resource.
     * @param resource path (relative to clazz) of resource to load
     * @return A byte array containing the resource's content
     * @throws IOException if the resource could not be loaded
     */
    public static byte[] getResource(Class<?> clazz, String resource) throws IOException {
        URL url = getResourceURL(clazz, resource);
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * Load a properties file in UTF-8 encoding.
     *
     * @param url URL to read from
     * @return the properties
     * @throws IOException on error
     */
    public static Properties loadProperties(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            return loadProperties(in);
        }
    }

    /**
     * Load a properties file in UTF-8 encoding.
     *
     * @param uri URI to read from
     * @return the properties
     * @throws IOException on error
     */
    public static Properties loadProperties(URI uri) throws IOException {
        try (InputStream in = IoUtil.openInputStream(uri)) {
            return loadProperties(in);
        }
    }

    /**
     * Load a properties file in UTF-8 encoding.
     *
     * @param path path to read from
     * @return the properties
     * @throws IOException on error
     */
    public static Properties loadProperties(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return loadProperties(in);
        }
    }

    /**
     * Load a properties file in UTF-8 encoding.
     *
     * @param in stream to read from
     * @return the properties
     * @throws IOException on error
     */
    public static Properties loadProperties(InputStream in) throws IOException {
        Properties p = new Properties();
        // make sure UTF-8 is used by explicitly instantiating the reader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            p.load(reader);
        }
        return p;
    }

    /**
     * Create an EnumSet. This method also works if values is empty.
     *
     * @param clss   the enum class
     * @param values the values
     * @param <E>    the enum type
     * @return the EnumSet
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> clss, E... values) {
        return enumSet(clss, List.of(values));
    }

    /**
     * Create an EnumSet. This method also works if values is empty.
     *
     * @param clss   the enum class
     * @param values the values
     * @param <E>    the enum type
     * @return the EnumSet
     */
    public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> clss, Collection<E> values) {
        return values.isEmpty() ? EnumSet.noneOf(clss) : EnumSet.copyOf(values);
    }

    /**
     * Get language suffix to use for resource lookup.
     * <p>
     * Using {@link Locale#toLanguageTag()} does not always work, like for Indonesian which
     * returns "id" whereas the bundle uses "in" as suffix.
     *
     * @param locale the locale
     * @return the language suffix as used by the resource bundle
     */
    public static String getLocaleSuffix(Locale locale) {
        String language = locale.getLanguage();
        if (language.isEmpty()) {
            return "";
        }

        String country = locale.getCountry();
        if (country.isEmpty()) {
            return "_" + language;
        }

        String variant = locale.getVariant();
        if (variant.isEmpty()) {
            return "_" + language + "_" + country;
        }

        return "_" + language + "_" + country + "_" + variant;
    }

    /**
     * Get localized resource.
     * <p>
     * This method follows the resource bundle lookup algorithm, starting to search from the most specific
     * resource name towards the general one, returning the first found valid URL.
     * <p>
     * Implementation note: The suffix is not determined using {@link Locale#toLanguageTag()} as that's not
     * how resource bundle lookup works. An example being the Indonesian locale for which {@code Locale.toLanguageTag()}
     * returns "id" whereas the bundle uses "in" as suffix.
     *
     * @param cls    the class for resource loading
     * @param name   the resource name
     * @param locale the locale
     * @return the URL of the resource
     * @throws MissingResourceException if no resource was found
     */
    public static URL getResourceURL(Class<?> cls, String name, Locale locale) {
        String basename = IoUtil.stripExtension(name);
        String extension = IoUtil.getExtension(name);

        // build the candidate list
        List<String> candidates = findCandidateResourceNames(locale, basename, extension);

        // try loading in reverse order
        for (int i = candidates.size() - 1; i >= 0; i--) {
            URL url = cls.getResource(candidates.get(i));
            if (url != null) {
                LOG.trace("requested resource '{}', localised resource found: {}", name, url);
                return url;
            }
        }

        // nothing found
        LOG.warn("resource '{}' not found. candidates: {}", name, candidates);
        throw new MissingResourceException("Resource not found: " + name, cls.getName(), name);
    }

    private static List<String> findCandidateResourceNames(Locale locale, String basename, String extension) {
        List<String> candidates = new ArrayList<>();

        String candidateName = basename;
        candidates.add(candidateName + "." + extension);

        String language = locale.getLanguage();
        if (!language.isEmpty()) {
            candidateName = candidateName + "_" + language;
            candidates.add(candidateName + "." + extension);

            String country = locale.getCountry();
            if (!country.isEmpty()) {
                candidateName = candidateName + "_" + country;
                candidates.add(candidateName + "." + extension);

                String variant = locale.getVariant();
                if (!variant.isEmpty()) {
                    candidateName = candidateName + "_" + variant;
                    candidates.add(candidateName + "." + extension);
                }
            }
        }
        return candidates;
    }

    /**
     * Filter surrounding items of a list.
     *
     * @param list   The unfiltered list
     * @param test   the predicate to use
     * @param before number of items to accept before each match
     * @param after  number of items to accept after each match
     * @param <T>    the element type
     * @return a list that contains all items within the given range before and after each match
     */
    public static <T extends @Nullable Object> List<T> surroundingItems(List<? extends T> list, Predicate<? super T> test, int before, int after) {
        return surroundingItemsInternal(list, test, before, after, null);
    }

    /**
     * Filter surrounding items of a list.
     *
     * @param list        The unfiltered list
     * @param test        the predicate to use
     * @param before      number of items to accept before each match
     * @param after       number of items to accept after each match
     * @param placeHolder generator for placeholder items; first argument is number of items replaced, second one is current item index in original list
     * @param <T>         the element type
     * @return a list that contains all items within the given range before and after each match
     */
    public static <T extends @Nullable Object> List<T> surroundingItems(List<? extends T> list, Predicate<? super T> test, int before, int after, BiFunction<? super Integer, ? super Integer, ? extends T> placeHolder) {
        return surroundingItemsInternal(list, test, before, after, placeHolder);
    }

    /**
     * Extracts specific items from the input list based on a given predicate and includes additional
     * items surrounding the matched elements within the specified range. This method supports optional
     * placeholders for omitted items in the result.
     *
     * @param <T> the type of elements in the list, which may be nullable
     * @param list the input list from which items will be filtered
     * @param test a predicate to determine the items of interest in the list
     * @param before the number of additional items to include before each matched element
     * @param after the number of additional items to include after each matched element
     * @param placeHolder an optional function to generate placeholder items for omitted sections;
     *                    takes the count of skipped elements and the starting index of those elements
     * @return a list containing the matched items along with the surrounding items and optional
     *         placeholders for omitted sections
     */
    private static <T extends @Nullable Object> List<T> surroundingItemsInternal(List<? extends T> list, Predicate<? super T> test, int before, int after, @Nullable BiFunction<? super Integer, ? super Integer, ? extends T> placeHolder) {
        List<T> filtered = new ArrayList<>();
        int lastIndex = -1;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            // find next difference
            while (i < size && !test.test(list.get(i))) {
                i++;
            }

            // not found
            if (i >= size) {
                break;
            }

            int startIndex = i;

            // add a placeholder if lines are omitted
            int count = startIndex - before - (lastIndex + 1);
            if (placeHolder != null && count > 0) {
                filtered.add(placeHolder.apply(count, lastIndex + 1));
            }

            // find end of difference
            while (i < size && test.test(list.get(i))) {
                i++;
            }
            int endIndex = i;

            // print changes
            int from = Math.max(startIndex - before, Math.max(0, lastIndex + 1));
            int to = Math.min(endIndex + after, size);

            filtered.addAll(list.subList(from, to));
            lastIndex = to - 1;
        }
        if (placeHolder != null && lastIndex < size - 1) {
            int count = size - (lastIndex + 1);
            filtered.add(placeHolder.apply(count, lastIndex + 1));
        }

        return filtered;
    }

    /**
     * Check if number is between two other numbers.
     *
     * @param x the number to test
     * @param a the lower bound
     * @param b the upper bound
     * @return true, exactly if a ≤ x and x ≤ b
     * @throws IllegalArgumentException if the intervall is invalid
     */
    public static boolean isBetween(long x, long a, long b) {
        if (a <= x && x <= b) {
            return true;
        }
        if (a <= b) {
            return false;
        }
        throw new IllegalArgumentException("invalid interval: a=" + a + ", b=" + b);
    }

    /**
     * Check if number is between two other numbers.
     *
     * @param x the number to test
     * @param a the lower bound
     * @param b the upper bound
     * @return true, exactly if a ≤ x and x ≤ b
     * @throws IllegalArgumentException if the intervall is invalid
     */
    public static boolean isBetween(double x, double a, double b) {
        if (a <= x && x <= b) {
            return true;
        }
        if (a <= b) {
            return false;
        }
        throw new IllegalArgumentException("invalid interval: a=" + a + ", b=" + b);
    }

    /**
     * Check if value of a {@link Comparable} is between two other values.
     *
     * @param <T> generic type of Comparable
     * @param x   the number to test
     * @param a   the lower bound
     * @param b   the upper bound
     * @return true, exactly if a.compareTo(x) ≤ 0 and x.compareTo(b) ≤ 0
     */
    public static <T extends Comparable<T>> boolean isBetween(T x, T a, T b) {
        if (a.compareTo(x) <= 0 && x.compareTo(b) <= 0) {
            return true;
        }
        if (a.compareTo(b) <= 0) {
            return false;
        }
        throw new IllegalArgumentException("invalid interval: a=" + a + ", b=" + b);
    }

    /**
     * Get stack trace as text
     *
     * @param e exception
     * @return the exception stack trace as text
     */
    public static String formatStackTrace(Exception e) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
             PrintStream s = new PrintStream(baos, true, StandardCharsets.UTF_8)) {
            e.printStackTrace(s);
            s.flush();
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "[error while formatting exception stack trace]";
        }
    }

    /**
     * Get default toString() for object.
     *
     * @param o the object
     * @return string generated like the default implementation of {@link Object#toString()} or "null" if o is null
     */
    public static String defaultToString(@Nullable Object o) {
        return o == null ? "null" : o.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(o));
    }

    /**
     * Select argument based on tri-state logic.
     *
     * @param b         the tristate {@link Boolean} argument
     * @param whenTrue  return value when b is true
     * @param whenFalse return value when b is false
     * @param otherwise return value when b is null
     * @param <T>       the generic argument type
     * @return one of the parameters whenTrue, whenFalse, otherwise depending on the value of b
     */
    public static <T extends @Nullable Object> T triStateSelect(@Nullable Boolean b, T whenTrue, T whenFalse, T otherwise) {
        if (b == null) {
            return otherwise;
        }
        return b ? whenTrue : whenFalse;
    }

    /**
     * Registers an object and a cleanup action to be executed when the object becomes phantom reachable.
     *
     * @param obj    the object to register for cleanup
     * @param action the cleanup action to be executed when the object becomes phantom reachable
     * @throws NullPointerException if {@code obj} or {@code action} is {@code null}
     */
    public static void registerForCleanup(Object obj, Runnable action) {
        getCleaner().register(obj, action);
    }

    /**
     * Returns the instance of the Cleaner class.
     *
     * @return the instance of the Cleaner class
     */
    public static Cleaner getCleaner() {
        return CLEANER;
    }

    /**
     * Determines if the given class `clsA` is a wrapper for the primitive class `clsB`.
     * This method checks if the `clsA` class is associated with the `TYPE` field pointing to the specified primitive type.
     *
     * @param clsA the class to check if it acts as a wrapper
     * @param clsB the primitive class to match against
     * @return true if `clsA` is a wrapper for the primitive class `clsB`, otherwise false
     */
    public static boolean isWrapperFor(Class<?> clsA, Class<?> clsB) {
        if (!clsB.isPrimitive()) {
            return false;
        }
        try {
            for (Field field : clsA.getDeclaredFields()) {
                if (field.getName().equals("TYPE") && field.get(null) == clsB) {
                    return true;
                }
            }
        } catch (IllegalAccessException e) {
            return false;
        }
        return false;
    }

    /**
     * Interface similar to {@link java.lang.Runnable} that declares thrown
     * exceptions on its {@code run()} method.
     *
     * @param <E> the exception type
     */
    @FunctionalInterface
    public interface RunnableThrows<E extends Exception> {
        /**
         * Equivalent to {@link Runnable#run()}, but may throw checked exceptions.
         *
         * @throws E depending on override
         */
        void run() throws E;
    }

    /**
     * Interface similar to {@link java.util.function.Function} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     * @param <R> the result type
     * @param <E> the exception type
     */
    @FunctionalInterface
    public interface FunctionThrows<T extends @Nullable Object, R extends @Nullable Object, E extends Exception> {
        /**
         * Applies this function to the given argument.
         *
         * @param t the function argument
         * @return the function result
         * @throws E depending on implementation
         */
        R apply(T t) throws E;
    }

    /**
     * Interface similar to {@link java.util.function.Consumer} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     * @param <E> the exception type
     */
    @FunctionalInterface
    public interface ConsumerThrows<T extends @Nullable Object, E extends @Nullable Exception> {
        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws E depending on implementation
         */
        void accept(@Nullable T t) throws E;

        /**
         * Returns a composed {@code Consumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing either
         * operation throws an exception, it is relayed to the caller of the
         * composed operation.  If performing this operation throws an exception,
         * the {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code Consumer} that performs in sequence this
         * operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         * @throws E                    depending on implementation
         */
        default ConsumerThrows<T, E> andThenTry(ConsumerThrows<? super T, ? extends E> after) throws E {
            return (T t) -> {
                accept(t);
                after.accept(t);
            };
        }

        /**
         * Returns a composed {@code Consumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing either
         * operation throws an exception, it is relayed to the caller of the
         * composed operation.  If performing this operation throws an exception,
         * the {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code Consumer} that performs in sequence this
         * operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         * @throws E                    depending on implementation
         */
        default ConsumerThrows<T, E> andThen(Consumer<? super T> after) throws E {
            return (T t) -> {
                accept(t);
                after.accept(t);
            };
        }
    }

    /**
     * Interface similar to {@link java.util.function.Supplier} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     * @param <E> the exception type
     */
    @FunctionalInterface
    public interface SupplierThrows<T extends @Nullable Object, E extends @Nullable Exception> {
        /**
         * Gets a result.
         *
         * @return a result
         * @throws E depending on implementation
         */
        T get() throws E;
    }

    /**
     * Interface AutoClosableSupplier, used in {@link #cache(Supplier, Consumer)}.
     *
     * @param <T> the base type
     */
    public interface AutoCloseableSupplier<T extends @Nullable Object> extends AutoCloseable, Supplier<T> {
        @Override
        void close();
    }

    /**
     * Exception derived from IllegalStateException thrown by
     * {@link LangUtil#check(boolean)}. The intent is to make it possible to
     * distinguish failed checks from other IllegalStateExceptions in try-blocks.
     */
    public static class FailedCheckException extends IllegalStateException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Default constructor.
         */
        public FailedCheckException() {
        }

        /**
         * Constructor.
         *
         * @param msg exception message
         */
        public FailedCheckException(String msg) {
            super(msg);
        }

        /**
         * Constructor.
         *
         * @param cause causing exception
         */
        public FailedCheckException(Throwable cause) {
            super(cause);
        }

        /**
         * Constructor.
         *
         * @param msg   exception message
         * @param cause causing exception
         */
        public FailedCheckException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private static class CachingSupplier<T extends @Nullable Object> implements AutoCloseableSupplier<T> {
        private final Supplier<? extends T> supplier;
        private final Consumer<? super T> cleaner;
        private @Nullable T obj;
        private boolean initialized;

        CachingSupplier(Supplier<? extends T> supplier, Consumer<? super T> cleaner) {
            this.supplier = supplier;
            this.cleaner = cleaner;
        }

        @Override
        public T get() {
            if (!initialized) {
                obj = supplier.get();
                initialized = true;
            }

            return obj;
        }

        @Override
        public void close() {
            if (initialized) {
                cleaner.accept(obj);
                obj = null;
                initialized = false;
            }
        }
    }

    /**
     * Decorate a {@code Map<T,U>} as a {@code Function<T,U>}.
     *
     * @param map the Map instance
     * @param <T> the map's key type
     * @param <U> the map's value typu
     * @return a function that returns the mapping of its input value
     */
    public static <T, U extends @Nullable Object> Function<T, U> asFunction(Map<? super T, ? extends U> map) {
        return map::get;
    }

    /**
     * Decorate a {@code Map<T,U>} as a {@code Function<T,U>}.
     *
     * @param map          the Map instance
     * @param defaultValue the value to use when there is no mapping for the argument
     * @param <T>          the map's key type
     * @param <U>          the map's value type
     * @param <V>          the type of the default value
     * @return a function that returns the mapping of its input value or {@code defaultValue} if no mapping exists
     */
    public static <T, U, V extends U> Function<@Nullable T, @Nullable U> asFunction(Map<? super T, @Nullable V> map, V defaultValue) {
        return (T t) -> map.getOrDefault(t, defaultValue);
    }

    /**
     * The LazyFormatter class provides a lazy evaluation implementation of string formatting.
     * It allows delayed formatting of a string with a given set of arguments.
     * The formatting is done the first time {@code toString()} is called.
     * Subsequent calls of {@code toString()} will return the same object.
     */
    private static final class LazyFormatter {
        public static final String NULL_STRING = String.valueOf((Object) null);

        private volatile @Nullable String s;
        private volatile @Nullable Object @Nullable [] args;

        private LazyFormatter(@Nullable String fmt, Object... fmtArgs) {
            assert fmt == null || isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
            if (fmt == null) {
                this.s = NULL_STRING;
                this.args = null;
            } else {
                this.s = fmt;
                this.args = fmtArgs;
            }
        }

        @SuppressWarnings("DataFlowIssue") // false positive
        @Override
        public String toString() {
            if (args != null) {
                synchronized (this) {
                    if (args != null) {
                        assert s != null;
                        s = s.formatted(args);
                        args = null;
                    }
                }
            }
            return s;
        }
    }

    /**
     * Creates a lazy formatted string using the given format string and arguments.
     *
     * @param fmt   the format string
     * @param fmtArgs  the arguments to be formatted
     * @return a lazy formatter object
     */
    public static Object formatLazy(@Nullable String fmt, Object... fmtArgs) {
        return new LazyFormatter(fmt, fmtArgs);
    }

    /**
     * Wraps a supplier of a string to defer execution of the string creation
     * until its actual usage. This can be useful in scenarios where the string
     * construction is expensive and may not always be needed.
     *
     * @param supplier a supplier that provides the string lazily when requested
     * @return an object that defers the evaluation of the supplier until its string representation is required
     */
    public static Supplier<String> cachingStringSupplier(Supplier<String> supplier) {
        return supplier instanceof CachingStringSupplier cs ? cs : new CachingStringSupplier(supplier);
    }

    /**
     * Returns the given value if it is non-negative, otherwise throws an IllegalArgumentException with a specified error message.
     *
     * @param value the value to check
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or {@link Double#NaN}
     */
    public static double requireNonNegative(double value) {
        return requireNonNegative(value, "value is negative: %f", value);
    }

    /**
     * Ensures that a given long value is non-negative. If the value is negative, an {@link IllegalArgumentException}
     * is thrown with the specified format string and arguments.
     *
     * @param value the long value to ensure non-negativity
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or {@link Double#NaN}
     */
    public static double requireNonNegative(double value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value >= 0.0) {
            return value;
        }
        throw new IllegalArgumentException(fmt.formatted(fmtArgs));
    }

    /**
     * Returns the given value if it is non-negative, otherwise throws an IllegalArgumentException with a specified error message.
     *
     * @param value the value to check
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static float requireNonNegative(float value) {
        return requireNonNegative(value, "value is negative: %f", value);
    }

    /**
     * Ensures that a given long value is non-negative. If the value is negative, an {@link IllegalArgumentException}
     * is thrown with the specified format string and arguments.
     *
     * @param value the long value to ensure non-negativity
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static float requireNonNegative(float value, String fmt, Object... fmtArgs) {
        return (float) requireNonNegative((double) value, fmt, fmtArgs);
    }

    /**
     * Returns the given value if it is non-negative, otherwise throws an IllegalArgumentException with a specified error message.
     *
     * @param value the value to check
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static long requireNonNegative(long value) {
        return requireNonNegative(value, "value is negative: %d", value);
    }

    /**
     * Ensures that a given long value is non-negative. If the value is negative, an {@link IllegalArgumentException}
     * is thrown with the specified format string and arguments.
     *
     * @param value the long value to ensure non-negativity
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static long requireNonNegative(long value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value < 0) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
        return value;
    }

    /**
     * Checks if the given value is non-negative.
     *
     * @param value the value to check for non-negativity
     * @return the given value
     * @throws IllegalArgumentException if the given value is negative
     */
    public static int requireNonNegative(int value) {
        return (int) requireNonNegative((long) value);
    }

    /**
     * Checks if the given value is a non-negative integer.
     *
     * @param value The value to be checked.
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static int requireNonNegative(int value, String fmt, Object... fmtArgs) {
        return (int) requireNonNegative((long) value, fmt, fmtArgs);
    }

    /**
     * Checks if the given value is non-negative.
     *
     * @param value the value to check for non-negativity
     * @return the given value
     * @throws IllegalArgumentException if the given value is negative
     */
    public static short requireNonNegative(short value) {
        return (short) requireNonNegative((long) value);
    }

    /**
     * Checks if the given value is a non-negative integer.
     *
     * @param value The value to be checked.
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative
     */
    public static short requireNonNegative(short value, String fmt, Object... fmtArgs) {
        return (short) requireNonNegative((long) value, fmt, fmtArgs);
    }

    /**
     * Returns the specified value if it is greater than 0. Otherwise, throws an IllegalArgumentException with a specific error message.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static double requirePositive(double value) {
        return requirePositive(value, "value must be greater than 0: %f", value);
    }

    /**
     * Checks if the given value is positive. If it is not, throws an {@link IllegalArgumentException} with a formatted
     * error message using the provided format string and arguments.
     *
     * @param value the value to check if it is positive
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static double requirePositive(double value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value > 0.0) {
            return value;
        }
        throw new IllegalArgumentException(fmt.formatted(fmtArgs));
    }

    /**
     * Returns the specified value if it is greater than 0. Otherwise, throws an IllegalArgumentException with a specific error message.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static float requirePositive(float value) {
        return requirePositive(value, "value must be greater than 0: %f", value);
    }

    /**
     * Checks if the given value is positive. If it is not, throws an {@link IllegalArgumentException} with a formatted
     * error message using the provided format string and arguments.
     *
     * @param value the value to check if it is positive
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static float requirePositive(float value, String fmt, Object... fmtArgs) {
        return (float) requirePositive((double) value, fmt, fmtArgs);
    }

    /**
     * Returns the specified value if it is greater than 0. Otherwise, throws an IllegalArgumentException with a specific error message.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static long requirePositive(long value) {
        return requirePositive(value, "value must be greater than 0: %d", value);
    }

    /**
     * Checks if the given value is positive. If it is not, throws an {@link IllegalArgumentException} with a formatted
     * error message using the provided format string and arguments.
     *
     * @param value the value to check if it is positive
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static long requirePositive(long value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value <= 0) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
        return value;
    }

    /**
     * Returns the given value if it is a positive integer. If the value is negative or zero,
     * it throws an IllegalArgumentException.
     *
     * @param value the integer value to check
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static int requirePositive(int value) {
        return (int) requirePositive((long) value);
    }

    /**
     * Returns the absolute value of the given integer if it is positive.
     * If the value is negative, it throws an IllegalArgumentException with a specified error message formatted using the given format string and arguments.
     *
     * @param value The integer value to be checked.
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static int requirePositive(int value, String fmt, Object... fmtArgs) {
        return (int) requirePositive((long) value, fmt, fmtArgs);
    }

    /**
     * Returns the given value if it is a positive integer. If the value is negative or zero,
     * it throws an IllegalArgumentException.
     *
     * @param value the integer value to check
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static short requirePositive(short value) {
        return (short) requirePositive((long) value);
    }

    /**
     * Returns the absolute value of the given integer if it is positive.
     * If the value is negative, it throws an IllegalArgumentException with a specified error message formatted using the given format string and arguments.
     *
     * @param value The integer value to be checked.
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is negative or zero
     */
    public static short requirePositive(short value, String fmt, Object... fmtArgs) {
        return (short) requirePositive((long) value, fmt, fmtArgs);
    }

    /**
     * Checks if the given value is a negative number.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static double requireNegative(double value) {
        return requireNegative(value, "value must be less than 0: %f", value);
    }

    /**
     * Throws an IllegalArgumentException if the specified value is not negative.
     *
     * @param value the value to check
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static double requireNegative(double value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value < 0.0) {
            return value;
        }
        throw new IllegalArgumentException(fmt.formatted(fmtArgs));
    }

    /**
     * Checks if the given value is a negative number.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static float requireNegative(float value) {
        return requireNegative(value, "value must be less than 0: %f", value);
    }

    /**
     * Throws an IllegalArgumentException if the specified value is not negative.
     *
     * @param value the value to check
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static float requireNegative(float value, String fmt, Object... fmtArgs) {
        return (float) requireNegative((double) value, fmt, fmtArgs);
    }

    /**
     * Checks if the given value is a negative number.
     *
     * @param value the value to be checked
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static long requireNegative(long value) {
        return requireNegative(value, "value must be less than 0: %d", value);
    }

    /**
     * Throws an IllegalArgumentException if the specified value is not negative.
     *
     * @param value the value to check
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static long requireNegative(long value, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (value >= 0) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
        return value;
    }

    /**
     * Returns the negative value of the given integer value. If the value is already negative, then it is returned as is.
     *
     * @param value the integer value to be checked and converted to negative
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static int requireNegative(int value) {
        return (int) requireNegative((long) value);
    }

    /**
     * Returns the negative value of the given integer if it is not already negative.
     * Otherwise, it returns the same value. The optional format string and arguments
     * can be used to specify a message if the value is not negative.
     *
     * @param value the integer value to be checked
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static int requireNegative(int value, String fmt, Object... fmtArgs) {
        return (int) requireNegative((long) value, fmt, fmtArgs);
    }

    /**
     * Returns the negative value of the given integer value. If the value is already negative, then it is returned as is.
     *
     * @param value the integer value to be checked and converted to negative
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static short requireNegative(short value) {
        return (short) requireNegative((long) value);
    }

    /**
     * Returns the negative value of the given integer if it is not already negative.
     * Otherwise, it returns the same value. The optional format string and arguments
     * can be used to specify a message if the value is not negative.
     *
     * @param value the integer value to be checked
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not negative
     */
    public static short requireNegative(short value, String fmt, Object... fmtArgs) {
        return (short) requireNegative((long) value, fmt, fmtArgs);
    }

    /**
     * Checks if a given value is within a specified interval.
     *
     * @param value the value to be checked
     * @param min the minimum value of the interval (inclusive)
     * @param max the maximum value of the interval (inclusive)
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the specified interval
     */
    public static double requireInInterval(double value, double min, double max) {
        return requireInInterval(value, min, max, "value must be between %f and %f: %f", min, max, value);
    }

    /**
     * Checks if the given value is within the specified interval and throws an exception if it is not.
     *
     * @param value the value to check
     * @param min   the minimum value of the interval (inclusive)
     * @param max   the maximum value of the interval (inclusive)
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the interval
     */
    public static double requireInInterval(double value, double min, double max, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (!isBetween(value, min, max)) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
        return value;
    }

    /**
     * Checks if a given value is within a specified interval.
     *
     * @param value the value to be checked
     * @param min the minimum value of the interval (inclusive)
     * @param max the maximum value of the interval (inclusive)
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the specified interval
     */
    public static float requireInInterval(float value, float min, float max) {
        return requireInInterval(value, min, max, "value must be between %f and %f: %f", min, max, value);
    }

    /**
     * Checks if the given value is within the specified interval and throws an exception if it is not.
     *
     * @param value the value to check
     * @param min   the minimum value of the interval (inclusive)
     * @param max   the maximum value of the interval (inclusive)
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the interval
     */
    public static float requireInInterval(float value, float min, float max, String fmt, Object... fmtArgs) {
        return (float) requireInInterval(value, min, (double) max, fmt, fmtArgs);
    }

    /**
     * Checks if a given value is within a specified interval.
     *
     * @param value the value to be checked
     * @param min the minimum value of the interval (inclusive)
     * @param max the maximum value of the interval (inclusive)
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the specified interval
     */
    public static long requireInInterval(long value, long min, long max) {
        return requireInInterval(value, min, max, "value must be between %d and %d: %d", min, max, value);
    }

    /**
     * Checks if the given value is within the specified interval and throws an exception if it is not.
     *
     * @param value the value to check
     * @param min   the minimum value of the interval (inclusive)
     * @param max   the maximum value of the interval (inclusive)
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is not within the interval
     */
    public static long requireInInterval(long value, long min, long max, String fmt, Object... fmtArgs) {
        assert isFormatValid(fmt, fmtArgs) : INVALID_FORMATTING;
        if (!isBetween(value, min, max)) {
            throw new IllegalArgumentException(fmt.formatted(fmtArgs));
        }
        return value;
    }

    /**
     * Checks if the given value is within the specified interval.
     *
     * @param value the value to be checked
     * @param min   the minimum value of the interval (inclusive)
     * @param max   the maximum value of the interval (inclusive)
     * @return the given value
     * @throws IllegalArgumentException if the value is outside the interval
     */
    public static int requireInInterval(int value, int min, int max) {
        return (int) requireInInterval(value, min, (long) max);
    }

    /**
     * Ensures that the given value is within the specified interval. If the value is outside the interval, an exception
     * is thrown with a formatted error message.
     *
     * @param value the value to check
     * @param min   the minimum value in the interval (inclusive)
     * @param max   the maximum value in the interval (inclusive)
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is outside the interval
     */
    public static int requireInInterval(int value, int min, int max, String fmt, Object... fmtArgs) {
        return (int) requireInInterval(value, min, (long) max, fmt, fmtArgs);
    }

    /**
     * Checks if the given value is within the specified interval.
     *
     * @param value the value to be checked
     * @param min   the minimum value of the interval (inclusive)
     * @param max   the maximum value of the interval (inclusive)
     * @return the given value
     * @throws IllegalArgumentException if the value is outside the interval
     */
    public static short requireInInterval(short value, short min, short max) {
        return (short) requireInInterval(value, min, (long) max);
    }

    /**
     * Ensures that the given value is within the specified interval. If the value is outside the interval, an exception
     * is thrown with a formatted error message.
     *
     * @param value the value to check
     * @param min   the minimum value in the interval (inclusive)
     * @param max   the maximum value in the interval (inclusive)
     * @param fmt   the format string for the error message
     * @param fmtArgs  the arguments to be formatted in the error message
     * @return the given value
     * @throws IllegalArgumentException if the value is outside the interval
     */
    public static short requireInInterval(short value, short min, short max, String fmt, Object... fmtArgs) {
        return (short) requireInInterval(value, min, (long) max, fmt, fmtArgs);
    }

    /**
     * A constant representing an empty byte array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = {};
    /**
     * A constant representing an empty array of characters.
     */
    public static final char[] EMPTY_CHAR_ARRAY = {};
    /**
     * A constant representing an empty array of type {@code short}.
     */
    public static final short[] EMPTY_SHORT_ARRAY = {};
    /**
     * An immutable empty array of integers.
     */
    public static final int[] EMPTY_INT_ARRAY = {};
    /**
     * Represents a statically defined empty array of long type.
     */
    public static final long[] EMPTY_LONG_ARRAY = {};
    /**
     * A constant representing an empty array of floats.
     */
    public static final float[] EMPTY_FLOAT_ARRAY = {};
    /**
     * A constant representing an empty array of doubles.
     */
    public static final double[] EMPTY_DOUBLE_ARRAY = {};

    /**
     * Returns an unmodifiable {@link ImmutableSortedListSet} containing the specified elements.
     * The elements are sorted according to their natural ordering as defined by
     * the {@link Comparable} interface.
     * <p>
     * The returned instance implements both the {@link List} and {@link SortedSet} interfaces.
     * It uses less memory than a {@link java.util.TreeSet}.
     *
     * @param <T> the type of elements that extends {@link Comparable} to ensure natural ordering
     * @param elements the elements to be included in the unmodifiable sorted set
     * @return an unmodifiable {@link SortedSet} containing the specified elements, sorted in their natural order
     */
    @SafeVarargs
    public static <T extends Comparable<T>> ImmutableSortedListSet<T> asUnmodifiableSortedListSet(T... elements) {
        return ImmutableListBackedSortedSet.ofNaturalOrder(elements);
    }

    /**
     * Converts the given {@code Properties} object into an unmodifiable {@code Map<String, String>}.
     *
     * @param properties the properties object to be converted; must not be null
     * @return an unmodifiable map containing the same entries as the provided properties object
     */
    public static Map<String, String> asUnmodifiableMap(Properties properties) {
        Map<String, String> map = LinkedHashMap.newLinkedHashMap(properties.size());
        properties.forEach((k, v) -> map.put(String.valueOf(k), String.valueOf(v)));
        return Collections.unmodifiableMap(map);
    }

    /**
     * Determines if the given object is of a known immutable type.
     * Immutable types are types whose state cannot be modified after they are created.
     *
     * @param obj the object to evaluate, which may be null
     * @return true if the object is of a known immutable type, false otherwise
     */
    public static boolean isOfKnownImmutableType(@Nullable Object obj) {
        return switch (obj) {
            case Character ignored -> true;
            case String ignored -> true;
            case Number ignored -> true;
            case Boolean ignored -> true;
            case LocalDateTime ignored -> true;
            case LocalDate ignored -> true;
            case LocalTime ignored -> true;
            case Duration ignored -> true;
            case Instant ignored -> true;
            case OffsetDateTime ignored -> true;
            case OffsetTime ignored -> true;
            case DateTimeFormatter ignored -> true;
            case Period ignored -> true;
            case ZonedDateTime ignored -> true;
            case UUID ignored -> true;
            case Enum<?> ignored -> true;
            case Color ignored -> true;
            case FontData ignored -> true;
            case Font ignored -> true;
            case Path ignored -> true;
            case URI ignored -> true;
            case Locale ignored -> true;
            case Pattern ignored -> true;
            case null, default -> false;
        };
    }

    /**
     * Removes trailing elements from the provided list that match the given predicate.
     *
     * @param <T> the type of the elements in the list
     * @param list the list from which trailing elements will be removed
     * @param predicate a predicate used to test elements; elements matching this predicate will be removed from the end of the list
     */
    public static <T> void removeTrailing(List<T> list, Predicate<? super T> predicate) {
        int i = list.size() - 1;
        while (i >= 0 && predicate.test(list.get(i))) {
            i--;
        }
        list.subList(i + 1, list.size()).clear();
    }

    /**
     * Removes leading elements from the list that satisfy the given predicate.
     * Iterates through the list from the start and removes all consecutive elements
     * that match the predicate until an element does not satisfy the condition.
     *
     * @param <T> the type of elements in the list
     * @param list the list from which leading elements are to be removed
     * @param predicate the predicate used to test each element for removal
     */
    public static <T> void removeLeading(List<T> list, Predicate<? super T> predicate) {
        int i = 0;
        while (i < list.size() && predicate.test(list.get(i))) {
            i++;
        }
        list.subList(0, i).clear();
    }

    /**
     * Removes elements from the beginning and the end of the specified list
     * that match the given predicate.
     *
     * @param list the list from which leading and trailing elements will be removed
     * @param predicate the condition used to determine which elements should be removed
     * @param <T> the type of elements in the list
     */
    public static <T> void removeLeadingAndTrailing(List<T> list, Predicate<? super T> predicate) {
        removeLeading(list, predicate);
        removeTrailing(list, predicate);
    }

    /**
     * Reverses the elements of the given array in place. The first element swaps with
     * the last, the second element swaps with the second-to-last, and so on, until
     * the entire array is reversed.
     *
     * @param <T> The type of the elements in the array.
     * @param array The array whose elements will be reversed. This array is modified in place.
     */
    public static <T> void reverseInPlace(T[] array) {
        reverseInPlace(array, 0, array.length);
    }

    /**
     * Reverses the elements of the specified array within the given range in place.
     *
     * @param <T>   the type of the array elements
     * @param array the array whose elements are to be reversed
     * @param from  the starting index of the range, inclusive
     * @param to    the ending index of the range, exclusive; must be greater than or equal to {@code from}
     *              and less than or equal to the array length
     * @throws IllegalArgumentException if the specified range is invalid (i.e., {@code from < 0}, {@code from > to},
     *                                  or {@code to > array.length})
     */
    public static <T> void reverseInPlace(T[] array, int from, int to) {
        check(
                from >= 0 && from <= to && to <= array.length,
                () -> new IllegalArgumentException("invalid range: from=%d, to=%d, length=%d".formatted(from, to, array.length))
        );

        int i = from;
        int j = to - 1;
        T tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }

    }

    /**
     * Generates a new UUID version 7 (UUIDv7) according to RFC 9562.
     * UUIDv7 is a time-ordered UUID format that uses Unix timestamp with millisecond precision
     * in the most significant 48 bits, followed by a random component.
     *
     * @return a new UUIDv7 with the current timestamp
     */
    public static UUID newUuidV7() {
        return newUuidV7(System.currentTimeMillis());
    }

    /**
     * Generates a new UUID version 7 (UUIDv7) according to RFC 9562 with the specified timestamp.
     * UUIDv7 is a time-ordered UUID format that uses Unix timestamp with millisecond precision
     * in the most significant 48 bits, followed by a random component.
     *
     * @param timestamp the timestamp to use for the UUID
     * @return a new UUIDv7 with the specified timestamp
     */
    public static UUID newUuidV7(Instant timestamp) {
        return newUuidV7(timestamp.toEpochMilli());
    }

    /**
     * Generates a new UUIDv7 based on the provided Unix timestamp in milliseconds.
     * UUIDv7 incorporates a 48-bit timestamp derived from Unix epoch time, ensuring
     * temporal ordering when UUIDs are generated.
     *
     * @param unixTimestampMs the Unix timestamp in milliseconds to be embedded into the UUID.
     *                        It must not exceed the 48-bit limit (up to 281474976710655 milliseconds since epoch).
     *                        If the value exceeds the permissible range, an {@link IllegalArgumentException} is thrown.
     * @return a new UUIDv7 with the specified timestamp and randomly generated portions for
     *         uniqueness and compliance with the UUIDv7 specification.
     * @throws IllegalArgumentException if the provided timestamp exceeds the allowable 48-bit limit.
     */
    public static UUID newUuidV7(long unixTimestampMs) {
        if ((unixTimestampMs & ~0xFFFFFFFFFFFFL) != 0) {
            throw new IllegalArgumentException("Timestamp exceeds 48-bit limit for UUIDv7: " + unixTimestampMs);
        }

        long version = 0x7L << 12;
        long rand12 = secureRandom().nextLong(0x1000L);
        long msb = (unixTimestampMs << 16) | version | rand12;

        long rand62 = secureRandom().nextLong(1L << 62);
        long lsb = (1L << 63) | rand62; // Variant '10' + 62 random bits

        return new UUID(msb, lsb);
    }

    /**
     * Extracts the raw timestamp value from the given UUID.
     * <p>
     * For UUIDv7, this returns the 48-bit Unix timestamp in milliseconds since the epoch (1970-01-01T00:00:00Z).
     * For UUIDv1, this returns the 60-bit timestamp in 100-nanosecond intervals since the UUID epoch
     * (1582-10-15T00:00:00Z), as defined by RFC 4122.
     *
     * @param uuid the UUID from which to extract the raw timestamp
     * @return the raw timestamp value encoded in the UUID
     * @throws UnsupportedOperationException if the UUID version does not include a timestamp (e.g. v4, v8)
     */
    public static long getTimestampRaw(UUID uuid) {
        if (uuid.version() == 7) {
            return (uuid.getMostSignificantBits() >>> 16) & 0xFFFFFFFFFFFFL;
        } else {
            return uuid.timestamp();
        }
    }

    /**
     * Converts the timestamp in the given UUID to a {@link Instant}.
     * <p>
     * For UUIDv7, this returns an {@code Instant} from the embedded Unix millisecond timestamp.
     * For UUIDv1, this returns an {@code Instant} converted from 100-nanosecond intervals since
     * the UUID epoch (1582-10-15T00:00:00Z).
     *
     * @param uuid the UUID containing a timestamp
     * @return the extracted timestamp as an {@link Instant}
     * @throws UnsupportedOperationException if the UUID version does not support timestamp extraction (e.g. v4, v8)
     */
    public static Instant getTimestampAsInstant(UUID uuid) {
        switch (uuid.version()) {
            case 7: {
                long epochMillis = getTimestampRaw(uuid);
                return Instant.ofEpochMilli(epochMillis);
            }
            case 1: {
                long timestamp100ns = getTimestampRaw(uuid);
                long uuidEpochMillis = -12219292800000L; // 1582-10-15T00:00:00Z
                long millisSinceEpoch = timestamp100ns / 10_000;
                long nanosRemainder = (timestamp100ns % 10_000) * 100;
                return Instant.ofEpochSecond(
                        (uuidEpochMillis + millisSinceEpoch) / 1000,
                        ((uuidEpochMillis + millisSinceEpoch) % 1000) * 1_000_000 + nanosRemainder
                );
            }
            default:
                throw new UnsupportedOperationException("Only UUID versions 1 and 7 support timestamps");
        }
    }

    /**
     * Converts the given UUID to a byte array representation.
     *
     * @param uuid the UUID to be converted to a byte array
     * @return a byte array representing the UUID
     */
    public static byte[] toByteArray(UUID uuid) {
        byte[] bytes = new byte[16];
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        // Store most significant bits (first 8 bytes)
        bytes[0] = (byte) (mostSigBits >>> 56);
        bytes[1] = (byte) (mostSigBits >>> 48);
        bytes[2] = (byte) (mostSigBits >>> 40);
        bytes[3] = (byte) (mostSigBits >>> 32);
        bytes[4] = (byte) (mostSigBits >>> 24);
        bytes[5] = (byte) (mostSigBits >>> 16);
        bytes[6] = (byte) (mostSigBits >>> 8);
        bytes[7] = (byte) mostSigBits;

        // Store least significant bits (last 8 bytes)
        bytes[8] = (byte) (leastSigBits >>> 56);
        bytes[9] = (byte) (leastSigBits >>> 48);
        bytes[10] = (byte) (leastSigBits >>> 40);
        bytes[11] = (byte) (leastSigBits >>> 32);
        bytes[12] = (byte) (leastSigBits >>> 24);
        bytes[13] = (byte) (leastSigBits >>> 16);
        bytes[14] = (byte) (leastSigBits >>> 8);
        bytes[15] = (byte) leastSigBits;

        return bytes;
    }

    /**
     * Converts a 16-byte array into a {@code UUID} instance.
     *
     * @param bytes a byte array representing the {@code UUID}.
     *              The array must be exactly 16 bytes in length.
     * @return a {@code UUID} created from the byte array.
     * @throws IllegalArgumentException if the provided byte array is not 16 bytes long.
     */
    public static UUID fromByteArray(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be 16 bytes long");
        }

        // Reconstruct most significant bits from first 8 bytes
        long mostSigBits = ((long) bytes[0] << 56) |
                ((long) (bytes[1] & 0xFF) << 48) |
                ((long) (bytes[2] & 0xFF) << 40) |
                ((long) (bytes[3] & 0xFF) << 32) |
                ((long) (bytes[4] & 0xFF) << 24) |
                ((long) (bytes[5] & 0xFF) << 16) |
                ((long) (bytes[6] & 0xFF) << 8) |
                (bytes[7] & 0xFF);

        // Reconstruct least significant bits from last 8 bytes
        long leastSigBits = ((long) bytes[8] << 56) |
                ((long) (bytes[9] & 0xFF) << 48) |
                ((long) (bytes[10] & 0xFF) << 40) |
                ((long) (bytes[11] & 0xFF) << 32) |
                ((long) (bytes[12] & 0xFF) << 24) |
                ((long) (bytes[13] & 0xFF) << 16) |
                ((long) (bytes[14] & 0xFF) << 8) |
                (bytes[15] & 0xFF);

        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Applies the given mapping function to the provided value if it is not null.
     * If the value is null, returns null directly.
     *
     * @param <T> the type of the input value
     * @param <U> the type of the result produced by the mapping function
     * @param value the input value to be mapped, can be null
     * @param mapper the function to apply to the input value, must not be null
     * @return the result of applying the mapper function to the value, or null if the value is null
     */
    public static <T, U extends @Nullable Object> U mapNonNull(@Nullable T value, Function<T, U> mapper) {
        return value == null ? null : mapper.apply(value);
    }

    /**
     * Applies the given mapping function to the provided non-null value, returning the result,
     * or returns a default value if the input is null.
     *
     * @param <T> the type of the input value
     * @param <U> the type of the output value
     * @param value the input value to map, which may be null
     * @param mapper the function used to transform the non-null input value
     * @param ifNull the default value to return if the input value is null
     * @return the result of applying the mapper function to the input value
     *         if it is non-null; otherwise, the default value
     */
    public static <T, U> U mapNonNullOrElse(@Nullable T value, Function<T, U> mapper, U ifNull) {
        return value == null ? ifNull : mapper.apply(value);
    }

    /**
     * Maps a non-null value using the provided mapper function, or supplies a default value
     * if the input value is null.
     *
     * @param <T> the type of the input value
     * @param <U> the type of the output value
     * @param value the input value which may be null
     * @param mapper the function to map the input value if it is non-null
     * @param ifNull the supplier to provide a value if the input value is null
     * @return the result of applying the mapper function to the input value if it is non-null,
     *         or the value provided by the supplier if the input value is null
     */
    public static <T, U> U mapNonNullElseGet(@Nullable T value, Function<T, U> mapper, Supplier<U> ifNull) {
        return value == null ? ifNull.get() : mapper.apply(value);
    }

    /**
     * Returns a comparator that compares {@link Comparable} objects in their natural order.
     * The returned comparator is null-friendly and considers null to be less than non-null elements.
     *
     * @param <T> the type of elements to be compared, which must implement {@link Comparable}
     * @return a comparator that compares objects in their natural order
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<@Nullable T> naturalOrder() {
        return (Comparator<@Nullable T>) NullableNaturalOrderComparator.INSTANCE;
    }

    /**
     * Returns the comparator or if the given comparator is null, return a natural order comparator.
     *
     * @param <T> the type of the elements compared by the comparator
     * @param comparator the comparator to use; if null, a natural order comparator is returned
     * @return a comparator that is either the provided comparator or a natural order comparator
     *         if the provided comparator is null
     */
    @SuppressWarnings("unchecked")
    public static <T> Comparator<@Nullable T> orNaturalOrder(@Nullable Comparator<T> comparator) {
        return Objects.requireNonNullElse(comparator, (Comparator<T>) NullableNaturalOrderComparator.INSTANCE);
    }

    /**
     * A comparator that provides natural ordering for {@link Comparable} objects,
     * with support for null values.
     * <p>
     * Objects are compared using their {@code compareTo} method when both are non-null.
     * Null values are considered less than any non-null values.
     */
    private static final class NullableNaturalOrderComparator implements Comparator<Comparable<Object>> {
        private static final NullableNaturalOrderComparator INSTANCE = new NullableNaturalOrderComparator();

        @Override
        public int compare(@Nullable Comparable<Object> a, @Nullable Comparable<Object> b) {
            if (a == null) {
                return b == null ? 0 : -1;
            } else if (b == null) {
                return 1;
            } else {
                return a.compareTo(b);
            }
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return obj != null && obj.getClass() == getClass();
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }
    }

    /**
     * Checks if the given comparator is either null or represents the natural ordering.
     *
     * @param comparator the comparator to check, which can be null
     * @return true if the comparator is null or represents natural order, false otherwise
     */
    public static boolean isNaturalOrder(@Nullable Comparator<?> comparator) {
        return comparator == null || comparator == Comparator.naturalOrder() || comparator == NullableNaturalOrderComparator.INSTANCE;
    }

    /**
     * Compares two keys using the provided comparator, or the natural order if the comparator is null.
     *
     * @param <T> the type of the keys being compared
     * @param comparator the comparator to determine the order of the keys; if null, the natural order is used
     * @param a the first key to compare
     * @param b the second key to compare
     * @return a negative integer, zero, or a positive integer as the first key is less than, equal to,
     *         or greater than the second key
     */
    public static <T extends @Nullable Object> int compare(@Nullable Comparator<T> comparator, T a, T b) {
        return orNaturalOrder(comparator).compare(a, b);
    }

    /**
     * Retrieves the value for the specified key from the provided map if it exists and is nnon-null.
     * If no value is found for the key or the value is null, a {@link NoSuchElementException} is thrown.
     *
     * @param <T> the map's key type
     * @param <U> the map's value type
     * @param map the map to search for the key-value pair
     * @param key the key whose presence and associated value are to be checked
     * @return the key if a value is successfully associated with it in the map
     * @throws NoSuchElementException if no value is found for the specified key in the map
     */
    public static <T, U> U getOrThrow(Map<T, U> map, T key) {
        U value = map.get(key);
        if (value == null) {
            throw new NoSuchElementException("no value for key: " + key);
        }
        return value;
    }

    /**
     * Retrieves the value for the specified key from the provided map if it exists and is nnon-null.
     * If no value is found for the key or the value is null, a {@link NoSuchElementException} is thrown.
     *
     * @param <T> the map's key type
     * @param <U> the map's value type
     * @param <E> the exception type
     * @param map the map to search for the key-value pair
     * @param key the key whose presence and associated value are to be checked
     * @param exceptionBuilder builder for exceptions; the key is passed as argument
     * @return the key if a value is successfully associated with it in the map
     * @throws E if no value is found for the specified key in the map
     */
    public static <T, U, E extends Exception> U getOrThrow(
            Map<T, U> map,
            T key,
            Function<T, E> exceptionBuilder) throws E {
        U value = map.get(key);
        if (value == null) {
            throw exceptionBuilder.apply(key);
        }
        return value;
    }

    /**
     * Adds the specified items to the collection if the given condition is true.
     * If the condition is false, no items will be added.
     *
     * @param <T>        the type of elements that can be added to the collection
     * @param condition  the condition that determines whether the items will be added
     * @param collection the collection to which the items will be added if the condition is true
     * @param items      the items to be added to the collection if the condition is true
     * @return {@code true} if elements were added to the collection, {@code  false} otherwise
     */
    @SafeVarargs
    public static <T extends @Nullable Object> boolean addIf(boolean condition, Collection<? super T> collection, T... items) {
        if (!condition || items.length == 0) {
            return false;
        }
        if (items.length == 1) {
            collection.add(items[0]);
        } else {
            collection.addAll(asUnmodifiableList(items));
        }
        return true;
    }

    /**
     * Adds the specified items to a collection if they satisfy the given predicate.
     * This method evaluates each item using the predicate and adds it to the collection
     * only if the predicate returns true.
     * <p>
     * The method returns true if at least one item is successfully added to the collection,
     * otherwise returns false.
     *
     * @param <T> the type of elements handled by the predicate and collection
     * @param predicate a predicate to test each item before adding it to the collection
     * @param collection the collection to which items should be added if they satisfy the predicate
     * @param items the items to be evaluated and potentially added to the collection
     * @return true if at least one item was added to the collection; false otherwise
     */
    @SafeVarargs
    public static <T extends @Nullable Object> boolean addIf(Predicate<? super T> predicate, Collection<? super T> collection, @Nullable T... items) {
        boolean changed = false;
        for (T item : items) {
            if (predicate.test(item)) {
                changed = collection.add(item) || changed;
            }
        }
        return changed;
    }

    /**
     * Adds the given non-null items to the collection. Items that are null are ignored.
     *
     * @param <T> the type of elements in the collection
     * @param collection the collection to which non-null items will be added
     * @param items the items to add to the collection, some of which may be null
     * @return true if the collection was modified as a result of the operation, false otherwise
     */
    @SafeVarargs
    public static <T extends @Nullable Object> boolean addIfNonNull(Collection<? super T> collection, @Nullable T... items) {
        return addIf(Objects::nonNull, collection, items);
    }

    /**
     * Executes the provided consumer if the specified key is present in the given map.
     *
     * @param <T> the type of keys maintained by the map
     * @param <U> the type of mapped values
     * @param map the map to be checked for the presence of the key
     * @param key the key whose presence in the map is to be tested
     * @param consumer the consumer to process the key if it is present in the map
     */
    public static <T, U> void ifPresent(Map<T, U> map, T key, Consumer<? super U> consumer) {
        U value = map.get(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

    /**
     * Creates a new {@link Set} backed by a {@link WeakHashMap}.
     * This set allows for entries to be garbage collected when they are no longer referenced elsewhere.
     *
     * @param <T> the type of elements in the set
     * @return a new weakly-referenced hash set
     */
    public static <T extends @Nullable Object> Set<T> newWeakHashSet() {
        return Collections.newSetFromMap(new WeakHashMap<>());
    }

    /**
     * Creates a new {@code Set} backed by a {@code WeakHashMap} that holds weak references to its keys.
     *
     * @param <T> the type of elements in the set
     * @param numEntries the initial capacity for the backing {@code WeakHashMap}
     * @return a new {@code Set} instance that is backed by a {@code WeakHashMap}
     */
    public static <T extends @Nullable Object> Set<T> newWeakHashSet(int numEntries) {
        return Collections.newSetFromMap(WeakHashMap.newWeakHashMap(numEntries));
    }

    /**
     * Executes the provided consumer if the given value is not null.
     *
     * @param <T> the type of the value to be checked and consumed
     * @param v the value to be checked for null
     * @param consumer the action to be performed on the value if it is not null
     * @return the passed value
     */
    public static <T> @Nullable T applyIfNonNull(@Nullable T v, Consumer<? super T> consumer) {
        if (v != null) {
            consumer.accept(v);
        }
        return v;
    }

    /**
     * Applies the given consumer to the CharSequence if it is not null and not empty.
     *
     * @param <T>      the type of the CharSequence
     * @param cs       the CharSequence to be checked and processed; may be null
     * @param consumer the consumer to be applied if the CharSequence is not empty
     * @return the original CharSequence, whether modified or not
     */
    public static <T extends CharSequence> @Nullable T applyIfNotEmpty(@Nullable T cs, Consumer<? super T> consumer) {
        if (cs != null && !cs.isEmpty()) {
            consumer.accept(cs);
        }
        return cs;
    }

    /**
     * Formats a {@link Throwable} into a string representation, including its message
     * and stack trace, for logging or debugging purposes.
     *
     * @param t the {@link Throwable} to append. If {@code null}, the string "null" will be returned.
     * @return a string representation of the given throwable, including its message
     *         and stack trace, or "null" if the throwable is {@code null}.
     */
    public static String formatThrowable(@Nullable Throwable t) {
        try {
            return appendThrowable(t, new StringBuilder()).toString();
        } catch (IOException e) {
            // this should never happen with a StringBuilder
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Appends the details of a {@link Throwable} to the provided {@link Appendable} instance.
     * This includes the exception's class name, its message, and its stack trace.
     *
     * @param <T> the type of the {@link Appendable} instance.
     * @param t the {@link Throwable} to append. If {@code null}, the string "null" will be appended.
     * @param app the {@link Appendable} instance to which the throwable's details are appended.
     * @return the same {@link Appendable} instance passed in the {@code app} parameter.
     * @throws IOException if an I/O error occurs while writing to the {@link Appendable}.
     */
    public static <T extends Appendable> T appendThrowable(@Nullable Throwable t, T app) throws IOException {
        if (t == null) {
            app.append("null");
            return app;
        }

        PrintWriter pw = new PrintWriter(IoUtil.getWriter(app));
        t.printStackTrace(pw);

        return app;
    }

    private static final class CachingStringSupplier implements Supplier<String> {
        private final Supplier<String> supplier;
        private @Nullable String s;

        CachingStringSupplier(Supplier<String> supplier) {
            this.supplier = supplier;
            s = null;
        }

        @Override
        public String get() {
            return s != null ? s : (s = supplier.get());
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
