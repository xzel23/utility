package com.dua3.utility.math;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Rectangle2f.java
 */
public class Rectangle2fTest {

    /**
     * Test for withCorners() method
     * This test case assumes that when the withCorners() method is provided with two Vector2f objects,
     * it will correctly calculate and return a Rectangle2f object.
     */
    @Test
    void testWithCorners() {
        Vector2f vector1 = Vector2f.of(2.0f, 3.0f);
        Vector2f vector2 = Vector2f.of(5.0f, 6.0f);
        Rectangle2f rectangle = Rectangle2f.withCorners(vector1, vector2);
        assertNotNull(rectangle, "Rectangle2f instance should not be null.");

        // check x, y, width, height
        assertEquals(2.0f, rectangle.xMin(), "Incorrect minimum x-coordinate.");
        assertEquals(3.0f, rectangle.yMin(), "Incorrect minimum y-coordinate.");
        assertEquals(5.0f, rectangle.xMax(), "Incorrect maximum x-coordinate.");
        assertEquals(6.0f, rectangle.yMax(), "Incorrect maximum y-coordinate.");
    }

    /**
     * This test case tests the scenario where vector1 and vector2 have the same coordinates.
     */
    @Test
    void testWithCornersWithSameCoordinates() {
        Vector2f vector1 = Vector2f.of(2.0f, 3.0f);
        Vector2f vector2 = Vector2f.of(2.0f, 3.0f);
        Rectangle2f rectangle = Rectangle2f.withCorners(vector1, vector2);
        assertNotNull(rectangle, "Rectangle2f instance should not be null.");

        // Since both vectors have the same coordinates, width and height should be 0.
        assertEquals(0.0f, rectangle.width(), "Incorrect width.");
        assertEquals(0.0f, rectangle.height(), "Incorrect height.");
    }
}