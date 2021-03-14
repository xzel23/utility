package com.dua3.utility.math;

import java.util.Objects;

/**
 * Defines an affine transformation in form of a matrix
 * <pre>
 * {@code
 *     a c e
 *     b d f
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
     *     a c e
     *     b d f
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
        return new AffineTransformation(cos, sin, -sin, cos, 0, 0);
    }

    /**
     * Create an affine transformation for a translation.
     * @param x the x-value
     * @param y the y-value
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(double x, double y) {
        return new AffineTransformation(1, 0, 0, 1, x, y);
    }
    
    /**
     * Create an affine transformation for a translation.
     * @param v the translation vector
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(Vec2d v) {
        return new AffineTransformation(1, 0, 0, 1, v.x(), v.y());
    }

    /**
     * Create an affine transformation for a scaling operation (using same factor for x and y coordinates).
     * @param s the scaling factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double s) {
        return new AffineTransformation(s, 0, 0, s, 0, 0);
    }

    /**
     * Create an affine transformation for a scaling operation.
     * @param sx the scaling factor for the x-coordinate
     * @param sy the scaling factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double sx, double sy) {
        return new AffineTransformation(sx, 0, 0, sy, 0, 0);
    }

    /**
     * Create an affine transformation for a shearing operation.
     * @param cx the shearing factor for the x-coordinate
     * @param cy the shearing factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation shear(double cx, double cy) {
        return new AffineTransformation(1, cx, cy, 1, 0, 0);
    }

    /**
     * Create an affine transformation for a shearing operation.
     * @param A the affine transformation to append
     * @return affine transformation (combination of this and A)
     */
    public AffineTransformation append(AffineTransformation A) {
        return new AffineTransformation(
                a*A.a+c*A.b,a*A.c+c*A.d,a*A.e+c*A.f+e, 
                b*A.a+b*A.d,b*A.c+d*A.d,b*A.e+d*A.f+f);  
    }

    /**
     * Transform vector by applying this affine transformation to it.
     * @param v the vector to transform
     * @return the result of transformation
     */
    public Vec2d transform(Vec2d v) {
        return transform(v.x(), v.y());
    }
    
    /**
     * Transform vector by applying this affine transformation to it.
     * @param x the x-coordinate of the point to transform
     * @param y the y-coordinate of the point to transform
     * @return the result of transformation
     */
    public Vec2d transform(double x, double y) {
        double xt = a*x + c*y + e;
        double yt = b*x + d*y + f;
        return Vec2d.of(xt,yt);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AffineTransformation that = (AffineTransformation) o;
        return  that.a == a &&
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
