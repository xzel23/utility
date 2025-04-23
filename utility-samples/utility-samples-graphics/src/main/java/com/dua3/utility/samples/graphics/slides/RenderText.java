package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.samples.graphics.FxGraphicsSample;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;

/**
 * The RenderText class implements the FxGraphicsSample.Slide interface and demonstrates
 * how to render stylized text using the graphical rendering framework. It defines the
 * specific slide content to display styled text on a canvas.
 */
public class RenderText implements FxGraphicsSample.Slide {
    @Override
    public String title() {
        return "renderText()";
    }

    @Override
    public void drawContent(Graphics g) {
        drawText(g);
    }

    /**
     * Draw styled text on a {@link Graphics}.
     * @param g the Graphics instance used for drawing
     */
    public void drawText(Graphics g) {
        RichText text = new RichTextBuilder()
                .push(Style.BLACK)
                .push(Style.BOLD)
                .append("Black")
                .pop(Style.BOLD)
                .append(" is ")
                .push(Style.ITALIC)
                .append("darker")
                .pop(Style.ITALIC)
                .append(" than ")
                .push(Style.BOLD)
                .push(Style.GRAY)
                .append("Gray")
                .pop(Style.GRAY)
                .pop(Style.BOLD)
                .append(".")
                .pop(Style.BLACK)
                .toRichText();

        float margin = 10;
        g.setFont(g.getDefaultFont().withSize(24));
        Rectangle2f r = Rectangle2f.of(Vector2f.ORIGIN, g.getDimension()).addMargin(-margin);
        g.renderText(
                r.min(),
                text,
                Graphics.HAnchor.LEFT,
                Graphics.VAnchor.TOP,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                r.getDimension(),
                Graphics.TextWrapping.WRAP
        );
    }
}
