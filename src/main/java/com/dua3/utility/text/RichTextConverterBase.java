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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	protected abstract RunTraits getTraits(Style style);

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
    	String suffix();
    }


    private static final RunTraits EMPTY_RUN_TRAITS = new SimpleRunTraits(TextAttributes.none());

    public static RunTraits emptyTraits() {
        return EMPTY_RUN_TRAITS;
    }

    public static class SimpleRunTraits implements RunTraits {
    	private final TextAttributes attributes;
		private final String prefix;
		private final String suffix;

		public SimpleRunTraits(TextAttributes attributes) {
			this(attributes, "", "");
		}
		
		public SimpleRunTraits(TextAttributes attributes, String prefix, String suffix) {
			this.attributes = attributes;
			this.prefix = extract(attributes, TextAttributes.STYLE_START_RUN, TextAttributes.TEXT_PREFIX)+prefix;
            this.suffix = suffix+extract(attributes, TextAttributes.STYLE_END_RUN, TextAttributes.TEXT_SUFFIX);
		}

		private static String extract(TextAttributes attributes, String tagStartOrEnd, String tag) {
            @SuppressWarnings("unchecked")
            List<Style> styles = (List<Style>) attributes.getOrDefault(tagStartOrEnd, Collections.emptyList());
            return styles.stream().map(s -> s.getOrDefault(tag,"").toString()).collect(Collectors.joining());
        }

        @Override
        public TextAttributes attributes() { return attributes; }
		@Override
        public String prefix() { return prefix; }
		@Override
        public String suffix() { return suffix; }
		
		@Override
		public String toString() {
			return attributes.toString()+","+prefix+","+suffix;
		}
    }

    private static class CollectingRunTraits implements RunTraits {
    	private final Map<String,Object> attributes = new HashMap<>();
    	private final Deque<String> prefix = new LinkedList<>();
    	private final Deque<String> suffix = new LinkedList<>();

    	/**
    	 * Merge properties of another RunTraits instance.
    	 * @param other the RunTraits instance to merge
    	 */
    	public void mergeIn(RunTraits other) {
    		attributes.putAll(other.attributes());
    		prefix.addLast(other.prefix());
    		suffix.addFirst(other.suffix());
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
    	public String suffix() {
    		return suffix.stream().collect(Collectors.joining());
    	}

    	@Override
		public String toString() {
			return attributes.toString()+","+prefix+","+suffix;
		}
    }

    /**
     * Constructor.
     */
    protected RichTextConverterBase() {
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
        CollectingRunTraits traits = handleRunStarts(run);
        appendUnquoted(traits.prefix());
        appendChars(run);
        appendUnquoted(traits.suffix());
    }

    private Deque<Map<String, Object>> resetAttr = new LinkedList<>();

    private void pushRunAttributes(Map<String, Object> resetAttributes) {
        resetAttr.push(resetAttributes);
    }

    void handleRunEnds(Run run) {
        // handle run ends
    	getStyleList(run, TextAttributes.STYLE_END_RUN)
    		.forEach(style -> popRunAttributes());
    }

    private void popRunAttributes() {
        TextAttributes attributes = TextAttributes.of(resetAttr.pop());
        applyAttributes(attributes);

        for (Entry<String, Object> e : attributes.entrySet()) {
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

    /**
     * Apply text attributes to use for the following characters.
     * @param attributes the attributes to apply
     */
    protected abstract void applyAttributes(TextAttributes attributes);

    private CollectingRunTraits extractRunTraits(Run run) {
    	List<Style> styles = getStyleList(run, TextAttributes.STYLE_START_RUN);

        // collect traits for this run
        RunTraits currentTraits = createTraits(run);

        CollectingRunTraits traits = new CollectingRunTraits();
        HashMap<String,Object> m = new HashMap<>();
		Stream.concat(styles.stream().map(this::getTraits), Stream.of(currentTraits))
			.forEach(t -> {
				// store current attributes for resetting at end of style
			    t.attributes().keySet().stream()
			        .filter(attr ->  !attr.startsWith("__"))
			        .forEach(attr -> m.put(attr, currentAttributes.get(attr)));
				// merge traits
				traits.mergeIn(t);
			});
		pushRunAttributes(m);

		return traits;
    }

    protected SimpleRunTraits createTraits(Run run) {
        TextAttributes attributes = run.getAttributes();
        return new SimpleRunTraits(attributes);
    }

	@SuppressWarnings("unchecked")
	private List<Style> getStyleList(Run run, String property) {
		TextAttributes attributes = run.getAttributes();
		Object value = attributes.getOrDefault(property, Collections.emptyList());

		LangUtil.check(value instanceof List, "expected instance of List but got %s", value.getClass());
        return (List<Style>) value;
	}

    protected void appendChars(CharSequence chars) {
    	appendUnquoted(chars);
    }
    
    protected abstract void appendUnquoted(CharSequence chars);

    protected abstract boolean isValid();

}

