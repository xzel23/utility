package com.dua3.utility.text;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.spi.SpiLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Interface for Font handling utility classes. The concrete implementation is automatically chosen at runtime
 * for use by the {@link TextUtil} class.
 *
 * @param <F> the implementation's underlying Font class
 */
public interface FontUtil<F> {

    /**
     * Get FontUtil instance.
     *
     * @return the default FontUtil instance
     */
    static FontUtil<?> getInstance() {
        final class SingletonHolder {
            static final FontUtil<?> INSTANCE = SpiLoader.builder(FontUtilProvider.class)
                    .defaultSupplier(() -> AwtFontUtil::getInstance)
                    .build()
                    .load()
                    .get();
        }

        return SingletonHolder.INSTANCE;
    }

    /**
     * Convert font.
     *
     * @param font the font
     * @return the font implementation
     */
    F convert(Font font);

    /**
     * Get text bounds.
     *
     * @param s the text
     * @param f the font
     * @return the text bounds
     */
    Rectangle2f getTextDimension(CharSequence s, Font f);

    /**
     * Calculates the dimension of the given rich text line using the specified font.
     *
     * @param s the text
     * @param f the base font to apply
     * @return the dimension of the rich text line
     */
    default Rectangle2f getRichTextDimension(CharSequence s, Font f) {
        if (s instanceof ToRichText t) {
            float w = 0;
            float h = 0;
            return t.toRichText().lines()
                    .map(line ->
                            // determine bounding rectangle for current line
                            line.runs().stream()
                                    .map(run -> getTextDimension(run, f.deriveFont(run.getFontDef())))
                                    .reduce( (a, b) -> Rectangle2f.withCorners(
                                                    Vector2f.of(
                                                            // A: x,y = left of first part, lowest border of both parts
                                                            a.xMin(),
                                                            Math.min(a.yMin(), b.yMin())
                                                    ),
                                                    Vector2f.of(
                                                            // B: x,y = left of first part + both widths, height from bottom to top for both parts
                                                            a.xMin() + a.width() + b.width(),
                                                            Math.max(a.yMax(), b.yMax())
                                                    )
                                            )
                                    )
                                    .orElseGet(() -> getTextDimension("", f))
                    )
                    .reduce((a,b) -> new Rectangle2f(Math.min(a.xMin(), b.xMin()), Math.min(a.yMin(), b.yMin()), Math.max(a.width(), b.width()), a.height() + b.height()))
                    .orElseGet(() -> getTextDimension("", f));
        } else {
            return getTextDimension(s, f);
        }
    }

    /**
     * Get text width.
     *
     * @param s the text
     * @param f the font
     * @return the text width
     */
    default double getTextWidth(CharSequence s, Font f) {
        return getTextDimension(s, f).width();
    }

    /**
     * Get text height.
     *
     * @param s the text
     * @param f the font
     * @return the text height
     */
    default double getTextHeight(CharSequence s, Font f) {
        return getTextDimension(s, f).height();
    }

    /**
     * Load font.
     *
     * @param in the {@link InputStream} to read the font data from.
     * @return the font loaded
     * @throws java.io.IOException      if an I/O error occurs
     * @throws IllegalArgumentException if the type is not supported
     */
    List<Font> loadFonts(InputStream in) throws IOException;

    /**
     * Get a list of the available font families.
     *
     * @param types the font types to return
     * @return list of font families
     */
    List<String> getFamilies(FontTypes types);

    /**
     * Get a list of the available font families.
     *
     * @return list of font families
     */
    default List<String> getFamilies() {
        return getFamilies(FontTypes.ALL);
    }

    /**
     * Load an embedded font.
     *
     * @param in   the stream to read font data from
     * @param font the font that is loaded
     * @return font instance
     * @throws IOException if an error occurs
     */
    Font loadFontAs(InputStream in, Font font) throws IOException;

    /**
     * Font type enumeration.
     */
    enum FontTypes {
        /**
         * Enum value for proportional fonts.
         */
        PROPORTIONAL,
        /**
         * Enum value for monospaced fonts.
         */
        MONOSPACED,
        /**
         * Enum value for all fonts.
         */
        ALL
    }

}
