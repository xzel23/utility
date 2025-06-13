// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            buffer.add("test " + (2 * CAPACITY +i));
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
}
