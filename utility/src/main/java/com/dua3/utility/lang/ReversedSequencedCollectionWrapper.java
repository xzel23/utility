package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;

/**
 * A wrapper class that reverses the order of elements in a {@link SequencedCollection}.
 * This class delegates the underlying operations to the provided {@link SequencedCollection}
 * while ensuring that the order of elements appears reversed when accessed through this wrapper.
 *
 * @param <T> the type of elements in the collection, which allows nullable elements
 */
public class ReversedSequencedCollectionWrapper<T extends @Nullable Object> implements SequencedCollection<T> {
    private final SequencedCollection<T> delegate;

    /**
     * Constructs a new {@code ReversedSequencedCollectionWrapper} that wraps the specified
     * {@link SequencedCollection}. The underlying collection's order will appear reversed
     * when accessed through this wrapper.
     *
     * @param delegate the {@link SequencedCollection} to be wrapped and reversed
     */
    public ReversedSequencedCollectionWrapper(SequencedCollection<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public SequencedCollection<T> reversed() {
        return delegate;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return new ArrayList<>(delegate).reversed().iterator();
    }

    @Override
    public Object[] toArray() {
        Object[] array = delegate.toArray();
        LangUtil.reverseInPlace(array);
        return array;
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        T1[] array = delegate.toArray(a);
        LangUtil.reverseInPlace(array, 0, size());
        return array;
    }

    @Override
    public boolean add(T t) {
        delegate.addFirst(t);
        return !isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c.isEmpty()) {
            return false;
        }

        c.forEach(delegate::addFirst);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }
}
