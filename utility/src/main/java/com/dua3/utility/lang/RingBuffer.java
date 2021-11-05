// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

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
 * @param <E>
 *        the element type
 */
public class RingBuffer<E> implements Collection<E> {

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
     * @return
     *             true 
     */
    public boolean add(E item) {
        if (entries < capacity()) {
            data[index(entries++)] = item;
        } else {
            start = (start + 1) % capacity();
            data[index(entries - 1)] = item;
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove() is not supported");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return Arrays.asList(toArray()).containsAll(c);
    }

    /**
     * Add item to end of collection.
     *
     * @param items
     *             collection containing the items to add
     * @return
     *             true, if the buffer changed as a result of this operation
     */
    public boolean addAll(@NotNull Collection<? extends E> items) {
        if (items.isEmpty()) {
            return false;
        }
        
        for (E item: items) {
            if (entries<capacity()) {
                data[index(entries++)] = item;
            } else {
                start = (start + 1) % capacity();
                data[index(entries - 1)] = item;
            }
        }
        
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("removeAll() is not supported");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("retainAll() is not supported");
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
    public @NotNull E get(int i) {
        checkIndex(i);
        return (E) data[index(i)];
    }

    /**
     * Test if collection is empty.
     *
     * @return true if this buffer is empty
     */
    public boolean isEmpty() {
        return entries == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (E item: this) {
            if (Objects.equals(item, o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        final int entries_ = entries;
        final int start_ = start;
        
        return new Iterator<>() {
            int idx = 0;

            private void checkValid() {
                LangUtil.check(
                        start_ == start && entries_ == entries, 
                        () -> new ConcurrentModificationException("RingBuffer was modified")
                );
            }

            @Override
            public boolean hasNext() {
                checkValid();
                return idx < entries_;
            }

            @Override
            public @NotNull E next() throws NoSuchElementException{
                checkValid();
                LangUtil.check(idx<entries_, NoSuchElementException::new);
                return get(idx++);
            }
        };
    }

    @Override
    public Object @NotNull [] toArray() {
        Object[] arr = new Object[entries];
        int n1 = Math.min(entries, data.length-start);
        int n2 = entries-n1;
        System.arraycopy(data, start, arr, 0, n1);
        System.arraycopy(data, 0, arr, n1, n2);
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T @NotNull [] toArray(T @NotNull [] a) {
        if (a.length < entries) {
            a = (T[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), entries);
        }

        // copy contents to array
        int n1 = Math.min(entries, data.length-start);
        int n2 = entries-n1;
        System.arraycopy(data, start, a, 0, n1);
        System.arraycopy(data, 0, a, n1, n2);

        if (a.length > entries)
            a[entries] = null;
        
        return a;
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
    public @NotNull String toString() {
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

    /**
     * Returns a view of the portion of this buffer between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this buffer.
     * 
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *         ({@code fromIndex < 0 || toIndex > size ||
     *         fromIndex > toIndex})
     */
    public @NotNull List<E> subList(int fromIndex, int toIndex) {
        int s1 = size();
        LangUtil.checkIndex(fromIndex, s1);
        LangUtil.check(toIndex<=s1, "toIndex>size(): %d", toIndex);

        final int s2 = toIndex-fromIndex;
        LangUtil.check(s2>=0, "toIndex<fromIndex: fromIndex=%d, toIndex=%d", fromIndex, toIndex);

        return new AbstractList<>() {
            @Override
            public @NotNull E get(int index) {
                LangUtil.checkIndex(index, s2);
                return RingBuffer.this.get(index + fromIndex);
            }

            @Override
            public int size() {
                return s2;
            }
        };
    }
}
