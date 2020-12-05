// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();
    private double scale = 1.0;
    
    void setDefaultFont(Font font) {
        this.defaultFont = Objects.requireNonNull(font);
    }
    
    void addAttributes(List<Pair<Object, Object>> attributes) {
        attributes.forEach(p -> defaultAttributes.addAttribute(p.first, p.second));
    }

    void addAttributes(AttributeSet attributes) {
       this.defaultAttributes.addAttributes(attributes); 
    }


    /**
     * Create AttributeSet for a Font instance
     * @param font the font
     * @return the AttributeSet
     */
    private AttributeSet createAttributeSet(Font font) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        attrs.addAttributes(defaultAttributes);
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
        
        StyledDocumentConverterImpl() {
            super(defaultFont);
            buffer = new DefaultStyledDocument();
            currentAttributes = defaultAttributes;
        }
        
        @Override
        protected StyledDocument get() {
            return buffer;
        }

        @Override
        protected void apply(Font font, FontDef changes) {
            currentAttributes = createAttributeSet(font);
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
