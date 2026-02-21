package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of {@link InputControlState} for standard objects.
 *
 * @param <R> the type of the value being managed
 */
public class ObjectInputControlState<R> extends InputControlState<R> {
    private final Property<@Nullable R> value;
    private final ObservableValue<?> baseValue;

    /**
     * Constructs an instance of {@code ObjectInputControlState} with the specified value property,
     * default value supplier, and validation function.
     *
     * @param value the {@link Property} holding the value being managed, which may be nullable
     * @param dflt a {@link Supplier} providing the default value, which may be nullable
     */
    public ObjectInputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt) {
        this(value, dflt, s -> Optional.empty());
    }

    /**
     * Constructs an instance of {@code ObjectInputControlState} for managing input control state with custom validation
     * and optional base value tracking.
     *
     * @param value the property holding the current value of the input control
     * @param dflt a supplier providing the default value for the input control
     * @param validate a function that performs validation on the input value and returns an {@code Optional} containing
     *                 an error message if validation fails, or an empty {@code Optional} if validation passes
     */
    public ObjectInputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate) {
        this(value, dflt, validate, value);
    }

    /**
     * Constructs an instance of {@code ObjectInputControlState} to manage the state of an object input control.
     *
     * @param value     A {@link Property} containing the current value of the input control.
     *                  Changes to this value will trigger validation and state updates.
     * @param dflt      A {@link Supplier} that provides the default value for the input control.
     *                  The supplier can return {@code null} to indicate no default value.
     * @param validate  A {@link Function} that validates the input's value.
     *                  It takes a value of type {@code R} or {@code null} and returns an
     *                  {@link Optional} containing an error message if validation fails, or an empty
     *                  {@code Optional} if the value is valid.
     * @param baseValue An {@link ObservableValue} that represents an external factor affecting the input control.
     *                  Changes to this value will trigger state invalidation.
     */
    public ObjectInputControlState(Property<@Nullable R> value, Supplier<? extends @Nullable R> dflt, Function<? super @Nullable R, Optional<String>> validate, ObservableValue<?> baseValue) {
        super(dflt, validate);
        this.value = value;
        this.baseValue = baseValue;

        requiredProperty().set(validate.apply(null).isPresent());
        validProperty().set(validate.apply(value.getValue()).isEmpty());
        errorProperty().setValue("");

        if (this.baseValue != this.value) {
            this.baseValue.addListener((ObservableValue<?> v, @Nullable Object o, @Nullable Object n) -> invalidateState());
        }
        this.value.addListener((ObservableValue<? extends @Nullable R> v, @Nullable R o, @Nullable R n) -> validate());

        reset();
    }

    @Override
    protected Property<@Nullable R> valueProperty() {
        return value;
    }
}
