package com.dua3.utility.awt;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * Utility class for getting font properties through AWT. This class should normally not be used directly by user code
 * as the functionality should be available in the {@link com.dua3.utility.text.TextUtil} utility class which
 * in turn uses this class via SPI (Java ServiceProvider interface).
 * See usage of {@link FontUtil} in {@link com.dua3.utility.text.TextUtil} for details.
 */
@SuppressWarnings("NumericCastThatLosesPrecision")
public class AwtFontUtil implements FontUtil<java.awt.Font> {

    private static class SingletonHolder {
        private static final AwtFontUtil INSTANCE = new AwtFontUtil();
    }

    private AwtFontUtil() {} // utility class constructor

    /**
     * Retrieves an instance of the AwtFontUtil class.
     *
     * @return an instance of AwtFontUtil
     */
    public static AwtFontUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private final WeakHashMap<Font, java.awt.Font> fontMap = new WeakHashMap<>();

    private static java.awt.Font getAwtFont(String family, float size, boolean bold, boolean italic) {
        int style = (bold ? java.awt.Font.BOLD : java.awt.Font.PLAIN)
                | (italic ? java.awt.Font.ITALIC : java.awt.Font.PLAIN);
        return new java.awt.Font(family, style, Math.round(size));
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     *
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
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Rectangle2D stringBounds(CharSequence text, java.awt.Font awtFont) {
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return awtFont.getStringBounds(text.toString(), frc);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public Dimension2f getTextDimension(CharSequence text, java.awt.Font awtFont) {
        Rectangle2D r = stringBounds(text, awtFont);
        return Dimension2f.of((float) r.getWidth(), (float) r.getHeight());
    }

    /**
     * Calculate the height of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextHeight(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getHeight();
    }

    /**
     * Calculate the width of a text using the font passed as argument.
     *
     * @param text    the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextWidth(CharSequence text, java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getWidth();
    }

    @Override
    public Dimension2f getTextDimension(CharSequence s, Font f) {
        Rectangle2D r = stringBounds(s, f);
        return Dimension2f.of((float) r.getWidth(), (float) r.getHeight());
    }

    @Override
    public double getTextHeight(CharSequence s, Font f) {
        return stringBounds(s, f).getHeight();
    }

    @Override
    public List<Font> loadFonts(InputStream in) throws IOException {
        try {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            List<Font> fonts = new ArrayList<>(awtFonts.length);
            for (var awtFont : awtFonts) {
                Font font = new Font(awtFont.getFamily(), awtFont.getSize(), Color.BLACK, awtFont.isBold(), awtFont.isItalic(), false, false);
                fontMap.putIfAbsent(font, awtFont);
                fonts.add(font);
            }
            return Collections.unmodifiableList(fonts);
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }

    @Override
    public List<String> getFamilies(FontTypes types) {
        List<String> fonts = List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

        boolean monospaced;
        switch (types) {
            case ALL -> {return fonts;}
            case MONOSPACED -> monospaced = true;
            case PROPORTIONAL -> monospaced = false;
            default -> throw new IllegalArgumentException("unknown value: " + types);
        }

        return listFonts(fonts, monospaced);
    }

    private static List<String> listFonts(List<String> fonts, boolean mono) {
        List<String> list = new ArrayList<>();

        // measure the width of two strings to find out if font is monospaced
        String thin = "1 l";
        String thick = "M_W";
        for (String family : fonts) {
            java.awt.Font font = new java.awt.Font(family, java.awt.Font.PLAIN, 14);
            FontRenderContext frc = new FontRenderContext(font.getTransform(), false, true);
            boolean monospaced = Objects.equals(font.getStringBounds(thin, frc), font.getStringBounds(thick, frc));
            if (mono == monospaced) {
                list.add(family);
            }
        }

        return list;
    }

    @Override
    public double getTextWidth(CharSequence s, Font f) {
        return stringBounds(s, f).getWidth();
    }

    @Override
    public java.awt.Font convert(Font font) {
        return fontMap.computeIfAbsent(font,
                fnt -> getAwtFont(
                        font.getFamily(),
                        font.getSizeInPoints(),
                        font.isBold(),
                        font.isItalic()
                )
        );
    }

    @Override
    public Font loadFontAs(InputStream in, Font font) throws IOException {
        try {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            LangUtil.check(awtFonts.length > 0, () -> new IOException("no font loaded"));
            java.awt.Font awtFont = awtFonts[0].deriveFont(font.getSizeInPoints());
            Font loadedFont = new Font(font.getFamily(), font.getSizeInPoints(), font.getColor(), font.isBold(), font.isItalic(), font.isUnderline(), font.isStrikeThrough());
            fontMap.putIfAbsent(loadedFont, awtFont);
            return font;
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }

}
