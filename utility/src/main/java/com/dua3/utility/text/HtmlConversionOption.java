package com.dua3.utility.text;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controlling the conversion process.
 */
public record HtmlConversionOption(Consumer<? super HtmlConverter> action) {

    /**
     * Constructor.
     *
     * @param action the action executed when applying the option to a {@link HtmlConverter}
     */
    public HtmlConversionOption {
        Objects.requireNonNull(action);
    }

    /**
     * Apply option to {@link HtmlConverter}.
     *
     * @param converter the converter
     */
    void apply(HtmlConverter converter) {
        action.accept(converter);
    }

}
