// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Predicate;

/**
 * A ring buffer implementation.
 * This class behaves much like @see {@link ArrayList}, but with a fixed maximum
 * size.
 * <p>
 * The collection grows when new elements are added until the capacity is reached. If even more items are added, the
 * oldest element is removed and the new element appended to the collection.
 * Adding is O(1).
 *
 * @param <T> the element type
 */
public class RingBuffer<T extends @Nullable Object> implements SequencedCollection<T> {

    private @Nullable T[] data;
    private int entries;
    private int start;

    /**
     * Construct a new RingBuffer instance.
     *
     * @param capacity the initial capacity
     */
    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        data = (@Nullable T[]) new Object[capacity];
        start = 0;
        entries = 0;
    }

    /**
     * Add item to the end of the collection.
     *
     * @param item the item to add
     * @return true, if the item was added; false if the capacity is zero
     */
    @Override
    public boolean add(T item) {
        return put(item);
    }

    /**
     * Add an item at the end of this collection. If the item count has reached the capacity, the first item will be
     * removed.
     *
     * @param item the item to add
     * @return true, if the buffer size increased as a result of this operation (in other words, false if an item
     *               previously contained in the buffer was replaced)
     */
    public boolean put(T item) {
        int n = capacity();
        if (n <= 0) {
            return false;
        }
        if (entries < n) {
            data[index(entries++)] = item;
            return true;
        } else {
            start = (start + 1) % n;
            data[index(entries - 1)] = item;
            return false;
        }
    }

    @Override
    public boolean remove(@Nullable Object o) {
        if (isEmpty()) {
            return false;
        }

        int i = indexOf(o);
        if (i < 0) {
            return false;
        }

        T current = data[start];
        data[start] = null;
        for (int j = 1; j <= i; j++) {
            int idx = index(j);
            T next = data[idx];
            data[idx] = current;
            current = next;
        }
        start = (start + 1) % capacity();
        entries--;
        return true;
    }

    /**
     * Finds the index of the first occurrence of the specified object in the buffer,
     * starting from the defined start position and including the entire range of entries.
     *
     * @param o the object to locate in the buffer. Can be null.
     * @return the index of the first occurrence of the specified object, or -1 if the object is not found.
     */
    private int indexOf(@Nullable Object o) {
        for (int i = 0; i < size(); i++) {
            if (Objects.equals(get(i), o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return new HashSet<>(this).containsAll(c);
    }

    /**
     * Add item to end of collection.
     *
     * @param items collection containing the items to add
     * @return true, if the buffer changed as a result of this operation
     */
    @Override
    public boolean addAll(Collection<? extends @Nullable T> items) {
        if (items.isEmpty() || capacity() == 0) {
            return false;
        }

        for (T item : items) {
            if (entries < capacity()) {
                data[index(entries++)] = item;
            } else {
                start = (start + 1) % capacity();
                data[index(entries - 1)] = item;
            }
        }

        return true;
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
    @Override
    public void clear() {
        if (capacity() > 0) {
            start = entries = 0;
            Arrays.fill(data, null);
        }
    }

    /**
     * Get element.
     *
     * @param i index
     * @return the i-th element
     */
    public @Nullable T get(int i) {
        checkIndex(i);
        return data[index(i)];
    }

    /**
     * Test if collection is empty.
     *
     * @return true if this buffer is empty
     */
    @Override
    public boolean isEmpty() {
        return entries == 0;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        for (T item : this) {
            if (Objects.equals(item, o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        final int entries_ = entries;
        final int start_ = start;

        return new Iterator<>() {
            int idx;

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
            public T next() throws NoSuchElementException {
                checkValid();
                //noinspection NewExceptionWithoutArguments - the index is an internal detail that would be confusing int this context
                LangUtil.check(idx < entries_, NoSuchElementException::new);
                return get(idx++);
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[entries];
        int n1 = Math.min(entries, data.length - start);
        int n2 = entries - n1;
        System.arraycopy(data, start, arr, 0, n1);
        System.arraycopy(data, 0, arr, n1, n2);
        return arr;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> @Nullable U[] toArray(@Nullable U[] a) {
        if (a.length < entries) {
            a = (U[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), entries);
        }

        // copy contents to array
        int n1 = Math.min(entries, data.length - start);
        int n2 = entries - n1;
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(data, start, a, 0, n1);
        //noinspection SuspiciousSystemArraycopy
        System.arraycopy(data, 0, a, n1, n2);

        if (a.length > entries) {
            a[entries] = null;
        }

        return a;
    }

    /**
     * Set the capacity. Elements are retained.
     *
     * @param n the new capacity.
     */
    @SuppressWarnings("unchecked")
    public void setCapacity(int n) {
        if (n != capacity()) {
            var dataNew = (@Nullable T[]) new Object[n];
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
    @Override
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

    /**
     * Returns a view of the portion of this buffer between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.  (If
     * {@code fromIndex} and {@code toIndex} are equal, the returned list is
     * empty.)  The returned list is backed by this buffer.
     *
     * @param fromIndex low endpoint (inclusive) of the subList
     * @param toIndex   high endpoint (exclusive) of the subList
     * @return a view of the specified range within this list
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   ({@code fromIndex < 0 || toIndex > size ||
     *                                   fromIndex > toIndex})
     */
    public List<T> subList(int fromIndex, int toIndex) {
        int len = size();
        Objects.checkFromToIndex(fromIndex, toIndex, len);
        int sz = toIndex - fromIndex;
        Objects.checkFromIndexSize(fromIndex, sz, len);

        //noinspection NullableProblems - false positive; T is @Nullable
        return new AbstractList<>() {
            @Override
            public T get(int index) {
                return RingBuffer.this.get(Objects.checkIndex(index, sz) + fromIndex);
            }

            @Override
            public int size() {
                return sz;
            }
        };
    }

    @Override
    public SequencedCollection<T> reversed() {
        return new ReversedSequencedCollectionWrapper<>(this);
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("RingBuffer is empty");
        }

        int n = capacity();
        int idx = index(0);
        T tmp = data[idx];
        data[idx] = null;
        start = (start + 1) % n;
        entries--;
        return tmp;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("RingBuffer is empty");
        }

        int idx = index(size() - 1);
        T tmp = data[idx];
        data[idx] = null;
        entries--;
        return tmp;
    }

    @Override
    public T getFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("RingBuffer is empty");
        }
        return data[index(0)];
    }

    @Override
    public T getLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("RingBuffer is empty");
        }
        return data[index(size() - 1)];
    }

    @Override
    public void addFirst(T t) {
        if (capacity() == 0) {
            return;
        }
        int n = capacity();
        if (entries >= n) {
            int idx = index(n - 1);
            data[idx] = t;
            start = Math.floorMod((start - 1), n);
        } else {
            start = Math.floorMod((start - 1), n);
            entries++;
            data[start] = t;
        }
    }

    @Override
    public void addLast(T t) {
        put(t);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        int currentSize = size();
        int readIndex = 0;
        int writeIndex = 0;

        while (readIndex < currentSize) {
            T item = get(readIndex);
            if (!filter.test(item)) {
                if (writeIndex != readIndex) {
                    data[index(writeIndex)] = item;
                }
                writeIndex++;
            }
            readIndex++;
        }

        if (writeIndex != currentSize) {
            // Clear any remaining elements
            for (int i = writeIndex; i < currentSize; i++) {
                data[index(i)] = null;
            }
            entries = writeIndex;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return removeIf(c::contains);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return removeIf(item -> !c.contains(item));
    }
}
