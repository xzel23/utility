// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A builder class for rich text data.
 * <p>
 * A rich text is created by appending strings to the builder using the
 * {@link #append(CharSequence)}
 *
 * @author axel
 */
public class RichTextBuilder implements Appendable, ToRichText {

    private final StringBuilder buffer;
    private final SortedMap<Integer, Map<String, Object>> parts;
    private final Deque<AttributeChange> openedAttributes = new ArrayDeque<>();

    /**
     * Construct a new empty builder.
     */
    public RichTextBuilder() {
        this(16);
    }

    /**
     * Construct a new empty builder.
     *
     * @param capacity the initial capacity
     */
    public RichTextBuilder(int capacity) {
        this.buffer = new StringBuilder(capacity);
        this.parts = new TreeMap<>();

        parts.put(0, new HashMap<>());
    }

    @Override
    public RichTextBuilder append(char c) {
        buffer.append(c);
        return this;
    }

    @Override
    public RichTextBuilder append(@Nullable CharSequence csq) {
        if (csq instanceof ToRichText trt) {
            trt.appendTo(this);
        } else {
            buffer.append(csq);
        }
        return this;
    }

    @Override
    public Appendable append(@Nullable CharSequence csq, int start, int end) {
        if (csq instanceof ToRichText trt) {
            trt.toRichText().subSequence(start, end).appendTo(this);
        } else {
            buffer.append(csq, start, end);
        }
        return this;
    }

    /**
     * Get attribute of the current Run.
     *
     * @param property the property
     * @return value of the property
     */
    public Object get(String property) {
        return parts.get(parts.lastKey()).get(property);
    }

    /**
     * Get attribute of the current Run.
     *
     * @param property     the property
     * @param defaultValue the default value for the property
     * @return value of the property
     */
    public Object getOrDefault(String property, Object defaultValue) {
        return parts.get(parts.lastKey()).getOrDefault(property, defaultValue);
    }

    /**
     * Convert to RichText.
     *
     * @return RichText representation of this builder's content
     */
    @Override
    public RichText toRichText() {
        Run[] runs = getRuns();
        return new RichText(runs);
    }

    private Run[] getRuns() {
        normalize();

        String text = buffer.toString();
        Run[] runs = new Run[parts.size()];

        int runIdx = 0;
        int start = parts.firstKey();
        Map<String, Object> attributes = parts.get(start);
        for (Map.Entry<Integer, Map<String, Object>> e : parts.entrySet()) {
            int end = e.getKey();
            int runLength = end - start;

            if (runLength == 0) {
                continue;
            }

            runs[runIdx++] = new Run(text, start, end - start, TextAttributes.of(attributes));
            start = end;
            attributes = e.getValue();
        }
        runs[runIdx] = new Run(text, start, text.length() - start, TextAttributes.of(attributes));
        return runs;
    }

    private void normalize() {
        // combine subsequent runs sharing the same attributes
        boolean first = true;
        List<Integer> keysToRemove = new ArrayList<>();
        Map<String, Object> lastAttributes = Collections.emptyMap();
        for (Entry<Integer, Map<String, Object>> entry : parts.entrySet()) {
            Integer pos = entry.getKey();
            Map<String, Object> attributes = entry.getValue();

            if (!first && attributes.equals(lastAttributes)) {
                keysToRemove.add(pos);
            }

            lastAttributes = attributes;
            first = false;
        }

        // remove splits
        keysToRemove.forEach(parts.keySet()::remove);

        // always remove a trailing empty run if it exists
        if (parts.size() > 1) {
            parts.remove(length());
        }
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + length());
        for (Run run : getRuns()) {
            builder.appendRun(run);
        }
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     *
     * @param minimumCapacity the minimum desired capacity.
     * @see StringBuilder#ensureCapacity(int)
     */
    public void ensureCapacity(int minimumCapacity) {
        buffer.ensureCapacity(minimumCapacity);
    }

    /**
     * Returns the length (character count).
     *
     * @return the length of the sequence of characters
     */
    public int length() {
        return buffer.length();
    }

    @SuppressWarnings("unchecked")
    void appendRun(Run run) {
        // set attributes
        Map<String, Object> attributes = split();
        Map<String, Object> backup = new HashMap<>();
        for (Entry<String, Object> entry : run.getAttributes().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals(RichText.ATTRIBUTE_NAME_STYLE_LIST)) {
                LangUtil.check(value instanceof List, "attribute '%s' must contain a list", key);
                attributes.compute(key, (k, oldValue) -> {
                    backup.put(key, oldValue);
                    if (oldValue == null) {
                        return new ArrayList<>((Collection<Style>) value);
                    } else {
                        Collection<Style> oldStyles = (Collection<Style>) oldValue;
                        Collection<Style> newStyles = (Collection<Style>) value;
                        LinkedHashSet<Style> styles = new LinkedHashSet<>(oldStyles.size() + newStyles.size());
                        styles.addAll(oldStyles);
                        styles.addAll(newStyles);
                        return new ArrayList<>(styles);
                    }
                });
            } else {
                Object oldValue = attributes.put(key, value);
                backup.put(key, oldValue);
            }
        }

        // append CharSequence
        append(run);

        // restore attributes
        attributes = split();
        for (Entry<String, Object> entry : backup.entrySet()) {
            if (entry.getValue() == null) {
                attributes.remove(entry.getKey());
            } else {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Push attribute. Remove the attribute again by calling {@link #pop(String)}.
     *
     * @param name  attribute name
     * @param value attribute value
     */
    public RichTextBuilder push(String name, Object value) {
        Objects.requireNonNull(value, "value must not be null");
        Object previousValue = split().put(name, value);
        openedAttributes.push(new AttributeChange(name, previousValue, value));
        return this;
    }

    /**
     * Pop attribute that has been set using {@link #push(String, Object)}.
     *
     * @param name attribute name
     * @return this instance
     */
    public RichTextBuilder pop(String name) {
        AttributeChange change = openedAttributes.pop();
        LangUtil.check(name.equals(change.name()), "name does not match: \"%s\", expected \"%s\"", name, change.name());
        Map<String, Object> attributes = split();
        if (change.previousValue() == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, change.previousValue());
        }
        return this;
    }

    /**
     * Push style. Remove the style again by calling {@link #pop(Style)}.
     *
     * @param style the {@link Style} to push
     * @return this instance
     */
    @SuppressWarnings("unchecked")
    public RichTextBuilder push(Style style) {
        List<Style> styles = (List<Style>) getOrDefault(RichText.ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList());
        List<Style> newStyles = new ArrayList<>(styles.size() + 1);
        newStyles.addAll(styles);
        newStyles.add(style);
        push(RichText.ATTRIBUTE_NAME_STYLE_LIST, newStyles);
        return this;
    }

    /**
     * Pop style that has been set using {@link #push(Style)}.
     *
     * @param style the style
     * @return this instance
     */
    public RichTextBuilder pop(Style style) {
        return pop(RichText.ATTRIBUTE_NAME_STYLE_LIST);
    }

    private Map<String, Object> split() {
        return parts.computeIfAbsent(length(), pos -> new HashMap<>(parts.get(parts.lastKey())));
    }

    @SuppressWarnings("unchecked")
    void apply(Style style) {
        for (Map<String, Object> attributes : parts.values()) {
            List<Style> styles = (List<Style>) attributes.computeIfAbsent(RichText.ATTRIBUTE_NAME_STYLE_LIST, k -> new ArrayList<>());
            styles.add(0, style); // add the style at the first position!
        }
    }

    private record AttributeChange(String name, @Nullable Object previousValue, @Nullable Object value) {
    }

}
