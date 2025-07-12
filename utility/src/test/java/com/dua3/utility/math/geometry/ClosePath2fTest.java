package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for ClosePath2f.
 */
class ClosePath2fTest {

    /**
     * Test the constructor and accessors.
     * Since ClosePath2f constructor is package-private, we need to create a ClosePath2f instance
     * through a PathBuilder2f.
     */
    @Test
    void testConstructorAndAccessors() {
        // Create a path with a close path segment
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 20.0f);
        Vector2f point3 = Vector2f.of(20.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();
        Path2f path = builder.build();

        // Get the close path segment
        Segment2f segment = path.segments().get(3); // Index 0 is MoveTo, 1 and 2 are LineTo, 3 is ClosePath
        assertInstanceOf(ClosePath2f.class, segment, "Segment should be a ClosePath2f");

        ClosePath2f closePath = (ClosePath2f) segment;

        // Test inherited methods
        assertEquals(point3, closePath.start(), "start point should be the last point before closing");
        assertEquals(start, closePath.end(), "end point should be the first point of the path");
        assertEquals("CLOSE_PATH", closePath.name(), "name should be CLOSE_PATH");
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        // Create a path with a close path segment
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 20.0f);
        Vector2f point3 = Vector2f.of(20.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();
        Path2f path = builder.build();

        // Get the close path segment
        ClosePath2f closePath = (ClosePath2f) path.segments().get(3);

        // Test toString
        String str = closePath.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("ClosePath2f"), "toString should contain class name");
        // The exact format of the vertex representation might vary, but it should contain the coordinates
        assertTrue(str.contains("20.000000,40.000000"), "toString should contain start point coordinates");
        assertTrue(str.contains("10.000000,20.000000"), "toString should contain end point coordinates");
    }
}