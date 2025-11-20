package com.dua3.utility.math;

import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vector2fTest class tests the functionality of the Vector2f class.
 */
class Vector2fTest {

    /**
     * Test the static angle method when both vectors are on the axes.
     */
    @Test
    void testAngle_TwoVectorsOnAxes() {
        Vector2f v1 = Vector2f.ONE_ZERO; // Vector (1, 0)
        Vector2f v2 = Vector2f.ZERO_ONE; // Vector (0, 1)
        double result = Vector2f.angle(v1, v2);
        // The angle between vector (1, 0) and (0, 1) should be 90 degrees or pi/2 radians
        assertEquals(Math.PI / 2, result, 1.0e-7);
    }

    /**
     * Test the static angle method when one vector is negative of another
     */
    @Test
    void testAngle_TwoVectorsAreNegatives() {
        Vector2f v1 = Vector2f.ONE_ZERO; // Vector (1, 0)
        Vector2f v2 = Vector2f.MINUS_ONE_ZERO; // Vector (-1, 0)
        double result = Vector2f.angle(v1, v2);
        // The angle between vector (1, 0) and (-1, 0) should be 180 degrees or pi radians
        assertEquals(Math.PI, result, 1.0e-7);
    }

    /**
     * Test the static angle method when the vectors have a denominator of zero
     */
    @Test
    void testAngle_VectorWithZeroDenominator() {
        Vector2f v1 = Vector2f.ORIGIN; // Vector (0, 0)
        Vector2f v2 = Vector2f.MINUS_ONE_ZERO; // Vector (-1, 0)
        double result = Vector2f.angle(v1, v2);
        // With a denominator of zero, the result should be NaN
        assertTrue(Double.isNaN(result));
    }

    /**
     * Test the of factory method.
     */
    @Test
    void testOf() {
        Vector2f v = Vector2f.of(2.5f, 3.5f);
        assertEquals(2.5f, v.x(), "x should be 2.5");
        assertEquals(3.5f, v.y(), "y should be 3.5");
    }

    /**
     * Test the max method.
     */
    @Test
    void testMax() {
        Vector2f v1 = Vector2f.of(2.0f, 5.0f);
        Vector2f v2 = Vector2f.of(4.0f, 3.0f);
        Vector2f result = Vector2f.max(v1, v2);

        assertEquals(4.0f, result.x(), "Max x should be 4.0");
        assertEquals(5.0f, result.y(), "Max y should be 5.0");
    }

    /**
     * Test the min method.
     */
    @Test
    void testMin() {
        Vector2f v1 = Vector2f.of(2.0f, 5.0f);
        Vector2f v2 = Vector2f.of(4.0f, 3.0f);
        Vector2f result = Vector2f.min(v1, v2);

        assertEquals(2.0f, result.x(), "Min x should be 2.0");
        assertEquals(3.0f, result.y(), "Min y should be 3.0");
    }

    /**
     * Test the scalarProduct method.
     */
    @Test
    void testScalarProduct() {
        Vector2f v1 = Vector2f.of(2.0f, 3.0f);
        Vector2f v2 = Vector2f.of(4.0f, 5.0f);
        float result = Vector2f.scalarProduct(v1, v2);

        assertEquals(23.0f, result, "Scalar product should be 2*4 + 3*5 = 23");
    }

    /**
     * Test the add method.
     */
    @Test
    void testAdd() {
        Vector2f v1 = Vector2f.of(2.0f, 3.0f);
        Vector2f v2 = Vector2f.of(4.0f, 5.0f);
        Vector2f result = v1.add(v2);

        assertEquals(6.0f, result.x(), "Added x should be 2 + 4 = 6");
        assertEquals(8.0f, result.y(), "Added y should be 3 + 5 = 8");
    }

    /**
     * Test the subtract method.
     */
    @Test
    void testSubtract() {
        Vector2f v1 = Vector2f.of(6.0f, 8.0f);
        Vector2f v2 = Vector2f.of(2.0f, 3.0f);
        Vector2f result = v1.subtract(v2);

        assertEquals(4.0f, result.x(), "Subtracted x should be 6 - 2 = 4");
        assertEquals(5.0f, result.y(), "Subtracted y should be 8 - 3 = 5");
    }

