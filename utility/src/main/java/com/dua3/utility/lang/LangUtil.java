// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Pair;
import com.dua3.utility.io.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A Utility class with general purpose methods.
 */
public final class LangUtil {

    /**
     * The byte order mark in UTF files
     */
    public static final char UTF_BYTE_ORDER_MARK = 0xfeff;
    private static final Logger LOG = LoggerFactory.getLogger(LangUtil.class);

    // private constructor for utility class
    private LangUtil() {
        // nop
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
     * @param args      format arguments
     * @throws FailedCheckException if condition does not evaluate to {@code true}
     */
    public static void check(boolean condition, String fmt, Object... args) {
        if (!condition) {
            String message = String.format(Locale.ROOT, fmt, args);
            throw new FailedCheckException(message);
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
     * @param <T> the parameter type
     * @param arg the variable to ignore
     */
    @SuppressWarnings("EmptyMethod")
    public static <T> void ignore(T arg) {
        // nop
    }

    /**
     * Test if first argument is equal to one of the other arguments.
     *
     * @param <T>  argument type
     * @param arg  first argument
     * @param rest remaining arguments
     * @return true, if {@code rest} contains at least one item that is equal
     * to
     * {@code arg}
     */
    @SafeVarargs
    public static <T> boolean isOneOf(@Nullable T arg, T... rest) {
        return List.of(rest).contains(arg);
    }

    /**
     * Return first argument if non-null, second argument otherwise.
     *
     * @param a   the first argument
     * @param b   the second argument
     * @param <T> the type
     * @return a, if a != null, else b
     */
    public static <T> T orElse(@Nullable T a, @Nullable T b) {
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
    public static <T> T orElseGet(@Nullable T a, Supplier<? extends T> b) {
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
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
    private static RuntimeException wrapException(Exception e) {
        if (e instanceof RuntimeException re) {
            return re;
        }
        if (e instanceof IOException ioe) {
            return new UncheckedIOException(ioe);
        }
        return new WrappedException(e);
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
    public static <T, E extends Exception> Consumer<T> uncheckedConsumer(ConsumerThrows<? super T, E> c) {
        return arg -> {
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
    public static <T, E extends Exception> Supplier<T> uncheckedSupplier(SupplierThrows<? extends T, E> s) {
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
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static <T, R, E extends Exception> Function<T, R> uncheckedFunction(FunctionThrows<T, ? extends R, E> f) {
        return arg -> {
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
    public static <K, V> void putAllIfAbsent(Map<? super K, ? super V> map, Pair<K, V>... items) {
        Arrays.stream(items).forEach(item -> map.putIfAbsent(item.first(), item.second()));
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
    public static <K, V> void putAll(Map<? super K, ? super V> map, Pair<K, V>... items) {
        Arrays.stream(items).forEach(item -> map.put(item.first(), item.second()));
    }

    /**
     * Create an unmodifiable map from key-value pairs.
     *
     * @param <K>   the key type
     * @param <V>   the value type
     * @param items the key-value pairs to put into the map
     * @return unmodifiable map
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map(Pair<K, V>... items) {
        Map<K, V> map = new HashMap<>();
        putAllIfAbsent(map, items);
        return Collections.unmodifiableMap(map);
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
    public static <T> Optional<T> map(OptionalInt opt, IntFunction<? extends T> f) {
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
    public static <T> Optional<T> map(OptionalLong opt, LongFunction<? extends T> f) {
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
    public static <T> Optional<T> map(OptionalDouble opt, DoubleFunction<? extends T> f) {
        return opt.isEmpty() ? Optional.empty() : Optional.ofNullable(f.apply(opt.getAsDouble()));
    }

    /**
     * Test streams for equality.
     *
     * @param <T> the element type
     * @param s1  first stream
     * @param s2  second stream
     * @return true, if and only if both streams are equal elementwise
     */
    public static <T> boolean equals(Stream<T> s1, Stream<T> s2) {
        Iterator<T> iter1 = s1.iterator();
        Iterator<T> iter2 = s2.iterator();
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
    public static <K, V> void consumeIfPresent(Map<K, V> map, K k, Consumer<? super V> consumer) {
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
    public static <K, V> void consumeIfPresent(Map<K, V> map, K k, BiConsumer<? super K, ? super V> consumer) {
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
    public static <T> Supplier<T> cache(Supplier<? extends T> supplier) {
        return new CachingSupplier<>(supplier, t -> {
        });
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
     * @throws NullPointerException if the resource could not be found
     */
    public static URL getResourceURL(Class<?> clazz, String resource) {
        return Objects.requireNonNull(clazz.getResource(resource), () -> "Resource not found: " + resource);
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
     * Get localised resource.
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
     * @return the URL of the resource if found, or {@code null}
     * @throws MissingResourceException if no resource was found
     */
    public static URL getResourceURL(Class<?> cls, String name, Locale locale) {
        String basename = IoUtil.stripExtension(name);
        String extension = IoUtil.getExtension(name);

        // build the candidate list
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

        // try loading in reverse order
        for (int i = candidates.size() - 1; i >= 0; i--) {
            URL url = cls.getResource(candidates.get(i));
            if (url != null) {
                LOG.debug("requested resource '{}', localised resource found: {}", name, url);
                return url;
            }
        }

        // nothing found
        LOG.warn("resource '{}' not found. candidates: {}", name, candidates);
        throw new MissingResourceException("Resource not found: " + name, cls.getName(), name);
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
    public static <T> List<T> surroundingItems(List<? extends T> list, Predicate<? super T> test, int before, int after) {
        return surroundingItems_(list, test, before, after, null);
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
    public static <T> List<T> surroundingItems(List<? extends T> list, Predicate<? super T> test, int before, int after, BiFunction<? super Integer, ? super Integer, ? extends T> placeHolder) {
        return surroundingItems_(list, test, before, after, placeHolder);
    }

    private static <T> List<T> surroundingItems_(List<? extends T> list, Predicate<? super T> test, int before, int after, @Nullable BiFunction<? super Integer, ? super Integer, ? extends T> placeHolder) {
        List<T> filtered = new ArrayList<>();
        int lastIndex = -1;
        for (int i = 0; i < list.size(); i++) {
            // find next difference
            while (i < list.size() && !test.test(list.get(i))) {
                i++;
            }

            // not found
            if (i >= list.size()) {
                break;
            }

            int startIndex = i;

            // add a placeholder if lines are omitted
            int count = startIndex - before - (lastIndex + 1);
            if (placeHolder != null && count > 0) {
                filtered.add(placeHolder.apply(count, lastIndex + 1));
            }

            // find end of difference
            while (i < list.size() && test.test(list.get(i))) {
                i++;
            }
            int endIndex = i;

            // print changes
            int from = Math.max(startIndex - before, Math.max(0, lastIndex + 1));
            int to = Math.min(endIndex + after, list.size());

            filtered.addAll(list.subList(from, to));
            lastIndex = to - 1;
        }
        if (placeHolder != null && lastIndex < list.size() - 1) {
            int count = list.size() - (lastIndex + 1);
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
     */
    public static boolean isBetween(long x, long a, long b) {
        assert a <= b : "invalid interval: a=" + a + ", b=" + b;
        return a <= x && x <= b;
    }

    /**
     * Check if number is between two other numbers.
     *
     * @param x the number to test
     * @param a the lower bound
     * @param b the upper bound
     * @return true, exactly if a ≤ x and x ≤ b
     */
    public static boolean isBetween(double x, double a, double b) {
        assert a <= b : "invalid interval: a=" + a + ", b=" + b;
        return a <= x && x <= b;
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
        assert a.compareTo(b) <= 0 : "invalid interval: a=" + a + ", b=" + b;
        return a.compareTo(x) <= 0 && x.compareTo(b) <= 0;
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
    public static <T> T triStateSelect(@Nullable Boolean b, T whenTrue, T whenFalse, T otherwise) {
        return b != null ? (b ? whenTrue : whenFalse) : otherwise;
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
    public interface FunctionThrows<T, R, E extends Exception> {
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
    public interface ConsumerThrows<T, E extends Exception> {
        /**
         * Performs this operation on the given argument.
         *
         * @param t the input argument
         * @throws E depending on implementation
         */
        void accept(T t) throws E;

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
        default ConsumerThrows<T, E> andThen(ConsumerThrows<? super T, ? extends E> after) throws E {
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
    public interface SupplierThrows<T, E extends Exception> {
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
    public interface AutoCloseableSupplier<T> extends AutoCloseable, Supplier<T> {
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

    private static class CachingSupplier<T> implements AutoCloseableSupplier<T> {
        private final Supplier<? extends T> supplier;
        private final Consumer<? super T> cleaner;
        private T obj;
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
    public static <T, U> Function<T, U> asFunction(Map<? super T, ? extends U> map) {
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
    public static <T, U, V extends U> Function<T, U> asFunction(Map<? super T, V> map, V defaultValue) {
        return t -> map.getOrDefault(t, defaultValue);
    }

}
