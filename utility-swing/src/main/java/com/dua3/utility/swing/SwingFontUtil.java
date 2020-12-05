package com.dua3.utility.swing;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import com.dua3.utility.data.Color;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

/**
 * Utility class for getting font properties through AWT. This class should normally not used directly by user code
 * as the functionality should be available in the {@link com.dua3.utility.text.TextUtil} utility class which
 * in turn uses this class via SPI (Java ServiceProvider interface). 
 * See {@link com.dua3.utility.text.TextUtil#FONT_UTIL} for details.
 */
public class SwingFontUtil implements FontUtil<java.awt.Font> {

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param font the font
     * @return the text's bounds (positioned at the origin)
     */
    public Rectangle2D stringBounds(CharSequence text, Font font) {
        java.awt.Font awtFont = convert(font);
        return stringBounds(text, awtFont);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public Rectangle2D stringBounds(CharSequence text, java.awt.Font awtFont) {
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return awtFont.getStringBounds(text.toString(), frc);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public Bounds getTextBounds(CharSequence text, java.awt.Font awtFont) {
        Rectangle2D r = stringBounds(text, awtFont);
        return new Bounds(r.getWidth(), r.getHeight());
    }

    /**
     * Calculate the height of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextHeight(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getHeight();
    }

    /**
     * Calculate the width of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextWidth(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getWidth();
    }

    @Override
    public Bounds getTextBounds(CharSequence s, Font f) {
        Rectangle2D r = stringBounds(s, f);
        return new Bounds(r.getWidth(), r.getHeight());
    }

    @Override
    public double getTextHeight(CharSequence s, Font f) {
        return stringBounds(s, f).getHeight();
    }

    @Override
    public double getTextWidth(CharSequence s, Font f) {
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
