package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PathBuilder2d {
    
    private Path2dImpl impl;

    private int current = -1;
    private int startOfSubPath = -1;

    public PathBuilder2d() {
        init();
    }

    private void init() {
        this.impl = new Path2dImpl();
        this.current = -1;
        this.startOfSubPath = -1;
    }
    
    private int addVertex(Vector2d v) {
        current =  impl.addVertex(v);
        return current;
    }

    public Vector2d vertex(int idx) {
        return impl.vertex(idx);
    }

    public Vector2d current() {
        assert current >= 0;
        return impl.vertex(current);
    }

    public void moveTo(Vector2d p) {
        startOfSubPath = addVertex(p);
        impl.addSegment(new MoveTo2d(impl, startOfSubPath));
    }

    public void lineTo(Vector2d v) {
        assert current>=0;
        int p = current;
        int q = addVertex(v);
        impl.addSegment(new Line2d(impl, p, q));
    }

    public void curveTo(Vector2d p1, Vector2d p2,Vector2d p3) {
        assert current>=0;
        int c0 = current;
        int c1 = addVertex(p1);
        int c2 = addVertex(p2);
        int c3 = addVertex(p3);
        impl.addSegment(new BezierCurve2d(impl, c0, c1, c2, c3));
    }

    public void closePath() {
        impl.addSegment(new ClosePath2d(impl, current, startOfSubPath));
    }
    
    public Path2d strokePath() {
        impl.addSegment(new StrokePath2d(impl, current));
        return finish();
    }
    
    public Path2d fillPath() {
        impl.addSegment(new FillPath2d(impl, current));
        return finish();
    }
    
    public Path2d fillAndStrokePath() {
        impl.addSegment(new FillAndStrokePath2d(impl, current));
        return finish();
    }
    
    private Path2d finish() {
        Path2d path = new Path2d(impl);
        init();
        return path;
    }
}

