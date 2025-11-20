package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PathBuilder2f.
 */
class PathBuilder2fTest {

    /**
     * Test the constructor and isEmpty method.
     */
    @Test
    void testConstructorAndIsEmpty() {
        PathBuilder2f builder = new PathBuilder2f();
        assertTrue(builder.isEmpty(), "New builder should be empty");
    }

    /**
     * Test the moveTo method.
     */
    @Test
    void testMoveTo() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point = new Vector2f(10.0f, 20.0f);

        builder.moveTo(point);

        assertFalse(builder.isEmpty(), "Builder should not be empty after moveTo");
        assertEquals(point, builder.current(), "Current point should be the moved-to point");
    }

    /**
     * Test the moveRel method.
     */
    @Test
    void testMoveRel() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f initialPoint = new Vector2f(10.0f, 20.0f);
        Vector2f offset = new Vector2f(5.0f, 10.0f);
        Vector2f expectedPoint = new Vector2f(15.0f, 30.0f);

        builder.moveTo(initialPoint);
        builder.moveRel(offset);

        assertEquals(expectedPoint, builder.current(), "Current point should be the initial point plus offset");
    }

    /**
     * Test the lineTo method.
     */
    @Test
    void testLineTo() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f endPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.lineTo(endPoint);

        assertEquals(endPoint, builder.current(), "Current point should be the end point");
    }

    /**
     * Test the lineRel method.
     */
    @Test
    void testLineRel() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f offset = new Vector2f(20.0f, 20.0f);
        Vector2f expectedPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.lineRel(offset);

        assertEquals(expectedPoint, builder.current(), "Current point should be the start point plus offset");
    }

    /**
     * Test the arcTo method.
     */
    @Test
    void testArcTo() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f endPoint = new Vector2f(30.0f, 40.0f);
        Vector2f radius = new Vector2f(10.0f, 10.0f);
        float angle = 45.0f;

        builder.moveTo(startPoint);
        builder.arcTo(endPoint, radius, angle, false, false);

        assertEquals(endPoint, builder.current(), "Current point should be the end point");
    }

    /**
     * Test the arcRel method.
     */
    @Test
    void testArcRel() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f offset = new Vector2f(20.0f, 20.0f);
        Vector2f radius = new Vector2f(10.0f, 10.0f);
        float angle = 45.0f;
        Vector2f expectedPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.arcRel(offset, radius, angle, false, false);

        assertEquals(expectedPoint, builder.current(), "Current point should be the start point plus offset");
    }

    /**
     * Test the quadratic curveTo method.
     */
    @Test
    void testQuadraticCurveTo() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f controlPoint = new Vector2f(20.0f, 30.0f);
        Vector2f endPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.curveTo(controlPoint, endPoint);

        assertEquals(endPoint, builder.current(), "Current point should be the end point");
    }

    /**
     * Test the cubic curveTo method.
     */
    @Test
    void testCubicCurveTo() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f controlPoint1 = new Vector2f(15.0f, 25.0f);
        Vector2f controlPoint2 = new Vector2f(25.0f, 35.0f);
        Vector2f endPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.curveTo(controlPoint1, controlPoint2, endPoint);

        assertEquals(endPoint, builder.current(), "Current point should be the end point");
    }

    /**
     * Test the closePath method.
     */
    @Test
    void testClosePath() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        Vector2f point2 = new Vector2f(30.0f, 20.0f);
        Vector2f point3 = new Vector2f(20.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();

        // After closePath, a new path should be started when adding segments
        Vector2f newPoint = new Vector2f(50.0f, 60.0f);
        builder.moveTo(newPoint);

        assertEquals(newPoint, builder.current(), "Current point should be the new point after closing path");
    }

    /**
     * Test the build method.
     */
    @Test
    void testBuild() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point1 = new Vector2f(10.0f, 20.0f);
        Vector2f point2 = new Vector2f(30.0f, 20.0f);
        Vector2f point3 = new Vector2f(20.0f, 40.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();

        Path2f path = builder.build();

        assertNotNull(path, "Built path should not be null");
        assertEquals(3, path.vertices().size(), "Path should have 3 vertices");
        assertEquals(4, path.segments().size(), "Path should have 4 segments (moveTo, lineTo, lineTo, closePath)");
        assertTrue(builder.isEmpty(), "Builder should be empty after build");
    }

    /**
     * Test building an empty path.
     */
    @Test
    void testBuildEmptyPath() {
        PathBuilder2f builder = new PathBuilder2f();
        Path2f path = builder.build();

        assertNotNull(path, "Built path should not be null");
        assertTrue(path.vertices().isEmpty(), "Path should have no vertices");
        assertTrue(path.segments().isEmpty(), "Path should have no segments");
    }

    /**
     * Test building a path without closing it.
     */
    @Test
    void testBuildWithoutClosing() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point1 = new Vector2f(10.0f, 20.0f);
        Vector2f point2 = new Vector2f(30.0f, 20.0f);
        Vector2f point3 = new Vector2f(20.0f, 40.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);
        builder.lineTo(point3);

        Path2f path = builder.build();

        assertNotNull(path, "Built path should not be null");
        assertEquals(3, path.vertices().size(), "Path should have 3 vertices");
        assertEquals(3, path.segments().size(), "Path should have 3 segments (moveTo, lineTo, lineTo)");
    }

    /**
     * Test the vertex method.
     */
    @Test
    void testVertex() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point1 = new Vector2f(10.0f, 20.0f);
        Vector2f point2 = new Vector2f(30.0f, 40.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);

        // Test retrieving vertices by index
        assertEquals(point1, builder.vertex(0), "First vertex should match point1");
        assertEquals(point2, builder.vertex(1), "Second vertex should match point2");
    }

    /**
     * Test the moveTo method with float parameters.
     */
    @Test
    void testMoveToWithFloats() {
        PathBuilder2f builder = new PathBuilder2f();
        float x = 10.0f;
        float y = 20.0f;
        Vector2f expectedPoint = new Vector2f(x, y);

        builder.moveTo(x, y);

        assertFalse(builder.isEmpty(), "Builder should not be empty after moveTo");
        assertEquals(expectedPoint, builder.current(), "Current point should be the moved-to point");
    }

    /**
     * Test the moveRel method with float parameters.
     */
    @Test
    void testMoveRelWithFloats() {
        PathBuilder2f builder = new PathBuilder2f();
        float initialX = 10.0f;
        float initialY = 20.0f;
        float offsetX = 5.0f;
        float offsetY = 10.0f;
        Vector2f initialPoint = new Vector2f(initialX, initialY);
        Vector2f expectedPoint = new Vector2f(15.0f, 30.0f);

        builder.moveTo(initialPoint);
        builder.moveRel(offsetX, offsetY);

        assertEquals(expectedPoint, builder.current(), "Current point should be the initial point plus offset");
    }

    /**
     * Test the lineTo method with float parameters.
     */
    @Test
    void testLineToWithFloats() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        float endX = 30.0f;
        float endY = 40.0f;
        Vector2f expectedPoint = new Vector2f(endX, endY);

        builder.moveTo(startPoint);
        builder.lineTo(endX, endY);

        assertEquals(expectedPoint, builder.current(), "Current point should be the end point");
    }

    /**
     * Test the lineRel method with float parameters.
     */
    @Test
    void testLineRelWithFloats() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f startPoint = new Vector2f(10.0f, 20.0f);
        float offsetX = 20.0f;
        float offsetY = 20.0f;
        Vector2f expectedPoint = new Vector2f(30.0f, 40.0f);

        builder.moveTo(startPoint);
        builder.lineRel(offsetX, offsetY);

        assertEquals(expectedPoint, builder.current(), "Current point should be the start point plus offset");
    }
}
