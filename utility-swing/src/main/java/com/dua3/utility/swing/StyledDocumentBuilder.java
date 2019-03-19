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
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextConverterBase;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextUtil;

/**
 * A {@link RichTextConverterBase} implementation for translating
 * {@code RichText} to StyledDocument.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends RichTextConverterBase<StyledDocument> {

    private static final Object[] PARAGRAPH_ATTRIBUTES = { StyleConstants.ParagraphConstants.LeftIndent,
            StyleConstants.ParagraphConstants.Alignment };

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

        return new StyledDocumentBuilder(new DefaultStyledDocument(), styleTraits, optionMap).add(text).get();
    }

    private StyledDocument buffer;

    private float scale = 1;

    private Deque<Pair<Integer, AttributeSet>> paragraphAttributes = new LinkedList<>();

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

        // consistency checks
        LangUtil.check(openedTags.isEmpty(), "there still are open tags");

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
                StyleConstants.setItalic(attributeSet, TextAttributes.FONT_STYLE_VALUE_ITALIC.equals(value)
                        || TextAttributes.FONT_STYLE_VALUE_OBLIQUE.equals(value));
                break;
            case TextAttributes.FONT_SIZE:
                StyleConstants.setFontSize(attributeSet, value == null ? Math.round(scale * defaultFontSize)
                        : Math.round(scale * TextUtil.decodeFontSize(String.valueOf(value))));
                break;
            case TextAttributes.FONT_SCALE:
                StyleConstants.setFontSize(attributeSet, value == null ? Math.round(scale * defaultFontSize)
                        : Math.round(scale * defaultFontSize * Float.parseFloat(String.valueOf(value))));
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

    private final Deque<String> openedTags = new LinkedList<>();

}
