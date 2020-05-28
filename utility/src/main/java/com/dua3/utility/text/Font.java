// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Objects;

import com.dua3.utility.data.Color;

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
    private int hash = 0;

    /**
     * Construct a new {@code GenericFont}.
     */
    public Font() {
        this("Helvetica", 10.0f, Color.BLACK, false, false, false, false);
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
        this.color = color;
        this.size = size;
        this.family = family;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikeThrough = strikeThrough;
    }

    /**
     * A mutable class holding font attributes to help creating immutable font
     * instances.
     */
    public static class FontDef {
        /**
         * Create FontDef instance with only the color attribute set.
         *
         * @param  col
         *             the color
         * @return     new FontDef instance
         */
        public static FontDef color(Color col) {
            FontDef fd = new FontDef();
            fd.setColor(col);
            return fd;
        }

        /**
         * Create FontDef instance with only the font family attribute set.
         *
         * @param  family
         *                the font family
         * @return        new FontDef instance
         */
        public static FontDef family(String family) {
            FontDef fd = new FontDef();
            fd.setFamily(family);
            return fd;
        }

        /**
         * Create FontDef instance with only the font size set.
         *
         * @param  size
         *              the font size in points
         * @return      new FontDef instance
         */
        public static FontDef size(Float size) {
            FontDef fd = new FontDef();
            fd.setSize(size);
            return fd;
        }

        private Color color;
        private Float size;
        private String family;
        private Boolean bold;
        private Boolean italic;
        private Boolean underline;
        private Boolean strikeThrough;

        public FontDef() {
            // nop - everything being initialized to null is just fine
        }

        /**
         * @return the bold
         */
        public Boolean getBold() {
            return bold;
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * @return the family
         */
        public String getFamily() {
            return family;
        }

        /**
         * @return the italic
         */
        public Boolean getItalic() {
            return italic;
        }

        /**
         * @return the size in points
         */
        public Float getSize() {
            return size;
        }

        /**
         * @return the strikeThrough
         */
        public Boolean getStrikeThrough() {
            return strikeThrough;
        }

        /**
         * @return the underline
         */
        public Boolean getUnderline() {
            return underline;
        }

        /**
         * @param bold
         *             the bold to set
         */
        public void setBold(Boolean bold) {
            this.bold = bold;
        }

        /**
         * @param color
         *              the color to set
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * @param family
         *               the family to set
         */
        public void setFamily(String family) {
            this.family = family;
        }

        /**
         * @param italic
         *               the italic to set
         */
        public void setItalic(Boolean italic) {
            this.italic = italic;
        }

        /**
         * @param size
         *             the size in points to set
         */
        public void setSize(Float size) {
            this.size = size;
        }

        /**
         * @param strikeThrough
         *                      the strikeThrough to set
         */
        public void setStrikeThrough(Boolean strikeThrough) {
            this.strikeThrough = strikeThrough;
        }

        /**
         * @param underline
         *                  the underline to set
         */
        public void setUnderline(Boolean underline) {
            this.underline = underline;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FontDef fontDef = (FontDef) o;
            return Objects.equals(color, fontDef.color) &&
                   Objects.equals(size, fontDef.size) &&
                   Objects.equals(family, fontDef.family) &&
                   Objects.equals(bold, fontDef.bold) &&
                   Objects.equals(italic, fontDef.italic) &&
                   Objects.equals(underline, fontDef.underline) &&
                   Objects.equals(strikeThrough, fontDef.strikeThrough);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, size, family, bold, italic, underline, strikeThrough);
        }

        @Override
        public String toString() {
            return "FontDef{" +
                   getCssStyle() +
                   "}";
        }

        /**
         * Get CSS compatible fontstyle definition.
         * @return fontstyle definition
         */
        public String getCssStyle() {
            return (color == null ? "" : "color: " + color.toString() + ";") +
                   (size == null ? "" : "size: " + size + "pt;") +
                   (family == null ? "" : "font-family: " + family + ";") +
                   (bold == null ? "" : "font-weight: " + (bold ? "bold" : "normal") + ";") +
                   (italic == null ? "" : "font-style: " + (italic ? "italic" : "regular") + ";") +
                   (underline == null && strikeThrough == null
                           ? ""
                           : "text-decoration:" +
                             (underline != null && underline ? " underline" : "") +
                             (strikeThrough != null && strikeThrough ? " line-through" : "") +
                             ";");
        }
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
        String fontFamily = fd.getFamily() != null ? fd.getFamily() : this.getFamily();
        float fontSize = fd.getSize() != null ? fd.getSize() : this.getSizeInPoints();
        Color fontColor = fd.getColor() != null ? fd.getColor() : this.getColor();
        boolean fontBold = fd.getBold() != null ? fd.getBold() : this.isBold();
        boolean fontItalic = fd.getItalic() != null ? fd.getItalic() : this.isItalic();
        boolean fontUnderline = fd.getUnderline() != null ? fd.getUnderline() : this.isUnderline();
        boolean fontStrikeThrough = fd.getStrikeThrough() != null ? fd.getStrikeThrough() : this.isStrikeThrough();

        return new Font(fontFamily, fontSize, fontColor, fontBold, fontItalic, fontUnderline,
                fontStrikeThrough);
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
     * Get underlined property.
     *
     * @return true if font is underlined.
     * @deprecated use {@link #isUnderline()} instead
     */
    @Deprecated(forRemoval = true)
    public boolean isUnderlined() {
        return underline;
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
        sb.append(getColor());

        return sb.toString();
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

        Font other = (Font) obj;
        return other.family.equals(family)
                && other.size == size
                && other.bold == bold
                && other.italic == italic
                && other.underline == underline
                && other.strikeThrough == strikeThrough
                && other.color.equals(color);
    }
}
