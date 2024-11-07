// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.swing;

import com.dua3.utility.data.Pair;
import com.dua3.utility.text.AttributeBasedConverter;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.TextAttributes;
import org.jspecify.annotations.Nullable;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A {@link AttributeBasedConverter} implementation for translating
 * {@code RichText} to StyledDocument.
 *
 * @author Axel Howind (axel@dua3.com)
 */
public final class StyledDocumentConverter extends AttributeBasedConverter<StyledDocument> {

    private static final Font DEFAULT_FONT = new Font();
    private final SimpleAttributeSet defaultStyledAttributes = new SimpleAttributeSet();
    // some settings controlling the conversion
    private Font defaultFont = DEFAULT_FONT;
    private Map<String, Object> defaultAttributes = new HashMap<>();
    private double scale = 1.0;
    // -- define a dictionary to map StyleConstants attribute keys to calls to Font getters
    @SuppressWarnings("NumericCastThatLosesPrecision")
    private final Map<Object, Function<Font, Object>> dictionary = Map.of(
            StyleConstants.Family, Font::getFamily,
            StyleConstants.Size, f -> (int) Math.round(scale * f.getSizeInPoints()),
            StyleConstants.Bold, Font::isBold,
            StyleConstants.Italic, Font::isItalic,
            StyleConstants.Underline, Font::isUnderline,
            StyleConstants.StrikeThrough, Font::isStrikeThrough,
            StyleConstants.Foreground, f -> SwingUtil.toAwtColor(f.getColor())
    );

    private StyledDocumentConverter() {
    }

    /**
     * Create a new converter instance.
     *
     * @param options the options to use
     * @return new converter instance
     */
    public static StyledDocumentConverter create(StyledDocumentConversionOption... options) {
        return create(List.of(options));
    }

    /**
     * Create a new converter instance.
     *
     * @param options the options to use
     * @return new converter instance
     */
    public static StyledDocumentConverter create(Collection<StyledDocumentConversionOption> options) {
        StyledDocumentConverter instance = new StyledDocumentConverter();
        options.forEach(o -> o.apply(instance));
        return instance;
    }

    /**
     * Set default attributes.
     *
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addStyledAttributes(Map<Object, Object> attributes) {
        return addStyledAttributes(attributes.entrySet());
    }

    /**
     * Set default attributes.
     *
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addStyledAttributes(Iterable<? extends Map.Entry<Object, Object>> attributes) {
        return new StyledDocumentConversionOption(c -> attributes.forEach(p -> c.defaultStyledAttributes.addAttribute(p.getKey(), p.getValue())));
    }

    /**
     * Set default attributes.
     *
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addStyledAttributes(AttributeSet attributes) {
        return new StyledDocumentConversionOption(c -> c.defaultStyledAttributes.addAttributes(attributes));
    }

    /**
     * Set default attributes.
     *
     * @param attributes the default Attributes
     * @return the option to use
     */
    public static StyledDocumentConversionOption defaultAttributes(Map<String, Object> attributes) {
        return new StyledDocumentConversionOption(c -> c.defaultAttributes = attributes);
    }

    /**
     * Set default font.
     *
     * @param font the default font
     * @return the option to use
     */
    public static StyledDocumentConversionOption defaultFont(Font font) {
        return new StyledDocumentConversionOption(c -> c.defaultFont = font);
    }

    /**
     * Set font scaling factor.
     *
     * @param scale the font scaling factor
     * @return the option to use
     */
    public static StyledDocumentConversionOption scale(double scale) {
        return new StyledDocumentConversionOption(c -> c.setScale(scale));
    }

    @Override
    protected AttributeBasedConverterImpl<StyledDocument> createConverter(RichText text) {
        return new StyledDocumentConverterImpl();
    }

    /**
     * Set the scale to use in conversion. This scale will be applied to all font sizes during conversion.
     *
     * @param scale the scale
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    class StyledDocumentConverterImpl extends AttributeBasedConverterImpl<StyledDocument> {

        private final StyledDocument buffer;
        private AttributeSet currentAttributes;
        private Font currentFont;

        StyledDocumentConverterImpl() {
            super(defaultAttributes);
            buffer = new DefaultStyledDocument();
            currentAttributes = defaultStyledAttributes;
            currentFont = defaultFont;
        }

        @Override
        protected StyledDocument get() {
            return buffer;
        }

        @Override
        protected void apply(Map<String, Pair<@Nullable Object, @Nullable Object>> changedAttributes) {
            Map<String, @Nullable Object> attributes = new HashMap<>();
            changedAttributes.forEach((attribute, values) -> attributes.put(attribute, values.second()));
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

        /**
         * Create AttributeSet for a Font instance
         *
         * @param font the font
         * @return the AttributeSet
         */
        private AttributeSet createAttributeSet(Font font) {
            SimpleAttributeSet attrs = new SimpleAttributeSet();
            attrs.addAttributes(defaultStyledAttributes);
            dictionary.forEach((key, getter) -> attrs.addAttribute(key, getter.apply(font)));
            return attrs;
        }

    }
}
