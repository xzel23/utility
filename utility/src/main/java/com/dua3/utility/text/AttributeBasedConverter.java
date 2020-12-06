// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;

import java.util.HashMap;
import java.util.Map;

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
        private Map<String,Object> currentAttributes = new HashMap<>();

        /**
         * Create a new instance.
         * @param defaultAttributes the default attributes to be used
         */
        protected AttributeBasedConverterImpl(Map<String,Object> defaultAttributes) {
            this.currentAttributes = defaultAttributes;
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
            run.getStyles().forEach(style -> style.forEach((attribute,value) -> styleAttributes.put(attribute,value)));
            return styleAttributes;
        }

        /**
         * Update style. There should be no need to override this method in implementations.
         * @param run the run for which the style should be updated
         */
        protected void setStyle(Run run) {
            // collect this run's attribute
            Map<String,Object> newAttributes = collectAttributes(run);
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
            for (Run run : text) {
                setStyle(run);
                appendChars(run);
            }
            return this;
        }
        
        protected abstract void appendChars(CharSequence s);
    }
}
