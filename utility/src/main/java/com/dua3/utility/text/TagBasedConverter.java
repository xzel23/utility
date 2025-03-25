package com.dua3.utility.text;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for tag based converters.
 *
 * @param <T> the conversion target type
 */
public abstract class TagBasedConverter<T> implements RichTextConverter<T> {

    /**
     * Create a converter for the given argument.
     * @param text the text to be converted
     * @return new converter instance
     */
    protected abstract TagBasedConverterImpl<T> createConverter(RichText text);

    @Override
    public T convert(RichText text) {
        return createConverter(text).append(text).get();
    }

    /**
     * Abstract base class for the tag based converter implementation classes.
     *
     * @param <T> the conversion target type
     */
    protected abstract static class TagBasedConverterImpl<T> {

        private List<Style> currentStyles = new ArrayList<>();

        /**
         * Appends opening tags corresponding to the provided styles. The implementation of this method
         * should generate and append the appropriate tag representations based on the given list of styles.
         *
         * @param openingStyles the list of styles for which opening tags should be appended
         */
        protected abstract void appendOpeningTags(List<Style> openingStyles);

        /**
         * Appends the required closing tags corresponding to the given list of styles.
         * This method ensures that the associated tags for the specified styles are properly
         * closed, maintaining the integrity of the output format.
         *
         * @param closingStyles the list of styles for which the closing tags need to be appended
         */
        protected abstract void appendClosingTags(List<Style> closingStyles);

        /**
         * Appends the given character sequence to the current conversion process.
         *
         * @param s the {@code CharSequence} to append
         */
        protected abstract void appendChars(CharSequence s);

        /**
         * Retrieves the current result of the conversion process or related operation.
         *
         * @return the result of type T produced by the implementation
         */
        protected abstract T get();

        /**
         * Appends the content of the given {@link RichText} to the current conversion,
         * managing the styles and ensuring proper nested tagging.
         *
         * @param text the rich text content to be appended
         * @return the current instance of {@code TagBasedConverterImpl<T>} after appending the given rich text
         */
        protected TagBasedConverterImpl<T> append(RichText text) {
            List<Style> openStyles = new ArrayList<>();
            for (Run run : text) {
                List<Style> runStyles = run.getStyles();

                // determine all styles to close
                List<Style> stylesToClose = new ArrayList<>(openStyles);
                stylesToClose.removeAll(runStyles);

                // to avoid interleaved styles, we have to close all tags that were opened after the first tag that is closed
                int stylesToKeepOpen = stylesToClose.stream().mapToInt(currentStyles::indexOf).min().orElseGet(() -> currentStyles.size());
                List<Style> closingStyles = currentStyles.subList(stylesToKeepOpen, currentStyles.size());

                // the styles that were closed but not contained in stylesToClose must be reopened again 
                List<Style> reopeningStyles = new ArrayList<>(closingStyles);
                reopeningStyles.removeAll(stylesToClose);

                // close styles ...
                appendClosingTags(closingStyles);
                currentStyles = new ArrayList<>(currentStyles.subList(0, stylesToKeepOpen));

                // ... then reopen the styles to keep
                appendOpeningTags(reopeningStyles);
                currentStyles.addAll(reopeningStyles);

                // add opening Tags for new styles
                List<Style> openingStyles = new ArrayList<>(runStyles);
                openingStyles.removeAll(openStyles);
                appendOpeningTags(openingStyles);
                currentStyles.addAll(openingStyles);

                // add text
                appendChars(run);

                // update open styles
                openStyles.removeAll(stylesToClose);
                openStyles.addAll(openingStyles);
            }
            // close all remaining styles
            appendClosingTags(openStyles);

            return this;
        }
    }
}
