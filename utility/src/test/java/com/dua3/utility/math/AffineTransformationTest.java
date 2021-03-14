package com.dua3.utility.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AffineTransformationTest {

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
        Vec2d v = Vec2d.of(7,-1);
        AffineTransformation at = AffineTransformation.translate(Vec2d.of(-2,1));
        Vec2d expected = Vec2d.of(5, 0);
        Vec2d actual = at.transform(v);
        assertEquals(expected.x(), actual.x(), 1e-6);
        assertEquals(expected.y(), actual.y(), 1e-6);
        
        at = at.append(AffineTransformation.rotate(Math.PI/6));
        expected = Vec2d.of(4.330127, 2.5);
        actual = at.transform(v);
        assertEquals(expected.x(), actual.x(), 1e-6);
        assertEquals(expected.y(), actual.y(), 1e-6);

        at = at.append(AffineTransformation.translate(2,3));
        expected = Vec2d.of(6.330127, 5.5);
        actual = at.transform(v);
        assertEquals(expected.x(), actual.x(), 1e-6);
        assertEquals(expected.y(), actual.y(), 1e-6);
    }
}
