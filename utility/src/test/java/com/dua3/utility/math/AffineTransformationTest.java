package com.dua3.utility.math;

import com.dua3.utility.math.Vector2f;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnnecessaryLocalVariable")
class AffineTransformationTest {

    @Test
    void constructorAndAccessors() {
        AffineTransformation2f at = new AffineTransformation2f(1, 2, 3, 4, 5, 6);

        assertEquals(1, at.getScaleX());
        assertEquals(2, at.getShearX());
        assertEquals(3, at.getTranslateX());
        assertEquals(4, at.getShearY());
        assertEquals(5, at.getScaleY());
        assertEquals(6, at.getTranslateY());
    }

    @Test
    void identity() {
        AffineTransformation2f at = AffineTransformation2f.identity();
        assertEquals(Vector2f.of(1, 0), at.transform(Vector2f.of(1, 0)));
    }

    @Test
    void rotate() {
        AffineTransformation2f at = AffineTransformation2f.rotate(Math.PI / 2);
        assertTrue(at.transform(Vector2f.of(1, 0)).subtract(Vector2f.of(0, 1)).length() < 1.0e-10);

        at = AffineTransformation2f.rotate(Math.PI / 6);
        Vector2f v = Vector2f.of(5, 0);
        Vector2f expected = Vector2f.of(4.330127f, 2.5f);
        Vector2f actual = at.transform(v);
        assertEquals(expected.x(), actual.x(), 1.0e-6);
        assertEquals(expected.y(), actual.y(), 1.0e-6);
    }

    @Test
    void translate() {
        AffineTransformation2f at = AffineTransformation2f.translate(1, 5);
        assertEquals(Vector2f.of(3, 4), at.transform(Vector2f.of(2, -1)));
    }

    @Test
    void scale() {
        AffineTransformation2f at = AffineTransformation2f.scale(7);
        assertEquals(Vector2f.of(7, 14), at.transform(Vector2f.of(1, 2)));

        at = AffineTransformation2f.scale(3, 4);
        assertEquals(Vector2f.of(3, 8), at.transform(Vector2f.of(1, 2)));
    }

    @Test
    void shear() {
        AffineTransformation2f at = AffineTransformation2f.shear(0.5f);
        assertEquals(Vector2f.of(12, 10), at.transform(Vector2f.of(7, 10)));
    }

    @Test
    void append() {
        /*
         * Perform three different transformations separately and check results. Then use a combined
         * affine transformation that should give the same result.
         */
        Vector2f v = Vector2f.of(7, -1);

        AffineTransformation2f translate = AffineTransformation2f.translate(Vector2f.of(-2, 1));
        Vector2f expected1 = Vector2f.of(5, 0);
        Vector2f actual1 = translate.transform(v);
        assertEquals(expected1.x(), actual1.x(), 1.0e-6);
        assertEquals(expected1.y(), actual1.y(), 1.0e-6);

        AffineTransformation2f rotate = AffineTransformation2f.rotate(Math.PI / 6);
        Vector2f expected2 = Vector2f.of((float) (5 * Math.cos(Math.PI / 6)), 2.5f);
        Vector2f actual2 = rotate.transform(actual1);
        assertEquals(expected2.x(), actual2.x(), 1.0e-6);
        assertEquals(expected2.y(), actual2.y(), 1.0e-6);

        Vector2f actual2combined = translate.append(rotate).transform(v);
        assertEquals(expected2.x(), actual2combined.x(), 1.0e-6);
        assertEquals(expected2.y(), actual2combined.y(), 1.0e-6);

        AffineTransformation2f translate2 = AffineTransformation2f.translate(2, 3);
        Vector2f expected3 = Vector2f.of(expected2.x() + 2, expected2.y() + 3);
        Vector2f actual3 = translate2.transform(actual2);
        assertEquals(expected3.x(), actual3.x(), 1.0e-6);
        assertEquals(expected3.y(), actual3.y(), 1.0e-6);

        // create a combined affine transformation
        AffineTransformation2f combined = translate.append(rotate).append(translate2);
        Vector2f expected4 = expected3;
        Vector2f actual4 = combined.transform(v);
        assertEquals(expected4.x(), actual4.x(), 1.0e-6);
        assertEquals(expected4.y(), actual4.y(), 1.0e-6);
    }
}
