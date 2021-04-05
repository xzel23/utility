package com.dua3.utility.text;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Interface for Font handling utility classes. The concrete implementation is automatically chosen at runtime
 * for use by the {@link TextUtil} class.
 * @param <F> the implementation's underlying Font class
 */
public interface FontUtil<F> {

    String NO_IMPLEMENTATION = "no FontUtil implementation present";

    static FontUtil getInstance() {
        //noinspection rawtypes
        Iterator<FontUtil> serviceIterator = ServiceLoader
                .load(FontUtil.class)
                .iterator();

        FontUtil<?> fu;
        if (serviceIterator.hasNext()) {
            fu = serviceIterator.next();
        } else {
            fu = new FontUtil<Void>() {
                @Override
                public Void convert(Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public Bounds getTextBounds(CharSequence s, Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public Optional<Font> loadFont(String type, InputStream in) throws IOException {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }
            };
        }
        
        return fu;
    }
    
    /** The font type string for TrueType fonts. */
    public static String FONT_TYPE_TRUETYPE = "ttf";
    
    /**
     * Dimensions.
     */
    class Bounds {
        public final double width;
        public final double height;

        public Bounds(double w, double h) {
            this.width = w;
            this.height = h;
        }
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
    Bounds getTextBounds(CharSequence s, Font f);

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
        return getTextBounds(s, f).width;
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
        return getTextBounds(s, f).height;
    }

    /**
     * Load font.
     * @param type the font type as String.
     * @param in the {@link InputStream} to read the font data from.
     * @return the font loaded
     * @throws java.io.IOException if an I/O error occurs
     * @throws IllegalArgumentException if the type is not supported
     */
    Optional<Font> loadFont(String type, InputStream in) throws IOException;
}
