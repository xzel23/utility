// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Font data record}.
 *
 * @param family        the font family
 * @param size          the font size in points
 * @param bold          if text should be displayed in bold letters
 * @param italic        if text should be displayed in italics
 * @param underline     if text should be displayed underlined
 * @param strikeThrough if text should be displayed strike-through
 */
public record FontData(
        String family,
        float size,
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

    public FontData {
        assert !family.isEmpty() : "family is the empty string";
        assert size >= 0 : "size is negative";
        assert ascent >= 0 : "ascent is negative";
        assert descent <= 0 : "descent is positive";
        assert height >= ascent : "inconsistent height";
        assert spaceWidth > 0 : "space width must be positive";
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
                && b.family.equals(a.family);
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
        deltaHelper(f1, f2, FontData::family, fd::setFamily);
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
     * @param o1     first object
     * @param o2     second object
     * @param getter attribute getter
     * @param setter attribute setter
     * @param <T>    object type
     * @param <U>    attribute type
     */
    static <T, U> void deltaHelper(@Nullable T o1, @Nullable T o2, Function<T, U> getter, Consumer<? super U> setter) {
        U v1 = o1 == null ? null : getter.apply(o1);
        U v2 = o2 == null ? null : getter.apply(o2);
        if (!Objects.equals(v1, v2) && v2 != null) {
            setter.accept(v2);
        }
    }

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
}
