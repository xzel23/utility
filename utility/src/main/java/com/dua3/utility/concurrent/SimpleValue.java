package com.dua3.utility.concurrent;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * A simple implementation of the Value interface that stores a single value of any type.
 *
 * @param <T> the type of the value
 */
public class SimpleValue<T extends @Nullable Object> implements Value<T> {
    private T v;
    private final transient List<BiConsumer<? super T, ? super T>> changeListeners = new ArrayList<>();

    /**
     * Initializes a new instance of the {@code SimpleValue} class.
     *
     * @param initialValue the initial value of the {@code SimpleValue}
     */
    public SimpleValue(T initialValue) {
        this.v = initialValue;
    }

    @Override
    public T get() {
        return v;
    }

    @Override
    public void addChangeListener(BiConsumer<? super T, ? super T> listener) {
        changeListeners.add(listener);
    }

    @Override
    public void removeChangeListener(BiConsumer<? super T, ? super T> listener) {
        changeListeners.remove(listener);
    }

    @Override
    public Collection<BiConsumer<? super T, ? super T>> getChangeListeners() {
        return changeListeners;
    }

    @Override
    public void set(T v) {
        T oldV = this.v;
        this.v = v;
        changeListeners.forEach(listener -> listener.accept(oldV, v));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleValue<?> that = (SimpleValue<?>) o;
        return Objects.equals(v, that.v);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        return Objects.hash(v);
    }

    @Override
    public String toString() {
        return String.valueOf(v);
    }
}
