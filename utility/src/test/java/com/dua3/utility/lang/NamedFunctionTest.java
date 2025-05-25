package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for NamedFunction class.
 */
class NamedFunctionTest {

    /**
     * Test to verify the apply() method of NamedFunction works correctly.
     */
    @Test
    void testApply() {
        Function<String, Integer> f = Integer::parseInt;
        NamedFunction<String, Integer> nf = new NamedFunction<>("TestFunction", f);

        // Apply number string
        int result = nf.apply("3");
        assertEquals(3, result, "The returned value should match the applied string's integer value.");
    }

    /**
     * Test to verify the toString() method of NamedFunction returns the given name.
     */
    @Test
    void testToString() {
        Function<String, Integer> f = Integer::parseInt;
        NamedFunction<String, Integer> nf = new NamedFunction<>("TestFunction", f);

        // Test toString
        String name = nf.toString();
        assertEquals("TestFunction", name, "The returned name should match the given name.");
    }
}