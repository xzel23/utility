package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.data.Color;
import com.dua3.utility.math.MathUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Scale2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.samples.graphics.IGraphicsSample;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;

/**
 * The RenderRotatedText class implements the Slide interface to demonstrate the rendering
 * of rotated text with various modes and angles. This class renders styled and formatted
 * text content while applying different rotations and anchoring options.
 */
public class RenderRotatedText implements IGraphicsSample.Slide {

    /**
     *  The RichText instance containing the text to be rendered.
     */
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
    private final Graphics.HAnchor hAnchor;
    private final Graphics.VAnchor vAnchor;
    private final double[] angles;


    public RenderRotatedText(Graphics.HAnchor hAnchor, Graphics.VAnchor vAnchor, double[] angles) {
        this.hAnchor = hAnchor;
        this.vAnchor = vAnchor;
        this.angles  = angles;
    }

    @Override
    public String title() {
        return "renderText() [%s,%s]".formatted(hAnchor, vAnchor);
    }

    @Override
    public void drawContent(Graphics g) {
        drawText(g);
    }

    public void drawText(Graphics g) {
        Graphics.TextRotationMode[] modes = Graphics.TextRotationMode.values();

        float margin = 10.0f;
        Dimension2f tileDimension = g.getDimension().addMargin(-margin).scaled(Scale2f.of(1.0f / angles.length, 1.0f / (modes.length + 1)));
        float tileWidth = tileDimension.width();
        float tileHeight = tileDimension.height();

        g.setFont(g.getDefaultFont().withSize(12));

        // draw column titles
        for (int j = 0; j < angles.length; j++) {
            float x = margin + (j + 0.025f) * tileWidth;
            float y = margin;
            double alpha = angles[j];
            g.drawText(
                    alpha + "° (quad " + MathUtil.quadrantDegrees(alpha) + ", oct " + ((int) (1 + MathUtil.normalizeDegrees(alpha) / 45.0)) + ")",
                    x,
                    y,
                    Graphics.HAnchor.LEFT,
                    Graphics.VAnchor.TOP
            );
        }

        for (int i = 0; i < modes.length; i++) {
            for (int j = 0; j < angles.length; j++) {
                float x = margin + (j + 0.25f) * tileWidth;
                float y = margin + (i + 1) * tileHeight;
                double rotation = MathUtil.rad(angles[j]);

                float w = tileWidth;
                float h = tileHeight * 0.75f;

                // draw pivot
                g.setFill(Color.RED);
                g.fillCircle(x, y, 3);

                // draw axis
                g.setStroke(Color.RED, 1);
                g.strokeRect(x, y, w, h);

                // draw rotated axis
                g.setStroke(Color.BLUE, 1);
                float dx = (float) (tileWidth / 3 * Math.cos(rotation));
                float dy = (float) (tileWidth / 3 * Math.sin(rotation));
                g.strokeLine(x, y, x + dx, y + dy);
                g.strokeLine(x, y, x - dy, y + dx);

                g.renderText(
                        Vector2f.of(x, y),
                        TEXT,
                        hAnchor, vAnchor,
                        Alignment.LEFT,
                        VerticalAlignment.TOP,
                        new Dimension2f(w, h),
                        Graphics.TextWrapping.WRAP,
                        rotation,
                        modes[i]
                );
            }
        }

        for (int i = 0; i < modes.length; i++) {
            float x = margin;
            float y = margin + (i + 0.8f) * tileHeight;
            String labelText = modes[i].toString();
            g.setFill(Color.WHITE);
            g.fillRect(FontUtil.getInstance().getTextDimension(labelText, g.getFont()).moveTo(x, y).addMargin(2));
            g.drawText(labelText, x, y, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP);
        }
    }
}
