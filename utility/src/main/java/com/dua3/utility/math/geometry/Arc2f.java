package com.dua3.utility.math.geometry;

/**
 * Arc Path segment.
 */
public final class Arc2f extends AbstractCurve2f {
    /**
     * Name of this segment type.
     */
    public static final String NAME = "ARC";

    private float rx;
    private float ry;
    private float angle;
    private boolean largeArc;
    private boolean sweep;

    Arc2f(Path2fImpl path, int b0, int b1, float rx, float ry, float angle, boolean largeArc, boolean sweep) {
        super(path, b0, b1);
        this.rx = rx;
        this.ry = ry;
        this.angle = angle;
        this.largeArc = largeArc;
        this.sweep = sweep;
    }

    @Override
    public String name() {
        return NAME;
    }

    public float rx() {
        return rx;
    }

    public float ry() {
        return ry;
    }

    public float angle() {
        return angle;
    }

    public boolean largeArc() {
        return largeArc;
    }

    public boolean sweep() {
        return sweep;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(16 + 16 * numberOfControls());

        sb.append(getClass().getSimpleName()).append("{");
        String sep = "";
        for (int control : controls) {
            sb.append(sep);
            sb.append(vertexToString(control));
            sb.append("\n");
            sep = ", ";
        }
        sb.append(String.format(", [r](%f,%f)", rx, ry));
        sb.append(String.format(", %frad", angle));
        sb.append(String.format(", largeArc(%d)", largeArc ? 1 : 0));
        sb.append(String.format(", sweep(%d)", sweep ? 1 : 0));
        sb.append("}");

        return sb.toString();
    }
}
