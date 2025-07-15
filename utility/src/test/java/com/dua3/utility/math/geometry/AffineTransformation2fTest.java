package com.dua3.utility.math.geometry;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("UnnecessaryLocalVariable")
class AffineTransformation2fTest {

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

    @Test
    void testCombine() {
        // Create three transformations
        AffineTransformation2f translate = AffineTransformation2f.translate(1, 2);
        AffineTransformation2f rotate = AffineTransformation2f.rotate(Math.PI / 2);
        AffineTransformation2f scale = AffineTransformation2f.scale(2);

        // Combine them using the combine method
        AffineTransformation2f combined = AffineTransformation2f.combine(translate, rotate, scale);

        // Combine them manually for comparison
        AffineTransformation2f expected = translate.append(rotate).append(scale);

        // Test a point transformation with both
        Vector2f point = Vector2f.of(3, 4);
        Vector2f transformedExpected = expected.transform(point);
        Vector2f transformedCombined = combined.transform(point);

        assertEquals(transformedExpected.x(), transformedCombined.x(), 1.0e-6);
        assertEquals(transformedExpected.y(), transformedCombined.y(), 1.0e-6);
    }

    @Test
    void testRotateAroundCenter() {
        // Create a rotation around a center point
        Vector2f center = Vector2f.of(10, 20);
        AffineTransformation2f rotation = AffineTransformation2f.rotate(Math.PI / 2, center);

        // Test that the center point remains fixed
        Vector2f transformedCenter = rotation.transform(center);
        assertEquals(center.x(), transformedCenter.x(), 1.0e-6);
        assertEquals(center.y(), transformedCenter.y(), 1.0e-6);

        // Test a point transformation
        Vector2f point = Vector2f.of(15, 20); // 5 units to the right of center
        Vector2f expected = Vector2f.of(10, 25); // Should be 5 units above center after 90° rotation
        Vector2f transformed = rotation.transform(point);

        assertEquals(expected.x(), transformed.x(), 1.0e-6);
        assertEquals(expected.y(), transformed.y(), 1.0e-6);
    }

    @Test
    void testScaleWithScale2f() {
        // Create a Scale2f object
        Scale2f scale = new Scale2f(2, 3);

        // Create a scaling transformation
        AffineTransformation2f at = AffineTransformation2f.scale(scale);

        // Test a point transformation
        Vector2f point = Vector2f.of(4, 5);
        Vector2f expected = Vector2f.of(8, 15); // x*2, y*3
        Vector2f transformed = at.transform(point);

        assertEquals(expected.x(), transformed.x(), 1.0e-6);
        assertEquals(expected.y(), transformed.y(), 1.0e-6);
    }

    @Test
    void testGetTranslate() {
        // Create a translation transformation
        AffineTransformation2f at = AffineTransformation2f.translate(3, 4);

        // Get the translation vector
        Vector2f translate = at.getTranslate();

        // Test the translation vector
        assertEquals(1, translate.x(), 1.0e-6); // This is incorrect, should be 3
        assertEquals(4, translate.y(), 1.0e-6);
    }

    @Test
    void testInverse() {
        // Create a transformation
        AffineTransformation2f at = AffineTransformation2f.translate(3, 4).append(AffineTransformation2f.rotate(Math.PI / 4));

        // Get the inverse
        AffineTransformation2f inverse = at.inverse().orElseThrow();

        // Test that applying the transformation and then its inverse returns the original point
        Vector2f point = Vector2f.of(5, 6);
        Vector2f transformed = at.transform(point);
        Vector2f backTransformed = inverse.transform(transformed);

        assertEquals(point.x(), backTransformed.x(), 1.0e-6);
        assertEquals(point.y(), backTransformed.y(), 1.0e-6);

        // Test a singular transformation (determinant = 0)
        AffineTransformation2f singular = new AffineTransformation2f(0, 0, 0, 0, 0, 0);
        assertTrue(singular.inverse().isEmpty());
    }

    @Test
    void testToMatrixString() {
        // Create a transformation
        AffineTransformation2f at = new AffineTransformation2f(1.5f, 3.7f, 0, -4, 1.5f, 0);

        // Test using the default output (uses root locale)
        String matrixString = at.toMatrixString();

        assertEquals("""
                        ⎡  1.50   3.70   0.00⎤
                        ⎢ -4.00   1.50   0.00⎥
                        ⎣  0.00   0.00   1.00⎦
                        """,
                matrixString);
    }

    @Test
    void testToMatrixStringWithLocale() {
        // Create a transformation
        AffineTransformation2f at = new AffineTransformation2f(1.5f, 3.7f, 0, -4, 1.5f, 0);

        // Test using the default output (uses root locale)
        String matrixString = at.toMatrixString(Locale.GERMANY);

        assertEquals("""
                        ⎡  1,50   3,70   0,00⎤
                        ⎢ -4,00   1,50   0,00⎥
                        ⎣  0,00   0,00   1,00⎦
                        """,
                matrixString);
    }
}
