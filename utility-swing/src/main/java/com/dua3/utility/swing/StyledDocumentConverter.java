// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.dua3.utility.data.Pair;
import com.dua3.utility.text.*;

/**
 * A {@link AttributeBasedConverter} implementation for translating
 * {@code RichText} to StyledDocument.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class StyledDocumentConverter extends AttributeBasedConverter<StyledDocument> {

    /**
     * Create a new converter instance.
     * @param options the options to use
     * @return new converter instance
     */
    public static StyledDocumentConverter create(StyledDocumentConversionOption... options)  {
        return create(Arrays.asList(options));
    }

    /**
     * Create a new converter instance.
     * @param options the options to use
     * @return new converter instance
     */
    public static StyledDocumentConverter create(Collection<StyledDocumentConversionOption> options) {
        StyledDocumentConverter instance = new StyledDocumentConverter();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    private StyledDocumentConverter() {
    }

    private static final Font DEFAULT_FONT = new Font();
    
    // some settings controlling the conversion
    private Font defaultFont = DEFAULT_FONT;
    private Map<String, Object> defaultAttributes = new HashMap<>();
    private SimpleAttributeSet defaultStyledAttributes = new SimpleAttributeSet();
    private double scale = 1.0;

    /**
     * Set default attributes.
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addStyledAttributes(List<Pair<Object,Object>> attributes) {
        return new StyledDocumentConversionOption(c -> attributes.forEach(p -> c.defaultStyledAttributes.addAttribute(p.first, p.second)));
    }

    /**
     * Set default attributes.
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addStyledAttributes(AttributeSet attributes) {
        return new StyledDocumentConversionOption(c -> c.defaultStyledAttributes.addAttributes(attributes));
    }

    /**
     * Set default attributes.
     * @param attributes the default Attributes
     * @return the option to use
     */
    public static StyledDocumentConversionOption defaultAttributes(Map<String, Object> attributes) {
        return new StyledDocumentConversionOption(c -> c.defaultAttributes = Objects.requireNonNull(attributes));
    }

    /**
     * Set default font.
     * @param font the default font
     * @return the option to use
     */
    public static StyledDocumentConversionOption defaultFont(Font font) {
        return new StyledDocumentConversionOption(c -> c.defaultFont = Objects.requireNonNull(font) );
    }

    /**
     * Set font scaling factor.
     * @param scale the font scaling factor
     * @return the option to use
     */
    public static StyledDocumentConversionOption scale(double scale) {
        return new StyledDocumentConversionOption(c -> c.setScale(scale));
    }

    /**
     * Create AttributeSet for a Font instance
     * @param font the font
     * @return the AttributeSet
     */
    private AttributeSet createAttributeSet(Font font) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttributes(defaultStyledAttributes);
        dictionary.forEach( (key,getter) -> attrs.addAttribute(key,getter.apply(font)));
        return attrs;
    }

    // -- define a dictionary to map StyleConstants attribute keys to calls to Font getters
    private final Map<Object, Function<Font, Object>> dictionary = createDictionary();

    private final Map<Object, Function<Font, Object>> createDictionary() {
        Map<Object,Function<Font,Object>> m = new HashMap<>();
        m.put(StyleConstants.Family, Font::getFamily);
        m.put(StyleConstants.Size, f -> (int) Math.round(scale*f.getSizeInPoints()));
        m.put(StyleConstants.Bold, Font::isBold);
        m.put(StyleConstants.Italic, Font::isItalic);
        m.put(StyleConstants.Underline, Font::isUnderline);
        m.put(StyleConstants.StrikeThrough, Font::isStrikeThrough);
        m.put(StyleConstants.Foreground, f -> SwingUtil.toAwtColor(f.getColor()));
        return m;
    }

    @Override
    protected AttributeBasedConverterImpl<StyledDocument> createConverter(RichText text) {
        return new StyledDocumentConverterImpl();
    }

    public void setScale(double scale) {
        this.scale=scale;
    }

    class StyledDocumentConverterImpl extends AttributeBasedConverterImpl<StyledDocument> {

        private final StyledDocument buffer;
        private AttributeSet currentAttributes;
        private Font currentFont;
        
        StyledDocumentConverterImpl() {
            super(defaultAttributes);
            buffer = new DefaultStyledDocument();
            currentAttributes = defaultStyledAttributes;
            currentFont=defaultFont;
        }
        
        @Override
        protected StyledDocument get() {
            return buffer;
        }

        @Override
        protected void apply(Map<String, Pair<Object, Object>> changedAttributes) {
            Map<String, Object> attributes = new HashMap<>();
            changedAttributes.forEach( (attribute, values) -> {
                attributes.put(attribute, values.second);
            });
            // apply the default font styles 
            currentFont = currentFont.deriveFont(TextAttributes.getFontDef(attributes));
            currentAttributes = createAttributeSet(currentFont);
        }

        @Override
        protected void appendChars(CharSequence s) {
            try {
                int pos = buffer.getLength();
                int length = s.length();
                buffer.insertString(pos, s.toString(), currentAttributes);
                buffer.setCharacterAttributes(pos, length, currentAttributes, true);
                buffer.setParagraphAttributes(pos, length, currentAttributes, true);
            } catch (BadLocationException e) {
                // this should never happen
                throw new IllegalStateException(e);
            }
        }
    }
}
