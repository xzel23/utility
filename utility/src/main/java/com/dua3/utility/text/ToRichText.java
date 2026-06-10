// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import java.util.Objects;

/**
 * An interface for classes that can be represented as RichText.
 */
public interface ToRichText {

    /**
     * Get RichText representation of this object (similar to {@code toString()}).
     *
     * @return RichText presentation of the object
     */
    RichText toRichText();

    /**
     * Append this object's rich text representation to a buffer.
     *
     * @param builder the builder
     */
    void appendTo(RichTextBuilder builder);

    /**
     * Appends a portion of this object's rich text representation to the specified
     * {@code RichTextBuilder}. The range is defined by the {@code from} and
     * {@code to} indices.
     *
     * @param builder the RichTextBuilder to append the rich text to
     * @param from the starting index (inclusive) of the portion to be appended
     * @param to the ending index (exclusive) of the portion to be appended
     */
    default void appendTo(RichTextBuilder builder, int from, int to) {
        RichText rt = toRichText();
        Objects.checkFromToIndex(from, to, rt.length());
        builder.append(rt.subSequence(from, to));
    }
}
