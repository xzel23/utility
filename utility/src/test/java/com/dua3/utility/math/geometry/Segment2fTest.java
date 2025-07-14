package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test class for Segment2f implementations.
 */
class Segment2fTest {

    /**
     * Test the MoveTo2f implementation.
     */
    @Test
    void testMoveTo2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add a vertex
        Vector2f point = Vector2f.of(10, 20);
        path.addVertex(point);

        // Create a MoveTo2f segment
        MoveTo2f moveTo = new MoveTo2f(path, 0);

        // Test name method
        assertEquals("MOVE_TO", moveTo.name());

        // Test start and end methods
        assertEquals(point, moveTo.start());
        assertEquals(point, moveTo.end());

        // Test toString method
        assertNotNull(moveTo.toString());
    }

    /**
     * Test the Line2f implementation.
     */
    @Test
    void testLine2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add vertices
        Vector2f startPoint = Vector2f.of(10, 20);
        Vector2f endPoint = Vector2f.of(30, 40);
        path.addVertex(startPoint);
        path.addVertex(endPoint);

        // Create a Line2f segment
        Line2f line = new Line2f(path, 0, 1);

        // Test name method
        assertEquals("LINE", line.name());

        // Test start and end methods
        assertEquals(startPoint, line.start());
        assertEquals(endPoint, line.end());

        // Test toString method
        assertNotNull(line.toString());
    }

    /**
     * Test the ClosePath2f implementation.
     */
    @Test
    void testClosePath2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add vertices
        Vector2f startPoint = Vector2f.of(10, 20);
        Vector2f endPoint = Vector2f.of(30, 40);
        path.addVertex(startPoint);
        path.addVertex(endPoint);

        // Create a ClosePath2f segment
        ClosePath2f closePath = new ClosePath2f(path, 1, 0);

        // Test name method
        assertEquals("CLOSE_PATH", closePath.name());

        // Test start and end methods
        assertEquals(endPoint, closePath.start());
        assertEquals(startPoint, closePath.end());

        // Test toString method
        assertNotNull(closePath.toString());
    }

    /**
     * Test the Arc2f implementation.
     */
    @Test
    void testArc2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add vertices
        Vector2f startPoint = Vector2f.of(10, 20);
        Vector2f endPoint = Vector2f.of(30, 40);
        path.addVertex(startPoint);
        path.addVertex(endPoint);

        // Create an Arc2f segment
        float rx = 15;
        float ry = 15;
        float angle = 45;
        boolean largeArc = false;
        boolean sweep = false;
        Arc2f arc = new Arc2f(path, 0, 1, rx, ry, angle, largeArc, sweep);

        // Test name method
        assertEquals("ARC", arc.name());

        // Test start and end methods
        assertEquals(startPoint, arc.start());
        assertEquals(endPoint, arc.end());

        // Test toString method
        assertNotNull(arc.toString());
    }

    /**
     * Test the Curve2f implementation with quadratic Bézier curve.
     */
    @Test
    void testQuadraticCurve2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add vertices
        Vector2f startPoint = Vector2f.of(10, 20);
        Vector2f controlPoint = Vector2f.of(20, 30);
        Vector2f endPoint = Vector2f.of(30, 40);
        path.addVertex(startPoint);
        path.addVertex(controlPoint);
        path.addVertex(endPoint);

        // Create a quadratic Curve2f segment
        Curve2f curve = new Curve2f(path, 0, 1, 2);

        // Test name method
        assertEquals("CURVE", curve.name());

        // Test start and end methods
        assertEquals(startPoint, curve.start());
        assertEquals(endPoint, curve.end());

        // Test toString method
        assertNotNull(curve.toString());
    }

    /**
     * Test the Curve2f implementation with cubic Bézier curve.
     */
    @Test
    void testCubicCurve2f() {
        // Create a path implementation
        Path2fImpl path = new Path2fImpl();

        // Add vertices
        Vector2f startPoint = Vector2f.of(10, 20);
        Vector2f controlPoint1 = Vector2f.of(15, 25);
        Vector2f controlPoint2 = Vector2f.of(25, 35);
        Vector2f endPoint = Vector2f.of(30, 40);
        path.addVertex(startPoint);
        path.addVertex(controlPoint1);
        path.addVertex(controlPoint2);
        path.addVertex(endPoint);

        // Create a cubic Curve2f segment
        Curve2f curve = new Curve2f(path, 0, 1, 2, 3);

        // Test name method
        assertEquals("CURVE", curve.name());

        // Test start and end methods
        assertEquals(startPoint, curve.start());
        assertEquals(endPoint, curve.end());

        // Test toString method
        assertNotNull(curve.toString());
    }
}
