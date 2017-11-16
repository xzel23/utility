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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A builder class for rich text data.
 * <p> A rich text is created by appending strings to the builder using the {@link #append(CharSequence)}
 * method or one of its overloads. Style properties are set using {@link #push(String, Object)} and then 
 * reset using {@link #pop(String)}.
 * </p>
 * @author axel
 */
public class RichTextBuilder implements Appendable {

    private final StringBuilder buffer = new StringBuilder();

    private final SortedMap<Integer, Map<String,Object>> parts = new TreeMap<>();

    /**
     * Construct a new empty builder.
     */
    public RichTextBuilder() {
        parts.put(0, new HashMap<>());
    }

    @Override
    public RichTextBuilder append(char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public RichTextBuilder append(CharSequence csq) {
        buffer.append(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        buffer.append(csq, start, end);
        return this;
    }

    /**
     * Get attribute of the current Run.
     *
     * @param property
     *            the property
     * @return value of the property
     */
    public Object get(String property) {
        return currentStyle().get(property);
    }

    /**
     * Remove and return the value of a property from the property stack.
     *
     * @param property
     *            the property
     * @return the last stored value for this property on the stack
     */
    public Object pop(String property) {
        Object prev = null;
        for (Map.Entry<Integer, Map<String,Object>> e : parts.entrySet()) {
            if (Objects.equals(e.getKey(), parts.lastKey())) {
                break;
            }
            prev = e.getValue().getOrDefault(property, prev);
        }
        Object current = currentStyle().get(property);
        if (prev != null) {
            currentStyle().put(property, prev);
        } else {
            currentStyle().remove(property);
        }
        return current;
    }

    /**
     * Push a style property.
     *
     * @param property
     *            the property to set
     * @param value
     *            the value to be set
     */
    public void push(String property, Object value) {
        currentStyle().put(property, value);
    }

    /**
     * Convert to RichText.
     *
     * @return RichText representation of this builder's content
     */
    public RichText toRichText() {
        String text = buffer.toString();
        Run[] runs = new Run[parts.size()];

        int runIdx = 0;
        int start = parts.firstKey();
        Map<String,Object> style = parts.get(start);
        for (Map.Entry<Integer, Map<String,Object>> e : parts.entrySet()) {
            int end = e.getKey();
            int runLength = end - start;

            if (runLength == 0) {
                continue;
            }

            runs[runIdx++] = new Run(text, start, end - start, TextAttributes.of(style));
            start = end;
            style = e.getValue();
        }
        runs[runIdx] = new Run(text, start, text.length() - start, TextAttributes.of(style));

        return new RichText(text, runs);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    private Map<String,Object> currentStyle() {
        final Map<String,Object> style;
        if (parts.lastKey() == buffer.length()) {
            style = parts.get(parts.lastKey());
        } else {
            style = new HashMap<>();
            parts.put(buffer.length(), style);
        }
        return style;
    }

}
