package com.dua3.utility.text;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controlling the conversion process.
 * @param action the action to execute when applying the option (must not be null)
 */
public record AnsiConversionOption(Consumer<? super AnsiConverter> action) {

    /**
     * Record constructor.
     * @param action the action to execute when applying the option (must not be null)
     */
    public AnsiConversionOption {
        Objects.requireNonNull(action);
    }

    void apply(AnsiConverter converter) {
        action.accept(converter);
    }
    
}
