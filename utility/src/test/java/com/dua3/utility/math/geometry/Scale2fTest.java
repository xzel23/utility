package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Scale2f.
 */
class Scale2fTest {

    /**
     * Test the constructor with two parameters and accessors.
     */
    @Test
    void testConstructorWithTwoParametersAndAccessors() {
        Scale2f scale = new Scale2f(2.5f, 3.5f);
        assertEquals(2.5f, scale.sx(), "sx should be 2.5");
        assertEquals(3.5f, scale.sy(), "sy should be 3.5");
    }

    /**
     * Test the constructor with one parameter.
     */
    @Test
    void testConstructorWithOneParameter() {
        Scale2f scale = new Scale2f(2.5f);
        assertEquals(2.5f, scale.sx(), "sx should be 2.5");
        assertEquals(2.5f, scale.sy(), "sy should be 2.5");
    }

    /**
     * Test the identity method.
     */
    @Test
    void testIdentity() {
        Scale2f identity = Scale2f.identity();
        assertEquals(1.0f, identity.sx(), "Identity sx should be 1.0");
        assertEquals(1.0f, identity.sy(), "Identity sy should be 1.0");
    }

    /**
     * Test the of factory method with two parameters.
     */
    @Test
    void testOfWithTwoParameters() {
        Scale2f scale = Scale2f.of(2.5f, 3.5f);
        assertEquals(2.5f, scale.sx(), "sx should be 2.5");
        assertEquals(3.5f, scale.sy(), "sy should be 3.5");
    }

    /**
     * Test the of factory method with one parameter.
     */
    @Test
    void testOfWithOneParameter() {
        Scale2f scale = Scale2f.of(2.5f);
        assertEquals(2.5f, scale.sx(), "sx should be 2.5");
        assertEquals(2.5f, scale.sy(), "sy should be 2.5");
    }

    /**
     * Test the multiply method with Scale2f parameter.
     */
    @Test
    void testMultiplyWithScale2f() {
        Scale2f scale1 = new Scale2f(2.0f, 3.0f);
        Scale2f scale2 = new Scale2f(4.0f, 5.0f);
        Scale2f result = scale1.multiply(scale2);

        assertEquals(8.0f, result.sx(), "Multiplied sx should be 8.0");
        assertEquals(15.0f, result.sy(), "Multiplied sy should be 15.0");
    }

    /**
     * Test the multiply method with two float parameters.
     */
    @Test
    void testMultiplyWithTwoFloats() {
        Scale2f scale = new Scale2f(2.0f, 3.0f);
        Scale2f result = scale.multiply(4.0f, 5.0f);

        assertEquals(8.0f, result.sx(), "Multiplied sx should be 8.0");
        assertEquals(15.0f, result.sy(), "Multiplied sy should be 15.0");
    }

    /**
     * Test the multiply method with one float parameter.
     */
    @Test
    void testMultiplyWithOneFloat() {
        Scale2f scale = new Scale2f(2.0f, 3.0f);
        Scale2f result = scale.multiply(4.0f);

        assertEquals(8.0f, result.sx(), "Multiplied sx should be 8.0");
        assertEquals(12.0f, result.sy(), "Multiplied sy should be 12.0");
    }
}