package com.dua3.utility.concurrent;

import org.jspecify.annotations.Nullable;

/**
 * Represents a mutable value of type T.
 * <p>
 * This class offers basic functionality comparable to the JavaFX {@code ObservableValue} class.
 *
 * @param <T> the type of the value
 */
public interface Value<T extends @Nullable Object> extends ReadOnlyValue<T> {
    /**
     * Sets the value of the method.
     *
     * @param v the value to be set
     */
    void set(T v);

    /**
     * Creates a new Value object with the initial value.
     *
     * @param initialValue the initial value for the Value object
     * @param <T> the type of the value
     * @return a new Value object with the initial value
     */
    static <T extends @Nullable Object> Value<T> create(T initialValue) {
        return new SimpleValue<>(initialValue);
    }

    /**
     * Creates a new ReadOnlyValue object with the initial value.
     *
     * @param initialValue the initial value for the ReadOnlyValue object
     * @param <T> the type of the value
     * @return a new ReadOnlyValue object with the initial value
     */
    static <T extends @Nullable Object> ReadOnlyValue<T> createReadOnly(T initialValue) {
        return create(initialValue);
    }
}

