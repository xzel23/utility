package com.dua3.utility.text;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for tag based converters.
 *
 * @param <T> the conversion target type
 */
public abstract class TagBasedConverter<T> implements RichTextConverter<T> {

    public record AttributeChange(String attribute, @Nullable Object oldValue, @Nullable Object newValue) {}

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
     * Abstract base class for the tag-based converter implementation classes.
     *
     * @param <T> the conversion target type
     */
    protected abstract static class TagBasedConverterImpl<T> {

        private List<Style> currentStyles = new ArrayList<>();

        /**
         * Get the list of attributes that are mapped to tags
         *
         * @return the attributes that are mapped directly to tags
         */
        protected Collection<String> relevantAttributes() {
            return Collections.emptyList();
        }

        /**
         * Appends opening tags corresponding to the provided attributes. Each attribute in the list
         * represents a specific property-value pair, and this method generates and appends the
         * appropriate tag representations for these attributes in an underlying format.
         *
         * @param attributesToClose the list of {@link AttributeChange} objects representing the attributes
         *                          for which opening tags need to be appended
         */
        protected void appendOpeningTagsForAttributes(List<AttributeChange> attributesToClose) {}

        /**
         * Appends the appropriate closing tags for the given list of attributes.
         * This method ensures that the closing tags corresponding to the specified
         * attributes are properly added to maintain formatting consistency.
         *
         * @param attributesToClose the list of {@link AttributeChange}
         *                           representing attributes for which closing tags
         *                           should be appended
         */
        protected void appendClosingTagsForAttributes(List<AttributeChange> attributesToClose) {}

        /**
         * Appends opening tags corresponding to the provided styles. The implementation of this method
         * should generate and append the appropriate tag representations based on the given list of styles.
         *
         * @param openingStyles the list of styles for which opening tags should be appended
         */
        protected abstract void appendOpeningTagsForStyles(List<Style> openingStyles);

        /**
         * Appends the required closing tags corresponding to the given list of styles.
         * This method ensures that the associated tags for the specified styles are properly
         * closed, maintaining the integrity of the output format.
         *
         * @param closingStyles the list of styles for which the closing tags need to be appended
         */
        protected abstract void appendClosingTagsForStyles(List<Style> closingStyles);

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
            List<AttributeChange> openAttributes = new ArrayList<>();
            TextAttributes currentAttributes = TextAttributes.none();

            List<Style> openStyles = new ArrayList<>();
            for (Run run : text) {
                // determine attribute related changes
                List<AttributeChange> attributesToClose = new ArrayList<>();
                List<AttributeChange> attributesToOpen = new ArrayList<>();
                Collection<String> relevantAttributes = relevantAttributes();
                for (String attribute : relevantAttributes) {
                    Object oldValue = currentAttributes.get(attribute);
                    Object newValue = run.attributes().get(attribute);
                    if (!Objects.equals(oldValue, newValue)) {
                        if (oldValue != null) {
                            attributesToClose.add(new AttributeChange(attribute, oldValue, newValue));
                        }
                        if (newValue != null) {
                            attributesToOpen.add(new AttributeChange(attribute, oldValue, newValue));
                        }
                    }
                }
                currentAttributes = run.attributes();

                // determine style related changes
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
                appendClosingTagsForStyles(closingStyles);
                currentStyles = new ArrayList<>(currentStyles.subList(0, stylesToKeepOpen));

                // ... close attribute related tags
                appendClosingTagsForAttributes(attributesToClose);
                openAttributes.removeAll(attributesToClose);

                // ... open attribute related tags
                appendOpeningTagsForAttributes(attributesToOpen);
                openAttributes.addAll(attributesToOpen);

                // ... then reopen the styles to keep
                appendOpeningTagsForStyles(reopeningStyles);
                currentStyles.addAll(reopeningStyles);

                // add opening Tags for new styles
                List<Style> openingStyles = new ArrayList<>(runStyles);
                openingStyles.removeAll(openStyles);
                appendOpeningTagsForStyles(openingStyles);
                currentStyles.addAll(openingStyles);

                // add text
                appendChars(run);

                // update open styles
                openStyles.removeAll(stylesToClose);
                openStyles.addAll(openingStyles);
            }
            // close all remaining styles
            appendClosingTagsForStyles(openStyles);
            appendClosingTagsForAttributes(openAttributes);

            return this;
        }
    }
}
