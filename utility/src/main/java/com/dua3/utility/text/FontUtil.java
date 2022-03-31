package com.dua3.utility.text;

import com.dua3.utility.math.geometry.Dimension2f;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Interface for Font handling utility classes. The concrete implementation is automatically chosen at runtime
 * for use by the {@link TextUtil} class.
 * @param <F> the implementation's underlying Font class
 */
public interface FontUtil<F> {

    /**
     * Get FontUtil instance.
     * @return the default FontUtil instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static FontUtil<?> getInstance() {
        Iterator<FontUtil> serviceIterator = ServiceLoader
                .load(FontUtil.class)
                .iterator();

        FontUtil<?> fu;
        if (serviceIterator.hasNext()) {
            fu = serviceIterator.next();
        } else {
            fu = new FontUtil<>() {
                private <T> T noImplementation() { throw new UnsupportedOperationException("no FontUtil implementation present"); }

                @Override
                public Void convert(Font f) {
                    return noImplementation();
                }

                @Override
                public Dimension2f getTextDimension(CharSequence s, Font f) {
                    return noImplementation();
                }

                @SuppressWarnings("RedundantThrows")
                @Override
                public List<Font> loadFonts(InputStream in) throws IOException {
                    return noImplementation();
                }

                @Override
                public List<String> getFamilies(FontTypes types) {
                    return noImplementation();
                }

                @Override
                public Font loadFontAs(InputStream in, Font font) throws IOException {
                    return noImplementation();
                }
            };
        }
        
        return fu;
    }
    
    /**
     * Convert font.
     *
     * @param  f
     *           the font
     * @return
     *           the font implementation
     */
    F convert(Font f);

    /**
     * Get text bounds.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text bounds
     */
    Dimension2f getTextDimension(CharSequence s, Font f);

    /**
     * Get text width.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text width
     */
    default double getTextWidth(CharSequence s, Font f) {
        return getTextDimension(s, f).width();
    }

    /**
     * Get text height.
     *
     * @param  s
     *           the text
     * @param  f
     *           the font
     * @return
     *           the text height
     */
    default double getTextHeight(CharSequence s, Font f) {
        return getTextDimension(s, f).height();
    }

    /**
     * Load font.
     * @param in the {@link InputStream} to read the font data from.
     * @return the font loaded
     * @throws java.io.IOException if an I/O error occurs
     * @throws IllegalArgumentException if the type is not supported
     */
    List<Font> loadFonts(InputStream in) throws IOException;

    /**
     * Font type enumeration.
     */
    enum FontTypes {
        /** Enum value for proportional fonts. */
        PROPORTIONAL,
        /** Enum value for monospaced fonts. */
        MONOSPACED,
        /** Enum value for all fonts. */
        ALL
    }

    /**
     * Get a list of the available font families.
     * @param types the font types to return
     * @return list of font families
     */
    List<String> getFamilies(FontTypes types);

    /**
     * Get a list of the available font families.
     * @return list of font families
     */
    default List<String> getFamilies() {
        return getFamilies(FontTypes.ALL);
    }

    /**
     * Load an embedded font.
     * @param in the stream to read font data from
     * @param font the font that is loaded
     * @return font instance
     * @throws IOException if an error occurs
     */
    Font loadFontAs(InputStream in, Font font) throws IOException;

}
