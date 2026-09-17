package com.dua3.utility.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class IndexRangeTest {

    @Test
    void testPropertiesAndLength() {
        IndexRange range = new IndexRange(3, 10);
        assertEquals(3, range.start());
        assertEquals(3, range.getStart());
        assertEquals(10, range.end());
        assertEquals(10, range.getEnd());
        assertEquals(7, range.length());
        assertEquals(7, range.getLength());

        IndexRange emptyRange = new IndexRange(4, 4);
        assertEquals(0, emptyRange.length());
        assertEquals(0, emptyRange.getLength());
    }

    @Test
    void testEqualsAndHashCode() {
        IndexRange r1 = new IndexRange(2, 6);
        IndexRange r2 = new IndexRange(2, 6);
        IndexRange r3 = new IndexRange(2, 7);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
        assertNotEquals(null, r1);
        assertEquals("IndexRange[start=2, end=6]", r1.toString());
    }
}
