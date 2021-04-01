package com.dua3.utility.data;

import java.util.Iterator;
import java.util.function.Function;

/**
 * An Iterator implementation that does on-the-fly conversion of elements.
 * @param <T>
 *  the source iterator element type
 * @param <U>
 *  the target iterator element type
 */
class MappingIterator<T,U> implements Iterator<U> {
    private final Iterator<T> iterator;
    private final Function<? super T, U> mapping;

    /**
     * Construct new instance.
     * @param iterator
     *  the base iterator
     * @param mapping
     *  the element mapping
     */
    MappingIterator(Iterator<T> iterator, Function<? super T, U> mapping) {
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
