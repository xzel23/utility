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
import java.util.Map;
import java.util.function.Function;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dua3.utility.text.RichText;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.TextAttributes;
import com.dua3.utility.text.TextBuilder;

/**
 * A {@link TextBuilder} implementation for translating {@code RichText} to
 * {@code StyledDocument}.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public class StyledDocumentBuilder extends TextBuilder<StyledDocument> {
    private static final Logger LOG = LoggerFactory.getLogger(StyledDocumentBuilder.class);

    private final Function<String,TextAttributes> styleSupplier;
    private Map<String,AttributeSet> styleAttributes = new HashMap<>();

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

    private final StyledDocument doc = new DefaultStyledDocument();
    private final double scale;

    public StyledDocumentBuilder(double scale, Function<String,TextAttributes> styleSupplier) {
        this.scale = scale;
        this.styleSupplier = styleSupplier;
    }

    public StyledDocumentBuilder(double scale) {
        this(scale, styleName -> TextAttributes.none());
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

    private AttributeSet getAttributes(TextAttributes style) {
		SimpleAttributeSet as = new SimpleAttributeSet();
		applyAttributes(style.properties(), as);
		return as;
    }

	private void applyAttributes(Map<String, Object> styleAttributes, SimpleAttributeSet as) {
		for (Map.Entry<String, Object> e : styleAttributes.entrySet()) {
            switch (e.getKey()) {
            case TextAttributes.STYLE_NAME:
            		// not used in styled document
            		break;
            case TextAttributes.FONT_FAMILY:
                StyleConstants.setFontFamily(as, String.valueOf(e.getValue()));
                break;
            case TextAttributes.FONT_SIZE:
                StyleConstants.setFontSize(as, (int) Math.round(scale * decodeFontSize(String.valueOf(e.getValue()))));
                break;
            case TextAttributes.COLOR:
                StyleConstants.setForeground(as, SwingUtil.toAwtColor(String.valueOf(e.getValue())));
                break;
            case TextAttributes.BACKGROUND_COLOR:
                StyleConstants.setBackground(as, SwingUtil.toAwtColor(String.valueOf(e.getValue())));
                break;
            case TextAttributes.FONT_WEIGHT:
                StyleConstants.setBold(as, e.getValue().equals("bold"));
                break;
            case TextAttributes.FONT_STYLE:
                switch (String.valueOf(e.getValue())) {
                case "normal":
                    StyleConstants.setItalic(as, false);
                    break;
                case "italic":
                case "oblique":
                    StyleConstants.setItalic(as, true);
                    break;
                default:
                    LOG.warn("Unknown value for FONT_STYLE: {}", e.getValue());
                    break;
                }
                break;
            case TextAttributes.TEXT_DECORATION:
                switch (String.valueOf(e.getValue())) {
                case "line-through":
                    StyleConstants.setStrikeThrough(as, true);
                    break;
                case "underline":
                    StyleConstants.setUnderline(as, true);
                    break;
                default:
                    LOG.warn("Unknown value for TEXT_DECORATION: {}", e.getValue());
                    break;
                }
                break;
            default:
                LOG.warn("Unknown Style: {}", e.getKey());
                break;
            }
        }
	}

    private AttributeSet getAttributes(String styleName) {
    		return styleAttributes.computeIfAbsent(styleName, s -> getAttributes(styleSupplier.apply(s)));
    }

	private SimpleAttributeSet getAttributeSet(Run run) {
		Map<String, Object> styleProps = run.getStyle().properties();
		AttributeSet das = getAttributes((String)styleProps.get(TextAttributes.STYLE_NAME));
		SimpleAttributeSet as = new SimpleAttributeSet(das);
		applyAttributes(styleProps, as);
		return as;
	}

    @Override
    public StyledDocument get() {
        return doc;
    }

}
