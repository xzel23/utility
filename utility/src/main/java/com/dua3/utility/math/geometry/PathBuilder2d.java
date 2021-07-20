package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public class PathBuilder2d {
    
    private Vector2d pos = Vector2d.ORIGIN;
    private Path2dImpl impl;
    private boolean open = false;

    public PathBuilder2d() {
        init();
    }

    private void init() {
        this.impl = new Path2dImpl();
        this.open = false;
    }
    
    private int addVertex(Vector2d v) {
        pos = v;
        impl.addVertex(v);
        return currentIndex();
    }

    public Vector2d vertex(int idx) {
        return impl.vertex(idx);
    }

    private int currentIndex() {
        return impl.vertexCount()-1;
    }

    public Vector2d current() {
        return impl.vertex(currentIndex());
    }

    public void moveTo(Vector2d p) {
        init();
        impl.addSegment(new MoveTo2d(impl, addVertex(p)));
        open = true;
    }

    public void lineTo(Vector2d v) {
        if (!open) {
            moveTo(pos);
        }
        
        int p = currentIndex();
        int q = addVertex(v);
        impl.addSegment(new Line2d(impl, p, q));
    }

    public void curveTo(Vector2d p1, Vector2d p2,Vector2d p3) {
        if (!open) {
            moveTo(pos);
        }

        int c0 = currentIndex();
        int c1 = addVertex(p1);
        int c2 = addVertex(p2);
        int c3 = addVertex(p3);
        impl.addSegment(new BezierCurve2d(impl, c0, c1, c2, c3));
    }

    public void closePath() {
        if (open) {
            impl.addSegment(new ClosePath2d(impl, currentIndex(), 0));
            pos = vertex(0);
            close();
        }
    }
    
    public void endPath() {
        if (open) {
            impl.addSegment(new EndPath2d(impl, currentIndex()));
            close();
        }
    }
    
    public Path2d strokePath() {
        impl.addSegment(new StrokePath2d(impl, currentIndex()));
        return new Path2d(impl);
    }
    
    public Path2d fillPath(FillRule fillRule) {
        impl.addSegment(new FillPath2d(impl, currentIndex(), fillRule));
        return new Path2d(impl);
    }
    
    public Path2d fillAndStrokePath(FillRule fillRule) {
        impl.addSegment(new FillAndStrokePath2d(impl, currentIndex(), fillRule));
        return new Path2d(impl);
    }

    public Path2d clipPath(FillRule fillRule) {
        impl.addSegment(new ClipPath2d(impl, currentIndex(), fillRule));
        return new Path2d(impl);
    }

    private void close() {
        open = false;
    }
}

