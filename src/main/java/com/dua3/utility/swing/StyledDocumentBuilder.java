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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.Pair;
import com.dua3.utility.text.MarkDownStyle;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextAttributes.Attribute;
import com.dua3.utility.text.TextBuilder;

/**
 * A {@link TextBuilder} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends TextBuilder<StyledDocument> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    private StyledDocument doc = new DefaultStyledDocument();
    private AttributeSet currentAttributes = new SimpleAttributeSet();

    /**
     * Enumeration of options that control generation of HTML.
     */
    public enum Option {
        /** Replace '.md' file extension in local links (i.e. with ".html") */
        REPLACEMENT_FOR_MD_EXTENSION_IN_LINK(String.class, null);

        final Class<?> valueClass;
        final Object defaultValue;

        private <T> Option(Class<T> clazz, T dflt) {
            this.valueClass = clazz;
            this.defaultValue = dflt;
        }
    }

    @SafeVarargs
    public static StyledDocument toStyledDocument(RichText text, Pair<Option, Object>... options) {
        return new StyledDocumentBuilder(options).add(text).get();
    }

    private static Object getOption(Map<Option, Object> optionMap, Option o) {
        return optionMap.getOrDefault(o, o.defaultValue);
    }

    Map<String, Function<Attribute, List<Pair<Object,Object>>>> attributes = new HashMap<>();


    private static void putAttributes(Map<String, Function<Attribute, List<Pair<Object,Object>>>> attributes,
            String styleName, Function<Attribute, List<Pair<Object,Object>>> f) {
        attributes.put(styleName, f);
    }

    @SafeVarargs
    private static void putAttributes(Map<String, Function<Attribute, List<Pair<Object,Object>>>> attributes,
            String styleName, Pair<Object,Object>... args) {
        attributes.put(styleName, attr -> Arrays.asList(args));
    }

    private final String replaceMdExtensionWith;

    private final Map<String, Function<Attribute, List<Pair<Object, Object>>>> styleAttributes = defaultStyleAttributes();

    @SafeVarargs
    public StyledDocumentBuilder(Pair<Option, Object>... options) {
        Map<Option, Object> optionMap = Pair.toMap(options);

        this.replaceMdExtensionWith = (String) getOption(optionMap, Option.REPLACEMENT_FOR_MD_EXTENSION_IN_LINK);
    }

    /**
     * Get Attributes that have to be set for this run.
     *
     * @param currentAttributes
     *      the current attributes
     * @param run
     *      the run
     * @return
     *      a pair consisting of the attributes to set and to reset
     */
    private Pair<MutableAttributeSet,MutableAttributeSet> getStyleAttributes(AttributeSet currentAttributes, Run run) {
        MutableAttributeSet setAttributes = new SimpleAttributeSet();
        MutableAttributeSet resetAttributes = new SimpleAttributeSet();

        // process styles whose runs terminate at this position and insert their closing tags (p.second)
        appendAttributesForRun(resetAttributes, run, TextAttributes.STYLE_END_RUN);
        // process styles whose runs start at this position and insert their opening tags (p.first)
        appendAttributesForRun(setAttributes, run, TextAttributes.STYLE_START_RUN);

        for (Map.Entry<String, Object> e : run.getStyle().properties().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            if (key.startsWith("__")) {
                // don't create spans for meta info
                continue;
            }

            setAttributes.addAttribute(key, value);
            resetAttributes.addAttribute(key, currentAttributes.getAttribute(key));
        }


        return Pair.of(setAttributes, resetAttributes);
    }

    private void appendAttributesForRun(MutableAttributeSet attributeSet, Run run, String property) {
        TextAttributes style = run.getStyle();
        Object value = style.properties().get(property);

        if (value != null) {
            if (!(value instanceof List)) {
                throw new IllegalStateException(
                        "expected a value of class List but got " + value.getClass() + " (property=" + property + ")");
            }

            @SuppressWarnings("unchecked")
            List<Attribute> attributes = (List<Attribute>) value;
            for (Attribute attr : attributes) {
                Function<Attribute, List<Pair<Object, Object>>> fAttr = styleAttributes.get(attr.style.name());
                if (fAttr != null) {
                    for (Pair<Object, Object> e: fAttr.apply(attr)) {
                        Object attrName = e.first;
                        Object attrValue = e.second;
                        BiConsumer<MutableAttributeSet, Object> consumer = styles.get(attrName);
                        consumer.accept(attributeSet, attrValue);
                    }
                }
            }
        }
    }

    private float scale = 1f;

    Map<String, BiConsumer<MutableAttributeSet, Object>> defaultStyles() {
        Map<String, BiConsumer<MutableAttributeSet,Object>> styles = new HashMap<>();

        // TextAttributes.STYLE_NAME: unused
        styles.put(TextAttributes.FONT_FAMILY, (as,v) -> StyleConstants.setFontFamily(as, String.valueOf(v)) );
        styles.put(TextAttributes.FONT_FAMILY, (as,v) -> StyleConstants.setFontFamily(as, String.valueOf(v)));
        styles.put(TextAttributes.FONT_SIZE, (as,v) -> StyleConstants.setFontSize(as, Math.round(scale * decodeFontSize(String.valueOf(v)))));
        styles.put(TextAttributes.COLOR, (as,v) -> StyleConstants.setForeground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        styles.put(TextAttributes.BACKGROUND_COLOR, (as,v) -> StyleConstants.setBackground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        styles.put(TextAttributes.FONT_WEIGHT, (as,v) -> StyleConstants.setBold(as, v.equals(TextAttributes.FONT_WEIGHT_VALUE_BOLD)));
        styles.put(TextAttributes.FONT_STYLE, (as,v) -> {
            switch (String.valueOf(v)) {
            case TextAttributes.FONT_STYLE_VALUE_NORMAL:
                StyleConstants.setItalic(as, false);
                break;
            case TextAttributes.FONT_STYLE_VALUE_ITALIC:
            case TextAttributes.FONT_STYLE_VALUE_OBLIQUE:
                StyleConstants.setItalic(as, true);
                break;
            default:
                LOG.warn("Unknown value for FONT_STYLE: {}", v);
                break;
            }
        });
        styles.put(TextAttributes.TEXT_DECORATION, (as,v) -> {
            switch (String.valueOf(v)) {
            case TextAttributes.TEXT_DECORATION_VALUE_LINE_THROUGH:
                StyleConstants.setStrikeThrough(as, true);
                break;
            case TextAttributes.TEXT_DECORATION_VALUE_UNDERLINE:
                StyleConstants.setUnderline(as, true);
                break;
            default:
                LOG.warn("Unknown value for TEXT_DECORATION: {}", v);
                break;
            }
        });
        return styles;
    }

    private final Map<String, BiConsumer<MutableAttributeSet, Object>> styles = defaultStyles();

    private Map<String, Function<Attribute, List<Pair<Object, Object>>>> defaultStyleAttributes() {
        Map<String, Function<Attribute, List<Pair<Object,Object>>>> attributes = new HashMap<>();

        putAttributes(attributes, MarkDownStyle.STRONG_EMPHASIS.name(), Pair.of(TextAttributes.FONT_WEIGHT, TextAttributes.FONT_WEIGHT_VALUE_BOLD));

        /*
        putAttributes(attributes, MarkDownStyle.BLOCK_QUOTE.name(), attr -> "<blockquote>");
        putAttributes(attributes, MarkDownStyle.BULLET_LIST.name(), attr -> "<ul>\n");
        putAttributes(attributes, MarkDownStyle.CODE.name(), attr -> "<code>");
        putAttributes(attributes, MarkDownStyle.DOCUMENT.name());
        putAttributes(attributes, MarkDownStyle.EMPHASIS.name(), attr -> "<em>");
        putAttributes(attributes, MarkDownStyle.FENCED_CODE_BLOCK.name(), attr -> "<pre><code>\n");
        putAttributes(attributes, MarkDownStyle.HARD_LINE_BREAK.name(), attr -> "\n<br>");
        putAttributes(attributes, MarkDownStyle.HEADING.name(),
                attr -> "<h" + attr.args.get(MarkDownStyle.ATTR_HEADING_LEVEL)
                        + attrText(attr.args, MarkDownStyle.ATTR_ID, "id", "")
                        + ">");
        putAttributes(attributes, MarkDownStyle.THEMATIC_BREAK.name(), attr -> "\n<hr>");
        // HTML_BLOCK
        // HTML_INLINE
        putAttributes(attributes, MarkDownStyle.IMAGE.name(),
                attr -> "<img"
                        + attrText(attr.args, MarkDownStyle.ATTR_IMAGE_SRC, "src", "")
                        + attrText(attr.args, MarkDownStyle.ATTR_IMAGE_TITLE, "title", null)
                        + attrText(attr.args, MarkDownStyle.ATTR_IMAGE_ALT, "alt", null)
                        + ">");
        putAttributes(attributes, MarkDownStyle.INDENTED_CODE_BLOCK.name(), attr -> "<pre><code>\n");
        putAttributes(attributes, MarkDownStyle.LINK.name(),
                attr -> {
                    String href = attr.args.getOrDefault(MarkDownStyle.ATTR_LINK_HREF, "").toString();
                    if (replaceMdExtensionWith != null) {
                        href = href.replaceAll("(\\.md|\\.MD)(\\?|#|$)", replaceMdExtensionWith + "$2");
                    }
                    String hrefAttr = " href=\"" + href + "\"";

                    //
                    return "<a"
                            + hrefAttr
                            + attrText(attr.args, MarkDownStyle.ATTR_LINK_TITLE, "title", null)
                            + ifSet(attr.args, MarkDownStyle.ATTR_LINK_EXTERN,
                                    " target=\"" + targetForExternLinks + "\"")
                            + ">";
                });
        putAttributes(attributes, MarkDownStyle.LIST_ITEM.name(), attr -> "<li>");
        putAttributes(attributes, MarkDownStyle.ORDERED_LIST.name(), attr -> "<ol>\n");
        putAttributes(attributes, MarkDownStyle.PARAGRAPH.name(), attr -> "<p>");
        putAttributes(attributes, MarkDownStyle.SOFT_LINE_BREAK.name(), attr -> "");
        putAttributes(attributes, MarkDownStyle.STRONG_EMPHASIS.name(), attr -> "<strong>");
        putAttributes(attributes, MarkDownStyle.TEXT.name(), attr -> "");
        // CUSTOM_BLOCK
        // CUSTOM_NODE
*/
        return Collections.unmodifiableMap(attributes);
    }

    private String ifSet(Map<String, Object> args, String textAttribute, String textIfPresent) {
        Object value = args.get(textAttribute);

        boolean isSet;
        if (value instanceof Boolean) {
            isSet = (boolean) value;
        } else {
            isSet = Boolean.valueOf(String.valueOf(value));
        }

        return isSet ? textIfPresent : "";
    }

    @Override
    protected void append(Run run) {
        // handle attributes
        Pair<? extends AttributeSet,? extends AttributeSet> attributes = getStyleAttributes(currentAttributes, run);

        AttributeSet setAttributes = attributes.first;
        AttributeSet resetAttributes = attributes.second;
        // append text (need to do characterwise because of escaping)
        append(run.toString(),setAttributes);

        currentAttributes = resetAttributes;
    }

    private void append(String text, AttributeSet as) {
        try {
            doc.insertString(doc.getLength(), text, as);
        } catch (BadLocationException e) {
            // this should not happen
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StyledDocument get() {
        StyledDocument ret = doc;
        doc=null;
        return ret;
    }

    @Override
    protected boolean wasGetCalled() {
        return doc == null;
    }

    @Override
    public String toString() {
        return doc.toString();
    }
}
