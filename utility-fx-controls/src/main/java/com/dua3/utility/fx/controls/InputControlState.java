package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.PlatformHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The InputControlState interface encapsulates the value, validation logic, error message, and validity state
 * of an {@link InputControl}.
 *
 * @param <R> the type of the value being managed
 */
public abstract class InputControlState<R> {

    private final BooleanProperty required = new SimpleBooleanProperty(true);
    private final BooleanProperty valid = new SimpleBooleanProperty(true);
    private final StringProperty error = new SimpleStringProperty("");
    private final Function<? super @Nullable R, Optional<String>> validate;
    private final Collection<Runnable> validationListeners = new ArrayList<>();
    private final Supplier<? extends @Nullable R> dflt;

    /**
     * Constructs an {@code InputControlState} instance with a provided default value supplier
     * and a validation function to validate the input's value.
     *
     * @param dflt    A {@link Supplier} that provides the default value for the control's state.
     *                This can supply {@code null} if no default value is needed.
     * @param validate A {@link Function} that applies validation logic to the input value.
     *                 It accepts a value of type {@code R} or {@code null} and returns an
     *                 {@link Optional} containing an error message if validation fails,
     *                 or an empty {@code Optional} if the value is valid.
     */
    protected InputControlState(Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate) {
        this.dflt = dflt;
        this.validate = validate;
    }

    /**
     * Provides a property representing whether the input is required or not.
     * The property's value determines if the input control mandates a value.
     *
     * @return a BooleanProperty that indicates whether the input is required
     */
    protected final BooleanProperty requiredProperty() {
        return required;
    }

    /**
     * Provides access to the property representing the current value of the input control.
     * The value can be updated or observed for changes.
     *
     * @return the property holding the value of type {@code R}, which may be {@code null}
     */
    protected abstract Property<@Nullable R> valueProperty();

    /**
     * Returns the property indicating the validity of the input control's state.
     *
     * @return a BooleanProperty representing whether the current state is valid.
     */
    protected final BooleanProperty validProperty() {
        return valid;
    }

    /**
     * Provides access to the property that holds error messages related to validation.
     * This property can be used to observe or update the error state of an input control.
     *
     * @return a {@code StringProperty} representing the current error message.
     *         If no error is present, the property will hold an empty string.
     */
    protected final StringProperty errorProperty() {
        return error;
    }

    /**
     * Returns a function that performs validation on the value managed by this {@code InputControlState}.
     * The function accepts a value of type {@code R} or {@code null} and returns an {@code Optional<String>}
     * representing the validation result. If the {@code Optional} is empty, the value is considered valid;
     * otherwise, it contains an error message describing the validation failure.
     *
     * @return a function used to validate the value of this control state
     */
    protected final Function<? super @Nullable R, Optional<String>> validateFunction() {
        return validate;
    }

    /**
     * Returns a collection of validation listeners associated with the input control state.
     * These listeners are executed whenever the validation logic is triggered, allowing for
     * custom behaviors or updates in response to validation changes.
     *
     * @return a collection of {@link Runnable} instances representing the validation listeners
     */
    protected final Collection<Runnable> validationListeners() {
        return validationListeners;
    }

    /**
     * Provides a supplier for the default value of the control's state.
     *
     * @return a {@link Supplier} that supplies the default value, or null if there is no default value.
     */
    protected final Supplier<? extends @Nullable R> defaultValueSupplier() {
        return dflt;
    }

    /**
     * Invalidates the current state of the input control and triggers a validation of its value.
     * This method ensures that the validation logic is executed on the JavaFX application thread.
     * <p>
     * The validation process may update the control's validation status, error messages, and invoke
     * any associated validation listeners. By default, this operation is scheduled to run later
     * on the JavaFX thread, ensuring thread safety.
     * <p>
     * It uses the {@link PlatformHelper#runLater(Runnable)} utility to schedule the validation logic.
     */
    public void invalidateState() {
        PlatformHelper.runLater(this::validate);
    }

