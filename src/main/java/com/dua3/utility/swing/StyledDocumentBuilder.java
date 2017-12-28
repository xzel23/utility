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
package com.dua3.utility.swing;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.Color;
import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.MarkDownStyle;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextConverterBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends RichTextConverterBase<DocumentExt> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    private static final Object[] PARAGRAPH_ATTRIBUTES = {
            StyleConstants.ParagraphConstants.LeftIndent,
            StyleConstants.ParagraphConstants.Alignment
    };

    /** Option to set the scaling factor. */
    public static final String SCALE = "scale";
    public static final String ATTRIBUTE_SET = "attribute-set";
    public static final String FONT_SIZE = "font-size";

    private static final Map<String, Object> DEFAULT_OPTIONS = LangUtil.map(Pair.of(SCALE, 1f));

    private static final List<String> VOID_HTML_ELEMENTS = Arrays.asList(
            "area", "base", "br", "col", "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param", "source", "track", "wbr"
        );

    @SafeVarargs
    public static DocumentExt toStyledDocument(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, Object>... options) {
        // create map with default options
        Map<String, Object> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrrides

        return new StyledDocumentBuilder(new DocumentExt(), styleTraits, optionMap).add(text).get();
    }

    private DocumentExt buffer;

    private float scale = 1;

    private Deque<Pair<Integer, AttributeSet>> paragraphAttributes = new LinkedList<>();

    private final float defaultFontSize;

    private StyledDocumentBuilder(DocumentExt buffer, Function<Style, TextAttributes> styleTraits, Map<String, Object> options) {
    	super(styleTraits);

    	this.buffer = buffer;

    	this.scale = ((Number) options.getOrDefault(SCALE, 1)).floatValue();
    	this.defaultFontSize = ((Number) options.getOrDefault(FONT_SIZE, 12)).floatValue();
    	this.attributeSet = (MutableAttributeSet) options.getOrDefault(ATTRIBUTE_SET, new SimpleAttributeSet());

        setDefaultFgColor(getColor(this.attributeSet, StyleConstants.Foreground, Color.BLACK));
        setDefaultBgColor(Color.TRANSPARENT_WHITE);
    }

    private static Color getColor(AttributeSet as, Object key, Color dfltColor) {
        Object value = as.getAttribute(key);
        return value != null ? SwingUtil.toColor((java.awt.Color) value) : dfltColor;
    }

    @Override
    public DocumentExt get() {
        // apply paragraph styles
        int pos = 0;
        for (Pair<Integer, AttributeSet> e : paragraphAttributes) {
            buffer.setParagraphAttributes(pos, e.first, e.second, false);
            pos += e.first;
        }

        // consistency checks
        LangUtil.check(openedTags.isEmpty(), "there still are open tags");

        // mark builder invalid by clearing buffer
        DocumentExt ret = buffer;
        buffer = null;

        return ret;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    private AttributeSet getParagraphAttributes(AttributeSet as) {
        SimpleAttributeSet pa = new SimpleAttributeSet();
        for (Object attr : PARAGRAPH_ATTRIBUTES) {
            Object value = as.getAttribute(attr);
            if (value != null) {
                pa.addAttribute(attr, value);
            }
        }
        return pa;
    }

    @Override
    protected boolean isValid() {
        return buffer != null;
    }

    private final MutableAttributeSet attributeSet;

    @Override
    protected void applyAttributes(TextAttributes attributes) {
        for (Entry<String, Object> entry : attributes.entrySet()) {
            String attribute = entry.getKey();
            Object value = entry.getValue();

            switch (attribute) {
            case TextAttributes.COLOR:
                StyleConstants.setForeground(attributeSet, SwingUtil.toAwtColor(getColor(value, getDefaultFgColor())));
                break;
            case TextAttributes.BACKGROUND_COLOR:
                StyleConstants.setBackground(attributeSet, SwingUtil.toAwtColor(getColor(value, getDefaultBgColor())));
                break;
            case TextAttributes.FONT_FAMILY:
                StyleConstants.setFontFamily(attributeSet, String.valueOf(value));
                break;
            case TextAttributes.FONT_STYLE:
                StyleConstants.setItalic(attributeSet,
                        TextAttributes.FONT_STYLE_VALUE_ITALIC.equals(value)
                        || TextAttributes.FONT_STYLE_VALUE_OBLIQUE.equals(value));
                break;
            case TextAttributes.FONT_SIZE:
                StyleConstants.setFontSize(
                		attributeSet,
                		value==null
                		? Math.round(scale*defaultFontSize)
                		: Math.round(scale*TextUtil.decodeFontSize(String.valueOf(value))));
                break;
            case TextAttributes.FONT_SCALE:
                StyleConstants.setFontSize(
                		attributeSet,
                		value==null
                		? Math.round(scale*defaultFontSize)
                		: Math.round(scale*defaultFontSize*Float.parseFloat(String.valueOf(value))));
                break;
            case TextAttributes.FONT_WEIGHT:
                StyleConstants.setBold(attributeSet, TextAttributes.FONT_WEIGHT_VALUE_BOLD.equals(value));
                break;
            case TextAttributes.TEXT_DECORATION:
                switch (String.valueOf(value)) {
                case TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE:
                    StyleConstants.setUnderline(attributeSet, true);
                    break;
                case TextAttributes.TEXT_DECORATION_VALUE_LINE_THROUGH:
                    StyleConstants.setStrikeThrough(attributeSet, true);
                    break;
                case TextAttributes.TEXT_DECORATION_VALUE_NONE:
                default:
                    StyleConstants.setUnderline(attributeSet, false);
                    StyleConstants.setStrikeThrough(attributeSet, false);
                    break;
                }
                break;
            case TextAttributes.FONT_VARIANT:
            	break;
            case TextAttributes.TEXT_INDENT_LEFT:
                // TODO
                break;
            case TextAttributes.STYLE_START_RUN:
            case TextAttributes.STYLE_END_RUN:
            	break;
            default:
            	LOG.warn("unknown: {}", attribute);
                break;
            }
        }
    }

	@Override
	protected void appendUnquoted(CharSequence chars) {
	    try {
            int pos = buffer.getLength();
            buffer.insertString(pos, chars.toString(), attributeSet);

            AttributeSet pa = getParagraphAttributes(attributeSet);
            paragraphAttributes.add(Pair.of(chars.length(), pa));
	    } catch (BadLocationException e) {
	        LOG.error("unexpected error", e);
	        throw new IllegalStateException(e);
	    }
	}

	private boolean htmlMode = false;

	@Override
	protected void append(Run run) {
		TextAttributes attrs = run.getAttributes();

		// look if HTML mode ends here
		List<?> styleEnd = (List<?>) attrs.getOrDefault(TextAttributes.STYLE_END_RUN, Collections.emptyList());
		toggleHtmlMode(styleEnd, false);

		// look if HTML mode starts here
		List<?> styleStart = (List<?>) attrs.getOrDefault(TextAttributes.STYLE_START_RUN, Collections.emptyList());
		toggleHtmlMode(styleStart, true);

		super.append(run);
	}

	@Override
	protected void appendChars(CharSequence chars) {
		if (htmlMode) {
			appendHTML(chars);
		} else {
			super.appendChars(chars);
		}

	}

	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile(
	        "(?<attribute>[a-zA-Z]\\w*)=(?<value>(\"[^\"]*\"|'[^']*'|\\w+))"
	    );
    private static final Pattern PATTERN_TAG_OPEN = Pattern.compile(
            "<(?<tagOpen>[a-zA-Z]\\w*)(?<attributes>( "+PATTERN_ATTRIBUTE+")*)(?<selfclosing>/)?>"
        );
    private static final Pattern PATTERN_TAG_CLOSE = Pattern.compile(
            "</(?<tagClose>[a-zA-Z]\\w*)>"
        );
    private static final Pattern PATTERN_TAG = Pattern.compile(
            PATTERN_TAG_OPEN.toString()+"|"+PATTERN_TAG_CLOSE.toString()
        );

    private final Deque<String> openedTags = new LinkedList<>();

    private void appendHTML(CharSequence chars) {
	    Matcher matcher = PATTERN_TAG.matcher(chars);

	    int pos = 0;
	    while (matcher.find(pos)) {
	        int start = matcher.start();
	        int end = matcher.end();

	        // append normal text that comes before the next tag found
	        CharSequence textBefore = chars.subSequence(pos, start);
	        super.appendChars(textBefore);

	        // handle tag
	        Optional<CharSequence> tagOpen = TextUtil.group(matcher, chars, "tagOpen");
            Optional<CharSequence> tagClose = TextUtil.group(matcher, chars, "tagClose");
            LangUtil.check(
                    tagOpen.isPresent()^tagClose.isPresent(),
                    "matcher is expected to find either an opening or a closing tag"
                );

            if (tagOpen.isPresent()) {
                // opening tag
                String tag = tagOpen.get().toString();
                Map<String,String> attributes = extractAttributes(TextUtil.group(matcher, chars, "attributes")
                        .orElseThrow(IllegalStateException::new));
                boolean selfClosing = TextUtil.group(matcher, chars, "selfclosing").isPresent();

                appendTag(tag, attributes, selfClosing);
            }

            if (tagClose.isPresent()) {
                // closing tag
                closeTag(tagClose.get().toString());
            }

            // advance position to the end of this match
            pos = end;
	    }

	    CharSequence text = chars.subSequence(pos, chars.length());
        super.appendChars(text);
	}

    static class Tag {
        private final String name;
        private final Map<String,String> attributes;

        public Tag(String name, Map<String,String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public Component createComponent() {
            switch (name) {
            case "input":
                return createInput();
            default:
                LOG.warn("unknown tag: <{}>", name);
                return new JLabel(attributes.getOrDefault("value", ""));
            }
        }

        Component createInput() {
            String type = attributes.getOrDefault("type", "");
            switch (type) {
            case "text": {
                    JTextField component = new JTextField();
                    LangUtil.consumeIfPresent(attributes, "size", v -> component.setColumns(Integer.parseInt(v)));
                    LangUtil.consumeIfPresent(attributes, "value", v -> component.setText(v));
                    return component;
                }
            case "checkbox": {
                    JCheckBox component = new JCheckBox();
                    LangUtil.consumeIfPresent(attributes, "checked", v -> component.setSelected(true));
                    return component;
                }
            case "radio": {
                JRadioButton component = new JRadioButton();
                LangUtil.consumeIfPresent(attributes, "selected", v -> component.setSelected(true));
                return component;
            }
            default:
                LOG.warn("unknown input type: '{}'", type);
                return new JLabel(attributes.getOrDefault("value", ""));
            }
        }
    }

	private void appendTag(String tag, Map<String, String> attributes, boolean selfClosing) {
        LOG.debug("insert {}tag <{}> with attributes {}", selfClosing?"selfclosing " : "", tag, attributes);

       addTag(new Tag(tag, attributes));

        if (selfClosing) {
            LOG.debug("<{}/>", tag);
        } else if (VOID_HTML_ELEMENTS.contains(tag)) {
            LOG.debug("<{}> [void element, no closing tag expected]", tag);
        } else {
            openedTags.push(tag);
            LOG.debug("<{}>", tag);
        }
    }

    private void addTag(Tag tag) {
        int pos = buffer.getLength();
        buffer.insertTag(pos, tag);
    }

    private void closeTag(String tag) {
        LangUtil.check(!openedTags.isEmpty(), "there are no tags open (trying to close <%s>)", tag);
        String popped = openedTags.pop();
        LangUtil.check(popped.equals(tag), "tag mismatch, expected </%s>, found </%s>", popped, tag);
        LOG.debug("</{}>", tag);
    }

    /**
	 * Extract attribute definitions from tag declaration.
	 * @param chars
	 *     the text to process, a sequence in the form {@code attr1=value1 attr2="value2" attr3='value3' ...}
	 * @return
	 *     map {@code attribute -> value}
	 */
	private Map<String, String> extractAttributes(CharSequence chars) {
	    if (chars==null || chars.length()==0) {
	        return Collections.emptyMap();
	    }

	    Matcher matcher = PATTERN_ATTRIBUTE.matcher(chars);
	    int pos=0;
	    Map<String,String> attributes = new HashMap<>();
	    while (matcher.find(pos)) {
	        String attribute = matcher.group("attribute");

	        String value = matcher.group("value");
            if (value.length()>=2
                    && (value.charAt(0)=='"' || value.charAt(0)=='\'')
                    && (value.charAt(value.length()-1)==value.charAt(0))) {
                // remove single and double quotes around value
                value = value.substring(1, value.length()-1);
            }
            attributes.put(attribute, value);
            pos = matcher.end();
	    }
        return attributes;
    }

    private void toggleHtmlMode(List<?> styleEnd, boolean enable) {
		for (Object obj: styleEnd) {
			if (obj instanceof Style) {
				Style s = (Style) obj;
				Object styleClass = s.get(TextAttributes.STYLE_CLASS);
				Object styleName = s.get(TextAttributes.STYLE_NAME);
				if (MarkDownStyle.CLASS.equals(styleClass)
				        && LangUtil.isOneOf(styleName,
				                MarkDownStyle.HTML_BLOCK.name(),
				                MarkDownStyle.HTML_INLINE.name())) {
					if (htmlMode==enable) {
						LOG.warn("HTML-mode: inconsistency detected");
					}
					htmlMode = enable;
				}
			}
		}
	}
}
