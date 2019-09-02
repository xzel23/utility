package com.dua3.utility.data;

import com.dua3.utility.lang.LangUtil;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;

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

        ConversionException(Class<?> sourceClass, Class<?> targetClass, Throwable cause) {
            super(cause);
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
     *     <li> if the target class is an integer type and the value is of type double, a conversion witgout loss of precision is tried;
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
    public static<T> T convert(Object value, Class<T> targetClass, boolean useConstructor) {
        // null -> null
        if (value==null) {
            return null;
        }

        // assignemt compatible?
        Class<?> sourceClass = value.getClass();
        if (targetClass.isAssignableFrom(sourceClass)) {
            return (T) value;
        }

        // target is String -> use toString()
        if (targetClass.equals(String.class)) {
            return (T) value.toString();
        }

        // convert floating point numbers without fractional part to integer types
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (targetClass.equals(Integer.class)) {
                int n = (int) d;
                LangUtil.check(n==d, "value cannot be converted to int without loss of precision: %f", value);
                return (T)(Integer) n;
            } else if (targetClass.equals(Long.class)) {
                long n = (long) d;
                LangUtil.check(n==d, "value cannot be converted to long without loss of precision: %f", value);
                return (T)(Long) n;
            }
            throw new ConversionException(sourceClass, targetClass, "unsupported numerical conversion");
        }

        // target provides public static valueOf(U) where value is instance of U
        // (reason for iterating methods: getDeclaredMethod() will throw if valueOf is not present)
        for (var method: targetClass.getDeclaredMethods()) {
            if ( method.getModifiers()==( Modifier.PUBLIC | Modifier.STATIC )
                 && method.getName().equals("valueOf")
                 && method.getParameterCount()==1
                 && method.getParameterTypes()[0].equals(sourceClass)
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
            for (var constructor: targetClass.getDeclaredConstructors()) {
                if ( constructor.getModifiers()==( Modifier.PUBLIC )
                        && constructor.getParameterCount()==1
                        && constructor.getParameterTypes()[0].equals(sourceClass)) {
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
     * Converts a {@code Collection<T>} to {@code T[]} by using {@link #convert(Object, Class)} on
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
    @SuppressWarnings("unchecked")
    public static <T,U> U[] convertToArray(Collection<T> data, Class<U> targetClass) {
        return convertToArray(data, targetClass, false);
    }

    /**
     * Convert Collection to array.
     * <p>
     * Converts a {@code Collection<T>} to {@code> T[]} by using {@link #convert(Object, Class)} on
     * the elements contained in the collection.
     *
     * @param data
     *  the collection to convert
     * @param targetClass
     *  the element target class
     * @param useStringConstructor
     *  flag whether a public constructor {@code T(String)} should be used in conversion if present
     * @param <T>
     *  the element source type
     * @param <U>
     *  the element target type
     * @return
     *  array containing the converted elements
     */
    @SuppressWarnings("unchecked")
    public static <T,U> U[] convertToArray(Collection<T> data, Class<U> targetClass, boolean useStringConstructor) {
        return data.stream()
                .map(obj -> DataUtil.convert(obj, targetClass, useStringConstructor))
                .toArray( n -> (U[]) Array.newInstance(targetClass, n));
    }

    // Utility class - private constructor
    private DataUtil() {}
}
