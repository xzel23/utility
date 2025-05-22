// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.data.Color;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;

/**
 * An immutable set of text attributes.
 */
public final class TextAttributes extends AbstractMap<String, @Nullable Object> {

    record Entry(String getKey, @Nullable Object getValue) implements Map.Entry<String, @Nullable Object>, Comparable<Entry> {
        @Override
        public @Nullable Object setValue(@Nullable Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(Entry o) {
            return getKey().compareTo(o.getKey());
        }
    }

    /**
     * empty instance
     */
    private static final TextAttributes NONE = new TextAttributes(new Entry[0]);
    private final SortedSet<Entry> entries;
    private int hash;
    private @Nullable FontDef fontDef;

    private TextAttributes(Entry[] entries) {
        assert Arrays.stream(entries).allMatch(Style::checkTypes) : "invalid attribute types";
        this.entries = LangUtil.asUnmodifiableSortedListSet(entries);
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
        Entry[] entries_ = new Entry[entries.length];
        for (int i = 0; i < entries.length; i++) {
            Pair<String, ?> entry = entries[i];
            assert entry.first() != null;
            entries_[i] = new Entry(entry.first(), entry.second());
        }
        return new TextAttributes(entries_);
    }

    /**
     * Construct style with attributes.
     *
     * @param entries the attribute/value pairs to add
     * @return TextAttributes instance
     */
    public static TextAttributes of(Iterable<Pair<String, ?>> entries) {
        List<Entry> entryList = new ArrayList<>();
        entries.forEach(entry -> entryList.addLast(new Entry(entry.first(), entry.second())));
        return new TextAttributes(entryList.toArray(Entry[]::new));
    }

    /**
     * Construct style with attributes.
     *
     * @param map mapping from attributes to values
     * @return TextAttributes instance
     */
    public static TextAttributes of(Map<String, @Nullable Object> map) {
        List<Entry> entries = new ArrayList<>(map.size());
        map.forEach((k, v) -> entries.addLast(new Entry(k,v)));
        return new TextAttributes(entries.toArray(Entry[]::new));
    }

    /**
     * Retrieves the {@link FontDef} associated with this instance. If the font definition is not
     * already present, it initializes it using an internal method and caches it for future use.
     * The returned {@link FontDef} is a cloned instance to ensure immutability of the original.
     *
     * @return a clone of the current {@link FontDef} instance associated with this object
     * @throws IllegalStateException if the cloning process fails unexpectedly
     */
    public FontDef getFontDef() {
        FontDef fd = fontDef;
        if (fd == null) {
            fd = getFontDefInternal(this);
            fontDef = fd;
        }
        try {
            return fd.clone();
        } catch (CloneNotSupportedException e) {
            // this should not happen
            throw new IllegalStateException();
        }
    }

    /**
     * Get {@link FontDef} from TextAttributes.
     *
     * @param attributes {@link Map} holding TextAttribute values
     * @return FontDef instance
     */
    public static FontDef getFontDef(Map<? super String, @Nullable Object> attributes) {
        if (attributes instanceof TextAttributes ta) {
            return ta.getFontDef();
        }
        return getFontDefInternal(attributes);
    }

    private static FontDef getFontDefInternal(Map<? super String, @Nullable Object> attributes) {
        Font font = (Font) attributes.get(Style.FONT);
        if (font != null) {
            return font.toFontDef();
        }

        FontDef fd = new FontDef();
        //noinspection unchecked
        DataUtil.ifPresent(attributes, Style.FONT_FAMILIES, v -> fd.setFamilies((List<String>) v));
        DataUtil.ifPresent(attributes, Style.FONT_SIZE, v -> fd.setSize((Float) v));
        DataUtil.ifPresent(attributes, Style.COLOR, v -> fd.setColor((Color) v));
        DataUtil.ifPresent(attributes, Style.FONT_WEIGHT, v -> fd.setBold(Objects.equals(v, Style.FONT_WEIGHT_VALUE_BOLD)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_UNDERLINE, v -> fd.setUnderline(Objects.equals(v, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.TEXT_DECORATION_LINE_THROUGH, v -> fd.setStrikeThrough(Objects.equals(v, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)));
        DataUtil.ifPresent(attributes, Style.FONT_STYLE, v -> fd.setItalic(Objects.equals(v, Style.FONT_STYLE_VALUE_ITALIC) || Objects.equals(v, Style.FONT_STYLE_VALUE_OBLIQUE)));
        return fd;
    }

    @Override
    public SortedSet<Map.Entry<String, @Nullable Object>> entrySet() {
        //noinspection rawtypes
        return (SortedSet) entries;
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
            for (var entry : entrySet()) {
                // only use the value when it is immutable
                String key = entry.getKey();
                Object value = entry.getValue();

                int h2 = key.hashCode();
                if (LangUtil.isOfKnownImmutableType(value)) {
                    h2 += 97 * (value == null ? 0 : value.hashCode());
                }
                h = h * 11 + h2;
            }
            hash = h;
        }
        return h;
    }

}

