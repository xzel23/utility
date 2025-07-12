package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Arc2f.
 */
class Arc2fTest {

    /**
     * Test the constructor and accessors.
     * Since Arc2f constructor is package-private, we need to create an Arc2f instance
     * through a PathBuilder2f.
     */
    @Test
    void testConstructorAndAccessors() {
        // Create a path with an arc
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 40.0f);
        Vector2f radius = Vector2f.of(15.0f, 25.0f);
        float angle = 45.0f;
        boolean largeArc = true;
        boolean sweep = false;

        builder.moveTo(start);
        builder.arcTo(end, radius, angle, largeArc, sweep);
        Path2f path = builder.build();

        // Get the arc segment
        Segment2f segment = path.segments().get(1); // Index 0 is MoveTo, 1 is Arc
        assertInstanceOf(Arc2f.class, segment, "Segment should be an Arc2f");

        Arc2f arc = (Arc2f) segment;

        // Test accessors
        assertEquals(15.0f, arc.rx(), "rx should be 15f");
        assertEquals(25.0f, arc.ry(), "ry should be 25f");
        assertEquals(45.0f, arc.angle(), "angle should be 45f");
        assertTrue(arc.largeArc(), "largeArc should be true");
        assertFalse(arc.sweep(), "sweep should be false");

        // Test inherited methods
        assertEquals(start, arc.start(), "start point should match");
        assertEquals(end, arc.end(), "end point should match");
        assertEquals("ARC", arc.name(), "name should be ARC");
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        // Create a path with an arc
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f start = Vector2f.of(10.0f, 20.0f);
        Vector2f end = Vector2f.of(30.0f, 40.0f);
        Vector2f radius = Vector2f.of(15.0f, 25.0f);
        float angle = 45.0f;
        boolean largeArc = true;
        boolean sweep = false;

        builder.moveTo(start);
        builder.arcTo(end, radius, angle, largeArc, sweep);
        Path2f path = builder.build();

        // Get the arc segment
        Arc2f arc = (Arc2f) path.segments().get(1); // Index 0 is MoveTo, 1 is Arc

        // Test toString
        String str = arc.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Arc2f"), "toString should contain class name");
        assertTrue(str.contains("15.000000,25.000000"), "toString should contain radius");
        assertTrue(str.contains("45.000000rad"), "toString should contain angle");
        assertTrue(str.contains("largeArc(1)"), "toString should contain largeArc");
        assertTrue(str.contains("sweep(0)"), "toString should contain sweep");
    }
}