// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.utility.data.Pair;

public class Style {
    private final String name;
    private final Map<String, Object> properties;

    private Style(String name, Map<String,Object> args) {
        this.name = name;
        this.properties = Collections.unmodifiableMap(args);
    }

    @Override
    public String toString() {
        return name+properties;
    }

    @SafeVarargs
    public static Style create(String styleName, String styleClass, Pair<String, Object>... args) {
        Map<String, Object> m = new HashMap<>();
        m.put(TextAttributes.STYLE_NAME, styleName);
        m.put(TextAttributes.STYLE_CLASS, styleClass);
        for (Pair<String, Object> arg : args) {
            m.put(arg.first, arg.second);
        }
        return new Style(styleName, m);
    }

    public static Style create(String name, Map<String, Object> args) {
        return new Style(name, new HashMap<>(args));
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
