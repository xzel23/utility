package com.dua3.utility.swing;

import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public class StyledDocumentConversionOption {

    private final Consumer<StyledDocumentConverter> action;

    protected StyledDocumentConversionOption(Consumer<StyledDocumentConverter> action) {
        this.action = action;
    }

    void apply(StyledDocumentConverter converter) {
        action.accept(converter);
    }
}
