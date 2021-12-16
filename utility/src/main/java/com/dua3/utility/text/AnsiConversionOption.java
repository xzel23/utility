package com.dua3.utility.text;

import com.dua3.cabe.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Options controling the conversion process.
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

    void apply(@NotNull AnsiConverter converter) {
        action.accept(converter);
    }
    
}
