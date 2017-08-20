/*
 * Copyright 2016 Axel Howind.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.dua3.utility.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.dua3.utility.Pair;

/**
 * A set of text attributes.
 */
public class TextAttributes {

    public static class Attribute {
        public final MarkDownStyle style;
        public final Map<String, Object> args;

        @SafeVarargs
        Attribute(MarkDownStyle style, Pair<String, Object>... args) {
            this.style = style;
            Map<String, Object> m = new HashMap<>();
            for (Pair<String, Object> arg : args) {
                m.put(arg.first, arg.second);
            }
            this.args = Collections.unmodifiableMap(m);
        }
        
        @Override
        public String toString() {
            return style.toString()+args;
        }
    }

    // meta
    public static final String STYLE_START_RUN = "__style-start-run";
    public static final String STYLE_END_RUN = "__style-end-run";

    // style name
    public static final String STYLE_NAME = "style-name";

    // font properties
    public static final String FONT_FAMILY = "font-family";
    public static final String FONT_STYLE = "font-style";
    public static final String FONT_SIZE = "font-size";
    public static final String FONT_WEIGHT = "font-weight";
    public static final String FONT_VARIANT = "font-variant";

    public static final String TEXT_DECORATION = "text-decoration";

    // colors
    public static final String COLOR = "color";
    public static final String BACKGROUND_COLOR = "background-color";

    private static final TextAttributes NONE = new TextAttributes();

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
     * @param entries
     *            the attribute/value pairs to add
     * @return the new style
     */
    @SafeVarargs
    public static TextAttributes of(Pair<String, String>... entries) {
        TextAttributes style = new TextAttributes();
        for (Pair<String, String> entry : entries) {
            style.put(entry.first, entry.second);
        }
        return style;
    }

    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        TextAttributes other = (TextAttributes) obj;
        return properties.equals(other.properties);
    }

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    public Map<String, Object> properties() {
        return Collections.unmodifiableMap(properties);
    }

    Object get(String property) {
        return properties.get(property);
    }

    Object getOrDefault(String property, Object def) {
        return properties.getOrDefault(property, def);
    }

    void put(String property, Object value) {
        properties.put(property, value);
    }

    void remove(String property) {
        properties.remove(property);
    }

}
