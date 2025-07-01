package com.dua3.utility.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * Utility class for data handling and conversion.
 */
@SuppressWarnings("BoundedWildcard")
public final class DataUtil {

    // Utility class - private constructor
    private DataUtil() {
    }

    /**
     * Convert an object to a different class.
     * <p>
     * Conversion works as follows:
     * <ul>
     *     <li> if value is {@code null}, {@code null} is returned;
     *     <li> if the target class is assignment compatible, a simple cast is performed;
     *     <li> if the target class is {@link String}, {@link Object#toString()} is used;
     *     <li> if the target class is an integer type and the value is of type double, a conversion without loss of precision is tried;
     *     <li> if the value is of type {@link String} and the target class provides a method {@code public static T valueOf(String)}, that method is invoked;
     *     <li> otherwise an exception is thrown.
     * </ul>
     *
     * @param value       the object to convert
     * @param targetClass the target class
     * @param <T>         target type
     * @return the object converted to the target class
     */
    public static <T extends @Nullable Object> T convert(@Nullable Object value, Class<T> targetClass) {
        return convert(value, targetClass, false);
    }

    /**
     * Convert an object to a different class.
     * <p>
     * Conversion works as follows:
     * <ul>
     *     <li> if value is {@code null}, {@code null} is returned;
     *     <li> if the target class is assignment compatible, a simple cast is performed;
     *     <li> if the target class is {@link String}, {@link Object#toString()} is used;
     *     <li> if the target class is an integer type and the value is of type double, a conversion without loss of precision is tried;
     *     <li> if the target class is {@link LocalDate} and the source class is {@link String}, use DateTimeFormatter.ISO_DATE;
     *     <li> if the target class is {@link java.time.LocalDateTime} and the source class is {@link String}, use DateTimeFormatter.ISO_DATE_TIME;
     *     <li> if the source and target classes is any of URI, URL, File, Path, the standard conversion rules are applied;
     *     <li> if the source class is {@link String} and the target class is any of URI, URL, File, Path, the standard conversion rules are applied;
     *     <li> if the target class provides a method {@code public static T valueOf(U)} and {value instanceof U}, that method is invoked;
     *     <li> if {@code useConstructor} is {@code true} and the target class provides a constructor taking a single argument of value's type, that constructor is used;
     *     <li> otherwise an exception is thrown.
     * </ul>
     *
     * @param value          the object to convert
     * @param targetClass    the target class
     * @param useConstructor flag whether a public constructor {@code T(U)} should be used in conversion if present where `U` is the value's class
     * @param <T>            target type
     * @return the object converted to the target class
     */
    @SuppressWarnings({"unchecked", "ChainOfInstanceofChecks"}) // types are checked with isAssignable()
    public static <T> @Nullable T convert(@Nullable Object value, Class<T> targetClass, boolean useConstructor) {
        // null -> null
        if (value == null) {
            return null;
        }

        Class<?> sourceClass = value.getClass();
        T result;

        // assignment compatible?
        result = (T) convertIf(targetClass.isAssignableFrom(sourceClass), targetClass::cast, value);
        if (result != null) {
            return result;
        }

        // target is String -> use toString()
        result = (T) convertIf(targetClass == String.class, Object::toString, value);
        if (result != null) {
            return result;
        }

        // convert floating point numbers without fractional part to integer types
        result = (T) convertIf(value instanceof Double || value instanceof Float, v -> convertToIntegralNumber(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // convert other numbers to double
        if (targetClass == Double.class && Number.class.isAssignableFrom(sourceClass)) {
            return (T) (Double) (((Number) value).doubleValue());
        }

        // convert other numbers to float
        if (targetClass == Float.class && Number.class.isAssignableFrom(sourceClass)) {
            return (T) (Float) (((Number) value).floatValue());
        }

        // convert String to LocalDate using the ISO format
        if (targetClass == LocalDate.class && sourceClass == String.class) {
            return (T) LocalDate.parse(value.toString(), DateTimeFormatter.ISO_DATE);
        }

        // convert String to LocalDateTime using the ISO format
        if (targetClass == LocalDateTime.class && sourceClass == String.class) {
            return (T) LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME);
        }

        // convert String to Boolean
        // Don't rely on Boolean.valueOf(String) because it might introduce subtle bugs,
        // i. e. "TRUE()", "yes", "hello" all evaluate to false; throw IllegalArgumentException instead.
        result = (T) convertIf(targetClass == Boolean.class && sourceClass == String.class, v -> convertToBoolean(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // convert to Path
        result = (T) convertIf(targetClass == Path.class, v -> convertToPath(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // convert to File
        result = (T) convertIf(targetClass == File.class, v -> convertToFile(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // convert to URI
        result = (T) convertIf(targetClass == URI.class, v -> convertToUri(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // convert to URL
        result = (T) convertIf(targetClass == URL.class, v -> convertToUrl(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        // target provides public static valueOf(U) where value is instance of U
        // (reason for iterating methods: getDeclaredMethod() will throw if valueOf is not present)
        result = convertUsingValueOf(targetClass, sourceClass, value);
        if (result != null) {
            return result;
        }

        // ... or provides a public constructor taking the value's class (and is enabled by parameter)
        result = convertIf(useConstructor, v -> convertUsingConstructor(targetClass, sourceClass, v), value);
        if (result != null) {
            return result;
        }

        throw new ConversionException(sourceClass, targetClass, "unsupported conversion");
    }

    private static <T> @Nullable T convertUsingValueOf(Class<T> targetClass, Class<?> sourceClass, @NonNull Object value) {
        // (reason for iterating methods: getDeclaredMethod() will throw if valueOf is not present)
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)
                    && method.getName().equals("valueOf")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == sourceClass
                    && targetClass.isAssignableFrom(method.getReturnType())) {
                try {
                    return (T) method.invoke(null, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking valueOf(String)", e);
                }
            }
        }
        return null;
    }

    private static <T> @Nullable T convertUsingConstructor(Class<T> targetClass, Class<?> sourceClass, @NonNull Object value) {
        for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
            if (constructor.getModifiers() == (Modifier.PUBLIC)
                    && constructor.getParameterCount() == 1
                    && constructor.getParameterTypes()[0] == sourceClass) {
                try {
                    return (T) constructor.newInstance(value);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking constructor " + targetClass.getName() + "(String)", e);
                }
            }
        }
        return null;
    }

    private static <T> @Nullable Boolean convertToBoolean(Class<T> targetClass, Class<?> sourceClass, Object value) {
        return switch (((String) value).toLowerCase(Locale.ROOT)) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("invalid text for boolean conversion: " + value);
        };
    }

    private static <T> @Nullable Number convertToIntegralNumber(Class<T> targetClass, Class<?> sourceClass, Object value) {
        double d = ((Number) value).doubleValue();
        if (targetClass == Integer.class) {
            //noinspection NumericCastThatLosesPrecision
            int n = (int) d;
            //noinspection FloatingPointEquality
            LangUtil.check(n == d, () -> new IllegalArgumentException("value cannot be converted to int without loss of precision: " + value));
            return n;
        } else if (targetClass == Long.class) {
            //noinspection NumericCastThatLosesPrecision
            long n = (long) d;
            LangUtil.check(n == d, () -> new IllegalArgumentException("value cannot be converted to long without loss of precision: " + value));
            return n;
        }
        return null;
    }

    private static <T> @Nullable Path convertToPath(Class<T> targetClass, Class<?> sourceClass, Object value) {
        if (sourceClass == String.class) {
            return Paths.get(value.toString());
        }
        if (sourceClass == File.class) {
            return ((File) value).toPath();
        }
        if (sourceClass == URI.class) {
            return Paths.get((URI) value);
        }
        if (sourceClass == URL.class) {
            try {
                return Paths.get(((URL) value).toURI());
            } catch (URISyntaxException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        return null;
    }

    private static <T> @Nullable File convertToFile(Class<T> targetClass, Class<?> sourceClass, Object value) {
        if (sourceClass == String.class) {
            return new File(value.toString());
        }
        if (Path.class.isAssignableFrom(sourceClass)) { // for Path the concrete implementation may vary
            assert value instanceof Path;
            return ((Path) value).toFile();
        }
        if (sourceClass == URI.class) {
            return Paths.get((URI) value).toFile();
        }
        if (sourceClass == URL.class) {
            try {
                return Paths.get(((URL) value).toURI()).toFile();
            } catch (URISyntaxException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        return null;
    }

    private static <T> @Nullable URI convertToUri(Class<T> targetClass, Class<?> sourceClass, Object value) {
        if (sourceClass == String.class) {
            return URI.create(value.toString());
        }
        if (sourceClass == File.class) {
            return ((File) value).toURI();
        }
        if (sourceClass == URL.class) {
            try {
                return ((URL) value).toURI();
            } catch (URISyntaxException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        if (Path.class.isAssignableFrom(sourceClass)) { // Path is abstract
            return ((Path) value).toUri();
        }
        return null;
    }

    private static <T> @Nullable URL convertToUrl(Class<T> targetClass, Class<?> sourceClass, Object value) {
        if (sourceClass == String.class) {
            try {
                return URI.create(value.toString()).toURL();
            } catch (MalformedURLException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        if (sourceClass == File.class) {
            try {
                return ((File) value).toURI().toURL();
            } catch (MalformedURLException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        if (sourceClass == URI.class) {
            try {
                return ((URI) value).toURL();
            } catch (MalformedURLException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        if (Path.class.isAssignableFrom(sourceClass)) { // Path is abstract
            try {
                return ((Path) value).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new ConversionException(sourceClass, targetClass, e);
            }
        }
        return null;
    }

    private static <T> @Nullable T convertIf(boolean condition, Function<Object, @Nullable T> convert, Object value) {
        return condition ? convert.apply(value) : null;
    }

    /**
     * Convert Collection to array.
     * <p>
     * Converts a {@code Collection<T>} to {@code U[]} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data        the collection to convert
     * @param targetClass the element target class
     * @param <T>         the element source type
     * @param <U>         the element target type
     * @return array containing the converted elements
     */
    public static <T, U> U[] convertToArray(Collection<T> data, Class<? extends U> targetClass) {
        return convertToArray(data, targetClass, false);
    }

    /**
     * Convert Collection to array.
     * <p>
     * Converts a {@code Collection<T>} to {@code U[]} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data           the collection to convert
     * @param targetClass    the element target class
     * @param useConstructor flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>            the element source type
     * @param <U>            the element target type
     * @return array containing the converted elements
     */
    @SuppressWarnings("unchecked")
    public static <T extends @Nullable Object, U extends @Nullable Object> U[] convertToArray(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        //noinspection DataFlowIssue
        return data.stream()
                .map((T obj) -> convert(obj, targetClass, useConstructor))
                .toArray(n -> (U[]) Array.newInstance(targetClass, n));
    }

    /**
     * Convert Collection to list.
     * <p>
     * Converts a {@code Collection<T>} to {@code List<U>} by using the supplied mapper function on
     * the elements contained in the collection.
     *
     * @param data   the collection to convert
     * @param mapper the mapping function
     * @param <T>    the element source type
     * @param <U>    the element target type
     * @return list containing the converted elements
     */
    public static <T, U> List<U> convert(Collection<T> data, Function<? super T, ? extends U> mapper) {
        return data.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * Convert Collection to List.
     * <p>
     * Converts a {@code Collection<T>} to {@code List<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     * <p>
     * <strong>The source collection must not contain {@code null} values.
     * Use {@link #convertCollection(Collection, Class, Supplier)} if the source collection contains
     * {@code null} values.</strong>
     *
     * @param data        the collection to convert
     * @param targetClass the element target class
     * @param <T>         the element source type
     * @param <U>         the element target type
     * @return list containing the converted elements
     */
    public static <T, U> List<U> convert(Collection<T> data, Class<U> targetClass) {
        return convert(data, targetClass, false);
    }

    /**
     * Convert Collection to list.
     * <p>
     * Converts a {@code Collection<T>} to {@code List<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     * <p>
     * <strong>The source collection must not contain {@code null} values.
     * Use {@link #convertCollection(Collection, Class, Supplier, boolean)} if the source collection contains
     * {@code null} values.</strong>
     *
     * @param data           the collection to convert
     * @param targetClass    the element target class
     * @param useConstructor flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>            the element source type
     * @param <U>            the element target type
     * @return list containing the converted elements
     */
    public static <T extends @Nullable Object, U extends @Nullable Object> List<U> convert(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        //noinspection DataFlowIssue
        return data.stream()
                .map((T obj) -> convert(obj, targetClass, useConstructor))
                .collect(Collectors.toList());
    }

    /**
     * Convert Collection.
     * <p>
     * Converts a {@code Collection<T>} to {@code Collection<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data        the collection to convert
     * @param targetClass the element target class
     * @param supplier    the collection supplier, i. e. {@code ArrayList::new}
     * @param <T>         the element source type
     * @param <U>         the element target type
     * @param <C>         the target collection type
     * @return collection containing the converted elements
     */
    public static <T, U, C extends Collection<U>> C convertCollection(Collection<T> data, Class<U> targetClass, Supplier<C> supplier) {
        return convertCollection(data, targetClass, supplier, false);
    }

    /**
     * Convert Collection to list.
     * <p>
     * Converts a {@code Collection<T>} to {@code Collection<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data           the collection to convert
     * @param targetClass    the element target class
     * @param supplier       the collection supplier, i. e. {@code ArrayList::new}
     * @param useConstructor flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>            the element source type
     * @param <U>            the element target type
     * @param <C>            the target collection type
     * @return collection containing the converted elements
     */
    public static <T, U, C extends Collection<U>> C convertCollection(Collection<T> data, Class<U> targetClass, Supplier<C> supplier, boolean useConstructor) {
        return data.stream()
                .map(obj -> convert(obj, targetClass, useConstructor))
                .collect(Collectors.toCollection(supplier));
    }

    /**
     * Create a filtering iterator that only lets through items matching a predicate.
     *
     * @param iterator  the base iterator
     * @param predicate the predicate to test items with
     * @param <T>       the item type
     * @return iterator instance that skips items not matching the predicate
     */
    public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<T> predicate) {
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * Create a mapping iterator that converts elements on the fly.
     *
     * @param iterator the base iterator
     * @param mapping  the mapping to apply to elements
     * @param <T>      the source iterator item type
     * @param <U>      the target iterator item type
     * @return iterator instance that converts items of type {@code T} to {@code U}
     */
    public static <T, U> Iterator<U> map(Iterator<T> iterator, Function<? super T, ? extends U> mapping) {
        return new MappingIterator<>(iterator, mapping);
    }

    /**
     * Collect items obtained from an iterable into a List.
     *
     * @param iterable the iterable
     * @param <T>      the element type
     * @return list of elements
     */
    public static <T> List<T> collect(Iterable<? extends T> iterable) {
        return collect(iterable.iterator());
    }

    /**
     * Collect items from an iterator into a List.
     *
     * @param iterator the iterator
     * @param <T>      the element type
     * @return list of elements
     */
    public static <T> List<T> collect(Iterator<? extends T> iterator) {
        List<T> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    /**
     * Collect items obtained from an iterable into an array.
     *
     * @param iterable the iterable
     * @param <T>      the element type
     * @return array of elements
     */
    public static <T> T[] collectArray(Iterable<T> iterable) {
        return collect(iterable.iterator()).toArray(genericArray());
    }

    /**
     * Collect items from an iterator into an array.
     *
     * @param <T>      the element type
     * @param iterator the iterator
     * @return array of elements
     */
    public static <T> T[] collectArray(Iterator<T> iterator) {
        return collect(iterator).toArray(genericArray());
    }

    /**
     * Create a generic array of the arguments.
     * @param args the arguments
     * @return generic array containing the arguments
     * @param <T> the generic array type
     */
    @SafeVarargs
    private static <T> T[] genericArray(T... args) {
        return args;
    }

    /**
     * Convert {@link Map} to {@link Function}.
     *
     * @param map          the map
     * @param defaultValue the value to return if the lookup key is not present
     * @param <K>          type of key
     * @param <V>          type of value
     * @return a Function instance returning the map entries
     */
    public static <K, V> Function<K, V> asFunction(Map<K, V> map, V defaultValue) {
        return k -> map.getOrDefault(k, defaultValue);
    }

    /**
     * Compute the change of mappings between two maps. The result is a mapping from keys to pairs {@code (value a, value b)}
     * of the changes. See also {@link #diff(Map, Map)}.
     *
     * @param a   the first map
     * @param b   the second map
     * @param <U> the key type
     * @param <V> the value type
     * @return a new map that contains the changes as pairs (value in {@code a}, value in {@code b})
     */
    public static <U, V extends @Nullable Object> Map<U, Pair<V, V>> changes(Map<? extends U, ? extends V> a, Map<? extends U, ? extends V> b) {
        Set<U> keys = new HashSet<>(a.keySet());
        keys.addAll(b.keySet());

        Map<U, Pair<V, V>> changes = new HashMap<>();
        keys.forEach(k -> {
            V va = a.get(k);
            V vb = b.get(k);
            if (!Objects.equals(va, vb)) {
                changes.put(k, Pair.of(va, vb));
            }
        });
        return changes;
    }

    /**
     * Compute the difference of mappings between two maps. The result is a map that maps keys to the new values
     * for all changed keys. See also {@link #changes(Map, Map)}.
     *
     * @param a   the first map
     * @param b   the second map
     * @param <U> the key type
     * @param <V> the value type
     * @return a new map that contains the changed mappings (k -> mapped value in b)
     */
    public static <U, V> Map<U, V> diff(Map<? extends U, ? extends V> a, Map<? extends U, ? extends V> b) {
        return diff(a, b, HashMap::new);
    }

    /**
     * Compute the difference of mappings between two maps. The result is a map obtained by calling
     * {@code mapFactory.get()} that maps keys to the new values for all changed keys.
     * See also{@link #changes(Map, Map)}.
     *
     * @param a   the first map
     * @param b   the second map
     * @param mapFactory the Map factory
     * @param <U> the key type
     * @param <V> the value type
     * @return a new map that contains the changed mappings (k -> mapped value in b)
     */
    public static <U, V> Map<U, V> diff(Map<? extends U, ? extends V> a, Map<? extends U, ? extends V> b, Supplier<? extends Map<U, V>> mapFactory) {
        Set<U> keys = new HashSet<>(a.keySet());
        keys.addAll(b.keySet());

        Map<U, V> diff = mapFactory.get();
        keys.forEach(k -> {
            V va = a.get(k);
            V vb = b.get(k);
            if (!Objects.equals(va, vb)) {
                diff.put(k, vb);
            }
        });
        return diff;
    }

    /**
     * Execute action if key is mapped. See also {@link #ifMapped(Map, Object, Consumer)}.
     *
     * @param map    the map
     * @param key    the key
     * @param action the action
     * @param <T>    the key type
     * @param <U>    the value type
     */
    public static <T, U> void ifPresent(Map<T, U> map, T key, Consumer<? super U> action) {
        // we need to check using containsKey() since key may be mapped to null
        if (map.containsKey(key)) {
            action.accept(map.get(key));
        }
    }

    /**
     * Execute action if key is mapped to a non-null value. See also {@link #ifPresent(Map, Object, Consumer)}.
     *
     * @param map    the map
     * @param key    the key
     * @param action the action
     * @param <T>    the key type
     * @param <U>    the value type
     * @return true, if action was called
     */
    public static <T, U> boolean ifMapped(Map<T, U> map, T key, Consumer<? super U> action) {
        // we need to check using containsKey() since the key may be mapped to null
        U value = map.get(key);
        if (value == null) {
            return false;
        }

        action.accept(value);
        return true;
    }

    /**
     * Exception thrown when data conversion fails.
     */
    public static class ConversionException extends IllegalArgumentException {
        /**
         * The name of the source class.
         */
        private final String sourceClassName;
        /**
         * The name of the target class.
         */
        private final String targetClassName;

        ConversionException(Class<?> sourceClass, Class<?> targetClass, Throwable cause) {
            this(sourceClass, targetClass, "could not convert from " + sourceClass.getSimpleName() + " to " + targetClass.getSimpleName(), cause);
        }

        ConversionException(Class<?> sourceClass, Class<?> targetClass, String message) {
            super(message);
            this.sourceClassName = sourceClass.getName();
            this.targetClassName = targetClass.getName();
        }

        ConversionException(Class<?> sourceClass, Class<?> targetClass, String message, Throwable cause) {
            super(message, cause);
            this.sourceClassName = sourceClass.getName();
            this.targetClassName = targetClass.getName();
        }

        @Override
        public String getMessage() {
            return String.format(Locale.ROOT, "%s\n[trying to convert %s -> %s]", super.getMessage(), sourceClassName, targetClassName);
        }
    }

    /**
     * Determines if the elements within the given collection are sorted in natural order.
     * <p>
     * This method has a time complexity of O(n) for collections that do not implement {@link SortedSet}
     * and use the natural order.
     * Its main purpose is to be used in assertions on method parameters that have to be sorted.
     *
     * @param <T> the type of elements in the collection, which must extend Comparable
     * @param collection the collection to check for sorted order
     * @return {@code true} if the collection is sorted in natural order, {@code false} otherwise
     */
    public static <T extends Comparable<T>> boolean isSorted(Collection<T> collection) {
        return switch (collection) {
            case SortedSet<?> ss when ss.comparator() == null -> true;
            default -> isSorted(collection, Comparator.naturalOrder());
        };
    }

    /**
     * Determines if the given collection is sorted according to the order defined by the provided comparator.
     * <p>
     * This method has a time complexity of O(n). Its main purpose is to be used in assertions on method
     * parameters that have to be sorted.
     *
     * @param <T> the type of objects in the collection
     * @param collection the collection of elements to check for sorting
     * @param comparator the comparator used to define the sorting order
     * @return {@code true} if the collection is sorted in the order defined by the comparator,
     *         otherwise {@code false}
     */
    public static <T> boolean isSorted(Collection<T> collection, Comparator<T> comparator) {
        T last = null;
        for (T t : collection) {
            if (last != null && comparator.compare(last, t) > 0) {
                return false;
            }
            last = t;
        }
        return true;
    }
}
