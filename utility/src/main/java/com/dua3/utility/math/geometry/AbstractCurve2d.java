package com.dua3.utility.math.geometry;

import com.dua3.utility.math.Vector2d;

public abstract class AbstractCurve2d extends Segment2d {

    int[] controls;

    AbstractCurve2d(Path2dImpl path, int... controls) {
        super(path);
        this.controls = controls;
    }

    public int numberOfControls() {
        return controls.length;
    } 
    
    public Vector2d control(int idx) {
        return path.vertex(controls[idx]);
    }
    
    @Override
    public Vector2d start() {
        return control(0);
    }

    @Override
    public Vector2d end() {
        return control(3);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(getClass().getSimpleName()+"{");
        String sep = "";
        for (int i=0; i<controls.length; i++) {
            sb.append(sep);
            sb.append(vertexToString(controls[i]));
            sb.append("\n");
            sep = ", ";
        }
        sb.append("}");
        
        return sb.toString();
    }
}
