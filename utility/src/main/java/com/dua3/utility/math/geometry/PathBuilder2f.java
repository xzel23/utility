package com.dua3.utility.math.geometry;

import com.dua3.utility.lang.LangUtil;

/**
 * A builder class for {@link Path2f} instances.
 */
public class PathBuilder2f {

    private static final String NO_CURRENT_PATH = "no current path";
    private Path2fImpl impl = new Path2fImpl();
    private boolean open = false;

    /**
     * Constructor.
     */
    PathBuilder2f() {
        // nothing to do
    }

    /**
     * Initialize this instance before creating a new path.
     */
    private void init() {
        this.impl = new Path2fImpl();
        this.open = false;
    }

    /**
     * Add a new vertex.
     */
    private int addVertex(Vector2f v) {
        impl.addVertex(v);
        return currentIndex();
    }

    /**
     * Get vertex by index.
     *
     * @param idx index
     * @return the vertex at the given index
     */
    public Vector2f vertex(int idx) {
        return impl.vertex(idx);
    }

    /**
     * Get current index.
     *
     * @return the current index, that is the index of the last added vertex
     */
    private int currentIndex() {
        return impl.vertexCount() - 1;
    }

    /**
     * Get the current (last added) vertex.
     *
     * @return the current vertex
     */
    public Vector2f current() {
        return impl.vertex(currentIndex());
    }

    /**
     * Move to a new position.
     * <br>
     * <strong>NOTE:</strong> This implicitly starts a new path.
     *
     * @param v the vertex that marks the start of the new path
     * @return this PathBuilder2f instance
     */
    public PathBuilder2f moveTo(Vector2f v) {
        if (open) {
            close();
        }

        impl.addSegment(new MoveTo2f(impl, addVertex(v)));
        open = true;
        return this;
    }

    /**
     * Move relative.
     * <br>
     * <strong>NOTE:</strong> This implicitly starts a new path.
     *
     * @param v the offset vertex to the start of the new path
     * @return this PathBuilder2f instance
     */
    public PathBuilder2f moveRel(Vector2f v) {
        return moveTo(current().add(v));
    }

    /**
     * Add a line from the current position to a new position.
     *
     * @param v the new position
     * @return this instance
     */
    public PathBuilder2f lineTo(Vector2f v) {
        LangUtil.check(open, NO_CURRENT_PATH);

        int p = currentIndex();
        int q = addVertex(v);
        impl.addSegment(new Line2f(impl, p, q));
        return this;
    }

    /**
     * Adds a line segment to the path, starting from the current position
     * and extending to the position defined by the relative offset vector.
     *
     * @param v the relative offset vector from the current position
     * @return this PathBuilder2f instance
     */
    public PathBuilder2f lineRel(Vector2f v) {
        return lineTo(current().add(v));
    }

    /**
     * Adds an arc segment to the path, defined by the endpoint, radii, angle, and arc flags.
     *
     * @param ep        the endpoint of the arc segment
     * @param r         the radii of the arc segment
     * @param angle     the angle of the arc segment in degrees
     * @param largeArc  if true, the arc should be greater than or equal to 180 degrees, otherwise less than 180 degrees
     * @param sweep     if true, the arc should be drawn in a "clockwise" direction, otherwise in a "counterclockwise" direction
     * @return this PathBuilder2f instance
     */
    public PathBuilder2f arcTo(Vector2f ep, Vector2f r, float angle, boolean largeArc, boolean sweep) {
        LangUtil.check(open, NO_CURRENT_PATH);

        int c0 = currentIndex();
        int c1 = addVertex(ep);
        impl.addSegment(new Arc2f(impl, c0, c1, r.x(), r.y(), angle, largeArc, sweep));
        return this;
    }

    /**
     * Adds an arc segment to the path defined by a relative endpoint, radii, angle,
     * and arc flags from the current position of the path.
     *
     * @param ep        the relative endpoint of the arc segment from the current position
     * @param r         the radii of the arc segment on both x and y axes
     * @param angle     the rotation angle of the arc segment in degrees
     * @param largeArc  if true, the arc should be greater than or equal to 180 degrees,
     *                  otherwise, it should be less than 180 degrees
     * @param sweep     if true, the arc should be drawn in a "clockwise" direction,
     *                  otherwise, it should be drawn in a "counterclockwise" direction
     * @return this PathBuilder2f instance
     */
    public PathBuilder2f arcRel(Vector2f ep, Vector2f r, float angle, boolean largeArc, boolean sweep) {
        return arcTo(current().add(ep), r, angle, largeArc, sweep);
    }

    /**
     * Add a quadratic Bézier curve from the current position to a new position.
     * <p>
     * The curve starts at the current position and ends at ep-
     *
     * @param cp the control point
     * @param ep the end point
     * @return this instance
     */
    public PathBuilder2f curveTo(Vector2f cp, Vector2f ep) {
        LangUtil.check(open, NO_CURRENT_PATH);

        int c0 = currentIndex();
        int c1 = addVertex(cp);
        int c2 = addVertex(ep);
        impl.addSegment(new Curve2f(impl, c0, c1, c2));
        return this;
    }

    /**
     * Add a cubic Bézier curve from the current position to a new position.
     * <p>
     * The curve starts at the current position (p0) and ends at p3.
     *
     * @param cp1 first control point
     * @param cp2 second control point
     * @param ep end control point
     * @return this instance
     */
    public PathBuilder2f curveTo(Vector2f cp1, Vector2f cp2, Vector2f ep) {
        LangUtil.check(open, NO_CURRENT_PATH);

        int c0 = currentIndex();
        int c1 = addVertex(cp1);
        int c2 = addVertex(cp2);
        int c3 = addVertex(ep);
        impl.addSegment(new Curve2f(impl, c0, c1, c2, c3));
        return this;
    }

    /**
     * Close the current path by appending a line from the current position to the path's starting position.
     * <p>
     * <strong>Notes</strong>
     * <ul>
     *     <li> this method does not affect the number of vertices in this path
     *     <li> the current position is set to the start of the path
     *     <li> the path is reset when new segments are added to it after calling this method
     * </ul>
     *
     * @return this instance
     */
    public PathBuilder2f closePath() {
        LangUtil.check(open, NO_CURRENT_PATH);

        impl.addSegment(new ClosePath2f(impl, currentIndex(), 0));
        close();

        return this;
    }

    /**
     * Mark the current path as complete. Adding new segments will start a new path.
     */
    private void close() {
        assert open;
        open = false;
    }

    /**
     * Builds a {@link Path2f} instance using the current state of the {@code PathBuilder2f}.
     *
     * @return a new instance of {@link Path2f} representing the constructed path
     */
    public Path2f build() {
        if (open) {
            close();
        }
        Path2f path2f = new Path2f(impl);
        init();
        return path2f;
    }

    /**
     * Checks if the path is empty, meaning it contains no vertices.
     *
     * @return true if the path is empty, false otherwise
     */
    public boolean isEmpty() {
        return impl.vertices().isEmpty();
    }
}
