 package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.AbstractList;
import java.util.List;

/**
 * A simple unmodifiable wrapper for an array that implements the {@link List} interface.
 * This class provides a fixed-size, read-only view of the elements.
 *
 * @param <T> the type of elements in this list
 */
public class UnmodifiableArrayListWrapper<T extends @Nullable Object> extends AbstractList<T> {
    private final T[] elements;

    /**
     * Constructs an unmodifiable wrapper around the provided array of elements.
     * The elements are stored internally and cannot be modified after construction.
     *
     * @param elements the array of elements to be wrapped; this array must not be null
     */
    @SafeVarargs
    public UnmodifiableArrayListWrapper(T... elements) {
        this.elements = elements;
    }

    @Override
    public T get(int index) {
        return elements[index];
    }

    @Override
    public int size() {
        return elements.length;
    }
}
