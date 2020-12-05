package com.dua3.utility.text;

/**
 * Interface for {@link RichText} converters.
 * 
 * There are basically two converter types based on the type of target document:
 * <ul>
 *     <li> tag based (i. e. HTML) where styles are declared using start and end tags and correct attributes are 
 *     automatically restored when an end tag is found.
 *     <li> attribute based (i. e. StyledDocument), where the converter has to keep track of attributes and correctly
 *     restore values. Imagine a text in a bold formatted paragraph where only a part is formatted using a style that
 *     also defines a bold text decoration. When that part ends, the remaining text of the paragraph has still to be
 *     formatted using a bold font, so the converter has to remember the prior state. 
 * </ul>
 * @param <T>
 */
public interface OldRichTextConverter<T> {

    /**
     * Add text.
     *
     * @param  text
     *              the rich text to add
     * @return      this TextBuilder
     */
    OldRichTextConverter<T> add(RichText text);

    /**
     * Get document.
     *
     * @return the document after transformation.
     */
    T get();

}
