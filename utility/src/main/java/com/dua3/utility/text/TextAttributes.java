// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

/**
 * A set of text attributes.
 */
public final class TextAttributes extends AbstractMap<String, Object> {

    /** 
     * The style class.
     * 
     * Since RichText can contain Style information for different media at the same time,
     * styles are distuingished by their 'class'. Think of it as a namespace for style.
     * For example, you could define a called "h1" with class "HTML", and another style
     * by the same name in the class "PDF" and chose the correct attributes when rendering.
     */
    public static final String STYLE_CLASS = "style-class";
    public static final String STYLE_CLASS_DEFAULT = "";
    
    /** 
     * The Style name.
     */
    public static final String STYLE_NAME = "style-name";
    
    /** property for the font. */
    public static final String FONT = "font";
    
    /** property name for the font family */
    public static final String FONT_TYPE = "font-type";
    public static final String FONT_TYPE_VALUE_SANS_SERIF = "sans-serif";
    public static final String FONT_TYPE_VALUE_SERIF = "serif";
    public static final String FONT_TYPE_VALUE_MONOSPACE = "monospace";

    /** property name for the font style */
    public static final String FONT_STYLE = "font-style";
    public static final String FONT_STYLE_VALUE_NORMAL = "normal";
    public static final String FONT_STYLE_VALUE_ITALIC = "italic";
    public static final String FONT_STYLE_VALUE_OBLIQUE = "oblique";

    /** property name for the font weight */
    public static final String FONT_SIZE = "font-size";

    /** property name for the font weight */
    public static final String FONT_SCALE = "font-scale";

    /** property name for the font weight */
    public static final String FONT_WEIGHT = "font-weight";
    public static final String FONT_WEIGHT_VALUE_BOLD = "bold";
    public static final String FONT_WEIGHT_VALUE_NORMAL = "normal";

    /** property name for the font variant */
    public static final String FONT_VARIANT = "font-variant";
    public static final String FONT_VARIANT_VALUE_NORMAL = "normal";

    /** property name for the underline text decoration */
    public static final String TEXT_DECORATION_UNDERLINE = "text-decoration-line-under";
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE = Boolean.FALSE;

    /** property name for the line-through text decoration */
    public static final String TEXT_DECORATION_LINE_THROUGH = "text-decoration-line-through";
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_LINE = Boolean.TRUE;
    public static final Boolean TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE = Boolean.FALSE;

    /** text indentation */
    public static final String TEXT_INDENT_LEFT = "indent-left";
    public static final String TEXT_INDENT_LEFT_VALUE_0 = "0";
    public static final String TEXT_INDENT_LEFT_VALUE_1 = "40";

    /** prefix */
    public static final String TEXT_PREFIX = "PREFIX";
    /** suffix */
    public static final String TEXT_SUFFIX = "SUFFIX";

    /** foreground color */
    public static final String COLOR = "color";
    /** background color */
    public static final String BACKGROUND_COLOR = "background-color";

    /** empty {@link TextAttributes} instance */
    private static final TextAttributes NONE = new TextAttributes(Collections.emptySet());

    // -- define some default styles
    public static final Style SANS_SERIF = Style.create("sans-serif", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_SANS_SERIF));
    public static final Style SERIF = Style.create("serif", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_SERIF));
    public static final Style MONOSPACE = Style.create("monospace", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_TYPE, TextAttributes.FONT_TYPE_VALUE_MONOSPACE));

    public static final Style BOLD = Style.create("bold", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));
    public static final Style NORMAL = Style.create("normal", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_NORMAL));

    public static final Style ITALIC = Style.create("italic", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_STYLE, TextAttributes.FONT_STYLE_VALUE_ITALIC));
    public static final Style REGULAR = Style.create("regular", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.FONT_STYLE, TextAttributes.FONT_STYLE_VALUE_NORMAL));
    
    public static final Style UNDERLINE = Style.create("underline", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.TEXT_DECORATION_UNDERLINE, TextAttributes.TEXT_DECORATION_UNDERLINE_VALUE_LINE));
    public static final Style NO_UNDERLINE = Style.create("no-underline", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.TEXT_DECORATION_UNDERLINE, TextAttributes.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE));
    
    public static final Style LINE_THROUGH = Style.create("line-through", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.TEXT_DECORATION_LINE_THROUGH, TextAttributes.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE));
    public static final Style NO_LINE_THROUGH = Style.create("no-line-through", STYLE_CLASS_DEFAULT, Pair.of(TextAttributes.TEXT_DECORATION_LINE_THROUGH, TextAttributes.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE));
    
    private static final Map<String, Object> DEFAULTS = LangUtil.map(
            Pair.of(FONT_TYPE, FONT_TYPE_VALUE_SANS_SERIF),
            Pair.of(FONT_STYLE, FONT_STYLE_VALUE_NORMAL),
            Pair.of(FONT_SIZE, "10pt"),
            Pair.of(FONT_SCALE, 1),
            Pair.of(FONT_WEIGHT, FONT_WEIGHT_VALUE_NORMAL),
            Pair.of(FONT_VARIANT, FONT_VARIANT_VALUE_NORMAL),
            Pair.of(TEXT_DECORATION_UNDERLINE, TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE),
            Pair.of(TEXT_DECORATION_LINE_THROUGH, TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE),
            Pair.of(TEXT_INDENT_LEFT, TEXT_INDENT_LEFT_VALUE_0),
            Pair.of(COLOR, Color.WHITE),
            Pair.of(BACKGROUND_COLOR, Color.BLACK));

    /**
     * Get default value for attribute.
     *
     * @param  attribute the attribute
     * @return           the default value for the attribute or {@code null} if not
     *                   set
     */
    public static Object getDefault(String attribute) {
        return DEFAULTS.get(attribute);
    }

    public static TextAttributes defaults() {
        return TextAttributes.of(DEFAULTS);
    }

    /**
     * The empty style instance.
     *
     * @return the empty style
     */
    public static TextAttributes none() {
        return NONE;
    }

    /**
     * Construct style with attributes.
     *
     * @param  entries
     *                 the attribute/value pairs to add
     * @return         the new style
     */
    @SafeVarargs
    public static TextAttributes of(Pair<String, ?>... entries) {
        return of(Arrays.asList(entries));
    }

    /**
     * Construct style with attributes.
     *
     * @param  entries
     *                 the attribute/value pairs to add
     * @return         the new style
     */
    public static TextAttributes of(Iterable<Pair<String, ?>> entries) {
        Set<Entry<String, Object>> entrySet = new HashSet<>();
        for (Pair<String, ?> entry : entries) {
            entrySet.add(new SimpleEntry<>(entry.first, entry.second));
        }
        return new TextAttributes(entrySet);
    }

    public static TextAttributes of(Map<String, Object> map) {
        return new TextAttributes(map.entrySet());
    }

    private TextAttributes(Set<Entry<String, Object>> entries) {
        this.entries = entries;
    }

    private final Set<Entry<String, Object>> entries;

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return entries;
    }

}
