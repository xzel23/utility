package com.dua3.utility.text;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Base class for tag based converters. 
 * @param <T> the conversion target type
 */
public abstract class TagBasedConverter<T> implements RichTextConverter<T> {
    
    protected abstract @NotNull TagBasedConverterImpl<T> createConverter(@NotNull RichText text);

    @Override
    public @NotNull T convert(@NotNull RichText text) {
        return createConverter(text).append(text).get();
    }

    /**
     * Abstract base class for the tag based converter implementation classes.
     * @param <T> the conversion target type
     */
    protected abstract static class TagBasedConverterImpl<T> {
        
        private @NotNull List<Style> currentStyles = new ArrayList<>();

        protected abstract void appendOpeningTags(@NotNull List<Style> openingStyles);

        protected abstract void appendClosingTags(@NotNull List<Style> closingStyles);

        protected abstract void appendChars(@NotNull CharSequence s);

        protected abstract @NotNull T get();
        
        protected @NotNull TagBasedConverterImpl<T> append(@NotNull RichText text) {
            List<Style> openStyles = new LinkedList<>();
            for (Run run: text) {
                List<Style> runStyles = run.getStyles();

                // determine all styles to close
                List<Style> stylesToclose = new LinkedList<>(openStyles);
                stylesToclose.removeAll(runStyles);
                
                // to avoid interleaved styles, we have to close all tags that were openend after the first tag that is closed
                int stylesToKeepOpen = stylesToclose.stream().mapToInt(currentStyles::indexOf).min().orElse(currentStyles.size());
                List<Style> closingStyles = currentStyles.subList(stylesToKeepOpen, currentStyles.size());
                
                // the styles that were closed but not contained in stylesToClose must be reopened again 
                List<Style> reopeningStyles = new LinkedList<>(closingStyles);
                reopeningStyles.removeAll(stylesToclose);

                // close styles ...
                appendClosingTags(closingStyles);
                currentStyles=currentStyles.subList(0,stylesToKeepOpen);
                        
                // ... then reopen the styles to keep
                appendOpeningTags(reopeningStyles);
                currentStyles.addAll(reopeningStyles);

                // add opening Tags for new styles
                List<Style> openingStyles = new LinkedList<>(runStyles);
                openingStyles.removeAll(openStyles);
                appendOpeningTags(openingStyles);
                currentStyles.addAll(openingStyles);

                // add text
                appendChars(run);

                // update open styles
                openStyles.removeAll(stylesToclose);
                openStyles.addAll(openingStyles);
            }
            // close all remeining styles
            appendClosingTags(openStyles);

            return this;
        }
    }
}
