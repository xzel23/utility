package com.dua3.utility.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenModeTest {

    @Test
    void testEnumValues() {
        assertEquals(4, OpenMode.values().length);
        assertEquals(OpenMode.NONE, OpenMode.valueOf("NONE"));
        assertEquals(OpenMode.READ, OpenMode.valueOf("READ"));
        assertEquals(OpenMode.WRITE, OpenMode.valueOf("WRITE"));
        assertEquals(OpenMode.READ_AND_WRITE, OpenMode.valueOf("READ_AND_WRITE"));
    }

    @Test
    void testIsIncluded() {
        assertTrue(OpenMode.READ_AND_WRITE.isIncluded(OpenMode.READ));
        assertTrue(OpenMode.READ_AND_WRITE.isIncluded(OpenMode.WRITE));
        assertTrue(OpenMode.READ_AND_WRITE.isIncluded(OpenMode.READ_AND_WRITE));
        assertTrue(OpenMode.READ_AND_WRITE.isIncluded(OpenMode.NONE));

        assertTrue(OpenMode.READ.isIncluded(OpenMode.READ));
        assertTrue(OpenMode.READ.isIncluded(OpenMode.NONE));
        assertFalse(OpenMode.READ.isIncluded(OpenMode.WRITE));
        assertFalse(OpenMode.READ.isIncluded(OpenMode.READ_AND_WRITE));

        assertTrue(OpenMode.WRITE.isIncluded(OpenMode.WRITE));
        assertTrue(OpenMode.WRITE.isIncluded(OpenMode.NONE));
        assertFalse(OpenMode.WRITE.isIncluded(OpenMode.READ));
        assertFalse(OpenMode.WRITE.isIncluded(OpenMode.READ_AND_WRITE));

        assertTrue(OpenMode.NONE.isIncluded(OpenMode.NONE));
        assertFalse(OpenMode.NONE.isIncluded(OpenMode.READ));
        assertFalse(OpenMode.NONE.isIncluded(OpenMode.WRITE));
    }
}
