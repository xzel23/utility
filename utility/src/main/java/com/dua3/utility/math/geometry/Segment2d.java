package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public abstract class Segment2d {
    protected final Path2dImpl path;
    
    protected Segment2d(Path2dImpl path) {
        assert path != null;
        this.path = path;
    }
    
    public abstract String name();
    
    public abstract Vector2d start();
    public abstract Vector2d end();
}
