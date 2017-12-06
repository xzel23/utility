package com.dua3.utility.text;

/**
 * An interface for classes that can be represented as RichText.
 */
public interface ToRichText {
    
    /**
     * Get RichText representation of this object (similar to {@code toString()}).
     * @return RichhText presentation of the object
     */
    default RichText toRichText() {
        RichTextBuilder buffer = new RichTextBuilder();
        appendTo(buffer);
        return buffer.toRichText();
    }
    
    /**
     * Append this object's rich text representation to a buffer.
     * @param buffer the buffer
     */
    void appendTo(RichTextBuilder buffer);
}
