package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Curve2f.
 */
class Curve2fTest {

    /**
     * Test the quadratic curve constructor and accessors.
     * Since Curve2f constructor is package-private, we need to create a Curve2f instance
     * through a PathBuilder2f.
     */
    @Test
    void testQuadraticCurveConstructorAndAccessors() {
        // Create a path with a quadratic curve
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f control = Vector2f.of(20.0f, 30.0f);
        Vector2f end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.curveTo(control, end);
        Path2f path = builder.build();

        // Get the curve segment
        Segment2f segment = path.segments().get(1); // Index 0 is MoveTo, 1 is Curve
        assertInstanceOf(Curve2f.class, segment, "Segment should be a Curve2f");

        Curve2f curve = (Curve2f) segment;

        // Test accessors
        assertEquals(3, curve.numberOfControls(), "Quadratic curve should have 3 control points");
        assertEquals(start, curve.control(0), "First control point should be start point");
        assertEquals(control, curve.control(1), "Second control point should be control point");
        assertEquals(end, curve.control(2), "Third control point should be end point");

        // Test inherited methods
        assertEquals(start, curve.start(), "start point should match");
        assertEquals(end, curve.end(), "end point should match");
        assertEquals("CURVE", curve.name(), "name should be CURVE");
    }

    /**
     * Test the cubic curve constructor and accessors.
     */
    @Test
    void testCubicCurveConstructorAndAccessors() {
        // Create a path with a cubic curve
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f control1 = Vector2f.of(15.0f, 30.0f);
        Vector2f control2 = Vector2f.of(25.0f, 30.0f);
        Vector2f end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.curveTo(control1, control2, end);
        Path2f path = builder.build();

        // Get the curve segment
        Segment2f segment = path.segments().get(1); // Index 0 is MoveTo, 1 is Curve
        assertInstanceOf(Curve2f.class, segment, "Segment should be a Curve2f");

        Curve2f curve = (Curve2f) segment;

        // Test accessors
        assertEquals(4, curve.numberOfControls(), "Cubic curve should have 4 control points");
        assertEquals(start, curve.control(0), "First control point should be start point");
        assertEquals(control1, curve.control(1), "Second control point should be first control point");
        assertEquals(control2, curve.control(2), "Third control point should be second control point");
        assertEquals(end, curve.control(3), "Fourth control point should be end point");

        // Test inherited methods
        assertEquals(start, curve.start(), "start point should match");
        assertEquals(end, curve.end(), "end point should match");
        assertEquals("CURVE", curve.name(), "name should be CURVE");
    }

    /**
     * Test the toString method for a quadratic curve.
     */
    @Test
    void testQuadraticCurveToString() {
        // Create a path with a quadratic curve
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f control = Vector2f.of(20.0f, 30.0f);
        Vector2f end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.curveTo(control, end);
        Path2f path = builder.build();

        // Get the curve segment
        Curve2f curve = (Curve2f) path.segments().get(1);

        // Test toString
        String str = curve.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Curve2f"), "toString should contain class name");
        assertTrue(str.contains("10.000000,20.000000"), "toString should contain start point coordinates");
        assertTrue(str.contains("20.000000,30.000000"), "toString should contain control point coordinates");
        assertTrue(str.contains("30.000000,20.000000"), "toString should contain end point coordinates");
    }

    /**
     * Test the toString method for a cubic curve.
     */
    @Test
    void testCubicCurveToString() {
        // Create a path with a cubic curve
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f control1 = Vector2f.of(15.0f, 30.0f);
        Vector2f control2 = Vector2f.of(25.0f, 30.0f);
        Vector2f end = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(start);
        builder.curveTo(control1, control2, end);
        Path2f path = builder.build();

        // Get the curve segment
        Curve2f curve = (Curve2f) path.segments().get(1);

        // Test toString
        String str = curve.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Curve2f"), "toString should contain class name");
        assertTrue(str.contains("10.000000,20.000000"), "toString should contain start point coordinates");
        assertTrue(str.contains("15.000000,30.000000"), "toString should contain first control point coordinates");
        assertTrue(str.contains("25.000000,30.000000"), "toString should contain second control point coordinates");
        assertTrue(str.contains("30.000000,20.000000"), "toString should contain end point coordinates");
    }
}