package com.dua3.utility.text;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;

public enum MarkDownStyle {
    BLOCK_QUOTE,
    BULLET_LIST,
    CODE,
    DOCUMENT,
    EMPHASIS,
    FENCED_CODE_BLOCK,
    HARD_LINE_BREAK,
    HEADING,
    THEMATIC_BREAK,
    HTML_BLOCK,
    HTML_INLINE,
    IMAGE,
    INDENTED_CODE_BLOCK,
    LINK,
    LIST_ITEM,
    ORDERED_LIST,
    PARAGRAPH,
    SOFT_LINE_BREAK,
    STRONG_EMPHASIS,
    TEXT,
    CUSTOM_BLOCK,
    CUSTOM_NODE;

    // Attributes
    public static final String ATTR_ID = "id";
    public static final String ATTR_HEADING_LEVEL = "level";
    public static final String ATTR_IMAGE_SRC = "src";
    public static final String ATTR_IMAGE_TITLE = "title";
    public static final String ATTR_IMAGE_ALT = "alt";
    public static final String ATTR_LINK_HREF = "href";
    public static final String ATTR_LINK_TITLE = "title";
    public static final String ATTR_LINK_EXTERN = "extern";

    // styles definitions to use when converting markdown source to StyledDocument
    private static final double HEADER_SIZES[] = { 2.0, 1.5, 1.17, 1.0, 0.83, 0.75 };
    public static Map<String, Function<Style, TextAttributes>> defaultStyles(float baseFontSizeInPt) {

        IntFunction<String> getFontSizeForHeading = level -> {
            double f = level >0&&level<=HEADER_SIZES.length ? HEADER_SIZES[level-1] : 1.0f;
            double pt = f*baseFontSizeInPt;
            return String.format(Locale.ROOT, "%.2fpt", pt);
        };
        return defaultStyles(getFontSizeForHeading);
    }

    private static Map<String, Function<Style,TextAttributes>> defaultStyles(IntFunction<String> getFontSizeForHeading) {
        Map<String, Function<Style,TextAttributes>> m = new HashMap<>();

        m.put(MarkDownStyle.BLOCK_QUOTE.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
                ));
        m.put(MarkDownStyle.BULLET_LIST.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
                ));
        m.put(MarkDownStyle.CODE.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE)
                ));
        m.put(MarkDownStyle.DOCUMENT.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.EMPHASIS.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.FONT_STYLE, TextAttributes.FONT_STYLE_VALUE_ITALIC)
                ));
        m.put(MarkDownStyle.FENCED_CODE_BLOCK.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE),
                Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
                ));
        m.put(MarkDownStyle.HARD_LINE_BREAK.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.HEADING.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.FONT_SIZE, getFontSizeForHeading.apply((Integer)attr.getOrDefault(MarkDownStyle.ATTR_HEADING_LEVEL, 1))),
                Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD)
                ));
        m.put(MarkDownStyle.THEMATIC_BREAK.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.HTML_BLOCK.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.HTML_INLINE.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.IMAGE.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.INDENTED_CODE_BLOCK.name(),
                attr -> TextAttributes.of(
                        Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE),
                        Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
                        ));
        m.put(MarkDownStyle.LINK.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.COLOR, Color.DARKBLUE.toString()),
                Pair.of(TextAttributes.TEXT_DECORATION, TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE)
                ));
        m.put(MarkDownStyle.LIST_ITEM.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.ORDERED_LIST.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
                ));
        m.put(MarkDownStyle.PARAGRAPH.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.SOFT_LINE_BREAK.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.STRONG_EMPHASIS.name(), attr -> TextAttributes.of(
                Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD)
                ));
        m.put(MarkDownStyle.TEXT.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.CUSTOM_BLOCK.name(), attr -> TextAttributes.of(
                ));
        m.put(MarkDownStyle.CUSTOM_NODE.name(), attr -> TextAttributes.of(
                ));

        return m;
    }

}
