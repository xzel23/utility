/*
 * Copyright 2015 Axel Howind (axel@dua3.com).
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
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;

/**
 * Base class for text converters. This class is intended a s a common base class
 * for creating converters that transform text represented as {@code RichText}
 * into other formats.
 *
 * @author Axel Howind (axel@dua3.com)
 * @param <T>
 *            class matching produced document type
 */
public abstract class RichTextConverterBase<T> implements RichTextConverter<T> {

    private final Function<Style, TextAttributes> styleSupplier;

    private final Map<String,Object> currentAttributes = new HashMap<>();

    /**
     * Constructor.
     */
    protected RichTextConverterBase(Function<Style, TextAttributes> styleSupplier) {
        this.styleSupplier = styleSupplier;
    }

    /**
     * Check state of this TextBuilder.
     *
     * @throws IllegalStateException
     *             if this builder's get() was already called
     */
    protected void checkState() {
        if (!isValid()) {
            throw new IllegalStateException("This builder's get() method was already called.");
        }
    }

    @Override
    public RichTextConverter<T> add(RichText text) {
        checkState();
        for (Run r : text) {
            append(r);
        }
        return this;
    }

    /**
     * Add text to document. Implementations must override this method to append
     * {@code text} which is attributed with {@code attributes} to the result
     * document.
     *
     * @param run
     *            the {@link com.dua3.utility.text.Run} to append
     */
    protected void append(Run run) {
        handleRunEnds(run);
        handleRunStarts(run);
        appendChars(run, currentAttributes);
    }

    private Deque<Map<String, Object>> resetAttr = new LinkedList<>();

    private void popRunAttributes() {
        for (Entry<String, Object> e : resetAttr.pop().entrySet()) {
            String attr = e.getKey();
            Object value = e.getValue();
            if (value != null) {
                currentAttributes.put(attr, value);
            } else {
                currentAttributes.remove(attr);
            }
        }
    }

    private void pushRunAttributes(Map<String, Object> resetAttributes) {
        resetAttr.push(resetAttributes);
    }

    void handleRunEnds(Run run) {
        // handle run ends
        List<?> runEnds = (List<?>) run.getAttributes().getOrDefault(TextAttributes.STYLE_END_RUN, Collections.emptyList());
        for (int i = 0; i < runEnds.size(); i++) {
            popRunAttributes();
        }
    }

    void handleRunStarts(Run run) {
        Map<String,Object> setAttributes = new HashMap<>();

        // process styles whose runs start at this position and insert their opening tags (p.first)
        appendAttributesForRun(setAttributes, run, TextAttributes.STYLE_START_RUN);

        for (Map.Entry<String, Object> e : run.getAttributes().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            assert key != null;
            assert value != null;

            if (key.startsWith("__")) {
                // don't create spans for meta info
                continue;
            }

            setAttributes.put(key, value);
        }

        applyRunAttributes(setAttributes);
    }

    protected abstract void applyRunAttributes(Map<String, Object> setAttributes);

    private void appendAttributesForRun(Map<String,Object> attributeSet, Run run, String property) {
        TextAttributes attributes = run.getAttributes();
        Object value = attributes.getOrDefault(property, Collections.emptyList());

        LangUtil.check(value instanceof List, "expected a value of class List but got %s (property=%s)",value.getClass(), property);

        @SuppressWarnings("unchecked")
        List<Style> styles = (List<Style>) value;
        for (Style style : styles) {
            // collect attributes
            TextAttributes styleAttributes = styleSupplier.apply(style);
            attributeSet.putAll(styleAttributes);
            // store current values
            Map<String,Object> resetAttributes = new HashMap<>();
            for (String attr: styleAttributes.keySet()) {
                resetAttributes.put(attr, currentAttributes.get(attr));
            }
            pushRunAttributes(resetAttributes);
        }
    }

    protected abstract void appendChars(CharSequence run, Map<String, Object> attributesToSet);

    protected abstract boolean isValid();

    /**
     * Enumeration of supported List types.
     */
    protected enum ListType {
        /** Unordered (bulleted) list. */
        UNORDERED,
        /** Ordered (numbered) list. */
        ORDERED
    }

    /** Stack of the currently processed lists. */
    private final Deque<Pair<ListType,AtomicInteger>> listStack = new LinkedList<>();

    /**
     * Update the item count for this list.
     * @return pair with list type and the item number for the new item (1-based)
     */
    protected Pair<ListType,Integer> newListItem() {
        LangUtil.check(!listStack.isEmpty(), "item definition is not inside list");
        Pair<ListType, AtomicInteger> current = listStack.peekLast();
        ListType type = current.first;
        int nr = current.second.incrementAndGet();
        return Pair.of(type, nr);
    }

    /**
     * Starts a new list definition.
     * @param type the type of the list
     */
    protected void startList(ListType type) {
        listStack.add(Pair.of(type, new AtomicInteger()));
    }

    /**
     * Closes the current list definition
     */
    protected void endList() {
        LangUtil.check(!listStack.isEmpty(), "there is no list open");
        listStack.removeLast();
    }

}

