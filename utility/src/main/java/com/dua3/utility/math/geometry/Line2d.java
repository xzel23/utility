package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

/**
 * Straight line segment.
 */
public final class Line2d extends Segment2d {

    /**
     * Segment type name.
     */
    public static final String NAME = "LINE";

    /**
     * Index of starting point. 
     */
    final int a;

    /**
     * Index of end point. 
     */
    final int b;

    /**
     * Constructor
     * @param path the path
     * @param a index of starting point
     * @param b index of end point
     */
    Line2d(Path2dImpl path, int a, int b) {
        super(path);
        this.a = a;
        this.b = b;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Vector2d start() {
        return path.vertex(a);
    }

    @Override
    public Vector2d end() {
        return path.vertex(b);
    }

    /**
     * Test if line is exactly horizontal.
     * @return true, if start and end points have the same y-coordinate
     */
    public boolean isHorizontal() {
        return start().y() == end().y();
    }

    /**
     * Test if line is exactly vertical.
     * @return true, if start and end points have the same x-coordinate
     */
    public boolean isVertical() {
        return start().x() == end().x();
    }

    /**
     * Get angle of inclination.
     * @return the inclination (angle between line and x-axis) 
     */
    public double inclination() {
        return Math.atan2(deltaY(), deltaX());
    }

    /**
     * Get difference in x-coordinates.
     * @return difference in x-coordinates
     */
    public float deltaX() {
        return end().x() - start().x();
    }

    /**
     * Get difference in y-coordinates.
     * @return difference in y-coordinates
     */
    public float deltaY() {
        return end().y() - start().y();
    }

    @Override
    public String toString() {
        return "LineTo2d{" +
               vertexToString(a) + "," +
               vertexToString(b) +
               '}';
    }
    
}
