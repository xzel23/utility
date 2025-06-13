package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RingBuffer with zero capacity.
 */
class RingBufferZeroCapacityTest {

    private final RingBuffer<Object> buffer = new RingBuffer<>(0);

    @Test
    void testAddAndGet() {
        // Adding to a zero capacity buffer should have no effect
        for (int i = 0; i < 10; i++) {
            assertEquals(0, buffer.size());
            buffer.add("test " + i);
            assertEquals(0, buffer.size());
        }

        // Attempting to get from an empty buffer should throw an exception
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(0));
    }

    @Test
    void testCapacity() {
        assertEquals(0, buffer.capacity());
        for (int i = 0; i < 10; i++) {
            buffer.add("test " + i);
            assertEquals(0, buffer.capacity());
        }
    }

    @Test
    void testIsEmpty() {
        buffer.clear();
        assertTrue(buffer.isEmpty());
        buffer.add("Test");
        assertTrue(buffer.isEmpty()); // Should still be empty with zero capacity
    }

    @Test
    void testSetCapacity() {
        assertEquals(0, buffer.capacity());

        // Set to non-zero capacity
        buffer.setCapacity(5);
        assertEquals(5, buffer.capacity());
        assertEquals(0, buffer.size());

        // Add some elements
        for (int i = 0; i < 3; i++) {
            buffer.add("test " + i);
        }
        assertEquals(3, buffer.size());

        // Set back to zero capacity
        buffer.setCapacity(0);
        assertEquals(0, buffer.capacity());
        assertEquals(0, buffer.size());
    }

    @Test
    void testSize() {
        for (int i = 0; i < 10; i++) {
            assertEquals(0, buffer.size());
            buffer.add("test " + i);
        }
    }

    @Test
    void testToString() {
        buffer.clear();
        assertEquals("[]", buffer.toString());
        buffer.add("Test1");
        assertEquals("[]", buffer.toString()); // Should be empty with zero capacity
    }

    @Test
    void testSubList() {
        buffer.clear();

        // Empty buffer should have empty subList
        assertEquals(Collections.emptyList(), buffer.subList(0, 0));

        // Adding elements should have no effect
        buffer.addAll(List.of(1, 2, 3, 4, 5));
        assertEquals(Collections.emptyList(), buffer.subList(0, 0));

        // Out of bounds indices should throw exception
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.subList(0, 1));
    }

    @Test
    void testAddAll() {
        buffer.clear();
        assertTrue(buffer.isEmpty());

        // Adding to zero capacity buffer should have no effect
        boolean result = buffer.addAll(List.of(1, 2, 3, 4, 5));
        assertFalse(result); // Should return false as buffer didn't change
        assertEquals(0, buffer.size());
        assertArrayEquals(new Object[0], buffer.toArray());
    }

    @Test
    void testPut() {
        buffer.clear();

        // Putting to a zero capacity buffer should have no effect and return false
        boolean result = buffer.put("test");
        assertFalse(result);
        assertEquals(0, buffer.size());
    }

    @Test
    void testContainsAll() {
        buffer.clear();

        // Empty buffer contains empty collection
        assertTrue(buffer.containsAll(Collections.emptyList()));

        // Empty buffer doesn't contain non-empty collection
        assertFalse(buffer.containsAll(List.of("test")));
    }

    @Test
    void testContains() {
        buffer.clear();

        // Empty buffer doesn't contain anything
        assertFalse(buffer.contains("test"));
        assertFalse(buffer.contains(null));
    }

    @Test
    void testIterator() {
        buffer.clear();

        // Iterator of empty buffer should have no elements
        Iterator<Object> iterator = buffer.iterator();
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    void testToArrayWithType() {
        buffer.clear();

        // toArray with type parameter should return empty array
        String[] array = new String[0];
        String[] result = buffer.toArray(array);
        assertEquals(0, result.length);

        // If provided array is larger, first element after size should be null
        String[] largerArray = new String[1];
        largerArray[0] = "not null";
        String[] largerResult = buffer.toArray(largerArray);
        assertSame(largerArray, largerResult); // Should return the same array
        assertNull(largerResult[0]); // First element should be set to null
    }
}
