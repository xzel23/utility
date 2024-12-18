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
public class ArcTo implements Slide {

    @Override
    public String title() {
        return "ArcTo";
    }

    @Override
    public void draw(Graphics g) {
        float rMax = (float) (0.95 * Math.min(g.getWidth(), g.getHeight()) / 2);

        Vector2f c = g.getBounds().center();

        int segments = 16;
        for (int i=0; i<=segments; i++) {
            float r = rMax / 2 + rMax / 2 * i/segments;
            float phi = (float) (2 * Math.PI * i / segments);

            Vector2f start = c.add(Vector2f.of(r, 0));
            Vector2f end = c.add(Vector2f.of((float) (r * Math.cos(phi)), (float) (r * Math.sin(phi))));

            // draw the input points
            drawPoint(g, c, Color.RED, 10);
            drawPoint(g, start, Color.BLUE, 10);
            drawPoint(g, end, Color.GREEN, 10);

            Path2f path = Path2f.builder()
                    .moveTo(start)
                    .arcTo(end, Vector2f.of(r, r), 0, i>=segments/2, i != segments)
                    .build();
            g.setStroke(Color.BLUE, 3);
            g.strokePath(path);
        }
    }

    private void drawPoint(Graphics g, Vector2f p, Color color, float size) {
        g.setFill(color);
        g.fillRect(p.x() - size / 2, p.y() - size / 2, size, size);
    }

}