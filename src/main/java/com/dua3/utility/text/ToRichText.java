package com.dua3.utility.text;

/**
 * An interface for classes that can be represented as RichText.
 */
public interface ToRichText {
    
    /**
     * Get RichText representation of this object (similar to {@code toString()}).
     * @return RichhText presentation of the object
     */
    RichText toRichText();
    
}
