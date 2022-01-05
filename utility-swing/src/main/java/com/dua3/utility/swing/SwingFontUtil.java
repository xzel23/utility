package com.dua3.utility.swing;

import com.dua3.utility.data.Color;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.cabe.annotations.NotNull;

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
 * Utility class for getting font properties through AWT. This class should normally not used directly by user code
 * as the functionality should be available in the {@link com.dua3.utility.text.TextUtil} utility class which
 * in turn uses this class via SPI (Java ServiceProvider interface). 
 * See usage of {@link FontUtil} in {@link com.dua3.utility.text.TextUtil} for details.
 */
public class SwingFontUtil implements FontUtil<java.awt.Font> {

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param font the font
     * @return the text's bounds (positioned at the origin)
     */
    public Rectangle2D stringBounds(@NotNull CharSequence text, @NotNull Font font) {
        java.awt.Font awtFont = convert(font);
        return stringBounds(text, awtFont);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    @SuppressWarnings("MethodMayBeStatic")
    public Rectangle2D stringBounds(@NotNull CharSequence text, @NotNull java.awt.Font awtFont) {
        FontRenderContext frc = new FontRenderContext(awtFont.getTransform(), false, true);
        return awtFont.getStringBounds(text.toString(), frc);
    }

    /**
     * Calculate the string bounds of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public Dimension2f getTextDimension(@NotNull CharSequence text, @NotNull java.awt.Font awtFont) {
        Rectangle2D r = stringBounds(text, awtFont);
        return Dimension2f.of((float) r.getWidth(), (float) r.getHeight());
    }

    /**
     * Calculate the height of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextHeight(@NotNull CharSequence text, @NotNull java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getHeight();
    }

    /**
     * Calculate the width of a text using the font passed as argument.
     * @param text the text
     * @param  awtFont the font
     * @return the text's bounds (positioned at the origin)
     */
    public double getTextWidth(@NotNull CharSequence text, @NotNull java.awt.Font awtFont) {
        return stringBounds(text, awtFont).getWidth();
    }

    @Override
    public Dimension2f getTextDimension(@NotNull CharSequence s, @NotNull Font f) {
        Rectangle2D r = stringBounds(s, f);
        return Dimension2f.of((float) r.getWidth(), (float) r.getHeight());
    }

    @Override
    public double getTextHeight(@NotNull CharSequence s, @NotNull Font f) {
        return stringBounds(s, f).getHeight();
    }

    private final WeakHashMap<Font, java.awt.Font> fontMap = new WeakHashMap<>();
    
    @Override
    public List<Font> loadFonts(@NotNull InputStream in) throws IOException {
        try (in) {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            List<Font> fonts =new ArrayList<>(awtFonts.length);
            for (var awtFont: awtFonts) {
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
    public List<String> getFamilies(@NotNull FontTypes types) {
        List<String> fonts = List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());

        boolean mono;
        switch (types) {
            case ALL:
                return fonts;
            case MONOSPACED:
                mono = true;
                break;
            case PROPORTIONAL:
                mono = false;
                break;
            default:
                throw new IllegalArgumentException("unknown value: "+types);
        }

        List<String> list = new ArrayList<>();
        
        String thin = "1 l";
        String thick = "M_W";
        for (String family: fonts) {
            java.awt.Font font = new java.awt.Font(family, 0, 14);
            FontRenderContext frc = new FontRenderContext(font.getTransform(), false, true);
            boolean monospaced = Objects.equals(font.getStringBounds(thin, frc), font.getStringBounds(thick, frc));
            if (mono == monospaced) {
                list.add(family);
            }
        }
        
        return list;
    }

    @Override
    public double getTextWidth(@NotNull CharSequence s, @NotNull Font f) {
        return stringBounds(s, f).getWidth();
    }

    @Override
    public java.awt.Font convert(@NotNull Font font) {
        return fontMap.computeIfAbsent(font, 
                fnt -> getAwtFont(
                    font.getFamily(),
                    font.getSizeInPoints(),
                        font.isBold(),
                    font.isItalic()
                )
        );
    }

    private static java.awt.Font getAwtFont(String family, float size, boolean bold, boolean italic) {
        int style = (bold ? java.awt.Font.BOLD : 0) | (italic ? java.awt.Font.ITALIC : 0);
        return new java.awt.Font(family, style, Math.round(size));
    }

    @Override
    public Font loadFontAs(@NotNull InputStream in, @NotNull Font font) throws IOException {
        try (in) {
            java.awt.Font[] awtFonts = java.awt.Font.createFonts(in);
            LangUtil.check(awtFonts.length>0, () -> new IOException("no font loaded"));
            java.awt.Font awtFont = awtFonts[0].deriveFont(font.getSizeInPoints());
            Font loadedFont = new Font(font.getFamily(), font.getSizeInPoints(), font.getColor(), font.isBold(), font.isItalic(), font.isUnderline(), font.isStrikeThrough());
            fontMap.putIfAbsent(loadedFont, awtFont);
            return font;
        } catch (FontFormatException e) {
            throw new IOException(e);
        }
    }
    
}
