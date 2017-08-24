/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.text;

import java.awt.font.FontRenderContext;

import com.dua3.utility.Color;

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

    /**
     * Construct a new {@code GenericFont}.
     */
    public Font() {
        this("Helvetica",10f,Color.BLACK,false,false,false,false);
    }

    /**
     * Construct a new {@code GenerivFont}.
     *
     * @param family
     *            the font family
     * @param size
     *            the font size in points
     * @param color
     *            the color to use for text
     * @param bold
     *            if text should be displayed in bold lettters
     * @param italic
     *            if text should be displayed in italics
     * @param underlined
     *            if text should be displayed underlined
     * @param strikeThrough
     *            if text should be displayed strike-through
     */
    public Font(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
            boolean strikeThrough) {
        this.color = color;
        this.size = size;
        this.family = family;
        this.bold = bold;
        this.italic = italic;
        this.underline = underlined;
        this.strikeThrough = strikeThrough;
        this.awtFont = getAwtFont(family, size, color, bold, italic, underlined, strikeThrough);
        this.awtFontRenderContext = new FontRenderContext(awtFont.getTransform(), false, true);
    }
    /**
     * A mutable class holding font attributes to help creating immutable font
     * instances.
     */
    public static class FontDef {
        /**
         * Create FontDef instance with only the color attribute set.
         *
         * @param col
         *            the color
         * @return new FontDef instance
         */
        public static FontDef color(Color col) {
            FontDef fd = new FontDef();
            fd.setColor(col);
            return fd;
        }

        /**
         * Create FontDef instance with only the font family attribute set.
         *
         * @param family
         *            the font family
         * @return new FontDef instance
         */
        public static FontDef family(String family) {
            FontDef fd = new FontDef();
            fd.setFamily(family);
            return fd;
        }

        /**
         * Create FontDef instance with only the font size set.
         *
         * @param size
         *            the font size in points
         * @return new FontDef instance
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
         *            the bold to set
         */
        public void setBold(Boolean bold) {
            this.bold = bold;
        }

        /**
         * @param color
         *            the color to set
         */
        public void setColor(Color color) {
            this.color = color;
        }

        /**
         * @param family
         *            the family to set
         */
        public void setFamily(String family) {
            this.family = family;
        }

        /**
         * @param italic
         *            the italic to set
         */
        public void setItalic(Boolean italic) {
            this.italic = italic;
        }

        /**
         * @param size
         *            the size in points to set
         */
        public void setSize(Float size) {
            this.size = size;
        }

        /**
         * @param strikeThrough
         *            the strikeThrough to set
         */
        public void setStrikeThrough(Boolean strikeThrough) {
            this.strikeThrough = strikeThrough;
        }

        /**
         * @param underline
         *            the underline to set
         */
        public void setUnderline(Boolean underline) {
            this.underline = underline;
        }
    }

    protected final java.awt.Font awtFont;
    protected final java.awt.font.FontRenderContext awtFontRenderContext;

    private static java.awt.Font getAwtFont(String family, float size, Color color, boolean bold, boolean italic, boolean underlined,
        boolean strikeThrough) {
      int style = (bold ? java.awt.Font.BOLD : 0) | (italic ? java.awt.Font.ITALIC : 0);
      return new java.awt.Font(family, style, Math.round(size));
    }

    /**
     * Derive font.
     * <p>
     * A new font based on this font is returned. The attributes defined
     * {@code fd} are applied to the new font. If an attribute in {@code fd} is
     * not set, the attribute is copied from this font.
     * </p>
     *
     * @param fd
     *            the {@link FontDef} describing the attributes to set
     * @return new {@link Font} instance
     */
    public Font deriveFont(FontDef fd) {
        String fontFamily = fd.getFamily() != null ? fd.getFamily() : this.getFamily();
        float fontSize = fd.getSize() != null ? fd.getSize() : this.getSizeInPoints();
        Color fontColor = fd.getColor() != null ? fd.getColor() : this.getColor();
        boolean fontBold = fd.getBold() != null ? fd.getBold() : this.isBold();
        boolean fontItalic = fd.getItalic() != null ? fd.getItalic() : this.isItalic();
        boolean fontUnderlined = fd.getUnderline() != null ? fd.getUnderline() : this.isUnderlined();
        boolean fontStrikeThrough = fd.getStrikeThrough() != null ? fd.getStrikeThrough() : this.isStrikeThrough();

        return new Font(fontFamily, fontSize, fontColor, fontBold, fontItalic, fontUnderlined,
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
     */
    public boolean isUnderlined() {
        return underline;
    }

    /**
     * Get a description of the font.
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
        if (isUnderlined()) {
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

    /**
     * Get the corresponding AWT font instance.
     * @return instance of java.awt.Font for this font
     */
    public java.awt.Font toAwtFont() {
        return awtFont;
    }

    /**
     * Calculate width of text.
     * @param text the text
     * @return width in points
     */
    public float getTextWidth(String text) {
        return (float) awtFont.getStringBounds(text, awtFontRenderContext).getWidth();
    }

    @Override
    public String toString() {
        return fontspec();
    }

    @Override
    public int hashCode() {
        // java.awt.Font caches its hashcode. no use trying to be smarter than that
        return toAwtFont().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // important: compare the actual classes using getClass()
        return obj!=null
                && obj.getClass()==getClass()
                && toAwtFont().equals(((Font)obj).toAwtFont())
                && getColor().equals(((Font)obj).getColor());
    }
}
