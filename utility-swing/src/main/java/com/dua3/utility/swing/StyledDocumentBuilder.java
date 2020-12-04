// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.*;

/**
 * A {@link RichTextConverterBase} implementation for translating
 * {@code RichText} to StyledDocument.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class StyledDocumentBuilder extends RichTextConverterBase<StyledDocument> {

    private static final Object[] PARAGRAPH_ATTRIBUTES = { StyleConstants.ParagraphConstants.LeftIndent,
            StyleConstants.ParagraphConstants.Alignment };

    /** Option to set the scaling factor. */
    public static final String SCALE = "scale";
    public static final String ATTRIBUTE_SET = "attribute-set";
    public static final String FONT_SIZE = "font-size";

    private static final Map<String, Object> DEFAULT_OPTIONS = LangUtil.map(Pair.of(SCALE, 1.0f));

    @SafeVarargs
    public static StyledDocument toStyledDocument(RichText text, Function<Style, TextAttributes> styleTraits,
            Pair<String, Object>... options) {
        // create map with default options
        Map<String, Object> optionMap = new HashMap<>(DEFAULT_OPTIONS);
        LangUtil.putAll(optionMap, options); // add overrides

        return new StyledDocumentBuilder(new DefaultStyledDocument(), styleTraits, optionMap).add(text).get();
    }

    private StyledDocument buffer;

    private float scale;

    private final Deque<Pair<Integer, AttributeSet>> paragraphAttributes = new LinkedList<>();

    private final float defaultFontSize;

    private StyledDocumentBuilder(StyledDocument buffer, Function<Style, TextAttributes> styleTraits,
            Map<String, Object> options) {
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

    private static AttributeSet getParagraphAttributes(AttributeSet as) {
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
            case Style.COLOR:
                StyleConstants.setForeground(attributeSet, SwingUtil.toAwtColor(getColor(value, getDefaultFgColor())));
                break;
            case Style.BACKGROUND_COLOR:
                StyleConstants.setBackground(attributeSet, SwingUtil.toAwtColor(getColor(value, getDefaultBgColor())));
                break; 
            case Style.FONT:
            {
                Font font = (Font) value;
                StyleConstants.setFontFamily(attributeSet, font.getFamily());
                StyleConstants.setFontSize(attributeSet, Math.round(scale*font.getSizeInPoints()));
                StyleConstants.setItalic(attributeSet, font.isItalic());
                StyleConstants.setBold(attributeSet, font.isBold());
                StyleConstants.setUnderline(attributeSet, font.isUnderline());
                StyleConstants.setStrikeThrough(attributeSet, font.isStrikeThrough());
                break;
            }
            case Style.FONT_TYPE:
                StyleConstants.setFontFamily(attributeSet, String.valueOf(value));
                break;
            case Style.FONT_STYLE:
                StyleConstants.setItalic(attributeSet, Style.FONT_STYLE_VALUE_ITALIC.equals(value)
                        || Style.FONT_STYLE_VALUE_OBLIQUE.equals(value));
                break;
            case Style.FONT_SIZE:
                StyleConstants.setFontSize(attributeSet, Math.round(scale * (value == null ? defaultFontSize : TextUtil.decodeFontSize(String.valueOf(value)))));
                break;
            case Style.FONT_SCALE:
                StyleConstants.setFontSize(attributeSet, value == null ? Math.round(scale * defaultFontSize)
                        : Math.round(scale * defaultFontSize * Float.parseFloat(String.valueOf(value))));
                break;
            case Style.FONT_WEIGHT:
                StyleConstants.setBold(attributeSet, Style.FONT_WEIGHT_VALUE_BOLD.equals(value));
                break;
            case Style.TEXT_DECORATION_UNDERLINE:
                StyleConstants.setUnderline(attributeSet, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE.equals(value));
                break;                
            case Style.TEXT_DECORATION_LINE_THROUGH:
                StyleConstants.setStrikeThrough(attributeSet, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE.equals(value));
                break;                
            case Style.FONT_VARIANT:
                break;
            case Style.TEXT_INDENT_LEFT:
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
            throw new IllegalStateException(e);
        }
    }

}
