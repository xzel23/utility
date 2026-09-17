package com.dua3.utility.ui;

import com.dua3.utility.math.geometry.Arc2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphicsTest {

    @Test
    void testEnumsAndConstants() {
        assertArrayEquals(new float[0], Graphics.EMPTY_DASHES);

        assertEquals(Graphics.TextWrapping.WRAP, Graphics.TextWrapping.valueOf("WRAP"));
        assertEquals(Graphics.TextWrapping.NO_WRAP, Graphics.TextWrapping.valueOf("NO_WRAP"));
        assertEquals(2, Graphics.TextWrapping.values().length);

        assertEquals(Graphics.TextRotationMode.ROTATE_OUTPUT_AREA, Graphics.TextRotationMode.valueOf("ROTATE_OUTPUT_AREA"));
        assertEquals(Graphics.TextRotationMode.ROTATE_AND_TRANSLATE, Graphics.TextRotationMode.valueOf("ROTATE_AND_TRANSLATE"));
        assertEquals(Graphics.TextRotationMode.ROTATE_LINES, Graphics.TextRotationMode.valueOf("ROTATE_LINES"));
        assertEquals(Graphics.TextRotationMode.ROTATE_AND_TRANSLATE_LINES, Graphics.TextRotationMode.valueOf("ROTATE_AND_TRANSLATE_LINES"));
        assertEquals(4, Graphics.TextRotationMode.values().length);
    }

    @Test
    void testApproximateArcValid() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(10, 0);
        Vector2f radii = Vector2f.of(10, 10);

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, moveTos::add, segments::add);

        assertEquals(1, moveTos.size());
        assertFalse(segments.isEmpty());
        for (Vector2f[] seg : segments) {
            assertEquals(3, seg.length);
        }
    }

    @Test
    void testApproximateArcObject() {
        Path2f path = Path2f.builder()
                .moveTo(0, 0)
                .arcTo(Vector2f.of(10, 10), Vector2f.of(10, 10), 0.0f, true, false)
                .build();
        Arc2f arc = (Arc2f) path.segments().get(1);

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(arc, moveTos::add, segments::add);

        assertEquals(1, moveTos.size());
        assertFalse(segments.isEmpty());
    }

    @Test
    void testApproximateArcDegenerateStraightLine() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(5, 0);
        Vector2f radii = Vector2f.of(0, 10); // rx is zero

        List<Vector2f> moveTos = new ArrayList<>();
        List<Vector2f[]> segments = new ArrayList<>();

        Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, moveTos::add, segments::add);

        assertEquals(0, moveTos.size());
        assertEquals(1, segments.size());
        assertEquals(3, segments.getFirst().length);
    }

    @Test
    void testApproximateArcInvalidDistanceThrows() {
        Vector2f p0 = Vector2f.of(0, 0);
        Vector2f p1 = Vector2f.of(100, 0);
        Vector2f radii = Vector2f.of(0, 10); // rx is zero and distance 100 > 2 * 10

        assertThrows(IllegalArgumentException.class, () ->
                Graphics.approximateArc(p0, p1, radii, 0.0f, false, true, p -> {}, s -> {}));
    }
}
