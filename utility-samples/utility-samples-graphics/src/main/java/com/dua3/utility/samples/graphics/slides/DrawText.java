package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.samples.graphics.FxGraphicsSample;
import com.dua3.utility.ui.Graphics;

import static com.dua3.utility.math.MathUtil.PI_QUARTER;

/**
 * The DrawText class implements the Slide interface and demonstrates the use of the drawText method
 * within a graphical context. It renders text with various horizontal and vertical alignment options
 * (HAnchor and VAnchor) using the com.dua3.utility.ui.Graphics API.
 */
public class DrawText implements FxGraphicsSample.Slide {
    @Override
    public String title() {
        return "drawText()";
    }

    @Override
    public void drawContent(Graphics g) {
        float w = g.getWidth();
        float margin = 10;

        float x = margin;
        float y = margin;
        g.setFont(g.getDefaultFont().withBold(true));
        g.drawText("drawText(CharSequence, float, float, HAnchor, VAnchor):", x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);
        y += 40;

        // show VAnchor values
        showVAnchors(g, x, y, w / 2);

        // show HAnchor values
        showHAnchors(g, x + w / 2 + margin, y, w / 2);
    }

    /**
     * Draws text using the different {@link com.dua3.utility.ui.Graphics.VAnchor} values.
     *
     * @param g the graphics context used for drawing
     * @param x the x-coordinate of the starting point for rendering
     * @param y the y-coordinate of the starting point for rendering
     * @param w the width used of the area to draw in
     */
    private void showVAnchors(Graphics g, float x, float y, float w) {
        AffineTransformation2f t0 = g.getTransformation();
        g.setFont(g.getDefaultFont().withBold(true));
        g.drawText("VAnchor:", x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);

        g.setFont(g.getDefaultFont());
        float r = 0.95f * w / 2;
        float cx = x + r;
        float cy = y + r;
        for (int i = 0; i < 8; i++) {
            AffineTransformation2f t = AffineTransformation2f.combine(
                    AffineTransformation2f.translate(-cx, -cy),
                    AffineTransformation2f.rotate(i * PI_QUARTER),
                    AffineTransformation2f.translate(cx, cy),
                    t0
            );

            g.setTransformation(t);

            g.setStroke(Color.RED, 1);
            g.strokeLine(cx, cy, cx + r, cy);

            float s = r / (Graphics.VAnchor.values().length + 1);
            float tx = cx + s;
            float ty = cy;
            for (Graphics.VAnchor v : Graphics.VAnchor.values()) {
                g.drawText(v.name(), tx, ty, Graphics.HAnchor.LEFT, v);
                tx += s;
            }
        }
        g.setTransformation(t0);
    }

    /**
     * Draws text using the different {@link com.dua3.utility.ui.Graphics.HAnchor} values.
     *
     * @param g the graphics context used for drawing
     * @param x the x-coordinate of the starting point for rendering
     * @param y the y-coordinate of the starting point for rendering
     * @param w the width used of the area to draw in
     */
    private void showHAnchors(Graphics g, float x, float y, float w) {
        AffineTransformation2f t0 = g.getTransformation();
        g.setFont(g.getDefaultFont().withBold(true));
        g.drawText("HAnchor:", x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);

        g.setFont(g.getDefaultFont());
        float r = 0.95f * w / 2;
        float cx = x + r;
        float cy = y + r;
        for (int i = 0; i < 8; i++) {
            AffineTransformation2f t = AffineTransformation2f.combine(
                    AffineTransformation2f.translate(-cx, -cy),
                    AffineTransformation2f.rotate(i * PI_QUARTER),
                    AffineTransformation2f.translate(cx, cy),
                    t0
            );

            g.setTransformation(t);

            g.setStroke(Color.RED, 1);
            g.strokeLine(cx, cy, cx, cy + r);

            float s = r / (Graphics.HAnchor.values().length + 1);
            float tx = cx;
            float ty = cy + s;
            for (Graphics.HAnchor h : Graphics.HAnchor.values()) {
                g.drawText(h.name(), tx, ty, h, Graphics.VAnchor.BOTTOM);
                ty += s;
            }
        }
        g.setTransformation(t0);
    }
}
