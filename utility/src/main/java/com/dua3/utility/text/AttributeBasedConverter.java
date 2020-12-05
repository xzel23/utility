// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

/**
 * Base class for attribute based converters.
 * @param  <T> target type of conversion
 */
public abstract class AttributeBasedConverter<T> implements RichTextConverter<T> {

    protected abstract AttributeBasedConverter.AttributeBasedConverterImpl<T> createConverter(RichText text);

    public T convert(RichText text) {
        return createConverter(text).append(text).get();
    }

    protected static abstract class AttributeBasedConverterImpl<T> {

        /** The current font used when appending text. */
        private Font currentFont;

        /**
         * Create a new instance.
         * @param defaultFont the default font to be used
         */
        protected AttributeBasedConverterImpl(Font defaultFont) {
            this.currentFont = defaultFont;
        }

        /**
         * Get the converted document once conversion is finished
         * @return converted document
         */
        protected abstract T get();

        /**
         * Apply new font. Depending on the implemetation, using either the font or the changes passed is more
         * convenient and the implementation is free to choose whichever is suitable and ignore the other. 
         * @param font the {@link Font}  
         * @param changes {@link FontDef} instance holding the changes to the last font
         */
        protected abstract void apply(Font font, FontDef changes);

        /**
         * Update style. There should be no need to override this method in implementations.
         * @param run the run for which the style should be updated
         */
        protected void setStyle(Run run) {
            FontDef changes = run.getFontDef();
            currentFont = currentFont.deriveFont(changes);
            apply(currentFont, changes);
        }

        /**
         * Append {@link RichText}.
         * @param text the text to append
         * @return this instance
         */
        protected AttributeBasedConverterImpl<T> append(RichText text) {
            for (Run run : text) {
                setStyle(run);
                appendChars(run);
            }
            return this;
        }
        
        protected abstract void appendChars(CharSequence s);
    }
}
