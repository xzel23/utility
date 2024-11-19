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

        protected abstract void appendOpeningTags(List<Style> openingStyles);

        protected abstract void appendClosingTags(List<Style> closingStyles);

        protected abstract void appendChars(CharSequence s);

        protected abstract T get();

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
