package com.dua3.utility.fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The InputControlState class encapsulates the value, validation logic, error message, and validity state
 * of an {@link InputControl}.
 *
 * @param <R> the type of the value being managed
 */
public final class InputControlState<R> {
    private final BooleanProperty required = new SimpleBooleanProperty(true);
    private final Property<@Nullable R> value;
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    private final StringProperty error = new SimpleStringProperty("");
    private final Function<? super @Nullable R, Optional<String>> validate;
    private final List<Runnable> validationListeners = new ArrayList<>();
    private final Supplier<? extends @Nullable R> dflt;

    /**
     * Constructs a State object with the given value.
     *
     * @param value the property representing the value managed by this State
     */
    public InputControlState(Property<@Nullable R> value) {
        this(value, freeze(value));
    }

    /**
     * Constructs a State object with the given value and default value supplier.
     *
     * @param value the property representing the value managed by this State
     * @param dflt  a supplier that provides the default value for the property
     */
    public InputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt) {
        this(value, dflt, s -> Optional.empty());
    }

    /**
     * Creates a supplier that always returns the current value of the given ObservableValue,
     * capturing its value at the moment this method is called.
     *
     * @param value the ObservableValue whose current value is to be captured
     * @return a Supplier that returns the captured value
     */
    private static <R> Supplier<R> freeze(ObservableValue<? extends R> value) {
        final R frozen = value.getValue();
        return () -> frozen;
    }

    /**
     * Constructs a State object with the given value, default value supplier, and validation function.
     *
     * @param value    the property representing the value managed by this State
     * @param dflt     a supplier that provides the default value for the property
     * @param validate a function that validates the value and returns an optional error message
     */
    public InputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate) {
        this.required.set(validate.apply(null).isPresent());
        this.value = value;
        this.value.addListener((ObservableValue<? extends @Nullable R> v, @Nullable R o, @Nullable R n) -> updateValidState(n));
        this.dflt = dflt;
        this.validate = validate;
        this.valid.set(validate.apply(value.getValue()).isEmpty());
        this.error.setValue("");
    }

    /**
     * Updates the validity state of the control based on the given value
     * and the validation function provided during initialization or set later.
     * It evaluates whether the value is valid and updates the valid and error
     * properties accordingly.
     *
     * @param r the value to be validated, or null if there's no value. If the value
     *          is invalid according to the validation function, an error message
     *          will be set.
     */
    private void updateValidState(@Nullable R r) {
        Optional<String> result = validate.apply(r);
        valid.setValue(result.isEmpty());
        error.setValue(result.orElse(""));
    }

    /**
     * Adds a validation listener to the State. The listener will be executed when
     * the validation process occurs, allowing custom logic to be triggered based
     * on validation events.
     *
     * @param listener the Runnable that will be executed during validation
     */
    public void addValidationListener(Runnable listener) {
        validationListeners.add(listener);
    }

    /**
     * Removes a validation listener from the state.
     *
     * @param listener the validation listener to be removed
     * @return true if the specified listener was successfully removed, false otherwise
     */
    public boolean removeValidationListener(Runnable listener) {
        return validationListeners.remove(listener);
    }

    /**
     * Provides a read-only boolean property indicating whether this State is marked as required.
     *
     * @return a {@link ReadOnlyBooleanProperty} representing the required status of this State
     */
    public ReadOnlyBooleanProperty requiredProperty() {
        return required;
    }

    /**
     * Returns the property representing the value managed by this State.
     *
     * @return the property representing the value
     */
    public Property<@Nullable R> valueProperty() {
        return value;
    }

    /**
     * Provides a read-only boolean property indicating the validity state.
     *
     * @return a {@link ReadOnlyBooleanProperty} representing whether the current state is valid
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }

    /**
     * Returns a read-only string property representing the current error message.
     * If the value is valid, the error message will be an empty string.
     *
     * @return ReadOnlyStringProperty representing the error message.
     */
    public ReadOnlyStringProperty errorProperty() {
        return error;
    }

    /**
     * Resets the state to its default value.
     *
     * <p>This method sets the current value of the property managed by this
     * state to the default value supplied during the creation of the state.
     */
    public void reset() {
        value.setValue(dflt.get());
    }

    /**
     * Validates the current state based on the value and validation function provided during
     * the creation of the State object or set later and updates the valid state of the control.
     *
     * @return true if the current value of the property is valid, otherwise false
     */
    boolean validate() {
        Optional<String> result;
        try {
            result = validate.apply(valueProperty().getValue());
        } catch (Exception e) {
            result = Optional.of(InputControl.INVALID_VALUE);
        }
        valid.setValue(result.isEmpty());
        error.setValue(result.orElse(""));

        validationListeners.forEach(Runnable::run);

        return result.isEmpty();
    }

    /**
     * Determines whether the current state is valid.
     *
     * @return true if the state is valid, otherwise false
     */
    public boolean isValid() {
        return valid.getValue();
    }

    /**
     * Retrieves the current value managed by this State.
     *
     * @return the current value of the property.
     */
    public @Nullable R getValue() {
        return value.getValue();
    }

    /**
     * Determines whether the state is marked as required.
     *
     * @return true if the state is required; false otherwise
     */
    public boolean isRequired() {
        return required.getValue();
    }

    /**
     * Sets a new value for the property managed by this State.
     *
     * @param arg the new value to be assigned to the property
     */
    public void setValue(@Nullable R arg) {
        value.setValue(arg);
    }
}
