package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.control.Control;
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
public final class SimpleInputControl<C extends Control, R extends @Nullable Object> implements InputControl<R> {

    private final C control;
    private final State<R> state;
    private final Supplier<? extends R> dflt;

    /**
     * Constructs a SimpleInputControl instance for managing an input control element and its state.
     *
     * @param control the control element of type C to be managed
     * @param value the property representing the value held by the input control
     * @param dflt a supplier that provides the default value for the control's state
     * @param validate a function that validates the value and returns an optional error message
     */
    protected SimpleInputControl(C control, Property<R> value, Supplier<? extends R> dflt, Function<R, Optional<String>> validate) {
        this.control = control;
        this.state = new State<>(value, dflt, validate);
        this.dflt = dflt;

        state.requiredProperty().addListener((v, o, n) -> {
            if (n) {
                control.getStyleClass().add(CSS_REQUIRED_INPUT);
            } else {
                control.getStyleClass().remove(CSS_REQUIRED_INPUT);
            }
        });

        reset();

        control.focusedProperty().addListener((v, o, n) -> state.validate());
        valueProperty().addListener((v, o, n) -> state.validate());
    }

    @Override
    public C node() {
        return control;
    }

    @Override
    public Property<R> valueProperty() {
        return state.valueProperty();
    }

    @Override
    public void reset() {
        state.valueProperty().setValue(dflt.get());
    }

    @Override
    public ReadOnlyBooleanProperty requiredProperty() {
        return state.requiredProperty();
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return state.validProperty();
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return state.errorProperty();
    }
}
