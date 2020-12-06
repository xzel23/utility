package com.dua3.utility.swing;

import com.dua3.utility.data.Pair;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.HtmlConverter;
import com.dua3.utility.text.HtmlTag;
import com.dua3.utility.text.TextAttributes;

import javax.swing.text.AttributeSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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
