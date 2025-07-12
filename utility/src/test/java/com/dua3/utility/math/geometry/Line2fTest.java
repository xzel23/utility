package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Line2f.
 */
class Line2fTest {

    /**
     * Test the constructor and accessors.
     * Since Line2f constructor is package-private, we need to create a Line2f instance
     * through a PathBuilder2f.
     */
    @Test
    void testConstructorAndAccessors() {
        // Create a path with a line segment
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        // Get the line segment
        Segment2f segment = path.segments().get(1); // Index 0 is MoveTo, 1 is Line
        assertInstanceOf(Line2f.class, segment, "Segment should be a Line2f");

        Line2f line = (Line2f) segment;

        // Test inherited methods
        assertEquals(start, line.start(), "start point should match");
        assertEquals(end, line.end(), "end point should match");
        assertEquals("LINE", line.name(), "name should be LINE");
    }

    /**
     * Test the isHorizontal method.
     */
    @Test
    void testIsHorizontal() {
        // Create a horizontal line
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        Line2f line = (Line2f) path.segments().get(1);
        assertTrue(line.isHorizontal(), "Line should be horizontal");

        // Create a non-horizontal line
        builder = new PathBuilder2f();
        start = Vector2f.of(10.0f, 20.0f);
        end = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        path = builder.build();

        line = (Line2f) path.segments().get(1);
        assertFalse(line.isHorizontal(), "Line should not be horizontal");
    }

    /**
     * Test the isVertical method.
     */
    @Test
    void testIsVertical() {
        // Create a vertical line
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(10.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        Line2f line = (Line2f) path.segments().get(1);
        assertTrue(line.isVertical(), "Line should be vertical");

        // Create a non-vertical line
        builder = new PathBuilder2f();
        start = Vector2f.of(10.0f, 20.0f);
        end = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        path = builder.build();

        line = (Line2f) path.segments().get(1);
        assertFalse(line.isVertical(), "Line should not be vertical");
    }

    /**
     * Test the deltaX and deltaY methods.
     */
    @Test
    void testDeltaXAndDeltaY() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 50.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        Line2f line = (Line2f) path.segments().get(1);

        assertEquals(20.0f, line.deltaX(), "deltaX should be 20");
        assertEquals(30.0f, line.deltaY(), "deltaY should be 30");
    }

    /**
     * Test the inclination method.
     */
    @Test
    void testInclination() {
        // Test a line with positive inclination
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        Line2f line = (Line2f) path.segments().get(1);

        // The inclination should be atan2(20, 20) = atan2(1, 1) = PI/4
        assertEquals(Math.PI / 4, line.inclination(), 1.0e-6, "Inclination should be PI/4");

        // Test a horizontal line (0 inclination)
        builder = new PathBuilder2f();
        start = Vector2f.of(10.0f, 20.0f);
        end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        path = builder.build();

        line = (Line2f) path.segments().get(1);

        assertEquals(0.0, line.inclination(), 1.0e-6, "Inclination of horizontal line should be 0");

        // Test a vertical line (PI/2 inclination)
        builder = new PathBuilder2f();
        start = Vector2f.of(10.0f, 20.0f);
        end = Vector2f.of(10.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        path = builder.build();

        line = (Line2f) path.segments().get(1);

        assertEquals(Math.PI / 2, line.inclination(), 1.0e-6, "Inclination of vertical line should be PI/2");
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(start);
        builder.lineTo(end);
        Path2f path = builder.build();

        Line2f line = (Line2f) path.segments().get(1);

        String str = line.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("LineTo2d"), "toString should contain class name");
        assertTrue(str.contains("10.000000,20.000000"), "toString should contain start point coordinates");
        assertTrue(str.contains("30.000000,40.000000"), "toString should contain end point coordinates");
    }
}