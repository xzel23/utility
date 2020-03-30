package com.dua3.utility.data;

import com.dua3.utility.lang.LangUtil;

import java.lang.reflect.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DataUtil {

    /**
     * Exception thrown when data conversion fails.
     */
    public static class ConversionException extends IllegalArgumentException {
        private final String sourceClassName;
        private final String targetClassName;

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
            return String.format("%s%n[trying to convert %s -> %s]", super.getMessage(), sourceClassName, targetClassName);
        }
    }

    /**
     * Convert object to a different class.
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
     * @param value
     *  the object to convert
     * @param targetClass
     *  the target class
     * @param <T>
     *  target type
     * @return
     *  the object converted to the target class
     */
    public static<T> T convert(Object value, Class<T> targetClass) {
        return convert(value, targetClass, false);
    }

    /**
     * Convert object to a different class.
     * <p>
     * Conversion works as follows:
     * <ul>
     *     <li> if value is {@code null}, {@code null} is returned;
     *     <li> if the target class is assignment compatible, a simple cast is performed;
     *     <li> if the target class is {@link String}, {@link Object#toString()} is used;
     *     <li> if the target class is an integer type and the value is of type double, a conversion without loss of precision is tried;
     *     <li> if the target class is {@link LocalDate} and the source class is {@link String}, use DateTimeFormatter.ISO_DATE;
     *     <li> if the target class is {@link java.time.LocalDateTime} and the source class is {@link String}, use DateTimeFormatter.ISO_DATE_TIME;
     *     <li> if the target class provides a method {@code public static T valueOf(U)} and {value instanceof U}, that method is invoked;
     *     <li> if {@code useConstructor} is {@code true} and the target class provides a constructor taking a single argument of value's type, that constructor is used;
     *     <li> otherwise an exception is thrown.
     * </ul>
     * @param value
     *  the object to convert
     * @param targetClass
     *  the target class
     * @param useConstructor
     *  flag whether a public constructor {@code T(U)} should be used in conversion if present where `U` is the value's class
     * @param <T>
     *  target type
     * @return
     *  the object converted to the target class
     */
    @SuppressWarnings("unchecked") // types are checked with isAssignable()
    public static<T> T convert(Object value, Class<T> targetClass, boolean useConstructor) {
        // null -> null
        if (value==null) {
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
                LangUtil.check(n==d, () -> new IllegalArgumentException("value cannot be converted to int without loss of precision: " + value));
                return (T)(Integer) n;
            } else if (targetClass == Long.class) {
                //noinspection NumericCastThatLosesPrecision
                long n = (long) d;
                LangUtil.check(n==d, () -> new IllegalArgumentException("value cannot be converted to long without loss of precision: " + value));
                return (T)(Long) n;
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
            //noinspection ConstantConditions
            switch (((String) value).toLowerCase(Locale.ROOT)) {
                case "true":
                    return (T) Boolean.TRUE;
                case "false":
                    return (T) Boolean.FALSE;
                default:
                    throw new IllegalArgumentException("invalid text for boolean conversion: "+value);
            }
        }

        // target provides public static valueOf(U) where value is instance of U
        // (reason for iterating methods: getDeclaredMethod() will throw if valueOf is not present)
        for (Method method: targetClass.getDeclaredMethods()) {
            if ( method.getModifiers()==( Modifier.PUBLIC | Modifier.STATIC )
                 && method.getName().equals("valueOf")
                 && method.getParameterCount()==1
                 && method.getParameterTypes()[0] == sourceClass
                 && targetClass.isAssignableFrom(method.getReturnType())) {
                try {
                    return (T) method.invoke(null, value);
                } catch (IllegalAccessException|InvocationTargetException e) {
                    throw new ConversionException(sourceClass, targetClass, "error invoking valueOf(String)", e);
                }
            }
        }

        // ... or provides a public constructor taking the value's class (and is enabled by parameter)
        if (useConstructor) {
            for (Constructor<?> constructor: targetClass.getDeclaredConstructors()) {
                if ( constructor.getModifiers()==( Modifier.PUBLIC )
                        && constructor.getParameterCount()==1
                        && constructor.getParameterTypes()[0] == sourceClass) {
                    try {
                        return (T) constructor.newInstance(value);
                    } catch (IllegalAccessException|InvocationTargetException|InstantiationException e) {
                        throw new ConversionException(sourceClass, targetClass, "error invoking constructor "+targetClass.getName()+"(String)", e);
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
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @return
     *  array containing the converted elements
     */
    public static <T,U> U[] convertToArray(Collection<T> data, Class<U> targetClass) {
        return convertToArray(data, targetClass, false);
    }

    /**
     * Convert Collection to array.
     * <p>
     * Converts a {@code Collection<T>} to {@code U[]} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param useConstructor
     *  flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @return
     *  array containing the converted elements
     */
    @SuppressWarnings("unchecked")
    public static <T,U> U[] convertToArray(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        return data.stream()
                .map(obj -> DataUtil.convert(obj, targetClass, useConstructor))
                .toArray( n -> (U[]) Array.newInstance(targetClass, n));
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
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @return
     *  list containing the converted elements
     */
    public static <T,U> List<U> convert(Collection<T> data, Class<U> targetClass) {
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
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param useConstructor
     *  flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @return
     *  list containing the converted elements
     */
    public static <T,U> List<U> convert(Collection<T> data, Class<U> targetClass, boolean useConstructor) {
        return data.stream()
                .map(obj -> DataUtil.convert(obj, targetClass, useConstructor))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Convert Collection.
     * <p>
     * Converts a {@code Collection<T>} to {@code Collection<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param supplier
     *  the collection supplier, i. e. {@code ArrayList::new}
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @param <C>
     *  the target collection type
     * @return
     *  collection containing the converted elements
     */
    public static <T,U,C extends Collection<U>> C convertCollection(Collection<T> data, Class<U> targetClass, Supplier<C> supplier) {
        return convertCollection(data, targetClass, supplier, false);
    }

    /**
     * Convert Collection to list.
     * <p>
     * Converts a {@code Collection<T>} to {@code Collection<U>} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param supplier
     *  the collection supplier, i. e. {@code ArrayList::new}
     * @param useConstructor
     *  flag whether a public constructor {@code U(T)} should be used in conversion if present
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @param <C>
     *  the target collection type
     * @return
     *  collection containing the converted elements
     */
    public static <T,U,C extends Collection<U>> C convertCollection(Collection<T> data, Class<U> targetClass, Supplier<C> supplier, boolean useConstructor) {
        return data.stream()
                .map(obj -> DataUtil.convert(obj, targetClass, useConstructor))
                .collect(Collectors.toCollection(supplier));
    }

    /**
     * Create a filtering iterator that only lets through items matching a predicate.
     * @param iterator
     *  the base iterator
     * @param predicate
     *  the predicate to test items with
     * @param <T>
     *  the item type
     * @return
     *  iterator instance that skips items not matching the predicate
     */
    public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<T> predicate) {
        return new FilterIterator<>(iterator, predicate);
    }

    /**
     * Create a mapping iterator that converts elements on the fly.
     * @param iterator
     *  the base iterator
     * @param mapping
     *  the mapping to apply to elements
     * @param <T>
     *  the source iterator item type
     * @param <U>
     *  the target iterator item type
     * @return
     *  iterator instance that converts items of type {@code T} to {@code U}
     */
    public static <T,U> Iterator<U> map(Iterator<T> iterator, Function<T,U> mapping) {
        return new MappingIterator<>(iterator, mapping);
    }

    /**
     * Collect items from an iterable into a List.
     * @param iterable
     *  the iterable
     * @param <T>
     *  the element type
     * @return
     *  list of elements
     */
    public static <T>  List<T> collect(Iterable<T> iterable) {
        return collect(iterable.iterator());
    }

    /**
     * Collect items from an iterator into a List.
     * @param iterator
     *  the iterator
     * @param <T>
     *  the element type
     * @return
     *  list of elements
     */
    public static <T>  List<T> collect(Iterator<T> iterator) {
        List<T> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result;
    }

    /**
     * Collect items from an iterable into an array.
     * @param iterable
     *  the iterable
     * @param <T>
     *  the element type
     * @return
     *  array of elements
     */
    @SuppressWarnings("unchecked")
    public static <T>  T[] collectArray(Iterable<T> iterable) {
        return (T[]) collect(iterable.iterator()).toArray();
    }

    /**
     * Collect items from an iterator into an array.
     * @param <T>
     *  the element type
     * @param iterator
     *  the iterator
     * @return
     *  array of elements
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] collectArray(Iterator<T> iterator) {
        return (T[]) collect(iterator).toArray();
    }

    // Utility class - private constructor
    private DataUtil() {}

}
