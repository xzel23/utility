package com.dua3.utility.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A Filtering Iteratoe implementation.
 * <p>
 * The constructor takes an {@link Iterator} instance and a predicate on the element type. It constrcuts a new
 * Iterator instance with the same element type, but that leaves out all elements that do not match the
 * predicate.
 * @param <T>
 *  the elemen t type
 */
public class FilterIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private final Predicate<T> predicate;
    private boolean done = false;
    private T current = null;

    /**
     * Construct a new {@link FilterIterator}.
     * @param iterator
     *  the base iterator
     * @param predicate
     * the predicate
     */
    public FilterIterator(Iterator<T> iterator, Predicate<T> predicate) {
        this.iterator = Objects.requireNonNull(iterator);
        this.predicate = Objects.requireNonNull(predicate);
        findNext();
    }

    /**
     * Move internal iterator to the next item that matches the predicate.
     */
    private void findNext() {
        if (done) {
            return;
        }

        // try to find the next matching item
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (predicate.test(item)) {
                current = item;
                return;
            }
        }

        // no more items found
        done = true;
    }

    @Override
    public T next() {
        if (done) {
            throw new NoSuchElementException();
        }

        T item = current;
        findNext();
        return item;
    }

    @Override
    public boolean hasNext() {
        return !done;
    }

}