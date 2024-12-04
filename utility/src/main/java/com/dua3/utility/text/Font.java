// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;

/**
 * Generic font class.
 */
public class Font {

    private final FontData fontData;
    private final Color color;

    public Font(FontData fontData, Color color) {
        this.fontData = fontData;
        this.color = color;
    }

    /**
     * Test if two fonts are similar. This method is provided to be used when inheriting fonts because equals
     * also tests both instances to be of the exact same class.
     *
     * @param a the first font
     * @param b the second font
     * @return true, if a and b have the same attributes
     */
    public static boolean similar(Font a, Font b) {
        return FontData.similar(a.fontData, b.fontData) && a.color.equals(b.color);
    }

    /**
     * Determine differences between two fonts.
     *
     * @param a the first font
     * @param b the second font
     * @return a {@link FontDef} instance that defines the changed values
     */
    public static FontDef delta(@Nullable Font a, @Nullable Font b) {
        FontDef delta = FontData.delta(a == null ? null : a.fontData, b == null ? null : b.fontData);
        FontData.deltaHelper(a, b, Font::getColor, delta::setColor);
        return delta;
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
        return fontData.family();
    }

    /**
     * Get font size.
     *
     * @return the font size in points.
     */
    public float getSizeInPoints() {
        return fontData.size();
    }

    /**
     * Get bold property.
     *
     * @return true if font is bold.
     */
    public boolean isBold() {
        return fontData.bold();
    }

    /**
     * Get italic property.
     *
     * @return true if font is italic.
     */
    public boolean isItalic() {
        return fontData.italic();
    }

    /**
     * Get strike-through property.
     *
     * @return true if font is strike-through.
     */
    public boolean isStrikeThrough() {
        return fontData.strikeThrough();
    }

    /**
     * Get underline property.
     *
     * @return true if font is underline.
     */
    public boolean isUnderline() {
        return fontData.underline();
    }

    /**
     * Get a description of the font.
     *
     * @return font description
     */
    public String fontspec() {
        return fontData.fontspec();
    }

    @Override
    public String toString() {
        return fontspec();
    }

    @Override
    public int hashCode() {
        return fontData.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj==null || obj.getClass() != getClass()) {
            return false;
        }
        Font other = (Font) obj;
        return fontData.equals(other.fontData) && color.equals(other.color);
    }

    /**
     * Get CSS compatible fontstyle definition.
     *
     * @return fontstyle definition
     */
    public String getCssStyle() {
        return fontData.cssStyle();
    }

    /**
     * Get a {@link FontDef} instance with all fields set according to this font.
     *
     * @return FontDef instance describing this font
     */
    public FontDef toFontDef() {
        FontDef fontDef = fontData.fontDef();
        fontDef.setColor(color);
        return fontDef;
    }

    /**
     * Get the ascent of this font.
     *
     * @return the ascent of this font
     */
    public double getAscent() {
        return fontData.ascent();
    }

    /**
     * Get the descent of this font.
     *
     * @return the descent of this font
     */
    public double getDescent() {
        return fontData.descent();
    }

    /**
     * Get the height of this font.
     *
     * @return the height of this font
     */
    public double getHeight() {
        return fontData.height();
    }

    /**
     * Get the space width of this font.
     *
     * @return the space width of this font
     */
    public double getSpaceWidth() {
        return fontData.spaceWidth();
    }

    /**
     * Return a font derived from this font by applying the given size.
     *
     * @param size the new font size
     * @return a copy of this font with the requested size, or this font, if the size matches
     */
    public Font withSize(float size) {
        return size == getSizeInPoints() ? this :FontUtil.getInstance().deriveFont(this, FontDef.size(size));
    }

    /**
     * Scales the font by a given factor.
     *
     * @param s the scaling factor
     * @return a new Font instance that is scaled by the given factor, or this font if the scaling factor is 1
     */
    public Font scaled(float s) {
        return s == 1 ? this :FontUtil.getInstance().deriveFont(this, FontDef.size(s * getSizeInPoints()));
    }

    /**
     * Return a font derived from this font by applying the given value for the bold attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the bold attribute set to the requested value, or this font if values match
     */
    public Font withBold(boolean flag) {
        return flag == isBold() ? this :FontUtil.getInstance().deriveFont(this, FontDef.bold(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the italic attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the italic attribute set to the requested value, or this font if values match
     */
    public Font withItalic(boolean flag) {
        return flag == isItalic() ? this :FontUtil.getInstance().deriveFont(this, FontDef.italic(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the underline attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the underline attribute set to the requested value, or this font if values match
     */
    public Font withUnderline(boolean flag) {
        return flag == isUnderline() ? this :FontUtil.getInstance().deriveFont(this, FontDef.underline(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the strike-through attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the strike-through attribute set to the requested value, or this font if values match
     */
    public Font withStrikeThrough(boolean flag) {
        return flag == isStrikeThrough() ? this :FontUtil.getInstance().deriveFont(this, FontDef.strikeThrough(flag));
    }

    /**
     * Return a font derived from this font by replacing the family with the given value.
     *
     * @param family the value to use
     * @return a copy of this font with the family set to the requested value, or this font if values match
     */
    public Font withFamily(String family) {
        return family.equals(this.getFamily()) ? this :FontUtil.getInstance().deriveFont(this, FontDef.family(family));
    }

    /**
     * Return a font derived from this font by replacing the color with the given value.
     *
     * @param color the value to use
     * @return a copy of this font with the color set to the requested value, or this font if values match
     */
    public Font withColor(Color color) {
        return color.equals(this.getColor()) ? this :FontUtil.getInstance().deriveFont(this, FontDef.color(color));
    }

    public FontData getFontData() {
        return fontData;
    }
}
