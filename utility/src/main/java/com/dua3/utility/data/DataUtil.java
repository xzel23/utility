package com.dua3.utility.data;

import com.dua3.cabe.annotations.Nullable;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    public static <T> T convert(@Nullable Object value, Class<T> targetClass) {
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
    public static <T> T convert(@Nullable Object value, Class<T> targetClass, boolean useConstructor) {
        // null -> null
        if (value == null) {
            return null;
        }

        // assignment compatible?
        Class<?> sourceClass = value.getClass();
        if (targetClass.isAssignableFrom(sourceClass)) {
            return (T) value;
        }

        // target is String -> use toString()
        if (targetClass == String.class) {
            return (T) value.toString();
        }

        // convert floating point numbers without fractional part to integer types
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (targetClass == Integer.class) {
                //noinspection NumericCastThatLosesPrecision
                int n = (int) d;
                //noinspection FloatingPointEquality
                LangUtil.check(n == d, () -> new IllegalArgumentException("value cannot be converted to int without loss of precision: " + value));
                return (T) (Integer) n;
            } else if (targetClass == Long.class) {
                //noinspection NumericCastThatLosesPrecision
                long n = (long) d;
                LangUtil.check(n == d, () -> new IllegalArgumentException("value cannot be converted to long without loss of precision: " + value));
                return (T) (Long) n;
            }
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
        if (targetClass == Boolean.class && sourceClass == String.class) {
            return switch (((String) value).toLowerCase(Locale.ROOT)) {
                case "true" -> (T) Boolean.TRUE;
                case "false" -> (T) Boolean.FALSE;
                default -> throw new IllegalArgumentException("invalid text for boolean conversion: " + value);
            };
        }

        // convert to Path
        if (targetClass == Path.class) {
            if (sourceClass == String.class) {
                return (T) Paths.get(value.toString());
            }
            if (sourceClass == File.class) {
                return (T) ((File) value).toPath();
            }
            if (sourceClass == URI.class) {
                return (T) Paths.get((URI) value);
            }
            if (sourceClass == URL.class) {
                try {
                    return (T) Paths.get(((URL) value).toURI());
                } catch (URISyntaxException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
        }

        // convert to File
        if (targetClass == File.class) {
            if (sourceClass == String.class) {
                return (T) new File(value.toString());
            }
            if (Path.class.isAssignableFrom(sourceClass)) { // for Path the concrete implementation may vary
                assert value instanceof Path;
                return (T) ((Path) value).toFile();
            }
            if (sourceClass == URI.class) {
                return (T) Paths.get((URI) value).toFile();
            }
            if (sourceClass == URL.class) {
                try {
                    return (T) Paths.get(((URL) value).toURI()).toFile();
                } catch (URISyntaxException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
        }

        // convert to URI
        if (targetClass == URI.class) {
            if (sourceClass == String.class) {
                return (T) URI.create(value.toString());
            }
            if (sourceClass == File.class) {
                return (T) ((File) value).toURI();
            }
            if (sourceClass == URL.class) {
                try {
                    return (T) ((URL) value).toURI();
                } catch (URISyntaxException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
            if (Path.class.isAssignableFrom(sourceClass)) { // Path is abstract
                return (T) ((Path) value).toUri();
            }
        }

        // convert to URL
        if (targetClass == URL.class) {
            if (sourceClass == String.class) {
                try {
                    return (T) new URL(value.toString());
                } catch (MalformedURLException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
            if (sourceClass == File.class) {
                try {
                    return (T) ((File) value).toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
            if (sourceClass == URI.class) {
                try {
                    return (T) ((URI) value).toURL();
                } catch (MalformedURLException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
            if (Path.class.isAssignableFrom(sourceClass)) { // Path is abstract
                try {
                    return (T) ((Path) value).toUri().toURL();
                } catch (MalformedURLException e) {
                    throw new ConversionException(sourceClass, targetClass, e);
                }
            }
        }

        // target provides public static valueOf(U) where value is instance of U
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

        // ... or provides a public constructor taking the value's class (and is enabled by parameter)
        if (useConstructor) {
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
        }

        throw new ConversionException(sourceClass, targetClass, "unsupported conversion");
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
    public static <T, U> U[] convertToArray(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        return data.stream()
                .map(obj -> convert(obj, targetClass, useConstructor))
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
    public static <T, U> List<U> convert(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        return data.stream()
                .map(obj -> convert(obj, targetClass, useConstructor))
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
    public static <U, V> Map<U, Pair<V, V>> changes(Map<? extends U, ? extends V> a, Map<? extends U, ? extends V> b) {
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
    public static <U, V> Map<U, V> diff(Map<? extends U, ? extends V> a, Map<? extends U, ? extends V> b, Supplier<? extends Map<U,V>> mapFactory) {
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
        private final String sourceClassName;
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

}
