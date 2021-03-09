package com.dua3.utility.text;

import com.dua3.utility.data.Color;

import java.util.Objects;
import java.util.function.Consumer;

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
        return (color == null ? "" : "color: " + color + ";") +
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

    /**
     * Update this FontDef with the non-null values of another FontDef instance.
     * @param delta the FontDef containing the values to apply
     */
    public void merge(FontDef delta) {
        if (delta.color != null) this.color = delta.color;
        if (delta.size != null) this.size = delta.size;
        if (delta.family != null) this.family = delta.family;
        if (delta.bold != null) this.bold = delta.bold;
        if (delta.italic != null) this.italic = delta.italic;
        if (delta.underline != null) this.underline = delta.underline;
        if (delta.strikeThrough != null) this.strikeThrough = delta.strikeThrough;
    }

    // a little helper for the consumeIfDefined... methods
    private static <T> boolean consumeIfDefined(T v, Consumer<T> c) {
        boolean run = v != null;
        if (run) {
            c.accept(v);
        }
        return run;
    }
    
    /**
     * Run action if a value for the color property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifColorDefined(Consumer<Color> c) {
        return consumeIfDefined(color, c);
    }

    /**
     * Run action if a value for the size property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifSizeDefined(Consumer<Float> c) {
        return consumeIfDefined(size, c);
    }

    /**
     * Run action if a value for the family property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifFamilyDefined(Consumer<String> c) {
        return consumeIfDefined(family, c);
    }

    /**
     * Run action if a value for the bold property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifBoldDefined(Consumer<Boolean> c) {
        return consumeIfDefined(bold, c);
    }

    /**
     * Run action if a value for the italic property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifItalicDefined(Consumer<Boolean> c) {
        return consumeIfDefined(italic, c);
    }

    /**
     * Run action if a value for the underline property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifUnderlineDefined(Consumer<Boolean> c) {
        return consumeIfDefined(underline, c);
    }

    /**
     * Run action if a value for the strike-through property is defined.
     * @param c consumer to run if the attribute value is defined. It is called with the attribute value as argument
     * @return true, if the action was run
     */
    public boolean ifStrikeThroughDefined(Consumer<Boolean> c) {
        return consumeIfDefined(strikeThrough, c);
    }

    /**
     * Test if this instance holds no data.
     * @return true, if none of the attributes is set
     */
    public boolean isEmpty() {
        return    color==null 
               && size==null 
               && family==null 
               && bold==null 
               && italic==null 
               && underline==null 
               && strikeThrough==null;
    }
}
