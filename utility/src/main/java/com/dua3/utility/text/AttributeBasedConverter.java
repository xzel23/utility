// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Base class for attribute based converters.
 * @param  <T> target type of conversion
 */
public abstract class AttributeBasedConverter<T> implements RichTextConverter<T> {

    protected abstract AttributeBasedConverter.AttributeBasedConverterImpl<T> createConverter(RichText text);

    public T convert(RichText text) {
        return createConverter(text).append(text).get();
    }

    /**
     * Abstract base class for the attribute based converter implementation classes.
     * @param <T> the conversion target type
     */
    protected static abstract class AttributeBasedConverterImpl<T> {

        /** The current font used when appending text. */
        private Map<String,Object> currentAttributes;
        /** Store the initial attributes so that they can be restored at the end. */
        private final Map<String,Object> initialAttributes;

        /**
         * Create a new instance.
         * @param defaultAttributes the default attributes to be used
         */
        protected AttributeBasedConverterImpl(Map<String,Object> defaultAttributes) {
            this.initialAttributes = defaultAttributes;
            // copy currentAttributes
            this.currentAttributes = new HashMap<>();
            copyAttributes(defaultAttributes.entrySet(), currentAttributes);
        }

        /**
         * Get the converted document once conversion is finished
         * @return converted document
         */
        protected abstract T get();

        /**
         * Apply new font. Depending on the implemetation, using either the font or the changes passed is more
         * convenient and the implementation is free to choose whichever is suitable and ignore the other. 
         * @param changedAttributes map of the changed attribute values  
         */
        protected abstract void apply(Map<String, Pair<Object, Object>> changedAttributes);

        /**
         * Collect all style attributes into a single map.
         * @param run the {@link Run}
         * @return Map containing all attributes set by this run's styles
         */
        protected Map<String, Object> collectAttributes(Run run) {
            Map<String,Object> styleAttributes = new HashMap<>();
            run.getStyles().forEach(style -> copyAttributes(style, styleAttributes));
            return styleAttributes;
        }

        /**
         * Copy attributes and make sure to replace the FONT attribute to avoid clashes between FONT and FONT_XXX
         * attributes.
         * @param sourceAttributes the source entries
         * @param destinationAttributes the destination map
         */
        private void copyAttributes(Iterable<? extends Map.Entry<String, Object>> sourceAttributes, Map<? super String, Object> destinationAttributes) {
            sourceAttributes.forEach( entry -> {
                String attribute = entry.getKey();
                Object value = entry.getValue();
                if (Objects.equals(Style.FONT, attribute)) {
                    // special handling if FONT is set: as FONT overrides all other font related attributes,
                    // once FONT is set it will override all subsequent font related changes until a new FONT
                    // is encountered. so filter out FONT and instead set the individual attributes.
                    Font font = (Font) value;
                    destinationAttributes.put(Style.FONT_TYPE, font.getFamily());
                    destinationAttributes.put(Style.FONT_SIZE, font.getSizeInPoints());
                    destinationAttributes.put(Style.COLOR, font.getColor());
                    destinationAttributes.put(Style.FONT_STYLE, font.isItalic() ? Style.FONT_STYLE_VALUE_ITALIC : Style.FONT_STYLE_VALUE_NORMAL);
                    destinationAttributes.put(Style.FONT_WEIGHT, font.isBold() ? Style.FONT_WEIGHT_VALUE_BOLD : Style.FONT_WEIGHT_VALUE_NORMAL);
                    destinationAttributes.put(Style.TEXT_DECORATION_UNDERLINE, font.isUnderline() ? Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE : Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE);
                    destinationAttributes.put(Style.TEXT_DECORATION_LINE_THROUGH, font.isUnderline() ? Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE : Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE);
                } else {
                    destinationAttributes.put(attribute, value);
                }
            });            
        }
        
        /**
         * Update style. There should be no need to override this method in implementations.
         * @param run the run for which the style should be updated
         */
        protected void setStyle(Run run) {
            // collect this run's attribute
            Map<String,Object> newAttributes = collectAttributes(run);
            handleAttributeChanges(newAttributes);
        }

        protected void handleAttributeChanges(Map<String, Object> newAttributes) {
            // determine attribute changes
            Map<String, Pair<Object, Object>> changedAttributes = DataUtil.changes(currentAttributes, newAttributes);
            // apply attribute changes
            apply(changedAttributes);
            // update current attributes
            currentAttributes = newAttributes;
        }

        /**
         * Append {@link RichText}.
         * @param text the text to append
         * @return this instance
         */
        protected AttributeBasedConverterImpl<T> append(RichText text) {
            // apply all runs of the text
            for (Run run : text) {
                setStyle(run);
                appendChars(run);
            }
            
            // reset attributes
            handleAttributeChanges(initialAttributes);
            
            return this;
        }
        
        protected abstract void appendChars(CharSequence s);
    }
}
