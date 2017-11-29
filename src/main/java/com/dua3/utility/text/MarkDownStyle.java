package com.dua3.utility.text;

import java.util.Optional;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;

public enum MarkDownStyle {
    BLOCK_QUOTE(
            Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
            ),
    BULLET_LIST(
            Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
            ),
    CODE(
            Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE)
            ),
    DOCUMENT(
            ),
    EMPHASIS(
            Pair.of(TextAttributes.FONT_STYLE, TextAttributes.FONT_STYLE_VALUE_ITALIC)
            ),
    FENCED_CODE_BLOCK(
            Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE),
            Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
            ),
    HARD_LINE_BREAK,
    HEADING(
            Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD)
            ),
    THEMATIC_BREAK,
    HTML_BLOCK,
    HTML_INLINE,
    IMAGE,
    INDENTED_CODE_BLOCK(
            Pair.of(TextAttributes.FONT_FAMILY, TextAttributes.FONT_FAMILY_VALUE_MONOSPACE),
            Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
            ),
    LINK(
            Pair.of(TextAttributes.COLOR, Color.DARKBLUE.toString()),
            Pair.of(TextAttributes.TEXT_DECORATION, TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE)
            ),
    LIST_ITEM,
    ORDERED_LIST(
            Pair.of(TextAttributes.TEXT_INDENT_LEFT, TextAttributes.TEXT_INDENT_LEFT_VALUE_1)
            ),
    PARAGRAPH,
    SOFT_LINE_BREAK,
    STRONG_EMPHASIS(
            Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD)
            ),
    TEXT,
    CUSTOM_BLOCK,
    CUSTOM_NODE;

    // Attributes
    public static final String ATTR_ID = "id";
    public static final String ATTR_HEADING_LEVEL = "level";
    public static final String ATTR_IMAGE_SRC = "src";
    public static final String ATTR_IMAGE_TITLE = "image_title";
    public static final String ATTR_IMAGE_ALT = "alt";
    public static final String ATTR_LINK_HREF = "href";
    public static final String ATTR_LINK_TITLE = "link_title";
    public static final String ATTR_LINK_EXTERN = "extern";
    public static final String ATTR_LIST_ITEM_TYPE = "LIST_ITEM_TYPE";
    public static final String ATTR_LIST_ITEM_NR = "LIST_ITEM_NR";
    public static final String ATTR_LIST_ITEM_PREFIX = "LIST_ITEM_PREFIX";

    /** Font sizes for headers as recommended by w3.org. */
    public static double getFontScaleForHeadingLevel(int level) {
        switch (level) {
        case 1:
            return 2;
        case 2:
            return 1.5;
        case 3:
            return 1.17;
        case 4:
            return 1;
        case 5:
            return 0.83;
        case 6:
            return 0.75;
        default:
            return 0.75;
        }
    }

    private final TextAttributes attributes;

    @SafeVarargs
    private MarkDownStyle(Pair<String,Object>... attributes) {
        this.attributes = TextAttributes.of(attributes);
    }

    public TextAttributes textAttributes() {
        return attributes;
    }

    public static Optional<MarkDownStyle> lookup(String name) {
        for (MarkDownStyle mds: values()) {
            if (mds.name().equals(name)) {
                return Optional.of(mds);
            }
        }
        return Optional.empty();
    }

    public static TextAttributes getAttributes(Style style) {
        return lookup(style.name()).map(MarkDownStyle::textAttributes).orElse(TextAttributes.none());
    }

}
