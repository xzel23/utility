package com.dua3.utility.text;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for tag based converters. 
 * @param <T> the conversion target type
 */
public abstract class TagBasedConverter<T> {
    
    protected abstract TagBasedConverterImpl<T> createConverter(RichText text);

    public T convert(RichText text) {
        return createConverter(text).append(text).get();
    }
    
    protected static abstract class TagBasedConverterImpl<T> {

        protected abstract void appendOpeningTags(List<Style> openingStyles);

        protected abstract void appendClosingTags(List<Style> closingStyles);

        protected abstract void appendChars(CharSequence s);

        protected abstract T get();
        
        public TagBasedConverterImpl<T> append(RichText text) {
            List<Style> openStyles = new LinkedList<>();
            for (Run run: text) {
                List<Style> runStyles = run.getStyles();

                // add closing Tags for styles
                List<Style> closingStyles = new LinkedList<>(openStyles);
                closingStyles.removeAll(runStyles);
                appendClosingTags(closingStyles);

                // add opening Tags for styles
                List<Style> openingStyles = new LinkedList<>(runStyles);
                openingStyles.removeAll(openStyles);
                appendOpeningTags(openingStyles);

                // add text
                appendChars(run);

                // update open styles
                openStyles.removeAll(closingStyles);
                openStyles.addAll(openingStyles);
            }
            // close all remeining styles
            appendClosingTags(openStyles);

            return this;
        }
    }
}
