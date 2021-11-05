package com.dua3.utility.text;

import com.dua3.utility.math.geometry.Dimension2f;
import org.jetbrains.annotations.NotNull;

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
    static @NotNull FontUtil<?> getInstance() {
        Iterator<FontUtil> serviceIterator = ServiceLoader
                .load(FontUtil.class)
                .iterator();

        FontUtil<?> fu;
        if (serviceIterator.hasNext()) {
            fu = serviceIterator.next();
        } else {
            fu = new FontUtil<>() {
                @Override
                public @NotNull Void convert(@NotNull Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public @NotNull Dimension2f getTextDimension(@NotNull CharSequence s, @NotNull Font f) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @SuppressWarnings("RedundantThrows")
                @Override
                public @NotNull List<Font> loadFonts(@NotNull InputStream in) throws IOException {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public @NotNull List<String> getFamilies(@NotNull FontTypes types) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public @NotNull Font loadFontAs(@NotNull InputStream in, @NotNull Font font) throws IOException {
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
    F convert(@NotNull Font f);

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
    @NotNull Dimension2f getTextDimension(@NotNull CharSequence s, @NotNull Font f);

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
    default double getTextWidth(@NotNull CharSequence s, @NotNull Font f) {
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
    default double getTextHeight(@NotNull CharSequence s, @NotNull Font f) {
        return getTextDimension(s, f).height();
    }

    /**
     * Load font.
     * @param in the {@link InputStream} to read the font data from.
     * @return the font loaded
     * @throws java.io.IOException if an I/O error occurs
     * @throws IllegalArgumentException if the type is not supported
     */
    @NotNull List<Font> loadFonts(@NotNull InputStream in) throws IOException;

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
    @NotNull List<String> getFamilies(@NotNull FontTypes types);

    /**
     * Get a list of the available font families.
     * @return list of font families
     */
    default @NotNull List<String> getFamilies() {
        return getFamilies(FontTypes.ALL);
    }

    /**
     * Load an embedded font.
     * @param in the stream to read font data from
     * @param font the font that is loaded
     * @return font instance
     * @throws IOException if an error occurs
     */
    @NotNull Font loadFontAs(@NotNull InputStream in, @NotNull Font font) throws IOException;

}
