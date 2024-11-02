package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

/**
 * An Iterator implementation that does on-the-fly conversion of elements.
 *
 * @param <T> the source iterator element type
 * @param <U> the target iterator element type
 */
class MappingIterator<T extends @Nullable Object, U extends @Nullable Object> implements Iterator<U> {
    private final Iterator<? extends T> iterator;
    private final Function<? super T, ? extends U> mapping;

    /**
     * Construct new instance.
     *
     * @param iterator the base iterator
     * @param mapping  the element mapping
     */
    MappingIterator(Iterator<? extends T> iterator, Function<? super T, ? extends U> mapping) {
        this.iterator = iterator;
        this.mapping = mapping;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public U next() {
        return mapping.apply(iterator.next());
    }
}
