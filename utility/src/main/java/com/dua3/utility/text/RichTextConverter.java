package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Interface for {@link RichText} converters.
 *
 * @param <T> the conversion target type
 */
@FunctionalInterface
public interface RichTextConverter<T> {
    
    /**
     * Add font properties of to a properties map.
     *
     * @param props the property map
     * @param font  the font
     */
    static void putFontProperties(Map<? super String, @Nullable Object> props, Font font) {
        props.put(Style.FONT_FAMILIES, font.getFamilies());
        String fontClass = determineFontClass(font);
        if (fontClass != null) {
            props.put(Style.FONT_CLASS, fontClass);
        }
        props.put(Style.FONT_SIZE, font.getSizeInPoints());
        props.put(Style.COLOR, font.getColor());
        props.put(Style.FONT_STYLE, font.isItalic() ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
        props.put(Style.FONT_WEIGHT, font.isBold() ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
        props.put(Style.TEXT_DECORATION_UNDERLINE, font.isUnderline() ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
        props.put(Style.TEXT_DECORATION_LINE_THROUGH, font.isStrikeThrough() ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
    }

    /**
     * Determines the CSS font class based on font type and family.
     *
     * @param font the font to analyze
     * @return the appropriate font class constant, or null if no specific class applies
     */
    private static @Nullable String determineFontClass(Font font) {
        if (font.getType() == FontType.MONOSPACED) {
            return Style.FONT_CLASS_VALUE_MONOSPACE;
        }

        String family = font.getFamily();
        if ("serif".equals(family)) {
            return Style.FONT_CLASS_VALUE_SERIF;
        }

        if ("sans-serif".equals(family)) {
            return Style.FONT_CLASS_VALUE_SANS_SERIF;
        }

        return null;
    }

    /**
     * Convert {@link RichText} to the target type.
     *
     * @param text the text to convert
     * @return conversion result
     */
    T convert(RichText text);

}
