package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

/**
 * An interface for converting between two types, A and B.
 * This interface provides methods to define forward and backward conversion functions
 * and to execute these conversions.
 *
 * @param <A> the type of the source object
 * @param <B> the type of the target object
 */
public interface Converter<A extends @Nullable Object, B extends @Nullable Object> {

    /**
     * Provides a function to convert an object of type A to an object of type B.
     *
     * @return a function that takes an instance of type A and returns an instance of type B
     */
    Function<A, B> a2b();

    /**
     * Provides a function for converting an object of type B to an object of type A.
     *
     * @return a function that converts an object of type B to type A
     */
    Function<B, A> b2a();

    /**
     * Converts an object of type A to type B using the conversion function defined in a2b().
     *
     * @param a the object of type A to be converted
     * @return the converted object of type B
     * @throws ConversionException if the conversion fails
     */
    default B convert(A a) {
        try {
            return a2b().apply(a);
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Converts the target object of type B back to a source object of type A using the provided conversion function.
     *
     * @param b the object of type B to be converted back to type A
     * @return the converted object of type A
     * @throws ConversionException if the conversion fails
     */
    default A convertBack(B b) {
        try {
            return b2a().apply(b);
        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Returns the inverse of this converter, swapping the source and target types.
     *
     * @return a converter that converts in the opposite direction, from target type to source type.
     */
    default Converter<B, A> inverse() {
        return new InverseConverter<>(this);
    }

    /**
     * Provides a converter that performs an identity transformation, where the input object
     * is returned as-is without any modifications.
     * This is useful for cases where no actual conversion is required.
     *
     * @param <T> the type of the object being transformed
     * @return a Converter that performs an identity transformation for objects of type T
     */
    static <T> Converter<T, T> identity() {
        return new SimpleConverter<>(Function.identity(), Function.identity());
    }

    /**
     * Creates a converter to transform objects of type T to their String representation
     * and parse Strings back to objects of type T.
     *
     * @param <T> the type of the object to convert to and from a String
     * @param parse a function that parses a String to create an object of type T
     * @return a Converter that handles the bidirectional conversion between objects of type T and Strings
     */
    static <T> Converter<String, T> stringConverter(Function<String, T> parse) {
        return new SimpleConverter<>(parse, Objects::toString);
    }

    /**
     * Creates a converter to transform objects of type T to their String representation
     * and parse Strings back to objects of type T using either the method {@code public static T valueOf(String)}
     * in the specified class (preferred) or the constructor {@code public T(String)} of the class. If neither are
     * found, a {@code ConverterException} is thrown.
     * <p>
     * Enums are formatted using {@code name()} instead of {@code toString()}.
     *
     * @param <T> the type of the object to convert to and from a String
     * @param type the class type for which the converter is created, requiring a public static {@code valueOf(String)} method
     * @return a Converter that handles the bidirectional conversion between objects of type T and Strings
     * @throws ConversionException if the specified class does not declare the matching factory method or constructor
     */
    static <T> Converter<String, T> stringConverter(Class<T> type) throws ConversionException {
        if (type.isEnum()) {
            return new SimpleConverter<>(
                    s -> (T) Enum.valueOf((Class<? extends Enum>) type, s),
                    v -> ((Enum<?>) v).name()
            );
        }

        // does the class declare public static T valueOf(String)?
        for (Method m : type.getDeclaredMethods()) {
            // check in order: public static T valueOf(String)
            if ((m.getModifiers() & Modifier.PUBLIC) != 0
                    && (m.getModifiers() & java.lang.reflect.Modifier.STATIC) != 0
                    && m.getReturnType().equals(type)
                    && m.getName().equals("valueOf")
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].equals(String.class)) {
                return stringConverter(s -> parse(s, type, m));
            }
        }

        // does the class declare public T(String)?
        for (Constructor<?> c : type.getDeclaredConstructors()) {
            // check in order: public static T valueOf(String)
            if ((c.getModifiers() & Modifier.PUBLIC) != 0
                    && c.getParameterCount() == 1
                    && c.getParameterTypes()[0].equals(String.class)) {
                return stringConverter(s -> create(s, type, c));
            }
        }

        throw new ConversionException(
                "%s declares neither `public static valueOf(String)` nor `public %s(String)`".formatted(
                        type.getName(),
                        type.getSimpleName()
                )
        );
    }

    /**
     * Parses a string into an object of the specified type using the provided method.
     *
     * @param <T> the type of the object to be returned
     * @param s the string to be parsed
     * @param type the class of the object to which the string will be converted
     * @param m the method used to parse the string, typically a static method like {@code valueOf(String)} or similar
     * @return the parsed object of the specified type
     * @throws ConversionException if unable to parse the string into the specified type
     */
    private static <T> T parse(String s, Class<T> type, Method m) throws ConversionException {
        try {
            return type.cast(m.invoke(null, s));
        } catch (Exception e) {
            throw new ConversionException("cannot parse string \""+s+"\" to "+type.getName(), e);
        }
    }

    /**
     * Creates an instance of the specified type by invoking the given constructor with the provided string argument.
     *
     * @param <T> the type of the object to be created
     * @param s the string to be passed as an argument to the constructor
     * @param type the class of the object to be created
     * @param c the constructor used to create the object
     * @return an instance of the specified type created using the provided constructor
     * @throws ConversionException if the object cannot be created, either due to an instantiation error
     * or if the specified string cannot be converted properly
     */
    private static <T> T create(String s, Class<T> type, Constructor<?> c) throws ConversionException {
        try {
            return type.cast(c.newInstance(s));
        } catch (Exception e) {
            throw new ConversionException("cannot parse string \""+s+"\" to "+type.getName(), e);
        }
    }

    /**
     * Creates a bidirectional converter between two types A and B.
     *
     * @param <A> the source type
     * @param <B> the target type
     * @param a2b a function that converts from type A to type B
     * @param b2a a function that converts from type B to type A
     * @return a Converter that performs conversions between types A and B using the specified functions
     */
    static <A, B> Converter<A, B> create(Function<A, B> a2b, Function<B, A> b2a) {
        return new SimpleConverter<>(a2b, b2a);
    }
}

/**
 * A record that represents an inverse converter. This class wraps around another
 * {@link Converter} and provides the reversed conversion functions.
 *
 * @param <A> the type of the source object in the inverse conversion
 * @param <B> the type of the target object in the inverse conversion
 * @param inverse the original converter being wrapped, which provides the conversion
 *                methods to be inverted
 */
record InverseConverter<A, B>(Converter<B, A> inverse) implements Converter<A, B> {

    @Override
    public Function<A, B> a2b() {
        return inverse.b2a();
    }

    @Override
    public Function<B, A> b2a() {
        return inverse.a2b();
    }
}

/**
 * A record implementation of the Converter interface that provides a bidirectional conversion
 * between two types using given functions. This implementation allows you to convert an
 * object of type A to an object of type B and vice versa.
 *
 * @param <A> the source type
 * @param <B> the target type
 */
record SimpleConverter<A, B>(Function<A, B> a2b, Function<B, A> b2a) implements Converter<A, B> {}
