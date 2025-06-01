package com.dua3.utility.math;

import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Rectangle2f.java
 */
class Rectangle2fTest {

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

    /**
     * Test for withCenter() method
     */
    @Test
    void testWithCenter() {
        Vector2f center = Vector2f.of(10.0f, 15.0f);
        Dimension2f dimension = new Dimension2f(6.0f, 8.0f);
        Rectangle2f rectangle = Rectangle2f.withCenter(center, dimension);

        assertEquals(7.0f, rectangle.x(), "Incorrect x-coordinate.");
        assertEquals(11.0f, rectangle.y(), "Incorrect y-coordinate.");
        assertEquals(6.0f, rectangle.width(), "Incorrect width.");
        assertEquals(8.0f, rectangle.height(), "Incorrect height.");
        assertEquals(10.0f, rectangle.xCenter(), "Incorrect center x-coordinate.");
        assertEquals(15.0f, rectangle.yCenter(), "Incorrect center y-coordinate.");
    }

    /**
     * Test for of() method with x, y, width, height parameters
     */
    @Test
    void testOfWithCoordinatesAndDimension() {
        Rectangle2f rectangle = Rectangle2f.of(2.0f, 3.0f, 4.0f, 5.0f);

        assertEquals(2.0f, rectangle.x(), "Incorrect x-coordinate.");
        assertEquals(3.0f, rectangle.y(), "Incorrect y-coordinate.");
        assertEquals(4.0f, rectangle.width(), "Incorrect width.");
        assertEquals(5.0f, rectangle.height(), "Incorrect height.");
    }

    /**
     * Test for of() method with Vector2f and Dimension2f parameters
     */
    @Test
    void testOfWithVectorAndDimension() {
        Vector2f position = Vector2f.of(2.0f, 3.0f);
        Dimension2f dimension = new Dimension2f(4.0f, 5.0f);
        Rectangle2f rectangle = Rectangle2f.of(position, dimension);

        assertEquals(2.0f, rectangle.x(), "Incorrect x-coordinate.");
        assertEquals(3.0f, rectangle.y(), "Incorrect y-coordinate.");
        assertEquals(4.0f, rectangle.width(), "Incorrect width.");
        assertEquals(5.0f, rectangle.height(), "Incorrect height.");
    }

    /**
     * Test for coordinate accessors
     */
    @Test
    void testCoordinateAccessors() {
        Rectangle2f rectangle = new Rectangle2f(2.0f, 3.0f, 4.0f, 5.0f);

        assertEquals(2.0f, rectangle.xMin(), "Incorrect minimum x-coordinate.");
        assertEquals(3.0f, rectangle.yMin(), "Incorrect minimum y-coordinate.");
        assertEquals(6.0f, rectangle.xMax(), "Incorrect maximum x-coordinate.");
        assertEquals(8.0f, rectangle.yMax(), "Incorrect maximum y-coordinate.");
        assertEquals(4.0f, rectangle.xCenter(), "Incorrect center x-coordinate.");
        assertEquals(5.5f, rectangle.yCenter(), "Incorrect center y-coordinate.");
    }

    /**
     * Test for getDimension() method
     */
    @Test
    void testGetDimension() {
        Rectangle2f rectangle = new Rectangle2f(2.0f, 3.0f, 4.0f, 5.0f);
        Dimension2f dimension = rectangle.getDimension();

        assertEquals(4.0f, dimension.width(), "Incorrect width.");
        assertEquals(5.0f, dimension.height(), "Incorrect height.");
    }

    /**
     * Test for min(), max(), and center() methods
     */
    @Test
    void testMinMaxCenter() {
        Rectangle2f rectangle = new Rectangle2f(2.0f, 3.0f, 4.0f, 5.0f);

        Vector2f min = rectangle.min();
        assertEquals(2.0f, min.x(), "Incorrect min x-coordinate.");
        assertEquals(3.0f, min.y(), "Incorrect min y-coordinate.");

        Vector2f max = rectangle.max();
        assertEquals(6.0f, max.x(), "Incorrect max x-coordinate.");
        assertEquals(8.0f, max.y(), "Incorrect max y-coordinate.");

        Vector2f center = rectangle.center();
        assertEquals(4.0f, center.x(), "Incorrect center x-coordinate.");
        assertEquals(5.5f, center.y(), "Incorrect center y-coordinate.");
    }

    /**
     * Test for translate() method with dx, dy parameters
     */
    @Test
    void testTranslateWithCoordinates() {
        Rectangle2f rectangle = new Rectangle2f(2.0f, 3.0f, 4.0f, 5.0f);
        Rectangle2f translated = rectangle.translate(10.0f, 20.0f);

        assertEquals(12.0f, translated.x(), "Incorrect translated x-coordinate.");
        assertEquals(23.0f, translated.y(), "Incorrect translated y-coordinate.");
        assertEquals(4.0f, translated.width(), "Width should not change after translation.");
        assertEquals(5.0f, translated.height(), "Height should not change after translation.");
    }

