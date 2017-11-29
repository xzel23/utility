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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final Function<Style, RunTraits> traitSupplier;

    private final Map<String,Object> currentAttributes = new HashMap<>();

    /**
     * The traits of a Run, consisting of:
     * <ul>
     * <li> the TextAttributes to apply
     * <li> text to prepend
     * <li> text to append
     * </ul>
     */
    public interface RunTraits {    	
    	TextAttributes attributes();
    	String prefix();
    	String postfix();
    }
    
    public static class SimpleRunTraits implements RunTraits {
    	private final TextAttributes attributes;
		private final String prefix;
		private final String postfix;

		public SimpleRunTraits(TextAttributes attributes, String prefix, String postfix) {
    		this.attributes = attributes;
    		this.prefix = prefix;
    		this.postfix = postfix;
    	}

		public SimpleRunTraits(TextAttributes attributes) {
			this(attributes, "", "");
		}

		public TextAttributes attributes() { return attributes; }
		public String prefix() { return prefix; }
		public String postfix() { return postfix; }
    }

    private static class CollectingRunTraits implements RunTraits {
    	private final Map<String,Object> attributes = new HashMap<>();
    	private final Deque<String> prefix = new LinkedList<>();
    	private final Deque<String> postfix = new LinkedList<>();
    	
    	/**
    	 * Merge properties of another RunTraits instance.
    	 * @param other the RunTraits instance to merge
    	 */
    	public void mergeIn(RunTraits other) {
    		attributes.putAll(other.attributes());
    		prefix.addLast(other.prefix());
    		postfix.addFirst(other.prefix());
    	}
    	
    	@Override
    	public TextAttributes attributes() {
    		return TextAttributes.of(attributes);
    	}
    	
    	@Override
    	public String prefix() {
    		return prefix.stream().collect(Collectors.joining());
    	}
    	
    	@Override
    	public String postfix() {
    		return postfix.stream().collect(Collectors.joining());
    	}    	
    }
    
    /**
     * Constructor.
     */
    protected RichTextConverterBase(Function<Style, RunTraits> traitSupplier) {
        this.traitSupplier = traitSupplier;
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
        resetAttr.add(Collections.emptyMap());
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
        CollectingRunTraits traits = handleRunStarts(run);
        appendChars(traits.prefix());
        appendChars(run);
        appendChars(traits.postfix());
    }

    private Deque<Map<String, Object>> resetAttr = new LinkedList<>();

    private void pushRunAttributes(Map<String, Object> resetAttributes) {
        resetAttr.push(resetAttributes);
    }

    void handleRunEnds(Run run) {
        // handle run ends
    	getStyleList(run, TextAttributes.STYLE_START_RUN)
    		.forEach(style -> popRunAttributes());
    }

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

    CollectingRunTraits handleRunStarts(Run run) {
    	// process styles whose runs start at this position
        CollectingRunTraits traits = extractRunTraits(run);
        applyAttributes(traits.attributes());
        return traits;
    }

    protected abstract void applyAttributes(TextAttributes attributes);

    private CollectingRunTraits extractRunTraits(Run run) {    	
    	List<Style> styles = getStyleList(run, TextAttributes.STYLE_START_RUN);

        // collect traits for this run
        RunTraits currentTraits = new SimpleRunTraits(run.getAttributes());
        
        CollectingRunTraits traits = new CollectingRunTraits();
		Stream.concat(styles.stream().map(traitSupplier), Stream.of(currentTraits)) 
			.forEach(t -> {
				// store current attributes for resetting at end of style
				pushRunAttributes(
        			t.attributes().keySet().stream()
            		.collect(Collectors.toMap(attr -> attr, attr -> currentAttributes.get(attr))));
				// merge traits
				traits.mergeIn(t);
			});
		
		return traits;
    }

	@SuppressWarnings("unchecked")
	private List<Style> getStyleList(Run run, String property) {
		TextAttributes attributes = run.getAttributes();
		Object value = attributes.getOrDefault(property, Collections.emptyList());
        
		LangUtil.check(value instanceof List, "expected instance of List but got %s", value.getClass());
        return (List<Style>) value;
	}

    protected abstract void appendChars(CharSequence run);

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

