// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Interface describing fonts used in workbooks.
 *
 * @author axel
 */
public class Font {

    private final Color color;
    private final float size;
    private final String family;
    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final boolean strikeThrough;

    private String fontspec = null;
    private int hash = 0;

    /**
     * Construct a new {@code Font}.
     */
    public Font() {
        this("Helvetica", 10.0f, Color.BLACK, false, false, false, false);
    }

    /**
     * Construct a new {@code Font} from a fontspec string.
     * 
     * @param fontspec the fontspec
     */
    public Font(String fontspec) {
        this(new Font(), FontDef.parseFontspec(fontspec));
    }

    /**
     * Construct a new {@code Font}.
     *
     * @param family
     *                      the font family
     * @param size
     *                      the font size in points
     * @param color
     *                      the color to use for text
     * @param bold
     *                      if text should be displayed in bold letters
     * @param italic
     *                      if text should be displayed in italics
     * @param underline
     *                      if text should be displayed underlined
     * @param strikeThrough
     *                      if text should be displayed strike-through
     */
    public Font(String family, float size, Color color, boolean bold, boolean italic, boolean underline,
            boolean strikeThrough) {
        this.size = size;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikeThrough = strikeThrough;
        this.family = family;
        this.color = color;
    }

    protected Font(Font baseFont, FontDef fd) {
        this(
            fd.getFamily() != null ? fd.getFamily() : baseFont.getFamily(),
            fd.getSize() != null ? fd.getSize() : baseFont.getSizeInPoints(),
            fd.getColor() != null ? fd.getColor() : baseFont.getColor(),
            fd.getBold() != null ? fd.getBold() : baseFont.isBold(),
            fd.getItalic() != null ? fd.getItalic() : baseFont.isItalic(),
            fd.getUnderline() != null ? fd.getUnderline() : baseFont.isUnderline(),
            fd.getStrikeThrough() != null ? fd.getStrikeThrough() : baseFont.isStrikeThrough()
        );
    }

    /**
     * Derive font.
     * <p>
     * A new font based on this font is returned. The attributes defined
     * {@code fd} are applied to the new font. If an attribute in {@code fd} is
     * not set, the attribute is copied from this font.
     * </p>
     *
     * @param  fd
     *            the {@link FontDef} describing the attributes to set
     * @return    new Font instance
     */
    public Font deriveFont(FontDef fd) {
        return new Font(this, fd);
    }

    /**
     * Get text color.
     *
     * @return the text color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Get font family.
     *
     * @return the font family as {@code String}.
     */
    public String getFamily() {
        return family;
    }

    /**
     * Get font size.
     *
     * @return the font size in points.
     */
    public float getSizeInPoints() {
        return size;
    }

    /**
     * Get bold property.
     *
     * @return true if font is bold.
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Get italic property.
     *
     * @return true if font is italic.
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Get strike-through property.
     *
     * @return true if font is strike-through.
     */
    public boolean isStrikeThrough() {
        return strikeThrough;
    }

    /**
     * Get underline property.
     *
     * @return true if font is underline.
     */
    public boolean isUnderline() {
        return underline;
    }

    /**
     * Get a description of the font.
     *
     * @return font description
     */
    public String fontspec() {
        if (fontspec==null) {
            StringBuilder sb = new StringBuilder(32);

            sb.append(getFamily());

            if (isBold()) {
                sb.append('-').append("bold");
            }
            if (isItalic()) {
                sb.append('-').append("italic");
            }
            if (isUnderline()) {
                sb.append('-').append("underline");
            }
            if (isStrikeThrough()) {
                sb.append('-').append("strikethrough");
            }
            sb.append('-');
            sb.append(getSizeInPoints());
            sb.append('-');
            sb.append(getColor().toCss());
            
            fontspec = sb.toString();
        }
        
        return fontspec;
    }

    @Override
    public String toString() {
        return fontspec();
    }

    @SuppressWarnings("boxing")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            hash = h = Objects.hash(family, size, bold, italic, underline, strikeThrough, color);
        }
        return h;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass() || obj.hashCode() != this.hashCode()) {
            return false;
        }

        return similar(this, (Font) obj);
    }

    /**
     * Test if two fonts are similar. This method is provided to be used when inheriting fonts because equals
     * also tests both instances to be of the exact same class.
     * @param a the first font
     * @param b the second font
     * @return true, if a and b have the same attributes
     */
    public static boolean similar(Font a, Font b) {
        return b.size == a.size
                && b.bold == a.bold
                && b.italic == a.italic
                && b.underline == a.underline
                && b.strikeThrough == a.strikeThrough
                && b.family.equals(a.family)
                && b.color.equals(a.color);
    }

    /**
     * Get CSS compatible fontstyle definition.
     * @return fontstyle definition
     */
    public String getCssStyle() {
        return String.format(Locale.ROOT, "color: %s; font-size: %spt; font-family: %s; font-weight: %s; font-style: %s;%s",
               color,
               size, // use "%s" for size to avoid unnecessary zeros after decimal point
               family,
               bold ? "bold" : "normal",
               italic ? "italic" : "normal",
               strikeThrough || underline
                   ? "text-decoration:" +
                     (underline ? " underline" : "") +
                     (strikeThrough ? " line-through": "") +
                     ";"
                   : ""
        );
    }

    /**
     * Compare values and store changes.
     * @param o1 first object
     * @param o2 second object
     * @param getter attribute getter
     * @param setter attribute setter
     * @param <T> object type
     * @param <U> attribute type
     */
    private static <T,U> void deltaHelper(T o1, T o2, Function<T,U> getter, Consumer<U> setter) {
        U v1 = getter.apply(o1);
        U v2 = getter.apply(o2);
        if (!Objects.equals(v1, v2)) {
            setter.accept(v2);
        }
    }

    /**
     * Determine differences between two fonts.
     * @param f1 the first font
     * @param f2 the second font
     * @return a {@link FontDef} instance that defines the changed values
     */
    public static FontDef delta(Font f1, Font f2) {
        FontDef fd = new FontDef();

        deltaHelper(f1, f2, Font::getFamily, fd::setFamily);
        deltaHelper(f1, f2, Font::getSizeInPoints, fd::setSize);
        deltaHelper(f1, f2, Font::isBold, fd::setBold);
        deltaHelper(f1, f2, Font::isItalic, fd::setItalic);
        deltaHelper(f1, f2, Font::isUnderline, fd::setUnderline);
        deltaHelper(f1, f2, Font::isStrikeThrough, fd::setStrikeThrough);
        deltaHelper(f1, f2, Font::getColor, fd::setColor);

        return fd;
    }

    /**
     * Get a {@link FontDef} instance with all fields set according to this font.
     * @return FontDef instance describing this font
     */
    public FontDef toFontDef() {
        FontDef fd = new FontDef();

        fd.setFamily(getFamily());
        fd.setSize(getSizeInPoints());
        fd.setBold(isBold());
        fd.setItalic(isItalic());
        fd.setUnderline(isUnderline());
        fd.setStrikeThrough(isStrikeThrough());
        fd.setColor(getColor());

        return fd;
    }
    
    /**
     * Get width of a space character in this font.
     * @return width of a single space character in this font
     */
    public double getSpaceWidth() {
        if (spaceWidth<0) {
            spaceWidth = TextUtil.getTextWidth(" ", this);
        }
        return spaceWidth;
    }
    private double spaceWidth=-1;
    
}
