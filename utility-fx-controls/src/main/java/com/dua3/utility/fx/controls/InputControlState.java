package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.PlatformHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
    private static final Logger LOG = LogManager.getLogger(InputControlState.class);

    private final BooleanProperty required = new SimpleBooleanProperty(true);
    private final Property<@Nullable R> value;
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    private final StringProperty error = new SimpleStringProperty("");
    private final Function<? super @Nullable R, Optional<String>> validate;
    private final Collection<Runnable> validationListeners = new ArrayList<>();
    private final Supplier<? extends @Nullable R> dflt;
    private final ObservableValue<?> baseValue;

    /**
     * Creates an InputControlState instance configured to manage a property of type Void.
     *
     * @return an InputControlState instance with a property of type Void and default value supplier
     */
    public static InputControlState<Void> voidState() {
        return new InputControlState<>(new SimpleObjectProperty<>(), freeze(new SimpleObjectProperty<>()));
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
     * Constructs a State object with the given value and default value supplier.
     *
     * @param value the property representing the value managed by this State
     * @param dflt  a supplier that provides the default value for the property
     */
    public InputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt) {
        this(value, dflt, s -> Optional.empty());
    }

    /**
     * Constructs a State object with the given value, default value supplier, and validation function.
     *
     * @param value    the property representing the value managed by this State
     * @param dflt     a supplier that provides the default value for the property
     * @param validate a function that validates the value and returns an optional error message
     */
    public InputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate) {
        this (value, dflt, validate, value);
    }

    /**
     * Creates an InputControlState object with the specified value, default value supplier,
     * validation function, and base value.
     *
     * @param value    the property representing the value managed by this State
     * @param dflt     a supplier that provides the default value for the property
     * @param validate a function that validates the value and returns an optional error message
     * @param baseValue the observable base value, which may differ from the value property in certain contexts
     */
    public InputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate, ObservableValue<?> baseValue) {
        this.required.set(validate.apply(null).isPresent());
        this.value = value;
        this.dflt = dflt;
        this.baseValue = baseValue;
        this.validate = validate;
        this.valid.set(validate.apply(value.getValue()).isEmpty());
        this.error.setValue("");

        if (this.baseValue != this.value) {
            this.baseValue.addListener((ObservableValue<?> v, @Nullable Object o, @Nullable Object n) -> invalidateState());
        }
        this.value.addListener((ObservableValue<? extends @Nullable R> v, @Nullable R o, @Nullable R n) -> validate());

        reset();
    }

    private void invalidateState() {
        LOG.debug("invalidateState()");
        PlatformHelper.runLater(this::validate);
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
        LOG.trace("validate()");
        Optional<String> result;
        try {
            result = validate.apply(valueProperty().getValue());
        } catch (Exception e) {
            result = Optional.of(InputControl.INVALID_VALUE);
        }
        LOG.trace("validation result: {}", result);

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

    /**
     * Tests if the control's base value is null.
     * <p>
     * In most controls, there will be no difference between a control's base value and its actual value.
     * An example where both differ is an input field for numbers where the base value is the text entered
     * into the input and the value is the text converted to a number. When conversion fails, the number
     * value will cleared or at least not updated. To distuigish whether nothingg has been entered at all
     * or some invalid input was entered that maps to null, we use the base value.
     *
     * @return true, if the base value is null or the empty string
     */
    public boolean isEmpty() {
        return switch (baseValue.getValue()) {
            case null -> true;
            case String s -> s.isEmpty();
            default -> false;
        };
    }
}
