package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

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
public record AffineTransformation2f(float a, float b, float c, float d, float e, float f) {

    /**
     * The identity transformation.
     */
    public static final AffineTransformation2f IDENTITY = new AffineTransformation2f(1, 0, 0, 0, 1, 0);
    
    /**
     * Return the identity transformation.
     * @return affine transformation (identity)
     */
    @SuppressWarnings("SameReturnValue")
    public static AffineTransformation2f identity() {
        return IDENTITY;
    }

    /**
     * Create an affine transformation for a rotation around the origin.
     * {@code (x,y) -> (x cos(alpha) - y sin(alpha), x sin(alpha) + y cos(alpha))}.
     * @param alpha the angle in radians
     * @return affine transformation (rotation)
     */
    @SuppressWarnings("NumericCastThatLosesPrecision")
    public static AffineTransformation2f rotate(double alpha) {
        float sin = (float) Math.sin(alpha);
        float cos = (float) Math.cos(alpha);
        return new AffineTransformation2f(cos, -sin, 0, sin, cos, 0);
    }

    /**
     * Create an affine transformation for a translation
     * {@code (x,y) -> (x+u,y+v)}.
     * @param tx the x-value
     * @param ty the y-value
     * @return affine transformation (translation)
     */
    public static AffineTransformation2f translate(float tx, float ty) {
        return new AffineTransformation2f(1, 0, tx, 0, 1, ty);
    }
    
    /**
     * Create an affine transformation for a translation.
     * See {@link #translate(float, float)}.
     * @param v the translation vector
     * @return affine transformation (translation)
     */
    public static AffineTransformation2f translate(Vector2f v) {
        return translate(v.x(), v.y());
    }

    /**
     * Create an affine transformation for a scaling operation (using same factor for x and y coordinates)
     * {@code (x,y) -> (sx,sy)}.
     * @param s the scaling factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation2f scale(float s) {
        return scale(s,s);
    }

    /**
     * Create an affine transformation for a scaling operation
     * {@code (x,y) -> (sx,ty)}.
     * @param sx the scaling factor for the x-coordinate
     * @param sy the scaling factor for the y-coordinate
     * @return affine transformation (scale)
     */
    public static AffineTransformation2f scale(float sx, float sy) {
        return new AffineTransformation2f(sx, 0, 0, 0, sy, 0);
    }

    /**
     * Create an affine transformation for a shearing operation 
     * {@code (x,y) -> (x+my,y)}.
     * @param m the shearing factor
     * @return affine transformation (scale)
     */
    public static AffineTransformation2f shear(float m) {
        return new AffineTransformation2f(1, m, 0, 0, 1, 0);
    }

    /**
     * Combine affine transformations.
     * @param A the affine transformation to append
     * @return affine transformation (combination of this and A)
     */
    public AffineTransformation2f append(AffineTransformation2f A) {
        return new AffineTransformation2f(
                A.a*a+A.b*d, A.a*b+A.b*e, A.a*c+A.b*f+A.c,
                A.d*a+A.e*d, A.d*b+A.e*e, A.d*c+A.e*f+A.f
        );  
    }

    /**
     * Transform vector by applying this affine transformation to it.
     * @param v the vector to transform
     * @return the result of transformation
     */
    public Vector2f transform(Vector2f v) {
        return transform(v.x(), v.y());
    }
    
    /**
     * Transform vector by applying this affine transformation to it.
     * @param x the x-coordinate of the point to transform
     * @param y the y-coordinate of the point to transform
     * @return the result of transformation
     */
    public Vector2f transform(float x, float y) {
        float xt = a*x + b*y + c;
        float yt = d*x + e*y + f;
        return Vector2f.of(xt,yt);
    }
    
    /** 
     * Get scaling factor for x-axis. 
     * @return the x-axis scaling factor
     */
    public float getScaleX() { return a; }

    /**
     * Get shearing factor for x-axis. 
     * @return the x-axis shearing factor
     */
    public float getShearX() { return b; }

    /**
     * Get translation for x-axis. 
     * @return the x-axis translation value
     */
    public float getTranslateX() { return c; }

    /**
     * Get shearing factor for y-axis. 
     * @return the y-axis shearing factor
     */
    public float getShearY() { return d; }

    /**
     * Get scaling factor for y-axis. 
     * @return the y-axis scaling factor
     */
    public float getScaleY() { return e; }

    /**
     * Get translation for y-axis. 
     * @return the y-axis translation value
     */
    public float getTranslateY() { return f; }

    /** 
     * Get translation vector. 
     * @return the translation vector
     */
    public Vector2f getTranslate() { return Vector2f.of(e,f); }

}
