package com.dua3.utility.data;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A peekable Iterator implementation.
 * <p>
 * The constructor takes an {@link Iterator} instance and creates an iterator that offers a peek() method to inspect
 * the next element.
 * @param <T>
 *  the element t type
 */
public class PeekIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private boolean done = false;
    private T current = null;

    /**
     * Construct a new PeekIterator.
     * @param iterator
     *  the base iterator
     */
    public PeekIterator(Iterator<T> iterator) {
        this.iterator = Objects.requireNonNull(iterator);
        move();
    }

    @Override
    public T next() {
        if (done) {
            throw new NoSuchElementException("there are no elements left");
        }

        T item = current;
        move();
        return item;
    }

    public T peek() {
        if (done) {
            throw new NoSuchElementException("there are no elements left");
        }

        return current;
    }

    @Override
    public boolean hasNext() {
        return !done;
    }

    private void move() {
        if (done) {
            return;
        }

        if (iterator.hasNext()) {
            T item = iterator.next();
            current = item;
            return;
        }

        // no more items found
        current = null;
        done = true;
    }

}
