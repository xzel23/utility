// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Style {
    private final String name;
    private final Map<String, Object> properties;

    private Style(String name, Map<String, Object> args) {
        this.name = name;
        this.properties = Collections.unmodifiableMap(args);
    }

    @Override
    public String toString() {
        return name + properties;
    }

    @SafeVarargs
    public static Style create(String styleName, String styleClass, Pair<String, Object>... args) {
        return create(styleName, styleClass, Pair.toMap(args));
    }

    public static Style create(String styleName, String styleClass, Map<String, Object> args) {
        Map<String, Object> m = new HashMap<>(args);
        LangUtil.check(m.put(TextAttributes.STYLE_NAME, styleName)==null, "STYLE_NAME must not be set in attribute map");
        LangUtil.check(m.put(TextAttributes.STYLE_CLASS, styleClass)==null, "STYLE_CLASS must not be set in attribute map");
        return new Style(styleName, m);
    }

    public String name() {
        return name;
    }

    public Object get(String property) {
        return properties.get(property);
    }

    public Object getOrDefault(String property, Object dflt) {
        return properties.getOrDefault(property, dflt);
    }

    public Map<String, Object> properties() {
        return properties;
    }
}
