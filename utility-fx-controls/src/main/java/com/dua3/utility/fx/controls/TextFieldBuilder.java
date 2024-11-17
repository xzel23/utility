package com.dua3.utility.fx.controls;

import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * A builder class for constructing customized TextFields.
 *
 * <p>This class supports building TextFields with specific configurations
 * such as setting text, type, and binding the disabled state.
 */
public final class TextFieldBuilder {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("\\d*|0");
    private static final UnaryOperator<TextFormatter.@Nullable Change> INTEGER_FILTER = change -> INTEGER_PATTERN.matcher(change.getControlNewText()).matches() ? change : null;

    private static final Pattern SIGNED_INTEGER_PATTERN = Pattern.compile("-?([1-9]\\d*|0)?");
    private static final UnaryOperator<TextFormatter.@Nullable Change> SIGNED_INTEGER_FILTER = change -> SIGNED_INTEGER_PATTERN.matcher(change.getControlNewText()).matches() ? change : null;

    private final Pattern floatPattern;
    private final UnaryOperator<TextFormatter.@Nullable Change> floatFilter;

    private static TextFormatter<Integer> getIntegerTextFormatter(UnaryOperator<TextFormatter.Change> filter) {
        return new TextFormatter<>(new IntegerStringConverter(), 0, filter);
    }

    private TextFormatter<BigDecimal> getFloatTextFormatter(UnaryOperator<TextFormatter.Change> filter) {
        return new TextFormatter<>(new BigDecimalStringConverter(), BigDecimal.ZERO, filter);
    }

    private final Locale locale;
    private @Nullable String text;
    private TextFieldType type = TextFieldType.TEXT;
    private @Nullable ObservableValue<Boolean> disabled;

    TextFieldBuilder(Locale locale) {
        this.locale = locale;

        this.floatPattern = getFloatPattern();
        this.floatFilter = change -> floatPattern.matcher(change.getControlNewText()).matches() ? change : null;
    }

    private Pattern getFloatPattern() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
        char decimalSeparator = symbols.getDecimalSeparator();
        char groupingSeparator = symbols.getGroupingSeparator();

        String escapedDecimalSeparator = Pattern.quote(String.valueOf(decimalSeparator));
        String escapedGroupingSeparator = Pattern.quote(String.valueOf(groupingSeparator));

        return Pattern.compile("[+-]?([0-9" + escapedGroupingSeparator + "]*" + escapedDecimalSeparator + ")?[0-9]+");
    }

    /**
     * Sets the text for the TextField being constructed.
     *
     * @param text the text to set in the TextField
     * @return this builder instance
     */
    public TextFieldBuilder text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the type of the TextField.
     *
     * <p>This method allows you to specify the type of input the TextField should accept.
     * The supported types are defined in the {@link TextFieldType} enum.
     *
     * @param type the type to set for the TextField
     * @return this instance for method chaining
     */
    public TextFieldBuilder type(TextFieldType type) {
        this.type = type;
        return this;
    }

    /**
     * Bind the control's disabled state to an {@link ObservableValue}.
     * @param disabled the value to bind the control's disableProperty to
     * @return this instance
     */
    public TextFieldBuilder bindDisabled(ObservableBooleanValue disabled) {
        this.disabled = disabled;
        return this;
    }

    /**
     * Constructs and returns a customized TextField based on the configurations set in the builder.
     *
     * @return the configured TextField instance
     */
    public TextField build() {
        TextField tf = new TextField();

        switch (type) {
            case INTEGER -> tf.setTextFormatter(getIntegerTextFormatter(INTEGER_FILTER));
            case SIGNED_INTEGER -> tf.setTextFormatter(getIntegerTextFormatter(SIGNED_INTEGER_FILTER));
            case FLOAT -> tf.setTextFormatter(getFloatTextFormatter(floatFilter));
            case TEXT -> {}
        }

        if (text != null) {
            tf.setText(text);
        }

        if (disabled != null) {
            tf.disableProperty().bind(disabled);
        }

        return tf;
    }

}
