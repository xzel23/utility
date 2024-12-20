package com.dua3.utility.data;

import java.util.function.Function;

/**
 * An interface for converting between two types, A and B.
 * This interface provides methods to define forward and backward conversion functions
 * and to execute these conversions.
 *
 * @param <A> the type of the source object
 * @param <B> the type of the target object
 */
public interface Converter<A, B> {
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
     */
    default B convert(A a) { return a2b().apply(a); }

    /**
     * Converts the target object of type B back to a source object of type A using the provided conversion function.
     *
     * @param b the object of type B to be converted back to type A
     * @return the converted object of type A
     */
    default A convertBack(B b) { return b2a().apply(b); }

    /**
     * Returns the inverse of this converter, swapping the source and target types.
     *
     * @return a converter that converts in the opposite direction, from target type to source type.
     */
    Converter<B,A> inverse();

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
        return new SimpleConverter<>(a2b,b2a);
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
record SimpleConverter<A, B>(Function<A, B> a2b, Function<B, A> b2a) implements Converter<A, B> {
    @Override
    public Converter<B, A> inverse() {
        return new SimpleConverter<>(b2a, a2b);
    }
}
