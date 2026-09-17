package com.dua3.utility.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangeRangeTest {

    @Test
    void testPropertiesAndIsEmpty() {
        ChangeRange empty = new ChangeRange(5, 5, 5);
        assertEquals(5, empty.start());
        assertEquals(5, empty.endInCurrent());
        assertEquals(5, empty.endInUpdated());
        assertTrue(empty.isEmpty());

        ChangeRange nonEqualEnd = new ChangeRange(5, 5, 6);
        assertFalse(nonEqualEnd.isEmpty());

        ChangeRange nonEqualStart = new ChangeRange(2, 5, 5);
        assertFalse(nonEqualStart.isEmpty());

        ChangeRange modified = new ChangeRange(3, 8, 12);
        assertFalse(modified.isEmpty());
    }

    @Test
    void testEqualsAndHashCode() {
        ChangeRange r1 = new ChangeRange(1, 4, 7);
        ChangeRange r2 = new ChangeRange(1, 4, 7);
        ChangeRange r3 = new ChangeRange(1, 4, 8);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
        assertNotEquals(null, r1);
        assertEquals("ChangeRange[start=1, endInCurrent=4, endInUpdated=7]", r1.toString());
    }
}
