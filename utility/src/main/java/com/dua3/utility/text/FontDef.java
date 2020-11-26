package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A mutable class holding font attributes to help creating immutable font
 * instances.
 */
public final class FontDef {
    /**
     * Create FontDef instance with only the color attribute set.
     *
     * @param col the color
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
     * @param family the font family
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
     * @param size the font size in points
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
     * Parse fontspec.
     * @param fontspec the fontspec
     * @return FonrDef instance matching fontspec 
     */
    public static FontDef parseFontspec(String fontspec) {
        String[] parts = fontspec.split("-");

        FontDef fd = new FontDef();

        // font-family must come first
        fd.setFamily(parts[0]);

        // check remaining parts
        for(int i=1;i<parts.length;i++) {
            String s = parts[i];
            // check for text-decoration
            switch (s) {
                case "bold":
                    fd.setBold(true);
                    break;
                case "italic":
                    fd.setItalic(true);
                    break;
                case "underline":
                    fd.setUnderline(true);
                    break;
                case "strikethrough":
                    fd.setStrikeThrough(true);
                    break;
                default:
                    // check for font size
                    if (s.matches("\\d+(\\.\\d*)?")) {
                        fd.setSize(Float.parseFloat(s));
                        break;
                    }
        
                    // check for color
                    fd.setColor(Color.valueOf(s));
                    break;
            }
        }

        return fd;
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
     * @param bold the bold to set
     */
    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @param family the family to set
     */
    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * @param italic the italic to set
     */
    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    /**
     * @param size the size in points to set
     */
    public void setSize(Float size) {
        this.size = size;
    }

    /**
     * @param strikeThrough the strikeThrough to set
     */
    public void setStrikeThrough(Boolean strikeThrough) {
        this.strikeThrough = strikeThrough;
    }

    /**
     * @param underline the underline to set
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
     *
     * @return fontstyle definition
     */
    public String getCssStyle() {
        boolean isUnderline = underline != null && underline;
        boolean isStrikeThrough = strikeThrough != null && strikeThrough;
        return (color == null ? "" : "color: " + color.toString() + ";") +
               (size == null ? "" : "font-size: " + size + "pt;") +
               (family == null ? "" : "font-family: " + family + ";") +
               (bold == null ? "" : "font-weight: " + (bold ? "bold" : "normal") + ";") +
               (italic == null ? "" : "font-style: " + (italic ? "italic" : "normal") + ";") +
               (isStrikeThrough || isUnderline
                       ? "text-decoration:" +
                         (isUnderline ? " underline" : "") +
                         (isStrikeThrough ? " line-through" : "") +
                         ";"
                       : "");
    }
    
    /**
     * Test if all attributes defined by this instance match those of the given {@link Font}.
     * @param font the {@link Font} to test
     * @return true, if the font's attributes match all attributes defined by this instance
     */
    public boolean matches(Font font) {
        return nullOrEquals(color, font.getColor())
               && nullOrEquals(size, font.getSizeInPoints())
               && nullOrEquals(family, font.getFamily())
               && nullOrEquals(bold, font.isBold())
               && nullOrEquals(italic, font.isItalic())
               && nullOrEquals(underline, font.isUnderline())
               && nullOrEquals(strikeThrough, font.isStrikeThrough());
    }

    private static boolean nullOrEquals(Object a, Object b) {
        return a==null || b==null || a.equals(b);
    }

    private static void textAttributesHelper(List<Pair<String, ?>> attributes, String key, Object value) {
        if (value != null) {
            attributes.add(Pair.of(key, value));
        }
    }

    private String textDecorationAttribute() {
        if (underline == null && strikeThrough == null) {
            return null;
        }

        int idx = (underline != null && underline ? 1 : 0) + (strikeThrough != null && strikeThrough ? 2 : 0);
        return Font.TEXT_DECORATION_VALUES[idx];
    }

    public TextAttributes getTextAttributes() {
        List<Pair<String, ?>> attributes = new LinkedList<>();
        textAttributesHelper(attributes, TextAttributes.COLOR, color);
        textAttributesHelper(attributes, TextAttributes.FONT_SIZE, size);
        textAttributesHelper(attributes, TextAttributes.FONT_FAMILY, family);

        String weight = bold == null ? null : bold ? TextAttributes.FONT_WEIGHT_VALUE_BOLD : TextAttributes.FONT_WEIGHT_VALUE_NORMAL;
        textAttributesHelper(attributes, TextAttributes.FONT_WEIGHT, weight);

        String style = italic == null ? null : italic ? TextAttributes.FONT_STYLE_VALUE_ITALIC : TextAttributes.FONT_STYLE_VALUE_NORMAL;
        textAttributesHelper(attributes, TextAttributes.FONT_STYLE, style);

        String decoration = textDecorationAttribute();
        textAttributesHelper(attributes, TextAttributes.TEXT_DECORATION, decoration);

        return TextAttributes.of(attributes);
    }
}
