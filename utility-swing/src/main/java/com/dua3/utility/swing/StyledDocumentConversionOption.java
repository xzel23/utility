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

    /**
     * Set default attributes.
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addDefaultAttributes(List<Pair<Object,Object>> attributes) {
        return new StyledDocumentConversionOption(c -> c.addAttributes(attributes));
    }

    /**
     * Set default attributes.
     * @param attributes attributes for new StyledDocuments
     * @return the option to use
     */
    public static StyledDocumentConversionOption addDefaultAttributes(AttributeSet attributes) {
        return new StyledDocumentConversionOption(c -> c.addAttributes(attributes));
    }

    /**
     * Set default font.
     * @param font the default font
     * @return the option to use
     */
    public static StyledDocumentConversionOption defaultFont(Font font) {
        return new StyledDocumentConversionOption(c -> c.setDefaultFont(font));
    }

    /**
     * Set font scaling factor.
     * @param scale the font scaling factor
     * @return the option to use
     */
    public static StyledDocumentConversionOption scale(double scale) {
        return new StyledDocumentConversionOption(c -> c.setScale(scale));
    }

    private final Consumer<StyledDocumentConverter> action;

    protected StyledDocumentConversionOption(Consumer<StyledDocumentConverter> action) {
        this.action = action;
    }

    void apply(StyledDocumentConverter converter) {
        action.accept(converter);
    }
}
