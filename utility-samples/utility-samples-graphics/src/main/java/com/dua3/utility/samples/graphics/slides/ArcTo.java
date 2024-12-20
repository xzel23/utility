package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
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
        int angles = 8;
        for (int j=0; j<=angles; j++) {
            float w = g.getBounds().width() / (angles + 1);
            float rMax = w/2;
            float beta = (float) (j * 2 * Math.PI / angles);
            Vector2f c = Vector2f.of(w*(j+0.5f), w);
            int segments = 16;
            for (int i = 0; i <= segments; i++) {
                float r = rMax / 2 + rMax / 2 * i / segments;
                float rx = 0.95f * r;
                float ry = 0.5f * r;
                float phi = (float) (2 * Math.PI * i / segments);

                AffineTransformation2f t = AffineTransformation2f.rotate(beta, c);
                Vector2f start = t.transform(c.add(Vector2f.of(rx, 0)));
                Vector2f end = t.transform(c.add(Vector2f.of((float) (rx * Math.cos(phi)), (float) (ry * Math.sin(phi)))));

                // draw the input points
                drawPoint(g, c, Color.RED, 3);
                drawPoint(g, start, Color.BLUE, 3);
                drawPoint(g, end, Color.GREEN, 3);

                Path2f path = Path2f.builder()
                        .moveTo(start)
                        .arcTo(end, Vector2f.of(rx, ry), beta, i >= segments / 2, i != segments)
                        .build();
                g.setStroke(Color.BLUE, 1);
                g.strokePath(path);
            }
        }
    }

    private void drawPoint(Graphics g, Vector2f p, Color color, float size) {
        g.setFill(color);
        g.fillRect(p.x() - size / 2, p.y() - size / 2, size, size);
    }

}