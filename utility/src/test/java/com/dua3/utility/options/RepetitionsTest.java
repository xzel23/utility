package com.dua3.utility.options;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Repetitions class.
 */
class RepetitionsTest {

    @Test
    void testConstants() {
        // Test the predefined constants
        assertEquals(0, Repetitions.ZERO.min());
        assertEquals(0, Repetitions.ZERO.max());

        assertEquals(0, Repetitions.ZERO_OR_ONE.min());
        assertEquals(1, Repetitions.ZERO_OR_ONE.max());

        assertEquals(0, Repetitions.ZERO_OR_MORE.min());
        assertEquals(Integer.MAX_VALUE, Repetitions.ZERO_OR_MORE.max());

        assertEquals(1, Repetitions.EXACTLY_ONE.min());
        assertEquals(1, Repetitions.EXACTLY_ONE.max());

        assertEquals(1, Repetitions.ONE_OR_MORE.min());
        assertEquals(Integer.MAX_VALUE, Repetitions.ONE_OR_MORE.max());
    }

    @Test
    void testConstructor() {
        Repetitions rep = new Repetitions(2, 5);
        assertEquals(2, rep.min());
        assertEquals(5, rep.max());

        // Test validation in constructor
        assertThrows(IllegalArgumentException.class, () -> new Repetitions(-1, 5));
        assertThrows(IllegalArgumentException.class, () -> new Repetitions(6, 5));
    }

    @Test
    void testBetween() {
        Repetitions rep = Repetitions.between(2, 5);
        assertEquals(2, rep.min());
        assertEquals(5, rep.max());
    }

    @Test
    void testExactly() {
        Repetitions rep = Repetitions.exactly(3);
        assertEquals(3, rep.min());
        assertEquals(3, rep.max());
    }

    @Test
    void testAtLeast() {
        Repetitions rep = Repetitions.atLeast(2);
        assertEquals(2, rep.min());
        assertEquals(Integer.MAX_VALUE, rep.max());
    }

    @Test
    void testAtMost() {
        Repetitions rep = Repetitions.atMost(5);
        assertEquals(0, rep.min());
        assertEquals(5, rep.max());
    }

    @Test
    void testEqualsAndHashCode() {
        Repetitions rep1 = new Repetitions(2, 5);
        Repetitions rep2 = new Repetitions(2, 5);
        Repetitions rep3 = new Repetitions(1, 5);
        Repetitions rep4 = new Repetitions(2, 6);

        // Test equality
        assertEquals(rep1, rep2);
        assertEquals(rep1.hashCode(), rep2.hashCode());

        // Test inequality
        assertNotEquals(rep1, rep3);
        assertNotEquals(rep1, rep4);
        assertNotEquals(rep3, rep4);

        // Test inequality with different types
        assertNotEquals("not a repetition", rep1);
    }

    @Test
    void testToString() {
        Repetitions rep = new Repetitions(2, 5);
        String repString = rep.toString();

        assertNotNull(repString);
        assertTrue(repString.contains("2"));
        assertTrue(repString.contains("5"));
    }
}