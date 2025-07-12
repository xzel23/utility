package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for MoveTo2f.
 */
class MoveTo2fTest {

    /**
     * Test the constructor and accessors.
     * Since MoveTo2f constructor is package-private, we need to create a MoveTo2f instance
     * through a PathBuilder2f.
     */
    @Test
    void testConstructorAndAccessors() {
        // Create a path with a move to segment
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point = Vector2f.of(10.0f, 20.0f);

        builder.moveTo(point);
        Path2f path = builder.build();

        // Get the move to segment
        Segment2f segment = path.segments().get(0);
        assertInstanceOf(MoveTo2f.class, segment, "Segment should be a MoveTo2f");

        MoveTo2f moveTo = (MoveTo2f) segment;

        // Test inherited methods
        assertEquals(point, moveTo.start(), "start point should match");
        assertEquals(point, moveTo.end(), "end point should match the start point for MoveTo");
        assertEquals("MOVE_TO", moveTo.name(), "name should be MOVE_TO");
    }

    /**
     * Test multiple moveTo operations in a path.
     */
    @Test
    void testMultipleMoveTo() {
        // Create a path with multiple move to segments
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point1 = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 40.0f);

        builder.moveTo(point1);
        builder.moveTo(point2);
        Path2f path = builder.build();

        // Check that there are two segments
        assertEquals(2, path.segments().size(), "Path should have 2 segments");

        // Get the move to segments
        MoveTo2f moveTo1 = (MoveTo2f) path.segments().get(0);
        MoveTo2f moveTo2 = (MoveTo2f) path.segments().get(1);

        // Test that they point to the correct locations
        assertEquals(point1, moveTo1.start(), "First moveTo should point to point1");
        assertEquals(point2, moveTo2.start(), "Second moveTo should point to point2");
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        // Create a path with a move to segment
        PathBuilder2f builder = new PathBuilder2f();
        Vector2f point = Vector2f.of(10.0f, 20.0f);

        builder.moveTo(point);
        Path2f path = builder.build();

        // Get the move to segment
        MoveTo2f moveTo = (MoveTo2f) path.segments().get(0);

        // Test toString
        String str = moveTo.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("MoveTo2f"), "toString should contain class name");
        assertTrue(str.contains("10.000000,20.000000"), "toString should contain point coordinates");
    }
}