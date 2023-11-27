// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable set of text attributes.
 */
public final class TextAttributes extends AbstractMap<String, Object> {

    /**
     * empty instance
     */
    private static final TextAttributes NONE = new TextAttributes(Collections.emptySortedSet());
    private final SortedSet<Entry<String, Object>> entries;
    private int hash;

    private TextAttributes(SortedSet<Entry<String, Object>> entries) {
        this.entries = entries;
    }

    /**
     * Empty TextAttributes instance.
     *
     * @return empty instance
     */
    public static TextAttributes none() {
        return NONE;
    }

    /**
     * Construct instance with attributes.
     *
     * @param entries the attribute/value pairs to add
     * @return TextAttributes instance
     */
    @SafeVarargs
    public static TextAttributes of(Pair<String, ?>... entries) {
        return of(List.of(entries));
    }

    /**
     * Construct style with attributes.
     *
     * @param entries the attribute/value pairs to add
     * @return TextAttributes instance
     */
    public static TextAttributes of(Iterable<Pair<String, ?>> entries) {
        SortedSet<Entry<String, Object>> entrySet = new TreeSet<>(Entry.comparingByKey());
        for (Pair<String, ?> entry : entries) {
            entrySet.add(new SimpleEntry<>(entry.first(), entry.second()));
        }
        return new TextAttributes(entrySet);
    }

    /**
     * Construct style with attributes.
     *
     * @param map mapping from attributes to values
     * @return TextAttributes instance
     */
    public static TextAttributes of(Map<String, Object> map) {
        SortedSet<Map.Entry<String, Object>> entries = new TreeSet<>(Map.Entry.comparingByKey());
        // do not use addAll() due to DMI_ENTRY_SETS_MAY_REUSE_ENTRY_OBJECTS
        map.entrySet().stream().map(Map.Entry::copyOf).forEach(entries::add);
        return new TextAttributes(entries);
    }

    /**
     * Get {@link FontDef} from TextAttributes.
     *
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
        DataUtil.ifPresent(attributes, Style.FONT_SIZE, v -> fd.setSize((Float) v));
        DataUtil.ifPresent(attributes, Style.COLOR, v -> fd.setColor((Color) v));
        DataUtil.ifPresent(attributes, Style.FONT_WEIGHT, v -> fd.setBold(Objects.equals(v, Style.FONT_WEIGHT_VALUE_BOLD)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_UNDERLINE, v -> fd.setUnderline(Objects.equals(v, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_LINE_THROUGH, v -> fd.setStrikeThrough(Objects.equals(v, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.FONT_STYLE, v -> fd.setItalic(Objects.equals(v, Style.FONT_STYLE_VALUE_ITALIC) || Objects.equals(v, Style.FONT_STYLE_VALUE_OBLIQUE)));
        return fd;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return entries;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return (o instanceof TextAttributes) && o.hashCode() == hashCode() && super.equals(o);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            hash = super.hashCode();
        }
        return h;
    }

}

