// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

/**
 * An interface for classes that can be represented as RichText.
 */
public interface ToRichText {

    /**
     * Get RichText representation of this object (similar to {@code toString()}).
     *
     * @return RichhText presentation of the object
     */
    default RichText toRichText() {
        RichTextBuilder builder = new RichTextBuilder();
        appendTo(builder);
        return builder.toRichText();
    }

    /**
     * Append this object's rich text representation to a buffer.
     *
     * @param builder the builder
     */
    void appendTo(RichTextBuilder builder);
}