    /**
     * Test for translate() method with Vector2f parameter
     */
    @Test
    void testTranslateWithVector() {
        Rectangle2f rectangle = new Rectangle2f(2.0f, 3.0f, 4.0f, 5.0f);
        Vector2f translation = Vector2f.of(10.0f, 20.0f);
        Rectangle2f translated = rectangle.translate(translation);

        assertEquals(12.0f, translated.x(), "Incorrect translated x-coordinate.");
        assertEquals(23.0f, translated.y(), "Incorrect translated y-coordinate.");
        assertEquals(4.0f, translated.width(), "Width should not change after translation.");
        assertEquals(5.0f, translated.height(), "Height should not change after translation.");
    }

    /**
     * Test for addMargin() method with one parameter
     */
    @Test
    void testAddMarginWithOneParameter() {
        Rectangle2f rectangle = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);
        Rectangle2f withMargin = rectangle.addMargin(5.0f);

        assertEquals(5.0f, withMargin.x(), "Incorrect x-coordinate after adding margin.");
        assertEquals(15.0f, withMargin.y(), "Incorrect y-coordinate after adding margin.");
        assertEquals(40.0f, withMargin.width(), "Incorrect width after adding margin.");
        assertEquals(50.0f, withMargin.height(), "Incorrect height after adding margin.");
    }

    /**
     * Test for addMargin() method with two parameters
     */
    @Test
    void testAddMarginWithTwoParameters() {
        Rectangle2f rectangle = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);
        Rectangle2f withMargin = rectangle.addMargin(5.0f, 10.0f);

        assertEquals(5.0f, withMargin.x(), "Incorrect x-coordinate after adding margin.");
        assertEquals(10.0f, withMargin.y(), "Incorrect y-coordinate after adding margin.");
        assertEquals(40.0f, withMargin.width(), "Incorrect width after adding margin.");
        assertEquals(60.0f, withMargin.height(), "Incorrect height after adding margin.");
    }

    /**
     * Test for addMargin() method with four parameters
     */
    @Test
    void testAddMarginWithFourParameters() {
        Rectangle2f rectangle = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);
        Rectangle2f withMargin = rectangle.addMargin(5.0f, 10.0f, 15.0f, 20.0f);

        assertEquals(5.0f, withMargin.x(), "Incorrect x-coordinate after adding margin.");
        assertEquals(10.0f, withMargin.y(), "Incorrect y-coordinate after adding margin.");
        assertEquals(50.0f, withMargin.width(), "Incorrect width after adding margin.");
        assertEquals(70.0f, withMargin.height(), "Incorrect height after adding margin.");
    }

    /**
     * Test for contains() method
     */
    @Test
    void testContains() {
        Rectangle2f rectangle = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);

        // Point inside the rectangle
        assertTrue(rectangle.contains(Vector2f.of(15.0f, 25.0f)), "Rectangle should contain the point inside it.");

        // Points on the edges of the rectangle
        assertTrue(rectangle.contains(Vector2f.of(10.0f, 20.0f)), "Rectangle should contain the point on its top-left corner.");
        assertTrue(rectangle.contains(Vector2f.of(40.0f, 60.0f)), "Rectangle should contain the point on its bottom-right corner.");

        // Points outside the rectangle
        assertFalse(rectangle.contains(Vector2f.of(5.0f, 25.0f)), "Rectangle should not contain the point outside it.");
        assertFalse(rectangle.contains(Vector2f.of(15.0f, 15.0f)), "Rectangle should not contain the point outside it.");
        assertFalse(rectangle.contains(Vector2f.of(45.0f, 25.0f)), "Rectangle should not contain the point outside it.");
        assertFalse(rectangle.contains(Vector2f.of(15.0f, 65.0f)), "Rectangle should not contain the point outside it.");
    }

    /**
     * Test for intersects() method
     */
    @Test
    void testIntersects() {
        Rectangle2f rectangle1 = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);

        // Rectangle that overlaps
        Rectangle2f rectangle2 = new Rectangle2f(30.0f, 40.0f, 20.0f, 30.0f);
        assertTrue(rectangle1.intersects(rectangle2), "Rectangles should intersect.");
        assertTrue(rectangle2.intersects(rectangle1), "Rectangles should intersect (commutative).");

        // Rectangle that is completely inside
        Rectangle2f rectangle3 = new Rectangle2f(15.0f, 25.0f, 10.0f, 10.0f);
        assertTrue(rectangle1.intersects(rectangle3), "Rectangles should intersect when one is inside the other.");
        assertTrue(rectangle3.intersects(rectangle1), "Rectangles should intersect when one is inside the other (commutative).");

        // Rectangle that is completely outside
        Rectangle2f rectangle4 = new Rectangle2f(50.0f, 70.0f, 10.0f, 10.0f);
        assertFalse(rectangle1.intersects(rectangle4), "Rectangles should not intersect when they are completely separate.");
        assertFalse(rectangle4.intersects(rectangle1), "Rectangles should not intersect when they are completely separate (commutative).");
    }

    /**
     * Test for moveTo() method
     */
    @Test
    void testMoveTo() {
        Rectangle2f rectangle = new Rectangle2f(10.0f, 20.0f, 30.0f, 40.0f);
        Rectangle2f moved = rectangle.moveTo(50.0f, 60.0f);

        assertEquals(50.0f, moved.x(), "Incorrect x-coordinate after moving.");
        assertEquals(60.0f, moved.y(), "Incorrect y-coordinate after moving.");
        assertEquals(30.0f, moved.width(), "Width should not change after moving.");
        assertEquals(40.0f, moved.height(), "Height should not change after moving.");
    }
}
