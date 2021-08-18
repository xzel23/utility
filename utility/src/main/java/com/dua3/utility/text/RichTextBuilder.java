// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.lang.LangUtil;

import java.util.*;
import java.util.Map.Entry;

import static com.dua3.utility.text.RichText.ATTRIBUTE_NAME_STYLE_LIST;

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
    private final Deque<AttributeChange> openedAttributes = new LinkedList<>();

    private record AttributeChange(String name, Object previousValue, Object value) {}
    
    /**
     * Construct a new empty builder.
     */
    public RichTextBuilder() {
        this(16);
    }

    /**
     * Construct a new empty builder.
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
    public RichTextBuilder append(CharSequence csq) {
        buffer.append(csq);
        return this;
    }

    @Override
    public Appendable append(CharSequence csq, int start, int end) {
        buffer.append(csq, start, end);
        return this;
    }

    public RichTextBuilder append(ToRichText trt) {
        trt.appendTo(this);
        return this;
    }

    public RichTextBuilder append(RichText rt) {
        rt.appendTo(this);
        return this;
    }

    /**
     * Get attribute of the current Run.
     *
     * @param  property
     *                  the property
     * @return          value of the property
     */
    public Object get(String property) {
        return parts.get(parts.lastKey()).get(property);
    }

    /**
     * Get attribute of the current Run.
     *
     * @param  property
     *                  the property
     * @return          value of the property
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
        Map<String, Object> style = parts.get(start);
        for (Map.Entry<Integer, Map<String, Object>> e : parts.entrySet()) {
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
        return runs;
    }

    @SuppressWarnings("unchecked")
    private void normalize() {
        // combine subsequent runs sharing the same attibutes
        boolean first = true;
        List<Integer> keysToRemove = new ArrayList<>();
        Map<String, Object> lastAttributes = Collections.emptyMap();
        for (Entry<Integer, Map<String, Object>> entry: parts.entrySet()) {
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
        if (parts.size()>1) {
            parts.remove(length());
        }
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public void appendTo(RichTextBuilder builder) {
        builder.ensureCapacity(builder.length() + this.length());
        for (Run run : getRuns()) {
            builder.appendRun(run);
        }
    }

    /**
     * Ensures that the capacity is at least equal to the specified minimum.
     *
     * @param minimumCapacity the minimum desired capacity.
     * @see                   StringBuilder#ensureCapacity(int)
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
            
            if (key.equals(ATTRIBUTE_NAME_STYLE_LIST)) {
                value = new ArrayList<>((List<Style>)value);
            }
            
            Object oldValue = attributes.put(key, value);
            backup.put(key, oldValue);
        }

        // append CharSequence
        append(run);
        
        // restore attributes
        attributes = split();
        for (Entry<String, Object> entry: backup.entrySet()) {
            if (entry.getValue()==null) {
                attributes.remove(entry.getKey());
            } else {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void push(String name, Object value) {
        Objects.requireNonNull(value, "value must not be null");
        Object previousValue = split().put(name, value);
        openedAttributes.push(new AttributeChange(name, previousValue, value));
    }

    public void pop(String name) {
        AttributeChange change = openedAttributes.pop();
        LangUtil.check(name.equals(change.name));
        Map<String, Object> attributes = split();
        if (change.previousValue==null) {
            attributes.remove(name);
        } else {
            attributes.put(name, change.previousValue);
        }
    }

    @SuppressWarnings("unchecked")
    public void push(Style style) {
        List<Style> styles = (List<Style>) getOrDefault(ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList());
        List<Style> newStyles = new ArrayList<>(styles.size()+1);
        newStyles.addAll(styles);
        newStyles.add(style);
        push("__styles", newStyles);
    }

    /**
     * Pop a style, i. e. stop using the style at the current position.
     * Also see {@link #push(Style)}.
     *
     * @param style the Style
     */
    public void pop(Style style) {
        pop("__styles");
    }

    private Map<String, Object> split() { 
        return parts.computeIfAbsent(length(), pos -> new HashMap<>(parts.get(parts.lastKey())));
    }

    @SuppressWarnings("unchecked")
    void apply(Style style) {
        for (Map<String,Object> attributes: parts.values()) {
            List<Style> styles = (List<Style>) attributes.computeIfAbsent(ATTRIBUTE_NAME_STYLE_LIST, k -> new ArrayList<>());
            styles.add(0, style); // add the style at the first position!
        }
    }

}
