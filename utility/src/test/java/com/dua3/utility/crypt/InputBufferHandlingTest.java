package com.dua3.utility.crypt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link InputBufferHandling} enum.
 */
class InputBufferHandlingTest {

    @Test
    void testEnumValues() {
        // Verify that the enum contains the expected values
        assertEquals(2, InputBufferHandling.values().length);
        assertNotNull(InputBufferHandling.CLEAR_AFTER_USE);
        assertNotNull(InputBufferHandling.PRESERVE);
    }

    @ParameterizedTest
    @EnumSource(InputBufferHandling.class)
    void testValueOf(InputBufferHandling value) {
        // Test that valueOf works correctly for all enum values
        assertEquals(value, InputBufferHandling.valueOf(value.name()));
    }

    @Test
    void testOrdinals() {
        // Verify the ordinal values are as expected
        assertEquals(0, InputBufferHandling.CLEAR_AFTER_USE.ordinal());
        assertEquals(1, InputBufferHandling.PRESERVE.ordinal());
    }
}