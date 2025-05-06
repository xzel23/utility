package com.dua3.utility.lang;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.Spliterator;

/**
 * An interface representing an immutable collection that combines the properties of a sorted set
 * and a list. This ensures that elements are ordered and unique, while also allowing indexed
 * access to the elements.
 *
 * @param <T> the type of elements in this collection.
 */
public interface ImmutableSortedListSet<T> extends List<T>, SortedSet<T> {
    @Override
    ImmutableSortedListSet<T> reversed();

    @Override
    T getFirst();

    @Override
    T getLast();

    @Override
    default boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default void addFirst(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    default void addLast(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    default T removeLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    default T removeFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Spliterator<T> spliterator() {
        return List.super.spliterator();
    }
}
