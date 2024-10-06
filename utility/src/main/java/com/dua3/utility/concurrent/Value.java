package com.dua3.utility.concurrent;

import org.jspecify.annotations.Nullable;

/**
 * Represents a mutable value of type T.
 *
 * @param <T> the type of the value
 */
public interface Value<T> extends ReadOnlyValue<T> {
    /**
     * Sets the value of the method.
     *
     * @param v the value to be set
     */
    void set(@Nullable T v);

    /**
     * Creates a new Value object with the initial value.
     *
     * @param initialValue the initial value for the Value object
     * @param <T> the type of the value
     * @return a new Value object with the initial value
     */
    static <T> Value<T> create(@Nullable T initialValue) {
        return new SimpleValue<>(initialValue);
    }

    /**
     * Creates a new ReadOnlyValue object with the initial value.
     *
     * @param initialValue the initial value for the ReadOnlyValue object
     * @param <T> the type of the value
     * @return a new ReadOnlyValue object with the initial value
     */
    static <T> ReadOnlyValue<T> createReadOnly(@Nullable T initialValue) {
        return create(initialValue);
    }
}

