// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.*;
import java.util.stream.Stream;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

/**
 * A set of text attributes.
 */
public final class TextAttributes extends AbstractMap<String, Object> {
    
    /** empty {@link TextAttributes} instance */
    private static final TextAttributes NONE = new TextAttributes(Collections.emptySet());
    
    /** Styles. */
    private final Map<String,List<Style>> styles = new HashMap<>();

    /** 
     * Stream Styles.
     * @return stream of pairs consisting of style class name and list of styles for that style class
     */
    Stream<Pair<String,List<Style>>> styles() {
        return styles.entrySet().stream().map(Pair::of);
    }

    /**
     * Stream Styles.
     * @param styleClass the style class
     * @return stream of styles
     */
    Stream<Style> styles(String styleClass) {
        return styles.getOrDefault(styleClass, Collections.emptyList()).stream();
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
            entrySet.add(new SimpleEntry<>(entry.first(), entry.second()));
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

    /**
     * Get {@link FontDef} from TextAttributes.
     * @param attributes {@link Map} holding TextAttribute values
     * @return FontDef instance
     */
    public static FontDef getFontDef(Map<? super String, Object> attributes) {
        Font font = (Font) attributes.get(Style.FONT);
        if (font != null) {
            return font.toFontDef();
        }

        FontDef fd = new FontDef();
        DataUtil.ifPresent(attributes, Style.FONT_TYPE, v -> fd.setFamily(String.valueOf(v)));
        DataUtil.ifPresent(attributes, Style.COLOR, v -> fd.setColor((Color) v));
        DataUtil.ifPresent(attributes, Style.FONT_WEIGHT, v -> fd.setBold(Objects.equals(v, Style.FONT_WEIGHT_VALUE_BOLD)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_UNDERLINE, v -> fd.setUnderline(Objects.equals(v, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_LINE_THROUGH, v -> fd.setStrikeThrough(Objects.equals(v, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.FONT_STYLE, v -> fd.setItalic(Objects.equals(v, Style.FONT_STYLE_VALUE_ITALIC) || Objects.equals(v, Style.FONT_STYLE_VALUE_OBLIQUE)));
        return fd;
    }

}

