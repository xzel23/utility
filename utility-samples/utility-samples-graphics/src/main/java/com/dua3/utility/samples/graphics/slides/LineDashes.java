package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.samples.graphics.IGraphicsSample;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.ui.HAnchor;
import com.dua3.utility.ui.VAnchor;

/**
 * Demonstrates dashed line patterns and dash offsets.
 */
public class LineDashes implements IGraphicsSample.Slide {

    public LineDashes() {
        // nothing to do
    }

    @Override
    public String title() {
        return "line dashes";
    }

    @Override
    public void drawContent(Graphics g) {
        float margin = 80;
        float lineWidth = g.getWidth() - 2 * margin;
        float y = 140;
        float lineSpacing = 120;

        g.setFont(g.getDefaultFont().withBold(true));
        g.drawText("setLineDashes() and setLineDashOffset()", margin, 50, HAnchor.LEFT, VAnchor.TOP);

        g.setFont(g.getDefaultFont());
        g.setStroke(Color.BLACK, 4);

        drawLine(g, "20 / 10", new float[]{20, 10}, 0, margin, y, lineWidth);
        drawLine(g, "10 / 10, offset 15", new float[]{10, 10}, 15, margin, y + lineSpacing, lineWidth);
        drawLine(g, "20 / 10 / 3 / 10", new float[]{20, 10, 3, 10}, 0, margin, y + 2 * lineSpacing, lineWidth);

        g.setLineDashes(new float[0]);
        g.setLineDashOffset(0);
        g.drawText("solid line", margin, y + 3 * lineSpacing - 35, HAnchor.LEFT, VAnchor.TOP);
        g.strokeLine(margin, y + 3 * lineSpacing, margin + lineWidth, y + 3 * lineSpacing);
    }

    private static void drawLine(Graphics g, String label, float[] dashes, float offset,
                                 float x, float y, float width) {
        g.setLineDashes(dashes);
        g.setLineDashOffset(offset);
        g.drawText(label, x, y - 35, HAnchor.LEFT, VAnchor.TOP);
        g.strokeLine(x, y, x + width, y);
    }
}
