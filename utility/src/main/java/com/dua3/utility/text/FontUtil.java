package com.dua3.utility.text;

import com.dua3.utility.math.geometry.Dimension2d;

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

    String NO_IMPLEMENTATION = "no FontUtil implementation present";

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
                @Override
                public Void convert(Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public Dimension2d getTextDimension(CharSequence s, Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @SuppressWarnings("RedundantThrows")
                @Override
                public List<Font> loadFonts(InputStream in) throws IOException {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public List<String> getFamilies(FontTypes types) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
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
    Dimension2d getTextDimension(CharSequence s, Font f);

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
        PROPORTIONAL,
        MONOSPACED,
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
}
