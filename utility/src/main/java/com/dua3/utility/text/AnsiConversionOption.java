package com.dua3.utility.text;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public record AnsiConversionOption(Consumer<? super AnsiConverter> action) {

    public AnsiConversionOption {
        Objects.requireNonNull(action);
    }

    void apply(AnsiConverter converter) {
        action.accept(converter);
    }
    
}
