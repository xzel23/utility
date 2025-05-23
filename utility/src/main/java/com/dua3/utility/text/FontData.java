// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents font data, containing attributes and metrics for a specific font style.
 *
 * @param families      the list of font family names;
 *                      it is always guaranteed to contain at least one entry with the family name at index 0
 *                      and alternative names following
 * @param size          the font size in points
 * @param monospaced    if the font is monospaced
 * @param bold          whether the font style is bold
 * @param italic        whether the font style is italicized
 * @param underline     whether the font style includes an underline
 * @param strikeThrough whether the font style includes a strike-through
 * @param fontDef       the {@link FontDef} object defining extended font properties
 * @param fontspec      the font specification string without color information
 * @param cssStyle      the CSS representation of the font's style
 * @param ascent        the distance above the baseline for the highest ascender
 * @param descent       the distance below the baseline for the lowest descender
 * @param height        the overall height of the font, including ascent and descent
 * @param spaceWidth    the width of the space character in the font
 */
public record FontData(
        List<String> families,
        float size,
        boolean monospaced,
        boolean bold,
        boolean italic,
        boolean underline,
        boolean strikeThrough,
        FontDef fontDef,
        String fontspec,
        String cssStyle,
        double ascent,
        double descent,
        double height,
        double spaceWidth
) {

    @SuppressWarnings("MissingJavadoc")
    public FontData {
        assert !families.isEmpty() : "family is the empty string";
        assert size >= 0 : "size is negative";
        assert ascent >= 0 : "ascent is negative";
        assert descent >= 0 : "descent is negative";
        assert height >= ascent : "inconsistent height";
        assert spaceWidth > 0 : "space width must be positive";
        assert fontDef.getFamily() != null : "fontDef.getFamily() is null";
        assert fontDef.getSize() != null : "fontDef.getSize() is null";
        assert fontDef.getBold() != null : "fontDef.getBold() is null";
        assert fontDef.getItalic() != null : "fontDef.getItalic() is null";
        assert fontDef.getUnderline() != null : "fontDef.getUnderline() is null";
        assert fontDef.getStrikeThrough() != null : "fontDef.getStrikeThrough() is null";
        assert fontDef.getColor() == null : "fontDef.getColor() must be null";

        // copy families to an immutable list
        families = List.copyOf(families);

        // remove color from fontspec
        assert fontspec.endsWith("-*") : "unexpected fontspec: " + fontspec;
        fontspec = fontspec.substring(0, fontspec.length() - 2);
    }

    /**
     * Creates and returns a new instance of {@code FontData} with the specified attributes and metrics.
     *
     * @param family        the list of font family names; must not be empty
     * @param size          the font size; must be non-negative
     * @param monospaced    whether the font is monospaced
     * @param bold          whether the font is bold
     * @param italic        whether the font is italic
     * @param underline     whether the font is underlined
     * @param strikeThrough whether the font is strike-through
     * @param ascent        the ascent metric of the font; must be non-negative
     * @param descent       the descent metric of the font; must be non-negative
     * @param height        the total height of the font; must be greater than or equal to ascent
     * @param spaceWidth    the width of the space character; must be positive
     * @return a new instance of {@code FontData} containing the specified attributes and metrics
     */
    public static FontData get(
            String family,
            float size,
            boolean monospaced,
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikeThrough,
            double ascent,
            double descent,
            double height,
            double spaceWidth
    ) {
        List<String> families = com.dua3.utility.text.FontDef.parseFontFamilies(family, false);
        if (families == null) {
            throw new IllegalArgumentException("invalid value for families: " + family);
        }
        return get(families, size, monospaced, bold, italic, underline, strikeThrough, ascent, descent, height, spaceWidth);
    }

    /**
     * Creates and returns a new instance of {@code FontData} with the specified attributes and metrics.
     *
     * @param families      the list of font family names; must not be empty
     * @param size          the font size; must be non-negative
     * @param monospaced    if the font is monospaced
     * @param bold          whether the font is bold
     * @param italic        whether the font is italic
     * @param underline     whether the font is underlined
     * @param strikeThrough whether the font is strike-through
     * @param ascent        the ascent metric of the font; must be non-negative
     * @param descent       the descent metric of the font; must be non-negative
     * @param height        the total height of the font; must be greater than or equal to ascent
     * @param spaceWidth    the width of the space character; must be positive
     * @return a new instance of {@code FontData} containing the specified attributes and metrics
     */
    public static FontData get(
            SequencedCollection<String> families,
            float size,
            boolean monospaced,
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikeThrough,
            double ascent,
            double descent,
            double height,
            double spaceWidth
    ) {
        FontDef fd = new FontDef();
        fd.setFamilies(families);
        fd.setSize(size);
        fd.setType(monospaced ? FontType.MONOSPACED : FontType.PROPORTIONAL);
        fd.setBold(bold);
        fd.setItalic(italic);
        fd.setUnderline(underline);
        fd.setStrikeThrough(strikeThrough);

        return new FontData(
                List.copyOf(families),
                size,
                monospaced,
                bold,
                italic,
                underline,
                strikeThrough,
                fd,
                fd.fontspec(),
                fd.getCssStyle(),
                ascent,
                descent,
                height,
                spaceWidth
        );
    }

    /**
     * Returns the negative value of the descent attribute for the font.
     *
     * @return the signed descent, which is the negative value of the descent.
     */
    public double descentSigned() {
        return -descent;
    }

    /**
     * Test if two fonts are similar. This method is provided to be used when inheriting fonts because equals
     * also tests both instances to be of the exact same class.
     *
     * @param a the first font
     * @param b the second font
     * @return true, if a and b have the same attributes
     */
    public static boolean similar(FontData a, FontData b) {
        return b.size == a.size
                && b.bold == a.bold
                && b.italic == a.italic
                && b.underline == a.underline
                && b.strikeThrough == a.strikeThrough
                && b.families().equals(a.families());
    }

    /**
     * Determine differences between two fonts.
     *
     * @param f1 the first font
     * @param f2 the second font
     * @return a {@link FontDef} instance that defines the changed values
     */
    public static FontDef delta(@Nullable FontData f1, @Nullable FontData f2) {
        FontDef fd = new FontDef();
        deltaHelper(f1, f2, FontData::families, fd::setFamilies);
        deltaHelper(f1, f2, FontData::size, fd::setSize);
        deltaHelper(f1, f2, FontData::bold, fd::setBold);
        deltaHelper(f1, f2, FontData::italic, fd::setItalic);
        deltaHelper(f1, f2, FontData::underline, fd::setUnderline);
        deltaHelper(f1, f2, FontData::strikeThrough, fd::setStrikeThrough);
        return fd;
    }

    /**
     * Compare values and store changes.
     *
     * @param <T>    object type
     * @param <U>    attribute type
     * @param o1     first object
     * @param o2     second object
     * @param getter attribute getter
     * @param setter attribute setter
     */
    static <T, U> void deltaHelper(@Nullable T o1, @Nullable T o2, Function<T, U> getter, Consumer<? super U> setter) {
        U v1 = o1 == null ? null : getter.apply(o1);
        U v2 = o2 == null ? null : getter.apply(o2);
        if (!Objects.equals(v1, v2) && v2 != null) {
            setter.accept(v2);
        }
    }

    /**
     * Get a clone of this {@code FontData} instance's {@link FontDef}.
     * <p>
     * {@code FontData} is immutable, but the contained {@code FontDef} is not.
     * By returning a clone, we can assert that this object's state will not change.
     *
     * @return clone of this {@code FontData} instance's {@link FontDef} member
     */
    @Override
    public FontDef fontDef() {
        try {
            return fontDef.clone();
        } catch (CloneNotSupportedException e) {
            // cannot happen
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String toString() {
        return fontspec();
    }

    /**
     * Get the font family name.
     *
     * @return the font family name
     */
    public String family() {
        return families.getFirst();
    }

}
