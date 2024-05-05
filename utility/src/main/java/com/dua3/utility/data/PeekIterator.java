package com.dua3.utility.data;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A peekable Iterator implementation.
 * <p>
 * The constructor takes an {@link Iterator} instance and creates an iterator that offers a peek() method to inspect
 * the next element.
 *
 * @param <T> the element t type
 */
public class PeekIterator<T> implements Iterator<T> {

    private final Iterator<? extends T> iterator;
    private boolean done;
    private T current;

    /**
     * Construct a new PeekIterator.
     *
     * @param iterator the base iterator
     */
    public PeekIterator(Iterator<? extends T> iterator) {
        this.iterator = iterator;
        this.done = false;
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

    /**
     * Peek next element.
     *
     * @return the element that will be returned when {@link #next()} is called without advancing the iterator.
     * @throws NoSuchElementException when there are no elements left
     */
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
            current = iterator.next();
            return;
        }

        // no more items found
        current = null;
        done = true;
    }

}
