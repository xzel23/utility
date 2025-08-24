package com.dua3.utility.lang;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the methods of the instance created by RingBuffer.reversed().
 */
class RingBufferReversedTest {

    private static final int CAPACITY = 10;
    private RingBuffer<Integer> buffer;
    private SequencedCollection<Integer> reversed;

    @BeforeEach
    void setUp() {
        buffer = new RingBuffer<>(CAPACITY);
        // Fill the buffer with some test data
        for (int i = 1; i <= 5; i++) {
            buffer.add(i);
        }
        // Get the reversed view
        reversed = buffer.reversed();
    }

    @Test
    void testReversed() {
        // The reversed() method of the reversed collection should return the original collection
        assertEquals(buffer, reversed.reversed());
    }

    @Test
    void testSize() {
        assertEquals(buffer.size(), reversed.size());
        buffer.add(6);
        assertEquals(buffer.size(), reversed.size());
    }

    @Test
    void testIsEmpty() {
        assertFalse(reversed.isEmpty());

        // Create an empty buffer and test its reversed view
        RingBuffer<Integer> emptyBuffer = new RingBuffer<>(CAPACITY);
        SequencedCollection<Integer> emptyReversed = emptyBuffer.reversed();
        assertTrue(emptyReversed.isEmpty());
    }

    @Test
    void testContains() {
        assertTrue(reversed.contains(1));
        assertTrue(reversed.contains(5));
        assertFalse(reversed.contains(10));
        assertFalse(reversed.contains("not an integer"));
    }

    @Test
    void testIterator() {
        Iterator<Integer> iterator = reversed.iterator();
        // The iterator should return elements in reverse order: 5, 4, 3, 2, 1
        assertTrue(iterator.hasNext());
        assertEquals(5, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(4, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(3, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(2, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(1, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(java.util.NoSuchElementException.class, iterator::next);
    }

    @Test
    void testToArray() {
        Object[] array = reversed.toArray();
        assertEquals(5, array.length);
        // The array should contain elements in reverse order: 5, 4, 3, 2, 1
        assertArrayEquals(new Object[]{5, 4, 3, 2, 1}, array);
    }

    @Test
    void testToArrayWithParameter() {
        Integer[] array = new Integer[5];
        Integer[] result = reversed.toArray(array);
        // The array should be the same instance if it's large enough
        assertArrayEquals(array, result);
        // The array should contain elements in reverse order: 5, 4, 3, 2, 1
        assertArrayEquals(new Integer[]{5, 4, 3, 2, 1}, result);

        // Test with a larger array
        Integer[] largerArray = new Integer[10];
        Integer[] largerResult = reversed.toArray(largerArray);
        assertArrayEquals(largerArray, largerResult);
        // The first 5 elements should be 5, 4, 3, 2, 1, and the 6th element should be null
        assertEquals(5, largerResult[0]);
        assertEquals(4, largerResult[1]);
        assertEquals(3, largerResult[2]);
        assertEquals(2, largerResult[3]);
        assertEquals(1, largerResult[4]);
        assertNull(largerResult[5]);

        // Test with a smaller array
        Integer[] smallerArray = new Integer[3];
        Integer[] smallerResult = reversed.toArray(smallerArray);
        // The array should be a new instance if it's too small
        assertNotSame(smallerArray, smallerResult);
        assertEquals(5, smallerResult.length);
        assertArrayEquals(new Integer[]{5, 4, 3, 2, 1}, smallerResult);
    }

    @Test
    void testAdd() {
        // Adding to the reversed collection should add to the beginning of the original collection
        reversed.add(6);
        assertEquals(6, buffer.size());
        assertEquals(6, buffer.get(0));
        assertEquals(5, buffer.get(5));

        // The reversed collection should now contain 6, 1, 2, 3, 4, 5 in reverse order
        assertArrayEquals(new Object[]{5, 4, 3, 2, 1, 6}, reversed.toArray());
    }

    @Test
    void testRemove() {
        // The remove method should remove the element from the original collection
        boolean removed = reversed.remove(3);
        assertTrue(removed);
        assertEquals(4, buffer.size());
        assertFalse(buffer.contains(3));

        // Try to remove an element that doesn't exist
        boolean notRemoved = reversed.remove(10);
        assertFalse(notRemoved);
        assertEquals(4, buffer.size());
    }

    @Test
    void testContainsAll() {
        Collection<Integer> collection = Arrays.asList(1, 3, 5);
        assertTrue(reversed.containsAll(collection));

        Collection<Integer> notContained = Arrays.asList(1, 10);
        assertFalse(reversed.containsAll(notContained));
    }

    @Test
    void testAddAll() {
        Collection<Integer> toAdd = Arrays.asList(6, 7, 8);
        boolean added = reversed.addAll(toAdd);
        assertTrue(added);
        assertEquals(8, buffer.size());

        // The elements should be added to the original collection
        // The buffer should now contain 8, 7, 6, 1, 2, 3, 4, 5
        assertEquals(8, buffer.get(0));
        assertEquals(7, buffer.get(1));
        assertEquals(6, buffer.get(2));

        // The reversed collection should contain 5, 4, 3, 2, 1, 6, 7, 8
        assertArrayEquals(new Object[]{5, 4, 3, 2, 1, 6, 7, 8}, reversed.toArray());
    }

    @Test
    void testRemoveAll() {
        Collection<Integer> toRemove = Arrays.asList(2, 4);
        boolean removed = reversed.removeAll(toRemove);
        assertTrue(removed);
        assertEquals(3, buffer.size());

        // The buffer should now contain 1, 3, 5
        assertTrue(buffer.contains(1));
        assertTrue(buffer.contains(3));
        assertTrue(buffer.contains(5));
        assertFalse(buffer.contains(2));
        assertFalse(buffer.contains(4));

        // The reversed collection should contain 5, 3, 1
        assertArrayEquals(new Object[]{5, 3, 1}, reversed.toArray());
    }

    @Test
    void testRetainAll() {
        Collection<Integer> toRetain = Arrays.asList(1, 3, 5);
        boolean modified = reversed.retainAll(toRetain);
        assertTrue(modified);
        assertEquals(3, buffer.size());

        // The buffer should now contain 1, 3, 5
        assertTrue(buffer.contains(1));
        assertTrue(buffer.contains(3));
        assertTrue(buffer.contains(5));
        assertFalse(buffer.contains(2));
        assertFalse(buffer.contains(4));

        // The reversed collection should contain 5, 3, 1
        assertArrayEquals(new Object[]{5, 3, 1}, reversed.toArray());
    }

    @Test
    void testClear() {
        reversed.clear();
        assertTrue(buffer.isEmpty());
        assertTrue(reversed.isEmpty());
    }
}
