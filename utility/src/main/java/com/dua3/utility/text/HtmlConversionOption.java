package com.dua3.utility.text;

import java.util.function.Consumer;

/**
 * Options controlling the conversion process.
 *
 * @param action the action that applies this option to a {@link HtmlConverter}
 */
public record HtmlConversionOption(Consumer<? super HtmlConverter> action) {

    /**
     * Apply option to {@link HtmlConverter}.
     *
     * @param converter the converter
     */
    void apply(HtmlConverter converter) {
        action.accept(converter);
    }

}
