package com.dua3.utility.fx.controls;

import java.util.Optional;
import java.util.function.Function;

import javafx.scene.control.TextArea;

/**
 * A builder class for creating and configuring text area input controls.
 * This builder provides methods to customize the properties of the text area,
 * including the number of columns, rows, and validation logic.
 */
public class TextAreaBuilder extends InputControlBuilder<TextAreaBuilder, String> {

    private int columns = 40;
    private int rows = 5;
    Function<String, Optional<String>> validate = s -> Optional.empty();

    TextAreaBuilder() {
        // nothing to do
    }

    /**
     * Sets the number of columns for the text area.
     * This method modifies the preferred column count for the text area
     * and allows method chaining through the returned builder instance.
     *
     * @param columns the number of columns to set for the text area
     * @return the current instance of {@code TextAreaBuilder} for method chaining
     */
    public TextAreaBuilder columns(int columns) {
        this.columns = columns;
        return self();
    }

    /**
     * Sets the number of rows for the text area input control.
     *
     * @param rows the number of rows to set for the text area
     * @return the current instance of {@code TextAreaBuilder} for method chaining
     */
    public TextAreaBuilder rows(int rows) {
        this.rows = rows;
        return self();
    }

    /**
     * Specifies a validation function for the text area's input.
     * The provided function takes a string input and returns an {@code Optional<String>}
     * containing an error message if the input is invalid, or an empty {@code Optional} if valid.
     *
     * @param validate a {@code Function} that validates the input string and returns
     *                 an {@code Optional<String>} with an error message or empty if valid
     * @return the current instance of {@code TextAreaBuilder} for method chaining
     */
    public TextAreaBuilder validate(Function<String, Optional<String>> validate) {
        this.validate = validate;
        return self();
    }

    @Override
    public InputControl<String> build() {
        TextArea textArea = new TextArea();
        textArea.setPrefColumnCount(columns);
        textArea.setPrefRowCount(rows);
        return new SimpleInputControl<>(textArea, textArea.textProperty(), getDefault(), validate);
    }
}
