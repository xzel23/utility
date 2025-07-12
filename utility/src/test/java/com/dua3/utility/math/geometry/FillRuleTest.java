package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for FillRule enum.
 */
class FillRuleTest {

    /**
     * Test that the enum values exist.
     */
    @Test
    void testEnumValues() {
        // Test that the enum values exist
        assertNotNull(FillRule.EVEN_ODD, "EVEN_ODD value should exist");
        assertNotNull(FillRule.NON_ZERO, "NON_ZERO value should exist");

        // Test that there are exactly 2 enum values
        assertEquals(2, FillRule.values().length, "There should be exactly 2 enum values");
    }

    /**
     * Test that the enum values can be used in a switch statement.
     */
    @Test
    void testEnumInSwitch() {
        // Test that the enum values can be used in a switch statement
        for (FillRule rule : FillRule.values()) {
            String description = switch (rule) {
                case EVEN_ODD -> "Even-odd rule";
                case NON_ZERO -> "Non-zero rule";
            };

            assertNotNull(description, "Description should not be null");

            if (rule == FillRule.EVEN_ODD) {
                assertEquals("Even-odd rule", description, "Description for EVEN_ODD should match");
            } else if (rule == FillRule.NON_ZERO) {
                assertEquals("Non-zero rule", description, "Description for NON_ZERO should match");
            }
        }
    }

    /**
     * Test valueOf method.
     */
    @Test
    void testValueOf() {
        // Test that the valueOf method works
        assertEquals(FillRule.EVEN_ODD, FillRule.valueOf("EVEN_ODD"), "valueOf(\"EVEN_ODD\") should return EVEN_ODD");
        assertEquals(FillRule.NON_ZERO, FillRule.valueOf("NON_ZERO"), "valueOf(\"NON_ZERO\") should return NON_ZERO");

        // Test that valueOf throws IllegalArgumentException for invalid values
        assertThrows(IllegalArgumentException.class, () -> FillRule.valueOf("INVALID"), "valueOf should throw IllegalArgumentException for invalid values");
    }
}