package com.dua3.utility.data;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * A Filtering Iterator implementation.
 * <p>
 * The constructor takes an {@link Iterator} instance and a predicate on the element type. It constructs a new
 * Iterator instance with the same element type, but that leaves out all elements that do not match the
 * predicate.
 *
 * @param <T> the element t type
 */
public class FilterIterator<T> implements Iterator<T> {

    private final Iterator<? extends T> iterator;
    private final Predicate<? super T> predicate;
    private boolean done;
    private @Nullable 
T current;

    /**
     * Construct a new FilterIterator.
     *
     * @param iterator  the base iterator
     * @param predicate the predicate
     */
    public FilterIterator(Iterator<? extends T> iterator, Predicate<? super T> predicate) {
        this.iterator = iterator;
        this.predicate = predicate;
        this.done = false;
        findNext();
    }

    /**
     * Move the internal iterator to the next item that matches the predicate.
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
            throw new NoSuchElementException("there are no elements left");
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
