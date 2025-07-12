package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for Path2f.
 */
class Path2fTest {

    /**
     * Test the builder method.
     */
    @Test
    void testBuilder() {
        PathBuilder2f builder = Path2f.builder();
        assertNotNull(builder, "builder should not return null");
    }

    /**
     * Test creating an empty path.
     */
    @Test
    void testEmptyPath() {
        PathBuilder2f builder = Path2f.builder();
        Path2f path = builder.build();

        assertNotNull(path, "path should not be null");
        assertTrue(path.vertices().isEmpty(), "vertices should be empty");
        assertTrue(path.segments().isEmpty(), "segments should be empty");
    }

    /**
     * Test creating a path with a single point.
     */
    @Test
    void testSinglePointPath() {
        PathBuilder2f builder = Path2f.builder();
        Vector2f point = Vector2f.of(10.0f, 20.0f);

        builder.moveTo(point);
        Path2f path = builder.build();

        assertNotNull(path, "path should not be null");
        assertEquals(1, path.vertices().size(), "vertices should have 1 element");
        assertEquals(1, path.segments().size(), "segments should have 1 element");

        assertEquals(point, path.vertices().get(0), "vertex should match the point");
        assertInstanceOf(MoveTo2f.class, path.segments().get(0), "segment should be a MoveTo2f");
    }

    /**
     * Test creating a path with multiple segments.
     */
    @Test
    void testMultiSegmentPath() {
        PathBuilder2f builder = Path2f.builder();
        Vector2f point1 = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 20.0f);
        Vector2f point3 = Vector2f.of(20.0f, 40.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);
        builder.lineTo(point3);
        builder.closePath();
        Path2f path = builder.build();

        assertNotNull(path, "path should not be null");
        assertEquals(3, path.vertices().size(), "vertices should have 3 elements");
        assertEquals(4, path.segments().size(), "segments should have 4 elements");

        List<Vector2f> vertices = path.vertices();
        assertEquals(point1, vertices.get(0), "first vertex should match point1");
        assertEquals(point2, vertices.get(1), "second vertex should match point2");
        assertEquals(point3, vertices.get(2), "third vertex should match point3");

        List<Segment2f> segments = path.segments();
        assertInstanceOf(MoveTo2f.class, segments.get(0), "first segment should be a MoveTo2f");
        assertInstanceOf(Line2f.class, segments.get(1), "second segment should be a Line2f");
        assertInstanceOf(Line2f.class, segments.get(2), "third segment should be a Line2f");
        assertInstanceOf(ClosePath2f.class, segments.get(3), "fourth segment should be a ClosePath2f");
    }

    /**
     * Test the toString method.
     */
    @Test
    void testToString() {
        PathBuilder2f builder = Path2f.builder();
        Vector2f point1 = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);
        Path2f path = builder.build();

        String str = path.toString();
        assertNotNull(str, "toString should not return null");
        assertTrue(str.contains("Path2f"), "toString should contain class name");
        assertTrue(str.contains("MoveTo2f"), "toString should contain MoveTo2f");
        assertTrue(str.contains("LineTo2d"), "toString should contain LineTo2d");
    }

    /**
     * Test that the vertices and segments lists are unmodifiable.
     */
    @Test
    void testUnmodifiableLists() {
        PathBuilder2f builder = Path2f.builder();
        Vector2f point1 = Vector2f.of(10.0f, 20.0f);
        Vector2f point2 = Vector2f.of(30.0f, 20.0f);

        builder.moveTo(point1);
        builder.lineTo(point2);
        Path2f path = builder.build();

        List<Vector2f> vertices = path.vertices();
        List<Segment2f> segments = path.segments();

        assertThrows(UnsupportedOperationException.class, () -> vertices.add(Vector2f.of(40.0f, 50.0f)), "vertices list should be unmodifiable");
        assertThrows(UnsupportedOperationException.class, segments::clear, "segments list should be unmodifiable");
    }
}