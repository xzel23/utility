package com.dua3.utility.text;

import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public class AnsiConversionOption {
    private final Consumer<? super AnsiConverter> action;

    protected AnsiConversionOption(Consumer<? super AnsiConverter> action) {
        this.action = action;
    }

    void apply(AnsiConverter converter) {
        action.accept(converter);
    }
}
