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
import java.util.Arrays;
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
     * A functional interface that defines a method for attempting to convert an object of one type
     * to another target type.
     */
    @FunctionalInterface
    private interface TryConvert {
        /**
         * Attempts to convert an object of the specified source class to the target class.
         * If the conversion is not possible, returns {@code null}.
         *
         * @param targetClass the target class to which the object should be converted
         * @param sourceClass the source class of the object being converted
         * @param value the object to be converted
         * @return the converted object if conversion is successful; otherwise {@code null}
         */
        @Nullable Object tryConvert(Class<?> targetClass, Class<?> sourceClass, Object value);
    }

    /**
     * An array of {@code TryConvert} instances that define various conversion strategies between types.
     * These converters handle a wide range of type conversions, including compatibility checks,
     * string conversions, number format conversions, date/time parsing, and object instantiation
     * from provided classes.
     * <p>
     * The priority of the converters is based on their order within the array. The first matching
     * converter that can handle the conversion is executed.
     */
    private static final TryConvert[] CONVERTERS = {
            // assignment compatible?
            (t, s, v) -> t.isAssignableFrom(s) ? t.cast(v) : null,
            // target is String -> use toString()
            (t, s, v) -> t == String.class ? v.toString() : null,
            // convert array of type A[] to array of type B[]
            DataUtil::convertArray,
            // convert floating point numbers without fractional part to integer types
            DataUtil::convertToIntegralNumber,
            // convert other numbers to double
            DataUtil::convertToDouble,
            // convert other numbers to float
            DataUtil::convertToFloat,
            // convert String to LocalDate using the ISO format
            DataUtil::convertToLocalDate,
            // convert String to LocalDateTime using the ISO format
            DataUtil::convertToLocalDateTime,
            // Don't rely on Boolean.vOf(String) because it might introduce subtle bugs,
            // i. e. "TRUE()", "yes", "hello" all evaluate to false; throw ConversionException instead.
            DataUtil::convertToBoolean,
            // convert to Path
            DataUtil::convertToPath,
            // convert to File
            DataUtil::convertToFile,
            // convert to URI
            DataUtil::convertToUri,
            // convert to URL
            DataUtil::convertToUrl,
            // target provides public static vOf(U) where v is instance of U
            // (reason for iterating methods: getDeclaredMethod() will throw if vOf is not present)
            DataUtil::convertUsingValueOf,
    };

    /**
     * Converts the provided object to a Float if the target class is Float and the source class
     * is a subclass of Number. If these conditions are not met, returns null.
     *
     * @param t the target class type to which the object is to be converted
     * @param s the source class type of the provided object
     * @param v the object to be converted to a Float
     * @return the converted Float value if the criteria are met, or null otherwise
     */
    private static @Nullable Float convertToFloat(Class<?> t, Class<?> s, Object v) {
        return t == Float.class && Number.class.isAssignableFrom(s) ? ((Number) v).floatValue() : null;
    }

    /**
     * Converts the given object to a Double if the provided target class is Double
     * and the source class is assignable from Number. If the conditions are not
     * met, returns null.
     *
     * @param t the target class to which the value should be converted
     * @param s the source class of the value
     * @param v the object to be converted
     * @return the converted Double value if the conditions are satisfied; otherwise null
     */
    private static @Nullable Double convertToDouble(Class<?> t, Class<?> s, Object v) {
        return t == Double.class && Number.class.isAssignableFrom(s) ? ((Number) v).doubleValue() : null;
    }

    /**
     * Converts the given object to a {@code LocalDate} if the specified target class is {@code LocalDate}
     * and the source class is assignable from {@code CharSequence}. If the conditions are not met,
     * the method returns {@code null}.
     *
     * @param t the target class to which the object might be converted
     * @param s the source class of the object
     * @param v the object to be converted
     * @return the converted {@code LocalDate} object if conditions are met, otherwise {@code null}
     */
    private static @Nullable Object convertToLocalDate(Class<?> t, Class<?> s, Object v) {
        return t == LocalDate.class && CharSequence.class.isAssignableFrom(s)
                ? LocalDate.parse(v.toString(), DateTimeFormatter.ISO_DATE)
                : null;
    }

    /**
     * Converts the given object to a LocalDateTime instance if the target class is LocalDateTime
     * and the source class is assignable from CharSequence. Otherwise, returns null.
     *
     * @param t the target class type to which the conversion is to be applied
     * @param s the source class type of the provided object
     * @param v the value to be converted
     * @return the converted LocalDateTime object if the target class is LocalDateTime and the source
     *         class is assignable from CharSequence, or null otherwise
     */
    private static @Nullable Object convertToLocalDateTime(Class<?> t, Class<?> s, Object v) {
        return t == LocalDateTime.class && CharSequence.class.isAssignableFrom(s)
                ? LocalDateTime.parse(v.toString(), DateTimeFormatter.ISO_DATE_TIME)
                : null;
    }

    private static @Nullable Object convertUsingValueOf(Class<?> targetClass, Class<?> sourceClass, @NonNull Object value) {
        // first try exact match of parameter type
        // (reason for iterating methods: getDeclaredMethod() will throw if valueOf is not present)
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)
                    && method.getName().equals("valueOf")
                    && method.getParameterCount() == 1
                    && method.getParameterTypes()[0] == sourceClass
                    && targetClass.isAssignableFrom(method.getReturnType())) {
                try {
                    return method.invoke(null, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking valueOf(String)", e);
                }
            }
        }

        // else try primitives
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC)
                    && method.getName().equals("valueOf")
                    && method.getParameterCount() == 1
                    && LangUtil.isWrapperFor(sourceClass, method.getParameterTypes()[0])
                    && targetClass.isAssignableFrom(method.getReturnType())) {
                try {
                    return method.invoke(null, value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking valueOf(String)", e);
                }
            }
        }

        return null;
    }

    /**
     * Converts the given value into an instance of the target class using a public single-argument constructor
     * that matches the provided source class. If no suitable constructor is found, or if an exception occurs
     * during instantiation, the method either returns null or throws a {@link ConversionException}.
     *
     * @param targetClass the class to which the value should be converted
     * @param sourceClass the class of the source value that matches the constructor parameter type
     * @param value the source object to convert
     * @return an instance of the target class created using the matched constructor, or null if no matching constructor exists
     * @throws ConversionException if there is an error invoking the constructor
     */
    private static @Nullable Object convertUsingConstructor(Class<?> targetClass, Class<?> sourceClass, @NonNull Object value) {
        // try exact match
        for (Constructor<?> constructor : targetClass.getDeclaredConstructors()) {
            if (constructor.getModifiers() == (Modifier.PUBLIC)
                    && constructor.getParameterCount() == 1
                    && constructor.getParameterTypes()[0] == sourceClass
            ) {
                try {
                    return constructor.newInstance(value);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking constructor " + targetClass.getName() + "(String)", e);
                }
            }
        }

        // else try primitives
        for(Constructor<?> constructor :targetClass.getDeclaredConstructors()) {
            try {
                if (constructor.getModifiers() == (Modifier.PUBLIC)
                        && constructor.getParameterCount() == 1
                        && LangUtil.isWrapperFor(sourceClass, constructor.getParameterTypes()[0])
                ) {
                    return constructor.newInstance(value);
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new ConversionException(sourceClass, targetClass, "error invoking constructor " + targetClass.getName() + "(String)", e);
            }
        }

        return null;
    }

    /**
     * Converts the given value to a {@link Boolean} if the target class is {@link Boolean}
     * and the source class is assignable from {@link CharSequence}.
     * If the conditions are not met, the method returns {@code null}.
     *
     * @param targetClass The target class to which the value is expected to be converted.
     *                    It should be {@link Boolean} for successful conversion.
     * @param sourceClass The source class of the value being converted.
     *                    It should be assignable from {@link CharSequence}.
     * @param value The object to be converted to a {@link Boolean}.
     *              This method expects the {@code value} to represent a boolean textual
     *              value (e.g., "true" or "false").
     * @return A {@link Boolean} if the conversion is successful; otherwise, {@code null}.
     * @throws ConversionException if {@code value} contains invalid text
     *                                  that cannot be converted to a boolean.
     */
    private static @Nullable Object convertToBoolean(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (targetClass != Boolean.class || !CharSequence.class.isAssignableFrom(sourceClass)) {
            return null;
        }

        return switch (value.toString().toLowerCase(Locale.ROOT)) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new ConversionException("invalid text for boolean conversion: " + value);
        };
    }

    /**
     * Converts an array of one type to another type, provided both the target and source are arrays.
     * The method validates if the provided targetClass and sourceClass are array types.
     * If the conversion cannot be performed or an error occurs, an exception is thrown.
     *
     * @param targetClass the target array type to which the input array should be converted
     * @param sourceClass the source array type of the input array
     * @param value the object representing the array to be converted
     * @return the converted array as an object, or null if either targetClass or sourceClass is not an array type
     * @throws ConversionException if an error occurs during the conversion process
     */
    @SuppressWarnings("unchecked")
    private static @Nullable Object convertArray(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (!targetClass.isArray() || !sourceClass.isArray()) {
            return null;
        }

        try {
            int n = Array.getLength(value);
            return Arrays.copyOf((Object[]) value, n, (Class<Object[]>) targetClass);
        } catch (Exception e) {
            throw new ConversionException(sourceClass, targetClass, e);
        }
    }

    /**
     * Converts a floating-point number to an integral number of the specified target class
     * if and only if the conversion does not result in a loss of precision.
     *
     * @param targetClass the target integral type class, such as {@code Integer.class} or {@code Long.class}
     * @param sourceClass the source type class, which must be {@code Double.class} or {@code Float.class}
     * @param value the floating-point number to be converted
     * @return a {@code Number} representing the converted value as an instance of the target class,
     *         or {@code null} if the conversion is not applicable
     * @throws ConversionException if the value cannot be converted to the target class
     *         without loss of precision
     */
    private static @Nullable Object convertToIntegralNumber(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (sourceClass != Double.class && sourceClass != Float.class) {
            return null;
        }

        double d = ((Number) value).doubleValue();
        if (targetClass == Integer.class) {
            //noinspection NumericCastThatLosesPrecision
            int n = (int) d;
            //noinspection FloatingPointEquality
            LangUtil.check(n == d, () -> new ConversionException("value cannot be converted to int without loss of precision: " + value));
            return n;
        } else if (targetClass == Long.class) {
            //noinspection NumericCastThatLosesPrecision
            long n = (long) d;
            LangUtil.check(n == d, () -> new ConversionException("value cannot be converted to long without loss of precision: " + value));
            return n;
        }
        return null;
    }

    /**
     * Converts the given value to a {@link Path} if the target class is {@link Path}
     * and the source class matches one of the supported types. Supported source
     * types include {@link String}, {@link File}, {@link URI}, and {@link URL}.
     *
     * @param targetClass The target class to which the value is to be converted. It must be {@link Path}.
     * @param sourceClass The source class of the value being converted. Supported classes include {@link String}, {@link File}, {@link URI}, and {@link URL}.
     * @param value The value to be converted to a {@link Path}. The type of the value must match the source class.
     * @return A {@link Path} object resulting from the conversion if successful, or {@code null} if the target class is not {@link Path}
     *         or the source class is unsupported.
     * @throws ConversionException If the source class is {@link URL} and the value cannot be successfully converted to a {@link URI}.
     */
    private static @Nullable Object convertToPath(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (targetClass != Path.class) {
            return null;
        }

        if (CharSequence.class.isAssignableFrom(sourceClass)) {
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

    /**
     * Converts the given value to a {@code File} object based on the source class provided.
     * This method supports multiple source types, such as {@code String}, {@code Path}, {@code URI}, and {@code URL}.
     * If the target class is not {@code File}, the method returns {@code null}.
     *
     * @param targetClass the class of the target type to which the value needs to be converted
     * @param sourceClass the class of the source type of the provided value
     * @param value the object to be converted to a {@code File}
     * @return a {@code File} object if the conversion is successful and the target class is {@code File},
     * or {@code null} if the target class is not {@code File} or the conversion cannot be performed
     * @throws ConversionException if the source class is {@code URL} and the conversion to {@code File} fails
     * due to a {@code URISyntaxException}
     */
    private static @Nullable Object convertToFile(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (targetClass != File.class) {
            return null;
        }

        if (CharSequence.class.isAssignableFrom(sourceClass)) {
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

    /**
     * Converts the given value to a URI if the target class is {@link URI}.
     * Supports conversion from the source classes {@link String}, {@link File}, {@link URL}, and {@link Path}.
     * If the target class is not {@link URI} or the source class is not supported, returns null.
     *
     * @param targetClass the desired target class for the conversion, expected to be {@link URI}
     * @param sourceClass the actual class type of the provided value
     * @param value the object to be converted to a URI; must be compatible with the supplied source class
     * @return a URI representation of the given value if conversion is possible, or null if the target class
     *         is not {@link URI} or the source class is unsupported
     */
    private static @Nullable Object convertToUri(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (targetClass != URI.class) {
            return null;
        }

        if (CharSequence.class.isAssignableFrom(sourceClass)) {
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

    /**
     * Converts the given value to a {@link URL} if the target class is {@link URL},
     * based on the type of the source class. If the conversion is unsupported
     * or fails, a {@link ConversionException} is thrown for invalid conversions.
     *
     * @param targetClass the class to which the value is being converted; must be {@link URL}
     * @param sourceClass the class of the input value; determines the conversion logic
     * @param value the input object to be converted to a {@link URL}
     * @return the corresponding {@link URL} if conversion is successful, or {@code null}
     *         if the target class is not {@link URL} or conversion is unsupported
     * @throws ConversionException if the conversion fails
     */
    private static @Nullable Object convertToUrl(Class<?> targetClass, Class<?> sourceClass, Object value) {
        if (targetClass != URL.class) {
            return null;
        }

        if (CharSequence.class.isAssignableFrom(sourceClass)) {
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
    @SuppressWarnings("unchecked") // types are checked with isAssignable()
    public static <T> @Nullable T convert(@Nullable Object value, Class<T> targetClass, boolean useConstructor) {
        // null -> null
        if (value == null) {
            return null;
        }

        Class<?> sourceClass = value.getClass();

        for (var c : CONVERTERS) {
            Object r = c.tryConvert(targetClass, sourceClass, value);
            if (r != null) {
                return (T) r;
            }
        }

        // ... or provides a public constructor taking the value's class (and is enabled by parameter)
        if (useConstructor) {
            Object r = convertUsingConstructor(targetClass, sourceClass, value);
            if (r != null) {
                return (T) r;
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
    public static <T> boolean isSorted(Iterable<T> collection, Comparator<? super T> comparator) {
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
