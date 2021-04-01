package com.dua3.utility.text;

import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public class HtmlConversionOption {
    private final Consumer<? super HtmlConverter> action;

    protected HtmlConversionOption(Consumer<? super HtmlConverter> action) {
        this.action = action;
    }

    void apply(HtmlConverter converter) {
        action.accept(converter);
    }
}
