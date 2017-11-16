package com.dua3.utility.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.utility.Pair;

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
    public static Style create(String name, Pair<String, Object>... args) {
        Map<String, Object> m = new HashMap<>();
        m.put(TextAttributes.STYLE_NAME, name);
        for (Pair<String, Object> arg : args) {
            m.put(arg.first, arg.second);
        }
        return new Style(name, m);
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
}
