package com.dua3.utility.lang;

import org.junit.Assert;
import org.junit.Test;

public class RingBufferTest {
    
    private final int CAPACITY = 10;
    private RingBuffer<Object> buffer = new RingBuffer<>(CAPACITY);
    
    @Test
    public void testCapacity() {
        Assert.assertEquals(CAPACITY, buffer.capacity());
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
            Assert.assertEquals(CAPACITY, buffer.capacity());
        }
    }
    
    @Test
    public void testSize() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            Assert.assertEquals(Math.min(CAPACITY, i), buffer.size());
            buffer.add("test " + i);
        }
    }
    
    @Test
    public void testAddAndGet() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            Assert.assertEquals(Math.min(CAPACITY, i), buffer.size());
            buffer.add("test " + i);
            // test buffer content
            int last = i;
            int first = Math.max(0, last - CAPACITY + 1);
            for (int j = first; j <= last; j++) {
                Assert.assertEquals("test " + j, buffer.get(j - first));
            }
        }
    }
    
    @Test
    public void testIsEmpty() {
        buffer.clear();
        Assert.assertTrue(buffer.isEmpty());
        buffer.add("Test");
        Assert.assertFalse(buffer.isEmpty());
        buffer.clear();
        Assert.assertTrue(buffer.isEmpty());
    }
    
    @Test
    public void testGet() {
        buffer.clear();
        try {
            buffer.get(0);
            Assert.fail("IndexOutOfBoundsException not thrown.");
        } catch (IndexOutOfBoundsException e) {
            // nop
        }
        buffer.add("Test1");
        Assert.assertEquals("Test1", buffer.get(0));
        try {
            buffer.get(1);
            Assert.fail("IndexOutOfBoundsException not thrown.");
        } catch (IndexOutOfBoundsException e) {
            // nop
        }
        try {
            buffer.get(-1);
            Assert.fail("IndexOutOfBoundsException not thrown.");
        } catch (IndexOutOfBoundsException e) {
            // nop
        }
        buffer.add("Test2");
        buffer.add("Test3");
        Assert.assertEquals("Test2", buffer.get(1));
        Assert.assertEquals("Test3", buffer.get(2));
    }
    
    @Test
    public void testToString() {
        buffer.clear();
        Assert.assertEquals("[]", buffer.toString());
        buffer.add("Test1");
        Assert.assertEquals("[Test1]", buffer.toString());
        buffer.add("Test2");
        Assert.assertEquals("[Test1, Test2]", buffer.toString());
        buffer.add("Test3");
        Assert.assertEquals("[Test1, Test2, Test3]", buffer.toString());
    }
    
    @Test
    public void testSetCapacity() {
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
        }
        Assert.assertEquals(CAPACITY, buffer.capacity());
        Assert.assertEquals(CAPACITY, buffer.size());
        
        // elements should be retained when capacity is increased
        String asText = buffer.toString(); // compare content after resetting capacity
        buffer.setCapacity(2 * CAPACITY);
        Assert.assertEquals(2 * CAPACITY, buffer.capacity());
        Assert.assertEquals(CAPACITY, buffer.size());
        Assert.assertEquals(asText, buffer.toString());
        
        // add elements to see if capacity is set as expected
        for (int i = 0; i < 2 * CAPACITY; i++) {
            buffer.add("test " + i);
        }
        Assert.assertEquals(2 * CAPACITY, buffer.capacity());
        Assert.assertEquals(2 * CAPACITY, buffer.size());
        
        // now reduce the size
        buffer.setCapacity(CAPACITY);
        Assert.assertEquals(CAPACITY, buffer.capacity());
        Assert.assertEquals(CAPACITY, buffer.size());
        for (int i = 0; i < CAPACITY; i++) {
            Assert.assertEquals("test " + (CAPACITY + i), buffer.get(i));
        }
    }
    
}
