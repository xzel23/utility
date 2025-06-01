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
        Vector2f point = new Vector2f(10f, 20f);
        
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
        Vector2f initialPoint = new Vector2f(10f, 20f);
        Vector2f offset = new Vector2f(5f, 10f);
        Vector2f expectedPoint = new Vector2f(15f, 30f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f endPoint = new Vector2f(30f, 40f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f offset = new Vector2f(20f, 20f);
        Vector2f expectedPoint = new Vector2f(30f, 40f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f endPoint = new Vector2f(30f, 40f);
        Vector2f radius = new Vector2f(10f, 10f);
        float angle = 45f;
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f offset = new Vector2f(20f, 20f);
        Vector2f radius = new Vector2f(10f, 10f);
        float angle = 45f;
        Vector2f expectedPoint = new Vector2f(30f, 40f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f controlPoint = new Vector2f(20f, 30f);
        Vector2f endPoint = new Vector2f(30f, 40f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f controlPoint1 = new Vector2f(15f, 25f);
        Vector2f controlPoint2 = new Vector2f(25f, 35f);
        Vector2f endPoint = new Vector2f(30f, 40f);
        
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
        Vector2f startPoint = new Vector2f(10f, 20f);
        Vector2f point2 = new Vector2f(30f, 20f);
        Vector2f point3 = new Vector2f(20f, 40f);
        
        builder.moveTo(startPoint);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();
        
        // After closePath, a new path should be started when adding segments
        Vector2f newPoint = new Vector2f(50f, 60f);
        builder.moveTo(newPoint);
        
        assertEquals(newPoint, builder.current(), "Current point should be the new point after closing path");
    }

    /**
     * Test the build method.
     */
    @Test
    void testBuild() {
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point1 = new Vector2f(10f, 20f);
        Vector2f point2 = new Vector2f(30f, 20f);
        Vector2f point3 = new Vector2f(20f, 40f);
        
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
        Vector2f point1 = new Vector2f(10f, 20f);
        Vector2f point2 = new Vector2f(30f, 20f);
        Vector2f point3 = new Vector2f(20f, 40f);
        
        builder.moveTo(point1);
        builder.lineTo(point2);
        builder.lineTo(point3);
        
        Path2f path = builder.build();
        
        assertNotNull(path, "Built path should not be null");
        assertEquals(3, path.vertices().size(), "Path should have 3 vertices");
        assertEquals(3, path.segments().size(), "Path should have 3 segments (moveTo, lineTo, lineTo)");
    }
}