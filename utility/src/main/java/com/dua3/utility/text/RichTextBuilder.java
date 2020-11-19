// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * A builder class for rich text data.
 * <p>
 * A rich text is created by appending strings to the builder using the
 * {@link #append(CharSequence)}
 * method or one of its overloads. Style properties are set using
 * {@link #put(String, Object)}.
 * </p>
 *
 * @author axel
 */
public class RichTextBuilder implements Appendable, ToRichText {

    private final StringBuilder buffer = new StringBuilder();
    private final SortedMap<Integer, Map<String, Object>> parts = new TreeMap<>();

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

    public RichTextBuilder append(ToRichText trt) {
        trt.appendTo(this);
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
        return currentStyle().get(property);
    }

    /**
     * Get attribute of the current Run, or compute and store a new one if not
     * present.
     *
     * @param  property
     *                  the property
     * @param  supplier
     *                  the supplier to create a new value if the property is not
     *                  yet set
     * @return          value of the property
     */
    public Object getOrSupply(String property, Supplier<?> supplier) {
        return currentStyle().computeIfAbsent(property, key -> supplier.get());
    }

    /**
     * Push a style property.
     *
     * @param  property
     *                  the property to set
     * @param  value
     *                  the value to be set
     * @return
     *                  the old value for this attribute
     */
    public Object put(String property, Object value) {
        return currentStyle().put(property, value);
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

    @Override
    public String toString() {
        return buffer.toString();
    }

    private Map<String, Object> currentStyle() {
        return parts.computeIfAbsent(buffer.length(), key -> new HashMap<>());
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

    void appendRun(Run run) {
        // set attributes
        for (Entry<String, Object> entry : run.getAttributes().entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        // append CharSequence
        append(run);
    }

    /**
     * Push a style, i. e. start using the style at the current position.
     * Call {@link #pop(Style)} with the same argument to stop using the style.
     * 
     * @param style the Style
     */
    public void push(Style style) {
        push(TextAttributes.STYLE_START_RUN, style);
    }

    /**
     * Pop a style, i. e. stop using the style at the current position.
     * Also see {@link #push(Style)}.
     *
     * @param style the Style
     */
    public void pop(Style style) {
        push(TextAttributes.STYLE_END_RUN, style);
    }

    private void push(String key, Style style) {
        @SuppressWarnings("unchecked")
        Collection<Style> current = (Collection<Style>) get(key);
        if (current == null) {
            current = new LinkedList<>();
            put(key, current);
        }
        current.add(style);
    }   
}
