package com.dua3.utility.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AffineTransformationTest {

    @Test
    void constructorAndAccessors() {
        AffineTransformation at = new AffineTransformation(1,2,3,4,5,6);
        
        assertEquals(1, at.getScaleX());
        assertEquals(2, at.getShearX());
        assertEquals(3, at.getTranslateX());
        assertEquals(4, at.getShearY());
        assertEquals(5, at.getScaleY());
        assertEquals(6, at.getTranslateY());
    }
    
    @Test
    void identity() {
        AffineTransformation at = AffineTransformation.identity();
        assertEquals(Vec2d.of(1,0), at.transform(Vec2d.of(1,0)));
    }

    @Test
    void rotate() {
        AffineTransformation at = AffineTransformation.rotate(Math.PI / 2);
        assertTrue(at.transform(Vec2d.of(1,0)).subtract(Vec2d.of(0,1)).length()<1e-10);

        at = AffineTransformation.rotate(Math.PI/6);
        Vec2d v = Vec2d.of(5,0);
        Vec2d expected = Vec2d.of(4.330127, 2.5);
        Vec2d actual = at.transform(v);
        assertEquals(expected.x(), actual.x(), 1e-6);
        assertEquals(expected.y(), actual.y(), 1e-6);
    }

    @Test
    void translate() {
        AffineTransformation at = AffineTransformation.translate(1, 5);
        assertEquals(Vec2d.of(3, 4), at.transform(Vec2d.of(2,-1)));
    }

    @Test
    void scale() {
        AffineTransformation at = AffineTransformation.scale(7);
        assertEquals(Vec2d.of(7, 14), at.transform(Vec2d.of(1,2)));

        at = AffineTransformation.scale(3,4);
        assertEquals(Vec2d.of(3, 8), at.transform(Vec2d.of(1,2)));
    }

    @Test
    void shear() {
        AffineTransformation at = AffineTransformation.shear(0.5);
        assertEquals(Vec2d.of(12, 10), at.transform(Vec2d.of(7,10)));
    }
    
    @Test
    void append() {
        /*
         * Perform three different transformations separately and check results. Then use a combined
         * affine transformation that should give the same result.
         */
        Vec2d v = Vec2d.of(7,-1);
        
        AffineTransformation translate = AffineTransformation.translate(Vec2d.of(-2,1));
        Vec2d expected1 = Vec2d.of(5, 0);
        Vec2d actual1 = translate.transform(v);
        assertEquals(expected1.x(), actual1.x(), 1e-6);
        assertEquals(expected1.y(), actual1.y(), 1e-6);

        AffineTransformation rotate = AffineTransformation.rotate(Math.PI / 6);
        Vec2d expected2 = Vec2d.of(5*Math.cos(Math.PI/6), 2.5);
        Vec2d actual2 = rotate.transform(actual1);
        assertEquals(expected2.x(), actual2.x(), 1e-6);
        assertEquals(expected2.y(), actual2.y(), 1e-6);

        Vec2d actual2combined = translate.append(rotate).transform(v);
        assertEquals(expected2.x(), actual2combined.x(), 1e-6);
        assertEquals(expected2.y(), actual2combined.y(), 1e-6);

        AffineTransformation translate2 = AffineTransformation.translate(2,3);
        Vec2d expected3 = Vec2d.of(expected2.x()+2, expected2.y()+3);
        Vec2d actual3 = translate2.transform(actual2);
        assertEquals(expected3.x(), actual3.x(), 1e-6);
        assertEquals(expected3.y(), actual3.y(), 1e-6);
        
        // create a combined affine transformation
        AffineTransformation combined = translate.append(rotate).append(translate2);
        Vec2d expected4 = expected3;
        Vec2d actual4 = combined.transform(v);
        assertEquals(expected4.x(), actual4.x(), 1e-6);
        assertEquals(expected4.y(), actual4.y(), 1e-6);
     }
}
