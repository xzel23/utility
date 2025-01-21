package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.geometry.AffineTransformation2f;
import com.dua3.utility.math.geometry.Path2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.samples.graphics.Slide;
import com.dua3.utility.ui.Graphics;

import static com.dua3.utility.math.MathUtil.TWO_PI;

public class ArcToAndEllipse implements Slide {

    @Override
    public String title() {
        return "arcTo(), strokeEllipse(), fillEllipse()";
    }

    @Override
    public void draw(Graphics g) {
        int angles = 8;

        float w = g.getBounds().width() / (angles + 1);
        float h = g.getBounds().height() / 3;

        g.drawText("""
                        Create Path2f instances that demonstrate the use of arcTo().
                        
                        Red dot: center of ellipse
                        Blue dot: start of arc
                        Green dot: end of arc
                        """,
                10, 10, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);

        g.drawText("""
                        Use Graphics.fillEllipse() and Graphics.strokeEllipse() to draw ellipses with varying rotation angles
                        """,
                10, h + 10, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);

        g.drawText("""
                        Demonstrate the result of using different values for the sweep and largeArc parameters of arcTo().
                        
                        Red circles: the start and end point of the arcs
                        Black line: largeArc = false, sweep = true
                        Blue line: largeArc = true, sweep = true
                        Red line: largeArc = false, sweep = false
                        Green line: largeArc = true, sweep = false
                        """,
                10, 2 * h + 10, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);

        for (int j = 0; j <= angles; j++) {
            float rMax = w / 2;
            float beta = (float) (j * 2 * Math.PI / angles);

            // draw arc segments
            Vector2f c = Vector2f.of(w * (j + 0.5f), 0.5f * h);
            int segments = 16;
            for (int i = 0; i < segments; i++) {
                float r = rMax / 2 + rMax / 2 * i / segments;
                float rx = 0.95f * r;
                float ry = 0.5f * r;
                float phi = (float) (TWO_PI * i / segments);

                AffineTransformation2f t = AffineTransformation2f.rotate(beta, c);
                Vector2f start = t.transform(c.add(Vector2f.of(rx, 0)));
                Vector2f end = t.transform(c.add(Vector2f.of((float) (rx * Math.cos(phi)), (float) (ry * Math.sin(phi)))));

                // draw the input points
                drawPoint(g, c, Color.RED, 3);
                drawPoint(g, start, Color.BLUE, 3);
                drawPoint(g, end, Color.GREEN, 3);

                Path2f path = Path2f.builder()
                        .moveTo(start)
                        .arcTo(end, Vector2f.of(rx, ry), beta, i >= segments / 2, true)
                        .build();
                g.setStroke(Color.BLUE, 1);
                g.strokePath(path);
            }

            // draw ellipses
            float angle = (float) (TWO_PI * j / angles);
            c = Vector2f.of(w * (j + 0.5f), 1.5f * h);
            g.setFill(Color.LIGHTGRAY);
            g.fillEllipse(c.x(), c.y(), rMax, rMax * 0.75f, angle);
            g.setStroke(Color.BLACK, 3);
            g.strokeEllipse(c.x(), c.y(), rMax, rMax * 0.75f, angle);
        }

        Vector2f c = Vector2f.of(g.getBounds().xCenter(), 2.5f * h);
        Vector2f p0 = c.translate(0, -50);
        Vector2f p1 = c.translate(0, 50);
        Vector2f r = Vector2f.of(150, 100);

        g.setStroke(Color.RED, 1);
        g.strokeCircle(p0, 5);
        g.strokeCircle(p1, 5);

        float angle = 0;

        g.setStrokeWidth(3);
        g.setStrokeColor(Color.BLACK);
        g.strokePath(Path2f.builder().moveTo(p0).arcTo(p1, r, angle, false, true).build());

        g.setStrokeColor(Color.BLUE);
        g.strokePath(Path2f.builder().moveTo(p0).arcTo(p1, r, angle, true, true).build());

        g.setStrokeColor(Color.RED);
        g.strokePath(Path2f.builder().moveTo(p0).arcTo(p1, r, angle, false, false).build());

        g.setStrokeColor(Color.GREEN);
        g.strokePath(Path2f.builder().moveTo(p0).arcTo(p1, r, angle, true, false).build());
    }

    private void drawPoint(Graphics g, Vector2f p, Color color, float size) {
        g.setFill(color);
        g.fillCircle(p.x(), p.y(), size);
    }

}