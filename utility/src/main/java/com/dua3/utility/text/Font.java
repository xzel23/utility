// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.ObjectCache;
import com.dua3.utility.data.Color;

import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Generic font class.
 */
public class Font {

    private static final ObjectCache fontCache = new ObjectCache();
    private final FontData fontData;
    private final Color color;
    private final int hash;

    /**
     * Constructor to create a Font instance with given font data and color.
     *
     * @param fontData The font data specifying the font's typeface, size, and style.
     * @param color The color of the font. It defines the text color associated with the font.
     */
    protected Font(FontData fontData, Color color) {
        this.fontData = fontData;
        this.color = color;
        this.hash = fontData.hashCode() + 17 * color.hashCode();
    }

    /**
     * Retrieves a Font instance from the cache based on the specified font data and color.
     *
     * @param fontData The font data specifying the font's typeface, size, and style.
     * @param color The color of the font, defining the text color associated with it.
     * @return A Font instance matching the provided font data and color from the cache.
     */
    public static Font getFont(FontData fontData, Color color) {
        return fontCache.get(new Font(fontData, color));
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
        return a.hash == b.hash && FontData.similar(a.fontData, b.fontData) && a.color.equals(b.color);
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
    public final Color getColor() {
        return color;
    }

    /**
     * Get font family.
     *
     * @return the font family as {@code String}.
     */
    public final String getFamily() {
        return fontData.families().getFirst();
    }

    /**
     * Get list of font families.
     * @return the list of font families
     */
    public final List<String> getFamilies() {
        return fontData.families();
    }

    /**
     * Get the type (monospaced or proportional) for this font.
     *
     * @return the spacing as a {@link FontType} constant
     */
    public FontType getType() {
        return fontData.monospaced() ? FontType.MONOSPACED : FontType.PROPORTIONAL;
    }

    /**
     * Get font size.
     *
     * @return the font size in points.
     */
    public final float getSizeInPoints() {
        return fontData.size();
    }

    /**
     * Get bold property.
     *
     * @return true if font is bold.
     */
    public final boolean isBold() {
        return fontData.bold();
    }

    /**
     * Get italic property.
     *
     * @return true if font is italic.
     */
    public final boolean isItalic() {
        return fontData.italic();
    }

    /**
     * Get strike-through property.
     *
     * @return true if font is strike-through.
     */
    public final boolean isStrikeThrough() {
        return fontData.strikeThrough();
    }

    /**
     * Get underline property.
     *
     * @return true if font is underline.
     */
    public final boolean isUnderline() {
        return fontData.underline();
    }

    @Override
    public String toString() {
        return fontspec();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Font other = (Font) obj;
        return hash == other.hash && fontData.equals(other.fontData) && color.equals(other.color);
    }

    /**
     * Get CSS compatible fontstyle definition.
     *
     * @return fontstyle definition
     */
    public final String getCssStyle() {
        return fontData.cssStyle() + " color: " + color.toCss() + ";";
    }

    /**
     * Get a description of the font.
     *
     * @return font description
     */
    public final String fontspec() {
        return fontData.fontspec() + "-" + color.toCss();
    }

    /**
     * Get a {@link FontDef} instance with all fields set according to this font.
     *
     * @return FontDef instance describing this font
     */
    public final FontDef toFontDef() {
        FontDef fontDef = fontData.fontDef();
        fontDef.setColor(getColor());
        return fontDef;
    }

    /**
     * Get the ascent of this font.
     *
     * @return the ascent of this font
     */
    public final double getAscent() {
        return fontData.ascent();
    }

    /**
     * Get the descent of this font.
     *
     * @return the descent of this font
     */
    public final double getDescent() {
        return fontData.descent();
    }

    /**
     * Get the height of this font.
     *
     * @return the height of this font
     */
    public final double getHeight() {
        return fontData.height();
    }

    /**
     * Get the space width of this font.
     *
     * @return the space width of this font
     */
    public final double getSpaceWidth() {
        return fontData.spaceWidth();
    }

    /**
     * Return a font derived from this font by applying the given size.
     *
     * @param size the new font size
     * @return a copy of this font with the requested size, or this font, if the size matches
     */
    public Font withSize(float size) {
        return size == getSizeInPoints() ? this : FontUtil.getInstance().deriveFont(this, FontDef.size(size));
    }

    /**
     * Scales the font by a given factor.
     *
     * @param s the scaling factor
     * @return a new Font instance that is scaled by the given factor, or this font if the scaling factor is 1
     */
    public Font scaled(float s) {
        return s == 1 ? this : FontUtil.getInstance().deriveFont(this, FontDef.size(s * getSizeInPoints()));
    }

    /**
     * Return a font derived from this font by applying the given value for the bold attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the bold attribute set to the requested value, or this font if values match
     */
    public Font withBold(boolean flag) {
        return flag == isBold() ? this : FontUtil.getInstance().deriveFont(this, FontDef.bold(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the italic attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the italic attribute set to the requested value, or this font if values match
     */
    public Font withItalic(boolean flag) {
        return flag == isItalic() ? this : FontUtil.getInstance().deriveFont(this, FontDef.italic(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the underline attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the underline attribute set to the requested value, or this font if values match
     */
    public Font withUnderline(boolean flag) {
        return flag == isUnderline() ? this : FontUtil.getInstance().deriveFont(this, FontDef.underline(flag));
    }

    /**
     * Return a font derived from this font by applying the given value for the strike-through attribute.
     *
     * @param flag the value to use
     * @return a copy of this font with the strike-through attribute set to the requested value, or this font if values match
     */
    public Font withStrikeThrough(boolean flag) {
        return flag == isStrikeThrough() ? this : FontUtil.getInstance().deriveFont(this, FontDef.strikeThrough(flag));
    }

    /**
     * Return a font derived from this font by replacing the family with the given value.
     *
     * @param family the value to use
     * @return a copy of this font with the family set to the requested value, or this font if values match
     */
    public Font withFamily(String family) {
        return family.equals(this.getFamily()) ? this : FontUtil.getInstance().deriveFont(this, FontDef.family(family));
    }

    /**
     * Return a font derived from this font by replacing the color with the given value.
     *
     * @param color the value to use
     * @return a copy of this font with the color set to the requested value, or this font if values match
     */
    public Font withColor(Color color) {
        return color.equals(this.getColor()) ? this : FontUtil.getInstance().deriveFont(this, FontDef.color(color));
    }

    /**
     * Retrieves the font data associated with this font instance.
     *
     * @return the FontData object containing information about the font's typeface, size, and style.
     */
    public final FontData getFontData() {
        return fontData;
    }
}
