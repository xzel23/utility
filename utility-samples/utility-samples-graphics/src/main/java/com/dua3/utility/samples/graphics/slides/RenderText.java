package com.dua3.utility.samples.graphics.slides;

import com.dua3.utility.samples.graphics.FxGraphicsSample;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;

public class RenderText implements FxGraphicsSample.Slide {
    @Override
    public String title() {
        return "renderText()";
    }

    @Override
    public void draw(Graphics g) {
        drawText(g);
    }

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
        g.renderText(g.getBounds().addMargin(-margin), text, Alignment.LEFT, VerticalAlignment.TOP, true);
    }
}
