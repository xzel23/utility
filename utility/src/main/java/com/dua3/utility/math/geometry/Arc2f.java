package com.dua3.utility.math.geometry;

import java.util.Locale;

/**
 * Represents a 2-dimensional arc curve.
 */
public final class Arc2f extends AbstractCurve2f {
    public static final String NAME = "ARC";

    private final float rx;
    private final float ry;
    private final float angle;
    private final boolean largeArc;
    private final boolean sweep;

    /**
     * Represents a 2-dimensional arc curve.
     *
     * @param path     the path this curve belongs to
     * @param b0       the index of the starting vertex
     * @param b1       the index of the ending vertex
     * @param rx       the x-radius of the arc
     * @param ry       the y-radius of the arc
     * @param angle    the angle of the arc in radians
     * @param largeArc true if the arc is greater than 180 degrees, false otherwise
     * @param sweep    true if the arc sweeps in a positive angle direction, false otherwise
     */
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

    /**
     * Returns the x-radius of the arc curve.
     *
     * @return the x-radius of the arc curve
     */
    public float rx() {
        return rx;
    }

    /**
     * Returns the y-radius of the arc curve.
     *
     * @return the y-radius of the arc curve
     */
    public float ry() {
        return ry;
    }

    /**
     * Returns the angle of the arc curve in radians.
     *
     * @return the angle of the arc curve in radians
     */
    public float angle() {
        return angle;
    }

    /**
     * Returns the value of the largeArc property for the Arc2f object.
     *
     * @return true if the arc is greater than 180 degrees, false otherwise
     */
    public boolean largeArc() {
        return largeArc;
    }

    /**
     * Returns the value of the sweep property for the Arc2f object.
     *
     * @return true if the arc sweeps in a positive angle direction, false otherwise
     */
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
        sb.append(String.format(Locale.ROOT, ", [r](%f,%f)", rx, ry));
        sb.append(String.format(Locale.ROOT, ", %frad", angle));
        sb.append(String.format(Locale.ROOT, ", largeArc(%d)", largeArc ? 1 : 0));
        sb.append(String.format(Locale.ROOT, ", sweep(%d)", sweep ? 1 : 0));
        sb.append("}");

        return sb.toString();
    }
}
