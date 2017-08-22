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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private MutableAttributeSet currentAttributes = new SimpleAttributeSet();

    public static StyledDocument toStyledDocument(RichText text) {
        return new StyledDocumentBuilder().add(text).get();
    }

    private final Function<Attribute, TextAttributes> styleSupplier;

    public StyledDocumentBuilder(Function<Attribute, TextAttributes> styleSupplier) {
        this.styleSupplier = styleSupplier;
    }

    public StyledDocumentBuilder() {
        this(s->TextAttributes.none());
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
                for (Entry<String, Object> e: styleSupplier.apply(attr).properties().entrySet()) {
                    Object attrName = e.getKey();
                    Object attrValue = e.getValue();
                    BiConsumer<MutableAttributeSet, Object> consumer = styles.get(attrName);
                    consumer.accept(attributeSet, attrValue);
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

    @Override
    protected void append(Run run) {
        // handle attributes
        Pair<? extends AttributeSet,? extends AttributeSet> attributes = getStyleAttributes(currentAttributes, run);

        AttributeSet setAttributes = attributes.first;
        AttributeSet resetAttributes = attributes.second;

        currentAttributes.removeAttributes(resetAttributes);
        currentAttributes.addAttributes(setAttributes);

        // append text (need to do characterwise because of escaping)
        append(run.toString(),currentAttributes);
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
