package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SimpleInputControl is a generic class designed to manage an input control element and its state.
 * It provides functionalities to reset the control's value, validate input, and obtain error messages.
 *
 * @param <C> the type of the control, which must extend from Control
 * @param <R> the type of the value held by the input control
 */
public final class SimpleInputControl<C extends Control, R> implements InputControl<R> {

    private final C control;
    private final InputControlState<R> state;

    /**
     * Constructs a SimpleInputControl instance for managing an input control element and its state.
     *
     * @param control the control element of type C to be managed
     * @param value the property representing the value held by the input control
     * @param dflt a supplier that provides the default value for the control's state
     * @param validate a function that validates the value and returns an optional error message
     */
    SimpleInputControl(C control, Property<R> value, Supplier<? extends @Nullable R> dflt, Function<@Nullable R, Optional<String>> validate) {
        this(control, value, dflt, validate, value);
    }

    SimpleInputControl(C control, Property<R> value, Supplier<? extends @Nullable R> dflt, Function<@Nullable R, Optional<String>> validate, ObservableValue<?> contentBase) {
        this.control = control;
        this.state = new InputControlState<>(value, dflt, validate, contentBase);

        state.requiredProperty().addListener((v, o, n) -> {
            if (n) {
                control.getStyleClass().add(CSS_REQUIRED_INPUT);
            } else {
                control.getStyleClass().remove(CSS_REQUIRED_INPUT);
            }
        });

        reset();

        // perform a validation when the control receives or looses focus
        control.focusedProperty().addListener((v, o, n) -> state.validate());
    }

    @Override
    public InputControlState<R> state() {
        return state;
    }

    @Override
    public Property<R> valueProperty() {
        return state.valueProperty();
    }

    @Override
    public @NonNull C node() {
        return control;
    }
}
