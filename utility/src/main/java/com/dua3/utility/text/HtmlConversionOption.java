package com.dua3.utility.text;

import com.dua3.cabe.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public record HtmlConversionOption(Consumer<? super HtmlConverter> action) {

    public HtmlConversionOption {
        Objects.requireNonNull(action);
    }

    void apply(@NotNull HtmlConverter converter) {
        action.accept(converter);
    }
    
}
