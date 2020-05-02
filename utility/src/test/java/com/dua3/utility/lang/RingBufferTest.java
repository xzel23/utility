// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class RingBufferTest {

    private static final int CAPACITY = 10;
    private final RingBuffer<Object> buffer = new RingBuffer<>(CAPACITY);

    @Test
    public void testAddAndGet() {
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
    public void testCapacity() {
        assertEquals(CAPACITY, buffer.capacity());
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
            assertEquals(CAPACITY, buffer.capacity());
        }
    }

    @Test
    public void testGet() {
        buffer.clear();
        try {
            System.out.println(buffer.get(0));
            fail("IndexOutOfBoundsException not thrown.");
        } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
            // nop
        }
        buffer.add("Test1");
        assertEquals("Test1", buffer.get(0));
        try {
            System.out.println(buffer.get(1));
            fail("IndexOutOfBoundsException not thrown.");
        } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
            // nop
        }
        try {
            System.out.println(buffer.get(-1));
            fail("IndexOutOfBoundsException not thrown.");
        } catch (@SuppressWarnings("unused") IndexOutOfBoundsException e) {
            // nop
        }
        buffer.add("Test2");
        buffer.add("Test3");
        assertEquals("Test2", buffer.get(1));
        assertEquals("Test3", buffer.get(2));
    }

    @Test
    public void testIsEmpty() {
        buffer.clear();
        assertTrue(buffer.isEmpty());
        buffer.add("Test");
        assertFalse(buffer.isEmpty());
        buffer.clear();
        assertTrue(buffer.isEmpty());
    }

    @Test
    public void testSetCapacity() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
        }
        assertEquals(CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());

        // elements should be retained when capacity is increased
        String asText = buffer.toString(); // compare content after resetting capacity
        buffer.setCapacity(2 * CAPACITY);
        assertEquals(2 * CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());
        assertEquals(asText, buffer.toString());

        // add elements to see if capacity is set as expected
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
        }
        assertEquals(2 * CAPACITY, buffer.capacity());
        assertEquals(2 * CAPACITY, buffer.size());

        // now reduce the size
        buffer.setCapacity(CAPACITY);
        assertEquals(CAPACITY, buffer.capacity());
        assertEquals(CAPACITY, buffer.size());
        for (int i = 0; i < CAPACITY; i++) {
            assertEquals("test " + (CAPACITY + i), buffer.get(i));
        }
    }

    @Test
    public void testSize() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            assertEquals(Math.min(CAPACITY, i), buffer.size());
            buffer.add("test " + i);
        }
    }

    @Test
    public void testToString() {
        buffer.clear();
        assertEquals("[]", buffer.toString());
        buffer.add("Test1");
        assertEquals("[Test1]", buffer.toString());
        buffer.add("Test2");
        assertEquals("[Test1, Test2]", buffer.toString());
        buffer.add("Test3");
        assertEquals("[Test1, Test2, Test3]", buffer.toString());
    }

}
