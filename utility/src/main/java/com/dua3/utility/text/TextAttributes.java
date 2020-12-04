// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

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
