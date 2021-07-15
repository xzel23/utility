// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.IOUtil;

/**
 * A Utility class with general purpose methods.
 */
public final class LangUtil {

    private static final Logger LOG = Logger.getLogger(LangUtil.class.getName());

    // private constructor for utility class
    private LangUtil() {
        // nop
    }

    /**
     * Exception derived from IllegalStateException thrown by
     * {@link LangUtil#check(boolean)}. The intent is to make it possible to
     * distinguish failed checks from other IllegalStateExceptions in try-blocks.
     */
    public static class FailedCheckException extends IllegalStateException {
        private static final long serialVersionUID = 1L;

        public FailedCheckException() {
            super();
        }

        public FailedCheckException(String msg) {
            super(msg);
        }

        public FailedCheckException(Throwable cause) {
            super(cause);
        }

        public FailedCheckException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param  condition            condition to test
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
     * @param  condition            condition to test
     * @param exceptionSupplier     the exception supplier
     * @param <E>                   the exception type
     * @throws E                    if condition does not evaluate to {@code true}
     */
    public static <E extends Exception> void check(boolean condition, Supplier<E> exceptionSupplier) throws E {
        if (!condition) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Check that condition is fulfilled.
     *
     * @param  condition            condition to test
     * @param  fmt                  message format (@see
     *                              {@link String#format(String, Object...)})
     * @param  args                 format arguments
     * @throws FailedCheckException if condition does not evaluate to {@code true}
     */
    public static void check(boolean condition, String fmt, Object... args) {
        if (!condition) {
            String message = String.format(Locale.ROOT, fmt, args);
            throw new FailedCheckException(message);
        }
    }

    /**
     * Check that index is valid.
     *
     * @param  idx                       index to test
     * @param  size                      collection size
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public static void checkIndex(int idx, int size) {
        if (idx < 0 || idx >= size) {
            throw new IndexOutOfBoundsException("index: " + idx);
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
     * @param     <T> the parameter type
     * @param arg the variable to ignore
     */
    @SuppressWarnings("EmptyMethod")
    public static <T> void ignore(T arg) {
        // nop
    }

    /**
     * Test if first argument is equal to one of the other arguments.
     *
     * @param       <T> argument type
     * @param  arg  first argument
     * @param  rest remaining arguments
     * @return      true, if {@code rest} contains at least one item that is equal
     *              to
     *              {@code arg}
     */
    @SafeVarargs
    public static <T> boolean isOneOf(T arg, T... rest) {
        return Arrays.asList(rest).contains(arg);
    }

    /**
     * Find enum by Predicate.
     * @param clazz the enum class
     * @param condition the predicate
     * @param <E> the generic enum parameter
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
     * @param clazz the enum class
     * @param value the value to look for
     * @param <E> the generic enum parameter
     * @return an Optional holding the enum constant or an empty Optional
     */
    public static <E extends Enum<E>> Optional<E> enumConstant(Class<? extends E> clazz, String value) {
        return enumConstant(clazz, ec -> ec.toString().equals(value));
    }

    /** The byte order mark in UTF files */
    public static final char UTF_BYTE_ORDER_MARK = 0xfeff;

    /**
     * Test if character is the byte order mark.
     *
     * @param  c the character to test
     * @return   true if c is the byte order mark
     */
    public static boolean isByteOrderMark(char c) {
        return c == UTF_BYTE_ORDER_MARK;
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param    <T> the argument type
     * @param  c the consumer to call (instance of {@link ConsumerThrows})
     * @return   instance of Consumer that invokes f and converts IOException to
     *           UncheckedIOException, CheckedException to WrappedException, and lets UncheckedExceptions through
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static <T> Consumer<T> uncheckedConsumer(ConsumerThrows<? super T> c) {
        return arg -> {
            try {
                c.apply(arg);
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new WrappedException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param    <T> the argument type
     * @param  s the supplier to call (instance of {@link SupplierThrows})
     * @return   instance of Sipplier that invokes f and converts IOException to
     *           UncheckedIOException, CheckedException to WrappedException, and lets UncheckedExceptions through
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static <T> Supplier<T> uncheckedSupplier(SupplierThrows<? extends T> s) {
        return () -> {
            try {
                return s.get();
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new WrappedException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException} and other checked exceptions to {@link WrappedException}.
     *
     * @param    <T> the argument type
     * @param    <R> the result type
     * @param  f the function to call (instance of {@link FunctionThrows})
     * @return   instance of Function that invokes f and converts IOException to
     *           UncheckedIOException and other checked exceptions to {@link WrappedException}
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static <T, R> Function<T, R> uncheckedFunction(FunctionThrows<? super T, ? extends R> f) {
        return arg -> {
            try {
                return f.apply(arg);
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new WrappedException(e);
            }
        };
    }

    /**
     * Helper method that converts checked {@link java.io.IOException} to
     * {@link java.io.UncheckedIOException}.
     *
     * @param  r the Runnable to call (instance of {@link RunnableThrows})
     * @return   instance of Function that invokes f and converts IOException to
     *           UncheckedIOException
     */
    @SuppressWarnings("ProhibitedExceptionThrown")
    public static Runnable uncheckedRunnable(RunnableThrows r) {
        return () -> {
            try {
                r.run();
            } catch (RuntimeException e) {
                throw e;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (Exception e) {
                throw new WrappedException(e);
            }
        };
    }

    /**
     * Trim string, remove prepending byte order mark.
     *
     * @param  s the string to trim
     * @return   the trimmed string
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
     * @param       <K> the key type
     * @param       <V> the value type
     * @param map   the map to insert into
     * @param items the key-value pairs to put into the map
     */
    @SafeVarargs
    public static <K, V> void putAllIfAbsent(Map<? super K, ? super V> map, Pair<K, V>... items) {
        Arrays.stream(items).forEach(item -> map.putIfAbsent(item.first, item.second));
    }

    /**
     * Insert key-value pairs into map, <em>replacing</em> existing mappings.
     *
     * @param       <K> the key type
     * @param       <V> the value type
     * @param map   the map to insert into
     * @param items the key-value pairs to put into the map
     */
    @SafeVarargs
    public static <K, V> void putAll(Map<? super K, ? super V> map, Pair<K, V>... items) {
        Arrays.stream(items).forEach(item -> map.put(item.first, item.second));
    }

    /**
     * Create an unmodifiable map from key-value pairs.
     *
     * @param        <K> the key type
     * @param        <V> the value type
     * @param  items the key-value pairs to put into the map
     * @return       unmodifiable map
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map(Pair<K, V>... items) {
        Map<K, V> map = new HashMap<>();
        putAllIfAbsent(map, items);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Test streams for equality.
     *
     * @param     <T> the element type
     * @param  s1 first stream
     * @param  s2 second stream
     * @return    true, if and only if both streams are equal elementwise
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
     * @param          <K> the key type
     * @param          <V> the value type
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
     * @param          <K> the key type
     * @param          <V> the value type
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
     * Interface similar to {@link java.lang.Runnable} that declares thrown
     * exceptions on its {@code run()} method.
     */
    @FunctionalInterface
    public interface RunnableThrows {
        void run() throws Exception;
    }

    /**
     * Interface similar to {@link java.util.function.Function} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     * @param <R> the result type
     */
    @FunctionalInterface
    public interface FunctionThrows<T, R> {
        R apply(T arg) throws Exception;
    }

    /**
     * Interface similar to {@link java.util.function.Consumer} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     */
    @FunctionalInterface
    public interface ConsumerThrows<T> {
        void apply(T arg) throws Exception;
    }

    /**
     * Interface similar to {@link java.util.function.Supplier} that declares thrown
     * exceptions on its {@code apply()} method.
     *
     * @param <T> the argument type
     */
    @FunctionalInterface
    public interface SupplierThrows<T> {
        T get() throws Exception;
    }

    /**
     * Create a lazy, caching Supplier. Upon first invocation of `get()`, `s.get()`
     * is called to create the object to be returned. Each subsequent call will
     * return the same object without invoking `s.get()` again.
     *
     * @param           <T> the result type
     * @param  supplier the Supplier
     * @return          caching Supplier
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
     * @param           <T> the result type
     * @param  supplier the Supplier
     * @param  cleaner  the cleanup operation to be executed on `close()`
     * @return          caching Supplier
     */
    public static <T> AutoCloseableSupplier<T> cache(Supplier<? extends T> supplier, Consumer<? super T> cleaner) {
        return new CachingSupplier<>(supplier, cleaner);
    }

    /**
     * Interface AutoClosableSupplier, used in {@link #cache(Supplier, Consumer)}.
     * @param <T> the base type
     */
    public interface AutoCloseableSupplier<T> extends AutoCloseable, Supplier<T> {
        @Override
        void close();
    }

    private static class CachingSupplier<T> implements AutoCloseableSupplier<T> {
        private final Supplier<? extends T> supplier;
        private final Consumer<? super T> cleaner;
        private T obj = null;
        private boolean initialized = false;

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
     * Get URL for a resource on the classpath.
     *
     * @param  clazz    the Class that's used to load the resource.
     * @param  resource path (relative to clazz) of resource to load
     * @return          URL for the given resource
     */
    public static URL getResourceURL(Class<?> clazz, String resource) {
        return Objects.requireNonNull(clazz.getResource(resource), () -> "Resource not found: " + resource);
    }

    /**
     * Read the content of a resource on the classpath into a String.
     *
     * @param  clazz       the Class that's used to load the resource.
     * @param  resource    path (relative to clazz) of resource to load
     * @return             A String containing the resource's content
     * @throws IOException if the resource could not be loaded
     */
    public static String getResourceAsString(Class<?> clazz, String resource) throws IOException {
        return new String(getResource(clazz, resource), StandardCharsets.UTF_8);
    }

    /**
     * Read the content of a resource on the classpath.
     *
     * @param  clazz       the Class that's used to load the resource.
     * @param  resource    path (relative to clazz) of resource to load
     * @return             A byte array containing the resource's content
     * @throws IOException if the resource could not be loaded
     */
    public static byte[] getResource(Class<?> clazz, String resource) throws IOException {
        URL url = getResourceURL(clazz, resource);
        try (InputStream in = url.openStream()) {
            return in.readAllBytes();
        }
    }

    /**
     * Create an EnumSet. This method also works if values is empty.
     * @param clss
     *  the enum class
     * @param values
     *  the values
     * @param <E>
     *  the enum type
     * @return
     *  the EnumSet
     */
    @SafeVarargs
    public static <E extends Enum<E>> EnumSet<E> enumSet(Class<E> clss, E... values) {
        return enumSet(clss, Arrays.asList(values));
    }

    /**
     * Create an EnumSet. This method also works if values is empty.
     * @param clss
     *  the enum class
     * @param values
     *  the values
     * @param <E>
     *  the enum type
     * @return
     *  the EnumSet
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
     * @param locale    the locale
     *                  
     * @return the language suffix as used by the resource bundle
     */
    public static String getLocaleSuffix(Locale locale) {
        String language = locale.getLanguage();
        if (language.isEmpty()) {
            return "";
        }

        String country = locale.getCountry();
        if (country.isEmpty()) {
            return "_"+language;
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
     * @param cls       the class for resource loading
     * @param name      the resource name
     * @param locale    the locale
     *
     * @return the URL of the resource if found, or {@code null}
     * 
     * @throws MissingResourceException if no resource was found
     */
    public static URL getResourceURL(Class<?> cls, String name, Locale locale) {
        String basename = IOUtil.stripExtension(name);
        String extension = IOUtil.getExtension(name);

        // build the candidate list
        List<String> candidates = new ArrayList<>();

        String candidateName = basename;
        candidates.add(candidateName+"."+extension);

        String language = locale.getLanguage();
        if (!language.isEmpty()) {
            candidateName = candidateName + "_" + language;
            candidates.add(candidateName+"."+extension);
            
            String country = locale.getCountry();
            if (!country.isEmpty()) {
                candidateName = candidateName + "_" + country;
                candidates.add(candidateName+"."+extension);

                String variant = locale.getVariant();
                if (!variant.isEmpty()) {
                    candidateName = candidateName + "_" + variant;
                    candidates.add(candidateName+"."+extension);
                }
            }
        }

        // try loading in reverse order
        for (int i=candidates.size()-1; i>=0; i--) {
            URL url = cls.getResource(candidates.get(i));
            if (url!=null) {
                LOG.fine("requested resource '"+name+"', localised rescource found: "+url);
                return url;
            }
        }
        
        // nothing found
        LOG.warning(() ->"resource '" + name + "' not found. candidates: " + candidates);
        throw new MissingResourceException("Resource not found: "+name, cls.getName(), name);
    }

    /**
     * Filter surrounding items of a list.
     * @param list The unfiltered list
     * @param test the predicate to use
     * @param before number of items to accept before each match
     * @param after number of items to accept after each match
     * @param placeHolder generator for placeholder items; first argument is number of items replaced, second one is current item index in original list
     * @param <T> the element type
     * @return a list that contains all items within a the given range before and after each match
     */
    public static <T> List<T> surroundingItems(List<? extends T> list, Predicate<? super T> test, int before, int after, BiFunction<? super Integer, ? super Integer, ? extends T> placeHolder) {
        List<T> filtered = new ArrayList<>();
        int lastIndex = -1;
        for (int i=0; i<list.size(); i++) {
            // find next difference
            while (i<list.size() && !test.test(list.get(i))) {
                i++;
            }

            // not found
            if (i>=list.size()) {
                break;
            }

            int startIndex = i;

            // add a placeholder if lines are omitted
            int count = startIndex-before-(lastIndex+1);
            if (placeHolder!= null && count>0) {
                filtered.add(placeHolder.apply(count, lastIndex+1));
            }

            // find end of difference
            while (i<list.size() && test.test(list.get(i))) {
                i++;
            }
            int endIndex = i;

            // print changes
            int changedLines = endIndex-startIndex;

            int from = Math.max(startIndex-before, Math.max(0, lastIndex));
            int to = Math.min(endIndex+after, list.size());
            
            filtered.addAll(list.subList(from,to));
            lastIndex = to-1;
        }
        if (placeHolder!= null && lastIndex<list.size()-1) {
            int count = list.size() - (lastIndex+1);
            filtered.add(placeHolder.apply(count, lastIndex+1));
        }
        
        return filtered;
    }
}