    /**
     * Test the translate method.
     */
    @Test
    void testTranslate() {
        Vector2f v = Vector2f.of(2.0f, 3.0f);
        Vector2f result = v.translate(4.0f, 5.0f);

        assertEquals(6.0f, result.x(), "Translated x should be 2 + 4 = 6");
        assertEquals(8.0f, result.y(), "Translated y should be 3 + 5 = 8");
    }

    /**
     * Test the normalized method.
     */
    @Test
    void testNormalized() {
        Vector2f v = Vector2f.of(3.0f, 4.0f);
        Vector2f result = v.normalized();

        // Length of (3, 4) is 5, so normalized vector should be (3/5, 4/5)
        assertEquals(0.6f, result.x(), 1.0e-6, "Normalized x should be 3/5 = 0.6");
        assertEquals(0.8f, result.y(), 1.0e-6, "Normalized y should be 4/5 = 0.8");

        // Test that the normalized vector has length 1
        assertEquals(1.0, result.length(), 1.0e-6, "Normalized vector should have length 1");
    }

    /**
     * Test the normalized method with zero vector.
     */
    @Test
    void testNormalizedWithZeroVector() {
        Vector2f v = Vector2f.ORIGIN;

        // Normalizing a zero vector should throw an IllegalArgumentException
        assertThrows(IllegalArgumentException.class, v::normalized);
    }

    /**
     * Test the orthogonal method with sweep = false.
     */
    @Test
    void testOrthogonalWithSweepFalse() {
        Vector2f v = Vector2f.of(3.0f, 4.0f);
        Vector2f result = v.orthogonal(false);

        // Orthogonal vector with sweep = false should be (-y, x)
        assertEquals(-4.0f, result.x(), "Orthogonal x should be -4");
        assertEquals(3.0f, result.y(), "Orthogonal y should be 3");

        // Test that the result is orthogonal to the original vector
        assertEquals(0.0f, Vector2f.scalarProduct(v, result), 1.0e-6, "Vectors should be orthogonal");
    }

    /**
     * Test the orthogonal method with sweep = true.
     */
    @Test
    void testOrthogonalWithSweepTrue() {
        Vector2f v = Vector2f.of(3.0f, 4.0f);
        Vector2f result = v.orthogonal(true);

        // Orthogonal vector with sweep = true should be (y, -x)
        assertEquals(4.0f, result.x(), "Orthogonal x should be 4");
        assertEquals(-3.0f, result.y(), "Orthogonal y should be -3");

        // Test that the result is orthogonal to the original vector
        assertEquals(0.0f, Vector2f.scalarProduct(v, result), 1.0e-6, "Vectors should be orthogonal");
    }

    /**
     * Test the length method.
     */
    @Test
    void testLength() {
        Vector2f v = Vector2f.of(3.0f, 4.0f);
        double result = v.length();

        assertEquals(5.0, result, 1.0e-6, "Length of (3, 4) should be 5");
    }

    /**
     * Test the instance angle method.
     */
    @Test
    void testInstanceAngle() {
        // Test angle of vector (1, 0)
        Vector2f v1 = Vector2f.ONE_ZERO;
        assertEquals(0.0, v1.angle(), 1.0e-6, "Angle of (1, 0) should be 0");

        // Test angle of vector (0, 1)
        Vector2f v2 = Vector2f.ZERO_ONE;
        assertEquals(Math.PI / 2, v2.angle(), 1.0e-6, "Angle of (0, 1) should be PI/2");

        // Test angle of vector (-1, 0)
        Vector2f v3 = Vector2f.MINUS_ONE_ZERO;
        assertEquals(Math.PI, v3.angle(), 1.0e-6, "Angle of (-1, 0) should be PI");

        // Test angle of vector (0, -1)
        Vector2f v4 = Vector2f.ZERO_MINUS_ONE;
        assertEquals(-Math.PI / 2, v4.angle(), 1.0e-6, "Angle of (0, -1) should be -PI/2");
    }

    /**
     * Test the negate method.
     */
    @Test
    void testNegate() {
        Vector2f v = Vector2f.of(3.0f, 4.0f);
        Vector2f result = v.negate();

        assertEquals(-3.0f, result.x(), "Negated x should be -3");
        assertEquals(-4.0f, result.y(), "Negated y should be -4");
    }
}
