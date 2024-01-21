package com.dua3.utility.text;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controlling the conversion process.
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
