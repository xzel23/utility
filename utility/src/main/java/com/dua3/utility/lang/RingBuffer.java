// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import java.util.ArrayList;

/**
 * A ring buffer implementation.
 * This class behaves much like @see {@link ArrayList}, but with a fixed maximum
 * size.
 * The collection grows when new elements are added until the capacity is
 * reached. If even more items
 * are added, the oldest element is removed and the new element is appended to
 * the collection.
 * Adding is O(1).
 *
 * @param <T>
 *        the element type
 */
public class RingBuffer<T> {

    private Object[] data;
    private int entries;
    private int start;

    /**
     * Construct a new RingBuffer instance.
     *
     * @param capacity
     *                 the initial capacity
     */
    public RingBuffer(int capacity) {
        data = new Object[capacity];
        start = 0;
        entries = 0;
    }

    /**
     * Add item to end of collection.
     *
     * @param item
     *             the item to add
     */
    public void add(T item) {
        if (entries < capacity()) {
            data[index(entries++)] = item;
        } else {
            start = (start + 1) % capacity();
            data[index(entries - 1)] = item;
        }
    }

    /**
     * Get collection's capacity.
     *
     * @return the capacity
     */
    public int capacity() {
        return data.length;
    }

    /**
     * Remove all elements.
     */
    public void clear() {
        start = entries = 0;
    }

    /**
     * Get element.
     *
     * @param  i
     *           index
     * @return   the i-th element
     */
    @SuppressWarnings("unchecked")
    public T get(int i) {
        checkIndex(i);
        return (T) data[index(i)];
    }

    /**
     * Test if collection is empty.
     *
     * @return true if this buffer is empty
     */
    public boolean isEmpty() {
        return entries == 0;
    }

    /**
     * Set the capacity. Elements are retained.
     *
     * @param n
     *          the new capacity.
     */
    public void setCapacity(int n) {
        if (n != capacity()) {
            Object[] dataNew = new Object[n];
            int itemsToCopy = Math.min(size(), n);
            int startIndex = Math.max(0, size() - n);
            for (int i = 0; i < itemsToCopy; i++) {
                dataNew[i] = get(startIndex + i);
            }
            data = dataNew;
            start = 0;
            entries = Math.min(entries, n);
        }
    }

    /**
     * Get number of items in collection.
     *
     * @return number of elements
     */
    public int size() {
        return entries;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16 * (1 + size()));
        sb.append("[");
        String d = "";
        for (int i = 0; i < size(); i++) {
            sb.append(d);
            sb.append(get(i));
            d = ", ";
        }
        sb.append("]");
        return sb.toString();
    }

    private void checkIndex(int i) {
        if (i < 0 || i >= size()) {
            throw new IndexOutOfBoundsException("size=" + size() + ", index=" + i);
        }
    }

    private int index(int i) {
        return (start + i) % capacity();
    }
}
