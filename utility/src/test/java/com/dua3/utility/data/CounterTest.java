package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link Counter}.
 */
class CounterTest {

    @Test
    void testInitialValue() {
        // Create a new Counter
        Counter counter = new Counter();

        // Test initial value is 0
        assertEquals(0, counter.get());
    }

    @Test
    void testIncrement() {
        // Create a new Counter
        Counter counter = new Counter();

        // Test increment
        counter.increment();
        assertEquals(1, counter.get());

        // Test multiple increments
        counter.increment();
        counter.increment();
        assertEquals(3, counter.get());
    }

    @Test
    void testDecrement() {
        // Create a new Counter
        Counter counter = new Counter();

        // Test decrement
        counter.decrement();
        assertEquals(-1, counter.get());

        // Test multiple decrements
        counter.decrement();
        counter.decrement();
        assertEquals(-3, counter.get());
    }

    @Test
    void testAdd() {
        // Create a new Counter
        Counter counter = new Counter();

        // Test add positive value
        counter.add(5);
        assertEquals(5, counter.get());

        // Test add negative value
        counter.add(-2);
        assertEquals(3, counter.get());

        // Test add zero
        counter.add(0);
        assertEquals(3, counter.get());
    }

    @Test
    void testSubtract() {
        // Create a new Counter
        Counter counter = new Counter();

        // Test subtract positive value
        counter.subtract(5);
        assertEquals(-5, counter.get());

        // Test subtract negative value
        counter.subtract(-2);
        assertEquals(-3, counter.get());

        // Test subtract zero
        counter.subtract(0);
        assertEquals(-3, counter.get());
    }

    @Test
    void testCompareTo() {
        // Create counters with different values
        Counter counter1 = new Counter();
        counter1.add(5);

        Counter counter2 = new Counter();
        counter2.add(10);

        Counter counter3 = new Counter();
        counter3.add(5);

        // Test comparisons
        assertTrue(counter1.compareTo(counter2) < 0);
        assertTrue(counter2.compareTo(counter1) > 0);
        assertEquals(0, counter1.compareTo(counter3));
    }

    @Test
    void testEquals() {
        // Create counters with different values
        Counter counter1 = new Counter();
        counter1.add(5);

        Counter counter2 = new Counter();
        counter2.add(10);

        Counter counter3 = new Counter();
        counter3.add(5);

        // Test equals
        assertNotEquals(counter1, counter2);
        assertEquals(counter1, counter3);

        // Test equals with non-Counter object
        assertNotEquals("not a counter", counter1);
    }

    @Test
    void testHashCode() {
        // Create counters with different values
        Counter counter1 = new Counter();
        counter1.add(5);

        Counter counter2 = new Counter();
        counter2.add(10);

        Counter counter3 = new Counter();
        counter3.add(5);

        // Test hashCode
        assertNotEquals(counter1.hashCode(), counter2.hashCode());
        assertEquals(counter1.hashCode(), counter3.hashCode());
    }
}