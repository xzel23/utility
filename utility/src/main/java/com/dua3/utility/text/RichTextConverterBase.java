// Copyright (c) 2019 Axel Howind
// 
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dua3.utility.Color;
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

    private final Map<String,Object> currentAttributes = new HashMap<>();
    private final Function<Style, TextAttributes> styleTraits;

    protected RunTraits getTraits(Style style) {
        return new RunTraits(styleTraits.apply(style));
    }

    private Color defaultFgColor = Color.BLACK;
    private Color defaultBgColor = Color.WHITE;

    public Color getDefaultFgColor() {
        return defaultFgColor;
    }

    public void setDefaultFgColor(Color c) {
        this.defaultFgColor = c;
    }

    public Color getDefaultBgColor() {
		return defaultBgColor;
	}

    public void setDefaultBgColor(Color defaultBgColor) {
		this.defaultBgColor = defaultBgColor;
	}

    protected Color getColor(Object color, Color defaultColor) {
        return color == null ? defaultColor : Color.valueOf(color.toString());
    }

    private static final RunTraits EMPTY_RUN_TRAITS = new RunTraits(TextAttributes.none());

    public static RunTraits emptyTraits() {
        return EMPTY_RUN_TRAITS;
    }

    /**
     * The traits of a Run, consisting of:
     * <ul>
     * <li> the TextAttributes to apply
     * <li> text to prepend
     * <li> text to append
     * </ul>
     */
    public static class RunTraits {
    	private final TextAttributes attributes;
		private final String prefix;
		private final String suffix;

		public RunTraits(TextAttributes attributes) {
			this(attributes, "", "");
		}

		public RunTraits(TextAttributes attributes, String prefix, String suffix) {
			this.attributes = attributes;
			this.prefix = extract(attributes, TextAttributes.STYLE_START_RUN, TextAttributes.TEXT_PREFIX)+prefix;
            this.suffix = suffix+extract(attributes, TextAttributes.STYLE_END_RUN, TextAttributes.TEXT_SUFFIX);
		}

		private static String extract(TextAttributes attributes, String tagStartOrEnd, String tag) {
            @SuppressWarnings("unchecked")
            List<Style> styles = (List<Style>) attributes.getOrDefault(tagStartOrEnd, Collections.emptyList());
            return styles.stream().map(s -> s.getOrDefault(tag,"").toString()).collect(Collectors.joining());
        }

        public TextAttributes attributes() { return attributes; }
        public String prefix() { return prefix; }
        public String suffix() { return suffix; }

		@Override
        public String toString() {
			return attributes.toString()+",\""+prefix+"\",\""+suffix+"\"";
		}
    }

    /**
     * Constructor.
     * @param styleTraits the style traits to use
     */
    protected RichTextConverterBase(Function<Style, TextAttributes> styleTraits) {
        this.styleTraits = styleTraits;
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
        String suffix = handleRunEnds(run);
        String prefix = handleRunStarts(run);
        appendUnquoted(suffix);
        appendUnquoted(prefix);
        appendChars(run);
    }

    private Deque<Map<String, Object>> resetAttr = new LinkedList<>();

    private void pushRunAttributes(Map<String, Object> resetAttributes) {
        resetAttr.push(resetAttributes);
    }

    private String handleRunEnds(Run run) {
        StringBuilder suffix = new StringBuilder();

        List<Style> styles = getStyleList(run, TextAttributes.STYLE_END_RUN);
        styles.stream().map(this::getTraits)
        .forEach(t -> {
            popRunAttributes();
            suffix.append(t.suffix());
        });

        return  suffix.toString();
    }

    private void popRunAttributes() {
        // restore the attributes
        TextAttributes attributes = TextAttributes.of(resetAttr.pop());
        applyAttributes(attributes);
        // update currentAttributes with the new values
        attributes.entrySet().forEach(entry -> currentAttributes.put(entry.getKey(), entry.getValue()));
    }

    String handleRunStarts(Run run) {
        StringBuilder prefix = new StringBuilder();

    	// process styles whose runs start at this position
        List<Style> styles = getStyleList(run, TextAttributes.STYLE_START_RUN);

        // collect traits for this run
        RunTraits currentTraits = createTraits(run);
        prefix.append(currentTraits.prefix());

        Map<String,Object> attributes = new HashMap<>(currentTraits.attributes());
        styles.stream().map(this::getTraits)
        	.forEach(t -> {
        		// store current attributes for resetting at end of style
                Map<String,Object> m = new HashMap<>();
        	    t.attributes().keySet().stream()
        	        .filter(attr ->  !attr.startsWith("__"))
        	        .forEach(attr -> m.put(attr, currentAttributes.get(attr)));
        	    pushRunAttributes(m);
        	    // also store for updating the actual style
                attributes.putAll(t.attributes());
        		// merge traits
        	    prefix.append(t.prefix());
        	});
        applyAttributes(TextAttributes.of(attributes));
        return prefix.toString();
    }

    /**
     * Apply text attributes to use for the following characters.
     * @param attributes the attributes to apply
     */
    protected abstract void applyAttributes(TextAttributes attributes);

    protected RunTraits createTraits(Run run) {
        TextAttributes attributes = run.getAttributes();
        return new RunTraits(attributes);
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

