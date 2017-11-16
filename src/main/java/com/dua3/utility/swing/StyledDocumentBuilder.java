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

import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.RichTextConverter;

/**
 * A {@link RichTextConverter} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends RichTextConverter<StyledDocument> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    private StyledDocument doc = new DefaultStyledDocument();
    private MutableAttributeSet currentAttributes = new SimpleAttributeSet();

    public static StyledDocument toStyledDocument(RichText text) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder();
        return builder.add(text).get();
    }

    public static StyledDocument toStyledDocument(RichText text, Function<Style, TextAttributes> styleSupplier) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder();
        builder.setStyleSupplier(styleSupplier);
        return builder.add(text).get();
    }

    public static StyledDocument toStyledDocument(RichText text, Map<String, Function<Style, TextAttributes>> styleMap) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder();
        builder.setStyleMap(styleMap);
        return builder.add(text).get();
    }

    public static StyledDocument toStyledDocument(RichText text, AttributeSet dfltAttr, double scale) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder();
        builder.setScale(scale);
        StyledDocument doc = builder.add(text).get();
        doc.setParagraphAttributes(0, doc.getLength(), dfltAttr, false);
        return doc;
    }

    private Function<Style, TextAttributes> styleSupplier;

    public void setStyleSupplier(Function<Style, TextAttributes> styleSupplier) {
        this.styleSupplier = Objects.requireNonNull(styleSupplier);
    }

    public void setStyleMap(Map<String, Function<Style, TextAttributes>> styleMap) {
        Function<Style, TextAttributes> supplier = s -> {
            String styleName = String.valueOf(s.get(TextAttributes.STYLE_NAME));
            return styleMap.getOrDefault(styleName, attr -> TextAttributes.none()).apply(s);
        };
        this.styleSupplier = supplier;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public StyledDocumentBuilder() {
        this.currentAttributes = new SimpleAttributeSet();
        this.styleSupplier = attr -> TextAttributes.none();
    }

    private int countRunEnds(Run run) {
        List<?> attrEndOfRun = (List<?>) run.getStyle().properties().get(TextAttributes.STYLE_END_RUN);
        return  attrEndOfRun == null ? 0 : attrEndOfRun.size();
    }

    private void appendAttributesForRun(MutableAttributeSet attributeSet, Run run, String property) {
        TextAttributes style = run.getStyle();
        Object value = style.properties().get(property);

        if (value == null) {
            return;
        }

        if (!(value instanceof List)) {
            throw new IllegalStateException(
                    "expected a value of class List but got " + value.getClass() + " (property=" + property + ")");
        }

        @SuppressWarnings("unchecked")
        List<Style> attributes = (List<Style>) value;
        for (Style attr : attributes) {
            // collect attributes
            MutableAttributeSet runAttributes = new SimpleAttributeSet();
            for (Entry<String, Object> e: styleSupplier.apply(attr).properties().entrySet()) {
                Object attrName = e.getKey();
                Object attrValue = e.getValue();
                BiConsumer<MutableAttributeSet, Object> consumer = styles.get(attrName);
                consumer.accept(runAttributes, attrValue);
            }
            // store current values
            List<Pair<Object,Object>> resetAttributes = new ArrayList<>();
            Enumeration<?> names = runAttributes.getAttributeNames();
            while (names.hasMoreElements()) {
                Object name = names.nextElement();
                resetAttributes.add(Pair.of(name, currentAttributes.getAttribute(attr)));
            }
            pushRunAttributes(resetAttributes);
            // apply run attributes to the set
            attributeSet.addAttributes(runAttributes);
        }
    }

    private double scale = 1;

    Map<String, BiConsumer<MutableAttributeSet, Object>> defaultStyles() {
        Map<String, BiConsumer<MutableAttributeSet,Object>> styles = new HashMap<>();

        // TextAttributes.STYLE_NAME: unused
        styles.put(TextAttributes.FONT_FAMILY, this::setFontFamily );
        styles.put(TextAttributes.FONT_SIZE, this::setFontSize);
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
        styles.put(TextAttributes.TEXT_INDENT_LEFT, (as,v) -> {
            StyleConstants.setLeftIndent(as, Float.valueOf(v.toString()));
        });
        return styles;
    }

    private void setFontSize(MutableAttributeSet as, Object value) {
        double fontSize = decodeFontSize(String.valueOf(value));
        StyleConstants.setFontSize(as, (int) Math.round(scale * fontSize));
    }

    private void setFontFamily(MutableAttributeSet as, Object value) {
        String family = String.valueOf(value);
        // translate standard fontspec families to corresponding Java names that are guaranteed to be present
        // see https://docs.oracle.com/javase/8/docs/technotes/guides/intl/fontconfig.html
        // > The Java Platform defines five logical font names that every implementation must support:
        // > Serif, SansSerif, Monospaced, Dialog, and DialogInput. These logical font names are mapped
        // > to physical fonts in implementation dependent ways.
        switch (family) {
        case TextAttributes.FONT_FAMILY_VALUE_MONOSPACE:
            StyleConstants.setFontFamily(as, "Monospaced");
            break;
        case TextAttributes.FONT_FAMILY_VALUE_SANS_SERIF:
            StyleConstants.setFontFamily(as, "SansSerif");
            break;
        case TextAttributes.FONT_FAMILY_VALUE_SERIF:
            StyleConstants.setFontFamily(as, "Serif");
            break;
        default:
            StyleConstants.setFontFamily(as, family);
            break;
        }
    }

    private final Map<String, BiConsumer<MutableAttributeSet, Object>> styles = defaultStyles();

    @Override
    protected void append(Run run) {
        handleRunEnds(run);
        handleRunStarts(run);
        append(run.toString(),currentAttributes);
    }

    void handleRunStarts(Run run) {
        MutableAttributeSet setAttributes = new SimpleAttributeSet();

        // process styles whose runs start at this position and insert their opening tags (p.first)
        appendAttributesForRun(setAttributes, run, TextAttributes.STYLE_START_RUN);

        for (Map.Entry<String, Object> e : run.getStyle().properties().entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();

            assert key != null;
            assert value != null;

            if (key.startsWith("__")) {
                // don't create spans for meta info
                continue;
            }

            setAttributes.addAttribute(key, value);
        }

        applyRunAttributes(setAttributes);
    }

    void handleRunEnds(Run run) {
        // handle run ends
        int runEnds = countRunEnds(run);
        for (int i=0;i<runEnds;i++) {
            popRunAttributes();
        }
    }

    private void applyRunAttributes(AttributeSet attrs) {
        currentAttributes.addAttributes(attrs);
    }

    private Deque<List<Pair<Object,Object>>> resetAttr = new LinkedList<>();

    private void pushRunAttributes(List<Pair<Object, Object>> as) {
        resetAttr.push(as);
    }

    private void popRunAttributes() {
        for (Pair<Object, Object> e: resetAttr.pop()) {
            Object attr = e.first;
            Object value = e.second;
            if(value!=null) {
                currentAttributes.addAttribute(attr, value);
            } else {
                currentAttributes.removeAttribute(attr);
            }
        }
    }

    private static final Object[] PARAGRAPH_ATTRIBUTES = {
      StyleConstants.ParagraphConstants.LeftIndent
    };

    private AttributeSet getParagraphAttributes(AttributeSet as) {
        SimpleAttributeSet pa = new SimpleAttributeSet();
        for (Object attr: PARAGRAPH_ATTRIBUTES) {
            Object value = as.getAttribute(attr);
            if (value != null) {
                pa.addAttribute(attr, value);
            }
        }
        return pa;
    }

    private Deque<Pair<Integer,AttributeSet>> paragraphAttributes = new LinkedList<>();

    private void append(String text, AttributeSet as) {
        try {
            int pos = doc.getLength();
            doc.insertString(pos, text, as);

            AttributeSet pa = getParagraphAttributes(as);
            paragraphAttributes.add(Pair.of(text.length(), pa));
        } catch (BadLocationException e) {
            // this should not happen
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StyledDocument get() {
        // apply paragraph styles
        int pos = 0;
        for (Pair<Integer,AttributeSet> e: paragraphAttributes) {
            doc.setParagraphAttributes(pos, e.first, e.second, false);
            pos += e.first;
        }

        // mark builder invalid by clearing doc
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
