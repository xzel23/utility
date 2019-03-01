// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

public interface RichTextConverter<T> {

    /**
     * Add text.
     *
     * @param  text
     *              the richt text to add
     * @return      this TextBuider
     */
    RichTextConverter<T> add(RichText text);

    /**
     * Get document.
     *
     * @return the document after transformation.
     */
    T get();

}
