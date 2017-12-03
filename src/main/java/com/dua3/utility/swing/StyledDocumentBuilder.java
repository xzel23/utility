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

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
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

import com.dua3.utility.Color;
import com.dua3.utility.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextConverterBase;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating {@code RichText} to
 * HTML.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends RichTextConverterBase<StyledDocument> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    private static final Object[] PARAGRAPH_ATTRIBUTES = {
            StyleConstants.ParagraphConstants.LeftIndent
    };

    /** Option to set the scaling factor. */
    public static final String SCALE = "scale";
    public static final String ATTRIBUTE_SET = "attribute-set";
    public static final String FONT_SIZE = "font-size";

    private static final Map<String, Object> DEFAULT_OPTIONS = LangUtil.map(Pair.of(SCALE, 1f));

    @SafeVarargs
    public static StyledDocument toStyledDocument(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, Object>... options) {
        // create map with default options
        Map<String, Object> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrrides

        return new StyledDocumentBuilder(styleTraits, optionMap).add(text).get();
    }

    private StyledDocument buffer = new DefaultStyledDocument();

    private float scale = 1;

    private Deque<Pair<Integer, AttributeSet>> paragraphAttributes = new LinkedList<>();

    private final float defaultFontSize;

    private StyledDocumentBuilder(Function<Style, TextAttributes> styleTraits, Map<String, Object> options) {
    	super(styleTraits);

    	this.scale = ((Number) options.getOrDefault(SCALE, 1)).floatValue();
    	this.defaultFontSize = ((Number) options.getOrDefault(FONT_SIZE, 12)).floatValue();
    	this.attributeSet = (MutableAttributeSet) options.getOrDefault(ATTRIBUTE_SET, new SimpleAttributeSet());

        setDefaultFgColor(getColor(this.attributeSet, StyleConstants.Foreground, Color.BLACK));
        setDefaultBgColor(getColor(this.attributeSet, StyleConstants.Background, Color.TRANSPARENT_WHITE));
    }

    private static Color getColor(AttributeSet as, Object key, Color dfltColor) {
        Object value = as.getAttribute(key);
        return value != null ? SwingUtil.toColor((java.awt.Color) value) : dfltColor;
    }
    
    @Override
    public StyledDocument get() {
        // apply paragraph styles
        int pos = 0;
        for (Pair<Integer, AttributeSet> e : paragraphAttributes) {
            buffer.setParagraphAttributes(pos, e.first, e.second, false);
            pos += e.first;
        }

        // mark builder invalid by clearing buffer
        StyledDocument ret = buffer;
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

    private void setFontScale(MutableAttributeSet as, Object value) {
        setFontSize(as, ((Number)value).floatValue()*defaultFontSize );
    }
    private void setFontSize(MutableAttributeSet as, Object value) {
        double fontSize = TextUtil.decodeFontSize(String.valueOf(value));
        StyleConstants.setFontSize(as, (int) Math.round(scale * fontSize));
    }

    @Override
    protected boolean isValid() {
        return buffer != null;
    }

    Map<String, BiConsumer<MutableAttributeSet, Object>> defaultStyles() {
        Map<String, BiConsumer<MutableAttributeSet, Object>> dfltStyles = new HashMap<>();

        // TextAttributes.STYLE_NAME: unused
        dfltStyles.put(TextAttributes.FONT_FAMILY, this::setFontFamily);
        dfltStyles.put(TextAttributes.FONT_SCALE, this::setFontScale);
        dfltStyles.put(TextAttributes.FONT_SIZE, this::setFontSize);
        dfltStyles.put(TextAttributes.COLOR,
                (as, v) -> StyleConstants.setForeground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        dfltStyles.put(TextAttributes.BACKGROUND_COLOR,
                (as, v) -> StyleConstants.setBackground(as, SwingUtil.toAwtColor(String.valueOf(v))));
        dfltStyles.put(TextAttributes.FONT_WEIGHT,
                (as, v) -> StyleConstants.setBold(as, v.equals(TextAttributes.FONT_WEIGHT_VALUE_BOLD)));
        dfltStyles.put(TextAttributes.FONT_STYLE, (as, v) -> {
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
        dfltStyles.put(TextAttributes.TEXT_DECORATION, (as, v) -> {
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
        dfltStyles.put(TextAttributes.TEXT_INDENT_LEFT, (as, v) -> {
            StyleConstants.setLeftIndent(as, Float.valueOf(v.toString()));
        });
        return dfltStyles;
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
            case TextAttributes.TEXT_INDENT_LEFT:
                // TODO
                break;
            default:
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

}
