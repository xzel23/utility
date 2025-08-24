// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ConstantConditions")
class RingBufferTest {

    private static final int CAPACITY = 10;
    private final RingBuffer<Object> buffer = new RingBuffer<>(CAPACITY);

    @Test
    void testAddAndGet() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            assertEquals(Math.min(CAPACITY, i), buffer.size());
            buffer.add("test " + i);
            // test buffer content
            int first = Math.max(0, i - CAPACITY + 1);
            for (int j = first; j <= i; j++) {
                assertEquals("test " + j, buffer.get(j - first));
            }
        }
    }

    @Test
    void testAddWorksCorrectly() {
        buffer.clear();
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(buffer.add("element " + i));
            assertEquals(i + 1, buffer.size());
            assertEquals("element " + i, buffer.get(buffer.size() - 1));
        }
    }

    @Test
    void testAddBeyondCapacity() {
        buffer.clear();
        for (int i = 0; i < CAPACITY * 2; i++) {
            buffer.add("item " + i);
        }
        assertEquals(CAPACITY, buffer.size());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals("item " + (CAPACITY + i), buffer.get(i));
        }
    }

    @Test
    void testAddWithZeroCapacity() {
        RingBuffer<Object> zeroCapacityBuffer = new RingBuffer<>(0);
        assertFalse(zeroCapacityBuffer.add("test"));
        assertEquals(0, zeroCapacityBuffer.size());
        assertTrue(zeroCapacityBuffer.isEmpty());
    }

    @Test
    void testCapacity() {
        assertEquals(CAPACITY, buffer.capacity());
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
            assertEquals(CAPACITY, buffer.capacity());
        }
    }

    @Test
    void testGet() {
        buffer.clear();
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(0));
        buffer.add("Test1");
        assertEquals("Test1", buffer.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(-1));
        buffer.add("Test2");
        buffer.add("Test3");
        assertEquals("Test2", buffer.get(1));
        assertEquals("Test3", buffer.get(2));
    }

    @Test
    void testIsEmpty() {
        buffer.clear();
        assertTrue(buffer.isEmpty());
        buffer.add("Test");
        assertFalse(buffer.isEmpty());
        buffer.clear();
        assertTrue(buffer.isEmpty());
    }

    @Test
    void testSetCapacity() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
        }
        assertEquals(CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());
        assertEquals("test " + CAPACITY, buffer.get(0));
        assertEquals("test " + (2 * CAPACITY - 1), buffer.get(CAPACITY - 1));

        // elements should be retained when capacity is increased
        String asText = buffer.toString(); // compare content after resetting capacity
        buffer.setCapacity(2 * CAPACITY);
        assertEquals(2 * CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());
        assertEquals(asText, buffer.toString());
        assertEquals("test " + CAPACITY, buffer.get(0));
        assertEquals("test " + (2 * CAPACITY - 1), buffer.get(CAPACITY - 1));

        // add elements to see if capacity is set as expected
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + (2 * CAPACITY + i));
        }
        assertEquals(2 * CAPACITY, buffer.capacity());
        assertEquals(2 * CAPACITY, buffer.size());
        assertEquals("test " + (2 * CAPACITY), buffer.get(0));
        assertEquals("test " + (3 * CAPACITY - 1), buffer.get(CAPACITY - 1));
        assertEquals("test " + (4 * CAPACITY - 1), buffer.get(2 * CAPACITY - 1));

        // now reduce the size
        buffer.setCapacity(CAPACITY);
        assertEquals(CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals("test " + (3 * CAPACITY + i), buffer.get(i));
        }
    }

    @Test
    void testSize() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            assertEquals(Math.min(CAPACITY, i), buffer.size());
            buffer.add("test " + i);
        }
    }

    @Test
    void testToString() {
        buffer.clear();
        assertEquals("[]", buffer.toString());
        buffer.add("Test1");
        assertEquals("[Test1]", buffer.toString());
        buffer.add("Test2");
        assertEquals("[Test1, Test2]", buffer.toString());
        buffer.add("Test3");
        assertEquals("[Test1, Test2, Test3]", buffer.toString());
    }

    @Test
    void testSubList() {
        buffer.clear();

        // test with a partially filled buffer
        buffer.addAll(List.of(1, 2, 3, 4, 5));
        assertEquals(Collections.emptyList(), buffer.subList(0, 0));
        assertEquals(Collections.emptyList(), buffer.subList(4, 4));
        assertEquals(List.of(1, 2, 3, 4, 5), buffer.subList(0, 5));
        assertEquals(List.of(1, 2, 3, 4), buffer.subList(0, 4));
        assertEquals(List.of(2, 3, 4, 5), buffer.subList(1, 5));
        assertEquals(List.of(2, 3, 4), buffer.subList(1, 4));

        // test with a fully filled buffer
        buffer.addAll(List.of(6, 7, 8, 9, 10));
        assertEquals(Collections.emptyList(), buffer.subList(0, 0));
        assertEquals(Collections.emptyList(), buffer.subList(9, 9));
        assertEquals(List.of(1, 2, 3, 4, 5), buffer.subList(0, 5));
        assertEquals(List.of(1, 2, 3, 4), buffer.subList(0, 4));
        assertEquals(List.of(2, 3, 4, 5), buffer.subList(1, 5));
        assertEquals(List.of(2, 3, 4), buffer.subList(1, 4));

        // test after elements are discarded (contiguous sublist)
        buffer.addAll(List.of(11, 12, 13, 14, 15));
        assertEquals(Collections.emptyList(), buffer.subList(0, 0));
        assertEquals(Collections.emptyList(), buffer.subList(9, 9));
        assertEquals(List.of(6, 7, 8, 9, 10), buffer.subList(0, 5));
        assertEquals(List.of(6, 7, 8, 9), buffer.subList(0, 4));
        assertEquals(List.of(7, 8, 9, 10), buffer.subList(1, 5));
        assertEquals(List.of(7, 8, 9), buffer.subList(1, 4));
        assertEquals(List.of(6, 7, 8, 9, 10, 11, 12, 13, 14, 15), buffer.subList(0, 10));

        // test after elements are discarded (non-contiguous sublist)
        assertEquals(List.of(9, 10, 11, 12), buffer.subList(3, 7));
        assertEquals(List.of(15), buffer.subList(9, 10));
    }

    @Test
    void testAddAll() {
        buffer.clear();
        assertTrue(buffer.isEmpty());
        buffer.addAll(List.of(1, 2, 3, 4, 5));
        assertArrayEquals(List.of(1, 2, 3, 4, 5).toArray(), buffer.toArray());
        buffer.addAll(List.of(6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        assertArrayEquals(List.of(6, 7, 8, 9, 10, 11, 12, 13, 14, 15).toArray(), buffer.toArray());
    }

    @Test
    void testZeroCapacityBuffer() {
        // Create a zero capacity buffer
        RingBuffer<Object> zeroBuffer = new RingBuffer<>(0);

        // Test basic properties
        assertEquals(0, zeroBuffer.capacity());
        assertEquals(0, zeroBuffer.size());
        assertTrue(zeroBuffer.isEmpty());

        // Adding elements should have no effect
        zeroBuffer.add("test");
        assertEquals(0, zeroBuffer.size());
        assertTrue(zeroBuffer.isEmpty());

        // Adding collections should have no effect and return false
        assertFalse(zeroBuffer.addAll(List.of(1, 2, 3)));
        assertEquals(0, zeroBuffer.size());

        // toString should show empty buffer
        assertEquals("[]", zeroBuffer.toString());

        // toArray should return empty array
        assertArrayEquals(new Object[0], zeroBuffer.toArray());

        // subList with valid indices should return an empty list
        assertEquals(Collections.emptyList(), zeroBuffer.subList(0, 0));

        // contains should return false
        assertFalse(zeroBuffer.contains(null));
    }

    @Test
    void testRemoveFirst() {
        buffer.clear();

        // Test removing from empty buffer
        assertThrows(NoSuchElementException.class, buffer::removeFirst);

        // Test removing from buffer with one element
        buffer.add("Test1");
        assertEquals("Test1", buffer.removeFirst());
        assertTrue(buffer.isEmpty());

        // Test removing from buffer with multiple elements
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertEquals("Test1", buffer.removeFirst());
        assertEquals(2, buffer.size());
        assertEquals("Test2", buffer.get(0));
        assertEquals("Test3", buffer.get(1));

        // Test removing after buffer has wrapped around
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertEquals("item" + 5, buffer.removeFirst());
        assertEquals(CAPACITY - 1, buffer.size());
        assertEquals("item" + 6, buffer.get(0));
    }

    @Test
    void testRemoveLast() {
        buffer.clear();

        // Test removing from empty buffer
        assertThrows(NoSuchElementException.class, buffer::removeLast);

        // Test removing from buffer with one element
        buffer.add("Test1");
        assertEquals("Test1", buffer.removeLast());
        assertTrue(buffer.isEmpty());

        // Test removing from buffer with multiple elements
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertEquals("Test3", buffer.removeLast());
        assertEquals(2, buffer.size());
        assertEquals("Test1", buffer.get(0));
        assertEquals("Test2", buffer.get(1));

        // Test removing after buffer has wrapped around
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertEquals("item" + (CAPACITY + 4), buffer.removeLast());
        assertEquals(CAPACITY - 1, buffer.size());
        assertEquals("item" + 5, buffer.get(0));
    }

    @Test
    void testGetFirst() {
        buffer.clear();

        // Test getting from empty buffer
        assertThrows(NoSuchElementException.class, buffer::getFirst);

        // Test getting from buffer with one element
        buffer.add("Test1");
        assertEquals("Test1", buffer.getFirst());
        assertEquals(1, buffer.size()); // Size should not change

        // Test getting from buffer with multiple elements
        buffer.add("Test2");
        buffer.add("Test3");
        assertEquals("Test1", buffer.getFirst());
        assertEquals(3, buffer.size()); // Size should not change

        // Test getting after buffer has wrapped around
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertEquals("item" + 5, buffer.getFirst());
        assertEquals(CAPACITY, buffer.size()); // Size should not change
    }

    @Test
    void testGetLast() {
        buffer.clear();

        // Test getting from empty buffer
        assertThrows(NoSuchElementException.class, buffer::getLast);

        // Test getting from buffer with one element
        buffer.add("Test1");
        assertEquals("Test1", buffer.getLast());
        assertEquals(1, buffer.size()); // Size should not change

        // Test getting from buffer with multiple elements
        buffer.add("Test2");
        buffer.add("Test3");
        assertEquals("Test3", buffer.getLast());
        assertEquals(3, buffer.size()); // Size should not change

        // Test getting after buffer has wrapped around
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertEquals("item" + (CAPACITY + 4), buffer.getLast());
        assertEquals(CAPACITY, buffer.size()); // Size should not change
    }

    @Test
    void testAddFirst() {
        buffer.clear();

        // Test adding to empty buffer
        buffer.addFirst("Test1");
        assertEquals(1, buffer.size());
        assertEquals("Test1", buffer.get(0));

        // Test adding to buffer with elements
        buffer.addFirst("Test2");
        assertEquals(2, buffer.size());
        assertEquals("Test2", buffer.get(0));
        assertEquals("Test1", buffer.get(1));

        // Test adding to full buffer
        buffer.clear();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.add("item" + i);
        }
        buffer.addFirst("newItem");
        assertEquals(CAPACITY, buffer.size());
        assertEquals("newItem", buffer.get(0));
        assertEquals("item0", buffer.get(1));
        // Last item should be dropped
        assertThrows(IndexOutOfBoundsException.class, () -> buffer.get(CAPACITY));

        // Test with zero capacity buffer
        RingBuffer<Object> zeroBuffer = new RingBuffer<>(0);
        zeroBuffer.addFirst("test");
        assertEquals(0, zeroBuffer.size());
        assertTrue(zeroBuffer.isEmpty());
    }

    @Test
    void testAddLast() {
        buffer.clear();

        // Test adding to empty buffer
        buffer.addLast("Test1");
        assertEquals(1, buffer.size());
        assertEquals("Test1", buffer.get(0));

        // Test adding to buffer with elements
        buffer.addLast("Test2");
        assertEquals(2, buffer.size());
        assertEquals("Test1", buffer.get(0));
        assertEquals("Test2", buffer.get(1));

        // Test adding to full buffer
        buffer.clear();
        for (int i = 0; i < CAPACITY; i++) {
            buffer.add("item" + i);
        }
        buffer.addLast("newItem");
        assertEquals(CAPACITY, buffer.size());
        // First item should be dropped
        assertEquals("item1", buffer.get(0));
        assertEquals("newItem", buffer.get(CAPACITY - 1));

        // Test with zero capacity buffer
        RingBuffer<Object> zeroBuffer = new RingBuffer<>(0);
        zeroBuffer.addLast("test");
        assertEquals(0, zeroBuffer.size());
        assertTrue(zeroBuffer.isEmpty());
    }

    @Test
    void testRemoveIf() {
        buffer.clear();

        // Test on empty buffer
        assertFalse(buffer.removeIf(e -> true));
        assertTrue(buffer.isEmpty());

        // Test with no matching elements
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertFalse(buffer.removeIf(e -> e.equals("Test4")));
        assertEquals(3, buffer.size());
        assertEquals("Test1", buffer.get(0));
        assertEquals("Test2", buffer.get(1));
        assertEquals("Test3", buffer.get(2));

        // Test with some matching elements
        assertTrue(buffer.removeIf(e -> e.equals("Test2")));
        assertEquals(2, buffer.size());
        assertEquals("Test1", buffer.get(0));
        assertEquals("Test3", buffer.get(1));

        // Test with all matching elements
        assertTrue(buffer.removeIf(e -> true));
        assertTrue(buffer.isEmpty());

        // Test with wrapped buffer
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertTrue(buffer.removeIf(e -> ((String) e).contains("8") || ((String) e).contains("9")));
        assertEquals(8, buffer.size());
        // items 5, 6, 7, 10, 11, 12, 13, 14 should remain
        assertEquals("item5", buffer.get(0));
        assertEquals("item6", buffer.get(1));
        assertEquals("item7", buffer.get(2));
        assertEquals("item10", buffer.get(3));
    }

    @Test
    void testRemoveAll() {
        buffer.clear();

        // Test on empty buffer
        assertFalse(buffer.removeAll(List.of("Test1")));
        assertTrue(buffer.isEmpty());

        // Test with no matching elements
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertFalse(buffer.removeAll(List.of("Test4")));
        assertEquals(3, buffer.size());

        // Test with some matching elements
        assertTrue(buffer.removeAll(List.of("Test1", "Test3")));
        assertEquals(1, buffer.size());
        assertEquals("Test2", buffer.get(0));

        // Test with all matching elements
        buffer.clear();
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertTrue(buffer.removeAll(List.of("Test1", "Test2", "Test3")));
        assertTrue(buffer.isEmpty());

        // Test with wrapped buffer
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertTrue(buffer.removeAll(List.of("item8", "item9", "item10")));
        assertEquals(7, buffer.size());
    }

    @Test
    void testRetainAll() {
        buffer.clear();

        // Test on empty buffer
        assertFalse(buffer.retainAll(List.of("Test1")));
        assertTrue(buffer.isEmpty());

        // Test with all elements to be retained
        buffer.addAll(List.of("Test1", "Test2", "Test3"));
        assertFalse(buffer.retainAll(List.of("Test1", "Test2", "Test3")));
        assertEquals(3, buffer.size());

        // Test with some elements to be retained
        assertTrue(buffer.retainAll(List.of("Test1", "Test3")));
        assertEquals(2, buffer.size());
        assertEquals("Test1", buffer.get(0));
        assertEquals("Test3", buffer.get(1));

        // Test with no elements to be retained
        assertTrue(buffer.retainAll(List.of("Test4")));
        assertTrue(buffer.isEmpty());

        // Test with wrapped buffer
        buffer.clear();
        for (int i = 0; i < CAPACITY + 5; i++) {
            buffer.add("item" + i);
        }
        assertTrue(buffer.retainAll(List.of("item5", "item6", "item7")));
        assertEquals(3, buffer.size());
        assertEquals("item5", buffer.get(0));
        assertEquals("item6", buffer.get(1));
        assertEquals("item7", buffer.get(2));
    }

    @Test
    void testNullableElements() {
        RingBuffer<@Nullable String> b = new RingBuffer<>(CAPACITY);
        b.add("test");
        b.add(null);
        assertEquals(2, b.size());
        assertEquals("test", b.get(0));
        assertNull(b.get(1));
    }
}
