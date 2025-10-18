package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.Spliterator;

/**
 * An immutable collection that combines the properties of a {@link java.util.SortedSet} and a
 * {@link java.util.List}. Elements are unique and kept in sorted order defined by the collection’s
 * comparator (if present) or by their natural order otherwise. Implementations are expected to
 * disallow null elements (the project uses a {@code @NullMarked} context).
 *
 * <p>The {@link java.util.SortedSet#comparator()} method returns the comparator used for ordering,
 * or {@code null} to indicate natural ordering, following the standard contract.
 *
 * @param <T> the type of elements in this collection
 */
public interface ImmutableSortedListSet<T extends @Nullable Object> extends List<T>, SortedSet<T> {
    @Override
    ImmutableSortedListSet<T> reversed();

    @Override
    T getFirst();

    @Override
    T getLast();

    @Override
    default boolean add(T t) {
        throw new UnsupportedOperationException("add() is not supported");
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException("remove() is not supported");
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll() is not supported");
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll() is not supported");
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll() is not supported");
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException("clear() is not supported");
    }

    @Override
    default void addFirst(T t) {
        throw new UnsupportedOperationException("addFirst() is not supported");
    }

    @Override
    default void addLast(T t) {
        throw new UnsupportedOperationException("addLast() is not supported");
    }

    @Override
    default T removeLast() {
        throw new UnsupportedOperationException("removeLast() is not supported");
    }

    @Override
    default T removeFirst() {
        throw new UnsupportedOperationException("removeFirst() is not supported");
    }

    @Override
    default Spliterator<T> spliterator() {
        return List.super.spliterator();
    }

    @Override
    ImmutableSortedListSet<T> subSet(T fromElement, T toElement);

    @Override
    List<T> subList(int fromIndex, int toIndex);
}
