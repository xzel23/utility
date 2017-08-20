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

import com.dua3.utility.text.MarkDownStyle;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextAttributes.Attribute;
import com.dua3.utility.text.TextBuilder;

/**
 * A {@link TextBuilder} implementation for translating {@code RichText} to
 * {@code StyledDocument}.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends TextBuilder<StyledDocument> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    /**
     * Convert {@code RichText} to {@code StyledDocument} conserving text
     * attributes.
     *
     * @param text
     *            an instance of {@code RichText}
     * @param dfltAttr
     *            the default attributes to use
     * @param scale
     *            scaling factor to start with
     * @return instance of {@code StyledDocument} with {@code text} as its
     *         content
     */
    public static StyledDocument toStyledDocument(RichText text, AttributeSet dfltAttr, double scale) {
        StyledDocumentBuilder builder = new StyledDocumentBuilder(scale);
        builder.add(text);
        final StyledDocument doc = builder.get();
        doc.setParagraphAttributes(0, doc.getLength(), dfltAttr, false);
        return doc;
    }
    
    public static StyledDocument toStyledDocument(RichText text, AttributeSet dfltAttr) {
        return toStyledDocument(text, dfltAttr, 1f);
    }
    
    public static StyledDocument toStyledDocument(RichText text) {
        return toStyledDocument(text, new SimpleAttributeSet());
    }
    
    private final Function<String, TextAttributes> styleSupplier;

    private Map<String, AttributeSet> styleAttributes = new HashMap<>();

    private StyledDocument doc = new DefaultStyledDocument();
    private final double scale;

    public StyledDocumentBuilder(double scale) {
        this(scale, styleName -> TextAttributes.none());
    }

    public StyledDocumentBuilder(double scale, Function<String, TextAttributes> styleSupplier) {
        this.scale = scale;
        this.styleSupplier = styleSupplier;
    }

    @Override
    public StyledDocument get() {
        StyledDocument tmp = doc;
        doc = null;
        return tmp;
    }

    Map<String, BiConsumer<MutableAttributeSet, Object>> styles = defaultStyles();
    
    @SuppressWarnings("unchecked")
    Map<String, BiConsumer<MutableAttributeSet, Object>> defaultStyles() {
        Map<String, BiConsumer<MutableAttributeSet,Object>> styles = new HashMap<>();
        
        // TextAttributes.STYLE_NAME: unused
        styles.put(TextAttributes.FONT_FAMILY, (as,v) -> StyleConstants.setFontFamily(as, String.valueOf(v)) );
        styles.put(TextAttributes.FONT_FAMILY, (as,v) -> StyleConstants.setFontFamily(as, String.valueOf(v)));
        styles.put(TextAttributes.FONT_SIZE, (as,v) -> StyleConstants.setFontSize(as, (int) Math.round(scale * decodeFontSize(String.valueOf(v)))));
        styles.put(TextAttributes.COLOR, (as,v) -> StyleConstants.setForeground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        styles.put(TextAttributes.BACKGROUND_COLOR, (as,v) -> StyleConstants.setBackground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        styles.put(TextAttributes.FONT_WEIGHT, (as,v) -> StyleConstants.setBold(as, v.equals("bold")));
        styles.put(TextAttributes.FONT_STYLE, (as,v) -> {
            switch (String.valueOf(v)) {
            case "normal":
                StyleConstants.setItalic(as, false);
                break;
            case "italic":
            case "oblique":
                StyleConstants.setItalic(as, true);
                break;
            default:
                LOG.warn("Unknown value for FONT_STYLE: {}", v);
                break;
            }
        });
        styles.put(TextAttributes.TEXT_DECORATION, (as,v) -> {
            switch (String.valueOf(v)) {
            case "line-through":
                StyleConstants.setStrikeThrough(as, true);
                break;
            case "underline":
                StyleConstants.setUnderline(as, true);
                break;
            default:
                LOG.warn("Unknown value for TEXT_DECORATION: {}", v);
                break;
            }
        });
        styles.put(TextAttributes.STYLE_START_RUN, (as,v) -> {
            List<Attribute> attrs = (List<Attribute>) v;
            for (Attribute attr: attrs) {
                MarkDownStyle style = attr.style;
                AttributeSet attributes = getAttributeSetForStyle(style.name());
                as.addAttributes(attributes);
                LOG.debug("style");
            }
        });
        styles.put(TextAttributes.STYLE_END_RUN, (as,v) -> {
            // nop: StyledDocument Runs don't need end tags
        });
        return styles;
    }

    /**
     * Add style Attributes to an attribute set.
     * @param styleAttributes the styleAttributes to add
     * @param as the target MutableAttributeSet
     */
    private void applyAttributes(Map<String, Object> styleAttributes, MutableAttributeSet as) {
        for (Map.Entry<String, Object> e : styleAttributes.entrySet()) {
            styles.getOrDefault(e.getKey(), (k,v) -> LOG.warn("Unknown style: {}.", v)).accept(as,e.getValue());
        }
    }

    private AttributeSet getAttributeSetForStyle(String styleName) {
        return styleAttributes.computeIfAbsent(styleName, s -> getAttributes(styleSupplier.apply(s)));
    }

    private AttributeSet getAttributes(TextAttributes style) {
        SimpleAttributeSet as = new SimpleAttributeSet();
        applyAttributes(style.properties(), as);
        return as;
    }

    private SimpleAttributeSet getAttributeSet(Run run) {
        Map<String, Object> styleProps = run.getStyle().properties();
        AttributeSet das = getAttributeSetForStyle((String) styleProps.get(TextAttributes.STYLE_NAME));
        SimpleAttributeSet as = new SimpleAttributeSet(das);
        applyAttributes(styleProps, as);
        return as;
    }

    @Override
    protected void append(Run run) {
        try {
            AttributeSet as = getAttributeSet(run);
            doc.insertString(doc.getLength(), run.toString(), as);
        } catch (BadLocationException ex) {
            LOG.error("Exception in StyledDocumentBuilder.append()", ex);
        }
    }

    @Override
    protected boolean wasGetCalled() {
        return doc == null;
    }

}
