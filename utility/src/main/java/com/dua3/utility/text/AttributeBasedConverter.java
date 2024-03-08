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
 * Base class for attribute-based converters.
 *
 * @param <T> target type of conversion
 */
public abstract class AttributeBasedConverter<T> implements RichTextConverter<T> {

    /**
     * Factory method to create a compatible converter implementation instance for this converter.
     *
     * @param text the text to be converted
     * @return instance of the implementation class
     */
    protected abstract AttributeBasedConverterImpl<T> createConverter(RichText text);

    /**
     * Convert {@link RichText} instance to the target class.
     *
     * @param text the text to convert
     * @return the conversion result
     */
    @Override
    public T convert(RichText text) {
        return createConverter(text).append(text).get();
    }

    /**
     * Abstract base class for the attribute-based converter implementation classes.
     *
     * @param <T> the conversion target type
     */
    protected abstract static class AttributeBasedConverterImpl<T> {

        /**
         * Store the initial attributes so that they can be restored at the end.
         */
        private final Map<String, Object> initialAttributes;
        /**
         * The current font used when appending text.
         */
        private Map<String, Object> currentAttributes;

        /**
         * Create a new instance.
         *
         * @param defaultAttributes the default attributes to be used
         */
        protected AttributeBasedConverterImpl(Map<String, Object> defaultAttributes) {
            this.initialAttributes = defaultAttributes;
            // copy currentAttributes
            this.currentAttributes = new HashMap<>();
            copyAttributes(defaultAttributes.entrySet(), currentAttributes);
        }

        /**
         * Collect all style attributes into a single map.
         *
         * @param run the {@link Run}
         * @return Map containing all attributes set by this run's styles
         */
        protected static Map<String, Object> collectAttributes(Run run) {
            Map<String, Object> styleAttributes = new HashMap<>();
            run.getStyles().forEach(style -> copyAttributes(style, styleAttributes));
            return styleAttributes;
        }

        /**
         * Copy attributes and make sure to replace the FONT attribute to avoid clashes between FONT and FONT_XXX
         * attributes.
         *
         * @param sourceAttributes      the source entries
         * @param destinationAttributes the destination map
         */
        private static void copyAttributes(Iterable<? extends Map.Entry<String, Object>> sourceAttributes,
                                           Map<? super String, Object> destinationAttributes) {
            sourceAttributes.forEach(entry -> {
                String attribute = entry.getKey();
                Object value = entry.getValue();
                if (Objects.equals(Style.FONT, attribute)) {
                    // special handling if FONT is set: as FONT overrides all other font-related attributes,
                    // once FONT is set, it will override all later font-related changes until a new FONT
                    // is encountered. so filter out FONT and instead set the individual attributes.
                    RichTextConverter.putFontProperties(destinationAttributes, (Font) value);
                } else {
                    destinationAttributes.put(attribute, value);
                }
            });
        }

        /**
         * Get the converted document once conversion is finished
         *
         * @return converted document
         */
        protected abstract T get();

        /**
         * Apply new font. Depending on the implementation, using either the font or the changes passed is more
         * convenient and the implementation is free to select whichever is suitable and ignore the other.
         *
         * @param changedAttributes map of the changed attribute values
         */
        protected abstract void apply(Map<String, Pair<Object, Object>> changedAttributes);

        /**
         * Update style. There should be no need to override this method in implementations.
         *
         * @param run the run for which the style should be updated
         */
        protected void setStyle(Run run) {
            // collect this run's attribute
            Map<String, Object> newAttributes = collectAttributes(run);
            handleAttributeChanges(newAttributes);
        }

        /**
         * Handle changes in attributes.
         *
         * @param newAttributes map containing the changed attributes and their new values
         */
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
         *
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

        /**
         * Append chars to the conversion result.
         *
         * @param s chars to append
         */
        protected abstract void appendChars(CharSequence s);
    }
}
