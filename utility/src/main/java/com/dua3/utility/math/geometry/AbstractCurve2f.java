package com.dua3.utility.math.geometry;

/**
 * Abstract Base class for 2-dimensional curves.
 */
public abstract class AbstractCurve2f extends Segment2f {

    /**
     * the control points
     */
    final int[] controls;

    /**
     * Constructor.
     * <p>
     * <strong>NOTE:</strong> The array of control points is used as is. It is the caller's
     * responsibility that the array is not modified after construction of the curve.
     *
     * @param path     the path this curve belongs to
     * @param controls the control points
     */
    AbstractCurve2f(Path2fImpl path, int... controls) {
        super(path);
        this.controls = controls;
    }

    /**
     * Get the number of control points.
     *
     * @return number of control points
     */
    public int numberOfControls() {
        return controls.length;
    }

    /**
     * Get control point by index
     *
     * @param idx index of control point
     * @return the control point
     */
    public Vector2f control(int idx) {
        return path.vertex(controls[idx]);
    }

    @Override
    public Vector2f start() {
        return control(0);
    }

    @Override
    public Vector2f end() {
        return control(controls.length - 1);
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
        sb.append("}");

        return sb.toString();
    }
}
