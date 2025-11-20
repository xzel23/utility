package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Dimension2f.
 */
class Dimension2fTest {

    /**
     * Test the constructor and accessors.
     */
    @Test
    void testConstructorAndAccessors() {
        Dimension2f dim = new Dimension2f(10.5f, 20.5f);
        assertEquals(10.5f, dim.width(), "Width should be 10.5");
        assertEquals(20.5f, dim.height(), "Height should be 20.5");
    }

    /**
     * Test the max method.
     */
    @Test
    void testMax() {
        Dimension2f dim1 = new Dimension2f(10.0f, 30.0f);
        Dimension2f dim2 = new Dimension2f(20.0f, 20.0f);
        Dimension2f maxDim = Dimension2f.max(dim1, dim2);

        assertEquals(20.0f, maxDim.width(), "Max width should be 20");
        assertEquals(30.0f, maxDim.height(), "Max height should be 30");
    }

    /**
     * Test the min method.
     */
    @Test
    void testMin() {
        Dimension2f dim1 = new Dimension2f(10.0f, 30.0f);
        Dimension2f dim2 = new Dimension2f(20.0f, 20.0f);
        Dimension2f minDim = Dimension2f.min(dim1, dim2);

        assertEquals(10.0f, minDim.width(), "Min width should be 10");
        assertEquals(20.0f, minDim.height(), "Min height should be 20");
    }

    /**
     * Test the of factory method.
     */
    @Test
    void testOf() {
        Dimension2f dim = Dimension2f.of(10.5f, 20.5f);
        assertEquals(10.5f, dim.width(), "Width should be 10.5");
        assertEquals(20.5f, dim.height(), "Height should be 20.5");
    }

    /**
     * Test the scaled method with Scale2f parameter.
     */
    @Test
    void testScaledWithScale2f() {
        Dimension2f dim = new Dimension2f(10.0f, 20.0f);
        Scale2f scale = new Scale2f(2.0f, 3.0f);
        Dimension2f scaledDim = dim.scaled(scale);

        assertEquals(20.0f, scaledDim.width(), "Scaled width should be 20");
        assertEquals(60.0f, scaledDim.height(), "Scaled height should be 60");
    }

    /**
     * Test the scaled method with float parameter.
     */
    @Test
    void testScaledWithFloat() {
        Dimension2f dim = new Dimension2f(10.0f, 20.0f);
        Dimension2f scaledDim = dim.scaled(2.5f);

        assertEquals(25.0f, scaledDim.width(), "Scaled width should be 25");
        assertEquals(50.0f, scaledDim.height(), "Scaled height should be 50");
    }

    /**
     * Test the addMargin method with one parameter.
     */
    @Test
    void testAddMarginWithOneParameter() {
        Dimension2f dim = new Dimension2f(10.0f, 20.0f);
        Dimension2f dimWithMargin = dim.addMargin(5.0f);

        assertEquals(20.0f, dimWithMargin.width(), "Width with margin should be 20");
        assertEquals(30.0f, dimWithMargin.height(), "Height with margin should be 30");
    }

    /**
     * Test the addMargin method with two parameters.
     */
    @Test
    void testAddMarginWithTwoParameters() {
        Dimension2f dim = new Dimension2f(10.0f, 20.0f);
        Dimension2f dimWithMargin = dim.addMargin(5.0f, 10.0f);

        assertEquals(20.0f, dimWithMargin.width(), "Width with margin should be 20");
        assertEquals(40.0f, dimWithMargin.height(), "Height with margin should be 40");
    }
}
