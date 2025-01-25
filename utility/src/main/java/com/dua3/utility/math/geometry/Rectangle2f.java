/*
 * Copyright (c) 2021. Axel Howind (axel@dua3.com)
 * This package is distributed under the Artistic License 2.0.
 */

package com.dua3.utility.math.geometry;

/**
 * Immutable rectangle with float coordinates.
 */
public record Rectangle2f(float x, float y, float width, float height) {

    /**
     * Constructor.
     *
     * @param x      the x-coordinate
     * @param y      the y-coordinate
     * @param width  the width (>= 0)
     * @param height the height (>= 0)
     */
    public Rectangle2f {
        assert width >= 0 : "width is negative: " + width;
        assert height >= 0 : "height is negative: " + height;
    }

    /**
     * Create rectangle from two vectors.
     *
     * @param p the first corner
     * @param q the second corner
     * @return rectangle
     */
    public static Rectangle2f withCorners(Vector2f p, Vector2f q) {
        float x, y, w, h;
        if (p.x() <= q.x()) {
            x = p.x();
            w = q.x() - p.x();
        } else {
            x = q.x();
            w = p.x() - q.x();
        }
        if (p.y() <= q.y()) {
            y = p.y();
            h = q.y() - p.y();
        } else {
            y = q.y();
            h = p.y() - q.y();
        }
        return new Rectangle2f(x, y, w, h);
    }

    /**
     * Creates a new rectangle with the specified center point and dimensions.
     *
     * @param p   the center point of the rectangle
     * @param dim the dimensions of the rectangle
     * @return a new {@code Rectangle2f} object
     */
    public static Rectangle2f withCenter(Vector2f p, Dimension2f dim) {
        return new Rectangle2f(p.x() - dim.width() / 2, p.y() - dim.height() / 2, dim.width(), dim.height());
    }

    /**
     * Create rectangle from coordinates and dimension. Both width and height must be non-negative.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the width
     * @param h the height
     * @return rectangle
     */
    public static Rectangle2f of(float x, float y, float w, float h) {
        return new Rectangle2f(x, y, w, h);
    }

    /**
     * Create rectangle from corner and dimension.
     *
     * @param p the vector to the corner
     * @param d the rectangle dimension
     * @return rectangle
     */
    public static Rectangle2f of(Vector2f p, Dimension2f d) {
        return of(p.x(), p.y(), d.width(), d.height());
    }

    /**
     * The minimum of the two x-coordinates.
     *
     * @return minimum x-coordinate
     */
    public float xMin() {
        return x;
    }

    /**
     * The minimum of the two y-coordinates.
     *
     * @return minimum y-coordinate
     */
    public float yMin() {
        return y;
    }

    /**
     * The maximum of the two x-coordinates.
     *
     * @return maximum x-coordinate
     */
    public float xMax() {
        return x + width;
    }

    /**
     * The maximum of the two y-coordinates.
     *
     * @return maximum y-coordinate
     */
    public float yMax() {
        return y + height;
    }

    /**
     * The center of the two x-coordinates.
     *
     * @return maximum x-coordinate
     */
    public float xCenter() {
        return x + width / 2;
    }

    /**
     * The center of the two y-coordinates.
     *
     * @return maximum y-coordinate
     */
    public float yCenter() {
        return y + height / 2;
    }

    /**
     * Retrieves the dimension of the rectangle.
     *
     * @return the dimension of the rectangle
     */
    public Dimension2f getDimension() {
        return new Dimension2f(width, height);
    }

    /**
     * Returns a new {@link Vector2f} instance with the minimum x and y coordinates.
     *
     * @return the {@link Vector2f} instance with the minimum coordinates
     */
    public Vector2f min() {
        return new Vector2f(xMin(), yMin());
    }

    /**
     * Returns a new {@link Vector2f} instance with the maximum x and y coordinates.
     *
     * @return a new {@link Vector2f} with the maximum x-coordinate and the maximum y-coordinate
     */
    public Vector2f max() {
        return new Vector2f(xMax(), yMax());
    }

