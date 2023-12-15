package com.dua3.utility.math;

import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Vector2fTest class tests the functionality of the method angle in the math utility class Vector2f.
 * For the simplicity of the tests, we are considering pi as 3.14.
 */
public class Vector2fTest {

    /**
     * Test the angle method when both vectors are on the axes.
     */
    @Test
    void testAngle_TwoVectorsOnAxes() {
        Vector2f v1 = Vector2f.ONE_ZERO; // Vector (1, 0)
        Vector2f v2 = Vector2f.ZERO_ONE; // Vector (0, 1)
        double result = Vector2f.angle(v1, v2);
        // The angle between vector (1, 0) and (0, 1) should be 90 degrees or pi/2 radians
        assertEquals(Math.PI/2, result, 1e-7);
    }

    /**
     * Test the angle method when one vector is negative of another
     */
    @Test
    void testAngle_TwoVectorsAreNegatives() {
        Vector2f v1 = Vector2f.ONE_ZERO; // Vector (1, 0)
        Vector2f v2 = Vector2f.MINUS_ONE_ZERO; // Vector (-1, 0)
        double result = Vector2f.angle(v1, v2);
        // The angle between vector (1, 0) and (-1, 0) should be 180 degrees or pi radians
        assertEquals(Math.PI, result, 1e-7);
    }

    /**
     * Test the angle method when the vectors have a denominator of zero
     */
    @Test
    void testAngle_VectorWithZeroDenominator() {
        Vector2f v1 = Vector2f.ORIGIN; // Vector (0, 0)
        Vector2f v2 = Vector2f.MINUS_ONE_ZERO; // Vector (-1, 0)
        double result = Vector2f.angle(v1, v2);
        // With a denominator of zero, the result should be NaN
        assertTrue(Double.isNaN(result));
    }
}