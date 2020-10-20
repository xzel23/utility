package com.dua3.utility.swing;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

public class SwingFontUtil implements FontUtil<java.awt.Font> {

    public Rectangle2D stringBounds(String text, Font font) {
        java.awt.Font awtFont = convert(font);
        return stringBounds(text, awtFont);
    }

    public Rectangle2D stringBounds(String text, java.awt.Font awtFont) {
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return awtFont.getStringBounds(text, frc);
    }

    public Bounds getTextBounds(String s, java.awt.Font f) {
        Rectangle2D r = stringBounds(s, f);
        return new Bounds(r.getWidth(), r.getHeight());
    }

    public double getTextHeight(String s, java.awt.Font f) {
        return stringBounds(s, f).getHeight();
    }

    public double getTextWidth(String s, java.awt.Font f) {
        return stringBounds(s, f).getWidth();
    }

    @Override
    public Bounds getTextBounds(String s, Font f) {
        Rectangle2D r = stringBounds(s, f);
        return new Bounds(r.getWidth(), r.getHeight());
    }

    @Override
    public double getTextHeight(String s, Font f) {
        return stringBounds(s, f).getHeight();
    }

    @Override
    public double getTextWidth(String s, Font f) {
        return stringBounds(s, f).getWidth();
    }

    @Override
    public java.awt.Font convert(Font font) {
        return getAwtFont(
                font.getFamily(),
                font.getSizeInPoints(),
                font.getColor(),
                font.isBold(),
                font.isItalic(),
                font.isUnderline(),
                font.isStrikeThrough());
    }

    private static java.awt.Font getAwtFont(String family, float size, Color color, boolean bold, boolean italic,
            boolean underlined,
            boolean strikeThrough) {
        int style = (bold ? java.awt.Font.BOLD : 0) | (italic ? java.awt.Font.ITALIC : 0);
        return new java.awt.Font(family, style, Math.round(size));
    }

}
