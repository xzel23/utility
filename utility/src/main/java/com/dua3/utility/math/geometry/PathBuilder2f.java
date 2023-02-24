package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2f;

/**
 * A builder class for {@link Path2f} instances.
 */
public class PathBuilder2f {

    private Vector2f pos = Vector2f.ORIGIN;
    private Path2fImpl impl;
    private boolean open;

    /**
     * Constructor.
     */
    public PathBuilder2f() {
        init();
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
        pos = v;
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
     */
    public void moveTo(Vector2f v) {
        init();
        impl.addSegment(new MoveTo2f(impl, addVertex(v)));
        open = true;
    }

    /**
     * Add a line from the current position to a new position.
     *
     * @param v the new position
     */
    public void lineTo(Vector2f v) {
        if (!open) {
            moveTo(pos);
        }

        int p = currentIndex();
        int q = addVertex(v);
        impl.addSegment(new Line2f(impl, p, q));
    }

    /**
     * Add a Bézier curve from the current position to a new position.
     * <p>
     * The curve starts at the current position (p0) and ends at p3.
     *
     * @param p1 second control point
     * @param p2 third control point
     * @param p3 fourth control point
     */
    public void curveTo(Vector2f p1, Vector2f p2, Vector2f p3) {
        if (!open) {
            moveTo(pos);
        }

        int c0 = currentIndex();
        int c1 = addVertex(p1);
        int c2 = addVertex(p2);
        int c3 = addVertex(p3);
        impl.addSegment(new BezierCurve2f(impl, c0, c1, c2, c3));
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
     */
    public void closePath() {
        if (open) {
            impl.addSegment(new ClosePath2f(impl, currentIndex(), 0));
            pos = vertex(0);
            close();
        }
    }

    /**
     * End the current path without appending a new segment.
     * <p>
     * <strong>Notes</strong>
     * <ul>
     *     <li> this method does not affect the number of vertices in this path
     *     <li> the path is reset when new segments are added to it after calling this method
     * </ul>
     */
    public void endPath() {
        if (open) {
            impl.addSegment(new EndPath2f(impl, currentIndex()));
            close();
        }
    }

    /**
     * Stroke the current path.
     *
     * @return {@link Path2f} instance holding the constructed path
     */
    public Path2f strokePath() {
        impl.addSegment(new StrokePath2f(impl, currentIndex()));
        return new Path2f(impl);
    }

    /**
     * Fill the current path.
     *
     * @param fillRule the {@link FillRule} to use
     * @return {@link Path2f} instance holding the constructed path
     */
    public Path2f fillPath(FillRule fillRule) {
        impl.addSegment(new FillPath2f(impl, currentIndex(), fillRule));
        return new Path2f(impl);
    }

    /**
     * Fill and stroke the current path.
     *
     * @param fillRule the {@link FillRule} to use
     * @return {@link Path2f} instance holding the constructed path
     */
    public Path2f fillAndStrokePath(FillRule fillRule) {
        impl.addSegment(new FillAndStrokePath2f(impl, currentIndex(), fillRule));
        return new Path2f(impl);
    }

    /**
     * Set clip region to the current path.
     *
     * @param fillRule the {@link FillRule} to use
     * @return {@link Path2f} instance holding the constructed path
     */
    public Path2f clipPath(FillRule fillRule) {
        impl.addSegment(new ClipPath2f(impl, currentIndex(), fillRule));
        return new Path2f(impl);
    }

    /**
     * Mark the current path as complete. Adding new segments will start a new path.
     */
    private void close() {
        open = false;
    }
}
