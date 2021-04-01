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

    public static final AffineTransformation IDENTITY = new AffineTransformation(1, 0, 0, 1, 0, 0);
    
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
     * Return the identity transformation.
     * @return affine transformation (identity)
     */
    @SuppressWarnings("SameReturnValue")
    public static AffineTransformation identity() {
        return IDENTITY;
    }

    /**
     * Create an affine transformation for a rotation around the origin.
     * {@code (x,y) -> (x cos(alpha) - y sin(alpha), x sin(alpha) + y cos(alpha))}.
     * @param alpha the angle in radians
     * @return affine transformation (rotation)
     */
    public static AffineTransformation rotate(double alpha) {
        double sin = Math.sin(alpha);
        double cos = Math.cos(alpha);
        return new AffineTransformation(cos, sin, -sin, cos, 0, 0);
    }

    /**
     * Create an affine transformation for a translation
     * {@code (x,y) -> (x+u,y+v)}.
     * @param u the x-value
     * @param v the y-value
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(double u, double v) {
        return new AffineTransformation(1, 0, 0, 1, u, v);
    }
    
    /**
     * Create an affine transformation for a translation.
     * See {@link #translate(double, double)}.
     * @param v the translation vector
     * @return affine transformation (translation)
     */
    public static AffineTransformation translate(Vec2d v) {
        return new AffineTransformation(1, 0, 0, 1, v.x(), v.y());
    }

    /**
     * Create an affine transformation for a scaling operation (using same factor for x and y coordinates)
     * {@code (x,y) -> (sx,sy)}.
     * @param s the scaling factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double s) {
        return new AffineTransformation(s, 0, 0, s, 0, 0);
    }

    /**
     * Create an affine transformation for a scaling operation
     * {@code (x,y) -> (sx,ty)}.
     * @param s the scaling factor for the x-coordinate
     * @param t the scaling factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation scale(double s, double t) {
        return new AffineTransformation(s, 0, 0, t, 0, 0);
    }

    /**
     * Create an affine transformation for a shearing operation 
     * {@code (x,y) -> (x+my,y)}.
     * @param m the shearing factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation shear(double m) {
        return new AffineTransformation(1, 0, m, 1, 0, 0);
    }

    /**
     * Combine affine transformations.
     * @param A the affine transformation to append
     * @return affine transformation (combination of this and A)
     */
    public AffineTransformation append(AffineTransformation A) {
        return new AffineTransformation(
                A.a*a+A.c*b,     A.b*a+A.d*b,
                A.a*c+A.c*d,     A.b*c+A.d*d,
                A.a*e+A.c*f+A.e, A.b*e+A.d*f+A.f);  
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

    public double a() { return a; }
    public double b() { return b; }
    public double c() { return c; }
    public double d() { return d; }
    public double e() { return e; }
    public double f() { return f; }
}
