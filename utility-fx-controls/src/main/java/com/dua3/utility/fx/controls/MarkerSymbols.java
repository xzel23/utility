package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;

import java.util.stream.Stream;

/**
 * A record that represents a set of marker symbols used for various states 
 * of required and optional fields. These symbols can be used in UI components 
 * to visually indicate the state of a field.
 *
 * @param optionalEmpty symbol for optional fields that are empty
 * @param optionalFilled symbol for optional fields that are filled
 * @param optionalError symbol for optional fields that have an error
 * @param requiredEmpty symbol for required fields that are empty
 * @param requiredFilled symbol for required fields that are filled
 * @param requiredError symbol for required fields that have an error
 */
public record MarkerSymbols(
        String optionalEmpty,
        String optionalFilled,
        String optionalError,
        String requiredEmpty,
        String requiredFilled,
        String requiredError
) {
    /**
     * Returns a MarkerSymbols instance with a predefined set of default symbols
     * for optional and required fields.
     * <p>
     * The default symbols are:
     * - Optional empty: (empty string)
     * - Optional filled: (empty string)
     * - Optional error: ⚠
     * - Required empty: *
     * - Required filled: *
     * - Required error: ⚠
     *
     * @return a MarkerSymbols instance representing the default marker symbols
     */
    public static MarkerSymbols defaultSymbols() {
        return new MarkerSymbols(
                "", "", "⚠",
                "*", "*", "⚠"
        );
    }

    /**
     * Returns a MarkerSymbols instance configured to only display error symbols
     * for both optional and required fields, while leaving other states blank.
     *
     * @return a MarkerSymbols instance with only error symbols defined for
     *         optional and required fields
     */
    public static MarkerSymbols onlyErrors() {
        return new MarkerSymbols(
                "", "", "⚠",
                "", "", "⚠"
        );
    }

    /**
     * Creates a new instance of MarkerSymbols with all symbol fields set to empty strings.
     * This can be used to represent a state where no symbols are needed or desired.
     *
     * @return a MarkerSymbols instance with all fields set to empty strings
     */
    public static MarkerSymbols noSymbols() {
        return new MarkerSymbols(
                "", "", "",
                "", "", ""
        );
    }

    /**
     * Calculates the maximum width required to render any of the marker symbols
     * in the provided font, ensuring it is at least the specified minimum width.
     * The width is determined based on the rendered size of the text for all marker
     * symbols associated with optional and required fields in various states.
     *
     * @param font the font used to calculate the rendered text width of the marker symbols
     * @param minWidth the minimum width to enforce, ensuring the resulting width is at least this value
     * @return the maximum calculated width required to render the marker symbols or the minimum width, whichever is larger
     */
    public double calculateWidth(Font font, double minWidth) {
        FontUtil fu = FxFontUtil.getInstance();
        return Math.max(Stream.of(
                                optionalEmpty,
                                optionalFilled,
                                optionalError,
                                requiredEmpty,
                                requiredFilled,
                                requiredError
                        )
                        .mapToDouble(s -> fu.getTextWidth(s, font))
                        .max()
                        .orElse(0.0),
                minWidth
        );
    }

    /**
     * Returns the marker symbol corresponding to the provided field state.
     *
     * @param required a boolean indicating whether the field is required (true) or optional (false)
     * @param filled a boolean indicating whether the field is filled (true) or empty (false)
     * @param error a boolean indicating whether the field has an error (true) or not (false)
     * @return a string representing the symbol for the field state based on the given parameters
     */
    public String getMarker(boolean required, boolean filled, boolean error) {
        if (error) {
            return required ? requiredError : optionalError;
        }
        if (filled) {
            return required ? requiredFilled : optionalFilled;
        }
        return required ? requiredEmpty : optionalEmpty;
    }
}
