package com.dua3.utility.text;

/**
 * Interface for {@link RichText} converters.
 * @param <T> the conversion target type
 */
@FunctionalInterface
public interface RichTextConverter<T> {

    /**
     * Convert {@link RichText} to the target type.
     * @param text the text to convert
     * @return conversion result
     */
    T convert(RichText text);

}
