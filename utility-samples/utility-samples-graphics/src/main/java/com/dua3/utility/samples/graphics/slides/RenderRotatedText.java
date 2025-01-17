package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.MathUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.samples.graphics.Slide;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;

import java.util.stream.DoubleStream;

public class RenderRotatedText implements Slide {

    public static final RichText TEXT = new RichTextBuilder()
            .append("rotated text\n")
            .append("using different modes\n")
            .append("and angles\n")
            .push(Style.BOLD)
            .append("bold ")
            .pop(Style.BOLD)
            .push(Style.ITALIC)
            .append("italic\n")
            .pop(Style.ITALIC)
            .push(Style.UNDERLINE)
            .append("underline ")
            .pop(Style.UNDERLINE)
            .push(Style.LINE_THROUGH)
            .append("line through")
            .pop(Style.LINE_THROUGH)
            .toRichText();

    @Override
    public String title() {
        return "renderText() [2]";
    }

    @Override
    public void draw(Graphics g) {
        drawText(g);
    }

    public void drawText(Graphics g) {
        double[] angles = DoubleStream.of(0, 45, 90, 135, 180, 225, 270, 315)
                .map( v -> v + 10)
                .toArray();

        record Mode(Graphics.TextRotationMode mode, Graphics.AlignmentAxis axis) {
            @Override
            public String toString() {
                return mode == Graphics.TextRotationMode.ROTATE_LINES
                        ? mode.name() + "[" + axis.name() + "]"
                        : mode.name();
            }
        }
        Mode[] modes = {
                new Mode(Graphics.TextRotationMode.ROTATE_BLOCK, Graphics.AlignmentAxis.AUTOMATIC),
                new Mode(Graphics.TextRotationMode.ROTATE_AND_TRANSLATE_BLOCK, Graphics.AlignmentAxis.AUTOMATIC),
                new Mode(Graphics.TextRotationMode.ROTATE_LINES, Graphics.AlignmentAxis.AUTOMATIC),
                new Mode(Graphics.TextRotationMode.ROTATE_LINES, Graphics.AlignmentAxis.X_AXIS),
                new Mode(Graphics.TextRotationMode.ROTATE_LINES, Graphics.AlignmentAxis.Y_AXIS)
        };

        float margin = 10.0f;
        Dimension2f tileDimension = g.getBounds().getDimension().withMargin(-margin).scaled(Scale2f.of(1.0f / angles.length, 1.0f / (modes.length + 1)));
        float tileWidth = tileDimension.width();
        float tileHeight = tileDimension.height();

        g.setFont(g.getDefaultFont().withSize(12));

        // draw column titles
        for (int j = 0; j < angles.length; j++) {
            float x = margin + (j + 0.025f) * tileWidth;
            float y = margin;
            double alpha = angles[j];
            g.drawText(
                    alpha + "° (quad " + MathUtil.quadrantDegrees(alpha) + ", oct " + ((int) (1 + MathUtil.normalizeDegrees(alpha)/45.0)) + ")",
                    x,
                    y,
                    Graphics.HAnchor.LEFT,
                    Graphics.VAnchor.TOP
            );
        }

        for (int i=0; i<modes.length; i++) {
            float x = margin;
            float y = margin + (i + 0.8f) * tileHeight;
            g.drawText(modes[i].toString(), x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);
        }

        for (int i=0; i<modes.length; i++) {
            for (int j = 0; j < angles.length; j++) {
                float x = margin + (j + 0.25f) * tileWidth;
                float y = margin + (i + 1) * tileHeight;
                double rotation = MathUtil.rad(angles[j]);

                Rectangle2f r = Rectangle2f.of(x, y, tileWidth, tileHeight * 0.75f);

                // draw pivot
                g.setFill(Color.RED);
                g.fillCircle(x, y, 3);

                // draw axis
                g.setStroke(Color.RED, 1);
                g.strokeRect(r);

                // draw rotated axis
                g.setStroke(Color.BLUE, 1);
                float dx = (float) (tileWidth / 3 * Math.cos(rotation));
                float dy = (float) (tileWidth / 3 * Math.sin(rotation));
                g.strokeLine(x,y, x + dx, y + dy);
                g.strokeLine(x,y, x - dy, y + dx);

                g.renderText(
                        r,
                        TEXT,
                        Alignment.LEFT,
                        VerticalAlignment.TOP,
                        true,
                        rotation,
                        modes[i].mode(),
                        modes[i].axis()
                );
            }
        }
    }
}