    /**
     * Returns the center of the rectangle as a new {@link Vector2f} object.
     *
     * @return the center of the rectangle
     */
    public Vector2f center() {
        return new Vector2f(xCenter(), yCenter());
    }

    /**
     * Translates the rectangle by the specified amounts in the x and y directions.
     *
     * @param dx the amount to translate along the x-direction
     * @param dy the amount to translate along the y-direction
     * @return a rectangle that is translated by dx and dy
     */
    public Rectangle2f translate(float dx, float dy) {
        if (dx == 0 && dy == 0) {
            return this;
        }

        return new Rectangle2f(x + dx, y + dy, width, height);
    }

    /**
     * Translates the rectangle by the specified amounts in the x and y directions.
     *
     * @param t the translation vector
     * @return a rectangle that is translated by t
     */
    public Rectangle2f translate(Vector2f t) {
        return translate(t.x(), t.y());
    }

    /**
     * Adds a margin to the current rectangle, i.e., create a rectangle with the same center as this rectangle
     * but with borders pushed out by the amount given by {@code mx} and {@code my}. Use negative values to shrink the
     * rectangle.
     *
     * @param mx the amount to the margin to apply to the x-coordinates
     * @param my the amount to the margin to apply to the y-coordinates
     * @return a new {@code Rectangle2f} instance with the added margin
     */
    public Rectangle2f addMargin(float mx, float my) {
        if (mx == 0 && my == 0) {
            return this;
        }

        return new Rectangle2f(x - mx, y - my, width + 2 * mx, height + 2 * my);
    }

    /**
     * Adds a margin to the current rectangle, i.e., create a rectangle with the same center as this rectangle
     * but with borders pushed out by the amount given by {@code m}. Use negative values to shrink the rectangle.
     *
     * @param m the amount to subtract the margin to add
     * @return a new {@code Rectangle2f} instance with the added margin
     */
    public Rectangle2f addMargin(float m) {
        if (m == 0) {
            return this;
        }

        return new Rectangle2f(x - m, y - m, width + 2 * m, height + 2 * m);
    }

    /**
     * Adds a margin to the current rectangle, creating a new rectangle with the same center as this rectangle but with borders pushed out
     * by the specified margins. Use negative values to shrink the rectangle.
     *
     * @param mLeft   the margin to be applied to the left side of the rectangle
     * @param mTop    the margin to be applied to the top side of the rectangle
     * @param mRight  the margin to be applied to the right side of the rectangle
     * @param mBottom the margin to be applied to the bottom side of the rectangle
     * @return a new {@code Rectangle2f} instance with the added margin
     */
    public Rectangle2f addMargin(float mLeft, float mTop, float mRight, float mBottom) {
        return new Rectangle2f(x - mLeft, y - mTop, width + mLeft + mRight, height + mTop + mBottom);
    }

    /**
     * Determines if the specified point is contained within the rectangle.
     *
     * @param p the point to check, represented as a {@code Vector2f}
     * @return {@code true} if the rectangle contains the point, otherwise {@code false}
     */
    public boolean contains(Vector2f p) {
        return x <= p.x() && p.x() <= x + width && y <= p.y() && p.y() <= y + height;
    }

    /**
     * Determines whether this rectangle intersects with the specified rectangle.
     *
     * @param r the rectangle to check for intersection
     * @return {@code true} if this rectangle intersects with the specified rectangle, otherwise {@code false}
     */
    public boolean intersects(Rectangle2f r) {
        return r.xMax() > xMin() && r.yMax() > yMin() && r.xMin() < xMax() && r.yMin() < yMax();
    }

    /**
     * Creates a new rectangle by moving the top-left corner to the specified
     * coordinates while preserving the current width and height.
     *
     * @param x the new x-coordinate for the top-left corner of the rectangle
     * @param y the new y-coordinate for the top-left corner of the rectangle
     * @return a new {@code Rectangle2f} instance with the updated position
     */
    public Rectangle2f moveTo(float x, float y) {
        return new Rectangle2f(x, y, width, height);
    }
}
