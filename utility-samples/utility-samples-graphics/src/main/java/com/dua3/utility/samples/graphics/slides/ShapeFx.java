package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.samples.graphics.Slide;
import com.dua3.utility.ui.Graphics;

/**
 * The ShapeFx class extends the JavaFX Application class to create and display a window
 * with two different paths drawn on a pane. The paths are created using different methods,
 * demonstrating the use of both JavaFX Path elements and a custom Path2f class with segmented paths.
 */
public class ShapeFx implements Slide {

    @Override
    public String title() {
        return "Paths";
    }

    @Override
    public void draw(Graphics g) {
        float r = (float) (0.95 * Math.min(g.getWidth(), g.getHeight()) / 2);
        Vector2f c = g.getBounds().center();
        Vector2f start = c.add(Vector2f.of(r, 0));
        Vector2f end = c.add(Vector2f.of(0, -r));
        Vector2f p = c.add(Vector2f.of((float) (r*Math.sin(Math.PI/4)), (float) -(r*Math.cos(Math.PI/4))));

        drawPoint(g, c, Color.RED);
        drawPoint(g, start, Color.RED);
        drawPoint(g, end, Color.GREEN);
        drawPoint(g, p, Color.GRAY);

        g.setStroke(Color.BLUE, 3);
        g.strokePath(createCircle(c, r));
    }

    private void drawPoint(Graphics g, Vector2f p, Color color) {
        g.setFill(color);
        float size = 10;
        g.fillRect(p.x() - size / 2, p.y() - size / 2, size, size);
    }

    private static Path2f createCircle(Vector2f c, float r) {
        return Path2f.builder()
                .moveTo(Vector2f.of(c.x() + r, c.y()))
                .arcTo(Vector2f.of(c.x(), c.y() - r), Vector2f.of(r, r), 0, false, false)
                .build();
    }

    private static Path2f createPath2() {
        return Path2f.builder()
                .moveTo(Vector2f.of(0.0f, 0.0f))
                .lineTo(Vector2f.of(70.0f, 0.0f))
                .curveTo(Vector2f.of(100.0f, 0.0f), Vector2f.of(120.0f, 60.0f))
                .lineTo(Vector2f.of(175.0f, 55.0f))
                .arcTo(Vector2f.of(50.0f, 50.0f), Vector2f.of(50.0f, 50.0f), 0.0f, false, false)
                .build();
    }
}