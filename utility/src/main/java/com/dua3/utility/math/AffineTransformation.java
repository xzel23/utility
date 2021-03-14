package com.dua3.utility.math;

import java.util.Objects;

/**
 * Defines an affine transformation in form of a matrix
 * <pre>
 * {@code
 *     a b c
 *     d e f
 *     0 0 1
 * }
 * </pre>.
 */
public class AffineTransformation {
    
    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final double e;
    private final double f;

    /**
     * Create affine transformation from the given matrix elements.
     * The affine transformation is defined by the matrix:
     * <pre>
     * {@code
     *     a b c
     *     d e f
     *     0 0 1
     * }
     * </pre>.
     * @param a matrix element a
     * @param b matrix element b
     * @param c matrix element c
     * @param d matrix element d
     * @param e matrix element e
     * @param f matrix element f
     */
    public AffineTransformation(
            double a,
            double b,
            double c,
            double d,
            double e,
            double f
    ) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    /**
     * Create an affine transformation for a rotation around the origin.
     * @param rotation the angle in radians
     * @return affine transformation (rotation)
     */
    public static AffineTransformation rotation(double rotation) {
        double sin = Math.sin(rotation);
        double cos = Math.cos(rotation);
        return new AffineTransformation(cos, -sin, 0, sin, cos, 0);
    }

    /**
     * Create an affine transformation for a translation.
     * @param x the x-value
     * @param y the y-value
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(double x, double y) {
        return new AffineTransformation(1, 0, x, 0, 1, y);
    }
    
    /**
     * Create an affine transformation for a translation.
     * @param v the translation vector
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(Vec2d v) {
        return new AffineTransformation(1, 0, v.x(), 0, 1, v.y());
    }

    /**
     * Create an affine transformation for a scaling operation (using same factor for x and y coordinates).
     * @param s the scaling factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double s) {
        return new AffineTransformation(s, 0, 0, 0, s, 0);
    }

    /**
     * Create an affine transformation for a scaling operation.
     * @param sx the scaling factor for the x-coordinate
     * @param sy the scaling factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double sx, double sy) {
        return new AffineTransformation(sx, 0, 0, 0, sy, 0);
    }

    /**
     * Create an affine transformation for a shearing operation.
     * @param cx the shearing factor for the x-coordinate
     * @param cy the shearing factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation shear(double cx, double cy) {
        return new AffineTransformation(1, cx, 0, cy, 1, 0);
    }

    /**
     * Create an affine transformation for a shearing operation.
     * @param A the affine transformation to append
     * @return affine transformation (combination of this and A)
     */
    public AffineTransformation append(AffineTransformation A) {
        return new AffineTransformation(
                a*A.a+b*A.b,a*A.b+b*A.e,a*A.c+b*A.f+c, 
                d*A.a+e*A.d,d*A.b+e*A.e, d*A.c+e*A.f+f);  
    }

    /**
     * Transform vector by applying this affine transformation to it.
     * @param v the vector to transform
     * @return the result of transformation
     */
    public Vec2d apply(Vec2d v) {
        double x = a*v.x() + b*v.y() + c;
        double y = d*v.x() + e*v.y() + f;
        return Vec2d.of(x,y);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffineTransformation that = (AffineTransformation) o;
        return that.a == a &&
        that.b == b &&    
        that.c == c &&    
        that.d == d &&    
        that.e == e &&    
        that.f == f;    
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d, e, f);
    }
}