    /**
     * Adds a validation listener to the collection of listeners associated with the input control state.
     * The provided listener will be executed whenever the validation logic is triggered, allowing for
     * custom behaviors or updates in response to validation changes.
     *
     * @param listener a {@link Runnable} instance representing the validation listener to be added
     */
    public void addValidationListener(Runnable listener) {
        validationListeners().add(listener);
    }

    /**
     * Removes a validation listener from the collection of validation listeners associated
     * with the input control state. If the specified listener is present, it will be removed.
     *
     * @param listener the {@link Runnable} instance representing the validation listener to remove
     * @return {@code true} if the listener was removed successfully, {@code false} otherwise
     */
    public boolean removeValidationListener(Runnable listener) {
        return validationListeners().remove(listener);
    }

    /**
     * Resets the state of the input control to its default value and triggers validation.
     * <p>
     * This method invokes the default value supplier to retrieve the default value
     * and assigns it to the input control's state through the {@code setValue} method.
     * After setting the default value, it performs validation by calling the {@code validate} method
     * to ensure the state reflects the appropriate validation results.
     */
    public final void reset() {
        setValue(defaultValueSupplier().get());
        validate();
    }

    /**
     * Validates the current value of the input control using the provided validation function.
     * Updates the validity state and error message based on the result of the validation.
     * Executes any registered validation listeners after the validation process.
     *
     * @return {@code true} if the value is valid; {@code false} otherwise
     */
    public boolean validate() {
        Optional<String> result;
        try {
            result = validateFunction().apply(valueProperty().getValue());
        } catch (Exception e) {
            result = Optional.of(InputControl.INVALID_VALUE);
        }

        validProperty().setValue(result.isEmpty());
        setError(result.orElse(""));

        validationListeners().forEach(Runnable::run);

        return result.isEmpty();
    }

    /**
     * Checks if the current state of the input control is valid.
     * This method evaluates the validity based on the value of the {@code validProperty}.
     *
     * @return {@code true} if the input control's state is valid, otherwise {@code false}
     */
    public final boolean isValid() {
        return validProperty().getValue();
    }

    /**
     * Retrieves the current value of the input control managed by this state.
     * The returned value represents the current state of the control's input and can be {@code null}.
     *
     * @return the current value of type {@code R}, or {@code null} if no value is set
     */
    public @Nullable R getValue() {
        return valueProperty().getValue();
    }

    /**
     * Determines whether the input control mandates a value.
     * This method checks the value of the required property to indicate if input is required.
     *
     * @return {@code true} if the input is required, otherwise {@code false}
     */
    public final boolean isRequired() {
        return requiredProperty().getValue();
    }

    /**
     * Sets a new value for the input control's state.
     * Updates the current value managed by the {@code valueProperty}.
     *
     * @param arg the new value of type {@code R} to set, or {@code null} if no value is provided
     */
    public void setValue(@Nullable R arg) {
        valueProperty().setValue(arg);
    }

    /**
     * Determines whether the input control's value is considered to be empty.
     *
     * @return {@code true} if the input control's value is empty, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return switch (getValue()) {
            case null -> true;
            case String s -> s.isEmpty();
            default -> false;
        };
    }

    /**
     * Updates the error property with the provided error message and adjusts the validity state
     * based on whether the error message is empty or not.
     *
     * @param s the error message to set; an empty string indicates no error
     */
    protected void setError(String s) {
        errorProperty().setValue(s);
        validProperty().setValue(s.isEmpty());
    }

    /**
     * Retrieves the current error message associated with the input control's state.
     * The error message typically represents the result of the validation process.
     *
     * @return the current error message as a {@code String}, or an empty string if no error is present
     */
    public final String getError() {
        return errorProperty().getValue();
    }

    /**
     * Creates and returns an {@code InputControlState} instance specifically designed for inputs
     * that have no associated value (i.e., {@code Void} type). This state model is initialized
     * with a default value supplier that always provides {@code null}.
     *
     * @return an {@code InputControlState<Void>} instance representing an input control state
     *         that doesn't necessitate a specific value.
     */
    public static InputControlState<Void> voidState() {
        return new ObjectInputControlState<>(new SimpleObjectProperty<>(), () -> null);
    }
}
