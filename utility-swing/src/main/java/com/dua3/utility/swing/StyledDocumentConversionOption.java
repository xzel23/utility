package com.dua3.utility.swing;

import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public record StyledDocumentConversionOption (Consumer<? super StyledDocumentConverter> action) {
    void apply(StyledDocumentConverter converter) {
        action.accept(converter);
    }
}
