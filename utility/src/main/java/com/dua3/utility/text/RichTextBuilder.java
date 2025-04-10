// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * A builder class for rich text data.
 * <p>
 * A rich text is created by appending strings to the builder using the
 * {@link #append(CharSequence)}.
 * <p>
 * Attributes can be set manipulated by using {@link #push(String, Object)}/{@link #pop(String)},
 * {@link #push(Style)}/{@link #pop(Style)} or {@link #compose(String, BiFunction)}/{@link #decompose(String)}
 * pairs of methods.
 */
public class RichTextBuilder implements Appendable, ToRichText, CharSequence {

    private record PositionAttributes(int pos, Map<String, Object> attributes) {}

    private final StringBuilder buffer;
    private final List<PositionAttributes> parts;
    private final List<AttributeChange> openedAttributes = new ArrayList<>(16);
    private final List<AttributeChange> openedCompositions = new ArrayList<>(16);

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
        this.parts = new ArrayList<>(16);

        parts.add(new PositionAttributes(0, new HashMap<>()));
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
        return parts.get(parts.size() - 1).attributes().get(property);
    }

    /**
     * Get attribute of the current Run.
     *
     * @param property     the property
     * @param defaultValue the default value for the property
     * @return value of the property
     */
    public Object getOrDefault(String property, Object defaultValue) {
        return parts.get(parts.size() - 1).attributes().getOrDefault(property, defaultValue);
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
        int start = parts.get(0).pos();
        Map<String, Object> attributes = parts.get(0).attributes();
        for (PositionAttributes part : parts) {
            int end = part.pos;
            int runLength = end - start;

            if (runLength == 0) {
                continue;
            }

            runs[runIdx++] = new Run(text, start, end - start, TextAttributes.of(attributes));
            start = end;
            attributes = part.attributes();
        }
        runs[runIdx] = new Run(text, start, text.length() - start, TextAttributes.of(attributes));
        return runs;
    }

    /**
     * Combine subsequent runs sharing the same attributes.
     */
    private void normalize() {
        // if there's only a single run, there's nothing to do.
        int size = parts.size();
        if (size <= 1) {
            return;
        }

        Map<String, Object> lastAttributes = parts.get(0).attributes();
        int writeIndex = 1;
        for (int readIndex = 1; readIndex < size; readIndex++) {
            Map<String, Object> attributes = parts.get(readIndex).attributes();
            if (!attributes.equals(lastAttributes)) {
                parts.set(writeIndex++, parts.get(readIndex));
                lastAttributes = attributes;
            }
        }

        // Bulk remove remaining elements
        while (parts.size() > writeIndex) {
            parts.remove(parts.size() - 1);
        }

        // always remove a trailing empty run if it exists
        if (parts.size() > 1 && parts.get(parts.size() - 1).pos() == length()) {
            parts.remove(parts.size() - 1);
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
     * @return the length of the character sequence
     */
    public int length() {
        return buffer.length();
    }

    @Override
    public char charAt(int index) {
        return buffer.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return buffer.subSequence(start, end);
    }

    public RichTextBuilder deleteCharAt(int index) {
        // remove character from buffer
        buffer.deleteCharAt(index);

        // fastpath when all parts can be retained 
        if (parts.get(parts.size() - 1).pos() < index) {
            return this;
        }

        // update parts starting at index + 1 (in reverse order)
        int i;
        for (i = parts.size() - 1; i > 0 && parts.get(i).pos() > index; i--) {
            PositionAttributes part = parts.get(i);
            parts.set(i, new PositionAttributes(part.pos() - 1, part.attributes()));
        }
        // if the character was part of a run with length 1, remove the now empty run
        if (i + 1 < parts.size() && parts.get(i).pos() == parts.get(i + 1).pos()) {
            parts.remove(i);
        }

        return this;
    }

    @SuppressWarnings("unchecked")
    void appendRun(Run run) {
        // set attributes
        Map<String, @Nullable Object> attributes = split();
        Map<String, @Nullable Object> backup = new HashMap<>();
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
        for (Entry<String, @Nullable Object> entry : backup.entrySet()) {
            if (entry.getValue() == null) {
                attributes.remove(entry.getKey());
            } else {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Push attribute. Remove the attribute again by calling {@link #pop(String)}.
     * <p>
     * If the attribute is already present, its value is overwritten.
     *
     * @param name  attribute name
     * @param value attribute value
     * @return this RichTextBuilder instance
     */
    public RichTextBuilder push(String name, Object value) {
        Object previousValue = split().put(name, value);
        openedAttributes.add(new AttributeChange(name, previousValue, value));
        return this;
    }

    /**
     * Pop attribute that has been set using {@link #push(String, Object)}.
     * <p>
     * The value the attribute had before calling {push()} is restored; if the attribute was created new,
     * it is removed.
     *
     * @param name attribute name
     * @return this instance
     */
    public RichTextBuilder pop(String name) {
        AttributeChange change = openedAttributes.remove(openedAttributes.size() - 1);
        LangUtil.check(name.equals(change.name()), "attribute name does not match: \"%s\", expected \"%s\"", name, change.name());
        Map<String, Object> attributes = split();
        if (change.previousValue() == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, change.previousValue());
        }
        return this;
    }

    /**
     * Compose new attribute value. Remove the attribute again by calling {@link #decompose(String)}.
     *
     * @param name  attribute name
     * @param composer function that computes the new value from the current value
     * @return this RichTextBuilder instance
     */
    public RichTextBuilder compose(String name, BiFunction<String, @Nullable Object, @Nullable Object> composer) {
        Map<String, Object> currentAttributes = split();
        Object previousValue = currentAttributes.get(name);
        Object value = composer.apply(name, previousValue);
        if (value == null) {
            currentAttributes.remove(name);
        } else {
            currentAttributes.put(name, value);
        }
        openedCompositions.add(new AttributeChange(name, previousValue, value));
        return this;
    }

    /**
     * Reverts the change made by a previous {@link #compose(String, BiFunction)} call.
     * Restores the attribute to the oldValue it had before the {@code compose()} call.
     *
     * @param name  attribute name
     * @return this RichTextBuilder instance
     */
    public RichTextBuilder decompose(String name) {
        AttributeChange change = openedCompositions.remove(openedCompositions.size() - 1);
        LangUtil.check(name.equals(change.name()), "composition name does not match: \"%s\", expected \"%s\"", name, change.name());
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
        if (parts.get(parts.size() - 1).pos() < length()) {
            parts.add(new PositionAttributes(length(), new HashMap<>(parts.get(parts.size() - 1).attributes())));
        }
        return parts.get(parts.size() - 1).attributes;
    }

    @SuppressWarnings("unchecked")
    void apply(Style style) {
        for (PositionAttributes part : parts) {
            List<Style> styles = (List<Style>) part.attributes().computeIfAbsent(RichText.ATTRIBUTE_NAME_STYLE_LIST, k -> new ArrayList<>());
            styles.add(0, style); // add the style at the first position!
        }
    }

    private record AttributeChange(String name, @Nullable Object previousValue, @Nullable Object value) {
    }

}
