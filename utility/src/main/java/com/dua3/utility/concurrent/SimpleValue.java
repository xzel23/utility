package com.dua3.utility.concurrent;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * A simple implementation of the Value interface that stores a single value of any type.
 *
 * @param <T> the type of the value
 */
public final class SimpleValue<T extends @Nullable Object> implements Value<T> {
    private T v;
    private final List<BiConsumer<? super T, ? super T>> changeListeners = new CopyOnWriteArrayList<>();

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
        return Collections.unmodifiableCollection(changeListeners);
    }

    @Override
    public void set(T v) {
        T oldV = this.v;
        if (!Objects.equals(oldV, v)) {
            this.v = v;
            changeListeners.forEach(listener -> listener.accept(oldV, v));
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof SimpleValue<?> other)) {
            return false;
        }
        return other == this || Objects.equals(v, other.v);
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
