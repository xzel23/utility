package com.dua3.utility.fx.controls;

import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * InputControlState for list-based input controls.
 *
 * @param <S> the type of elements in the list
 */
public class ListInputControlState<S> extends InputControlState<List<S>> {

    private final ListProperty<S> value;

    /**
     * Constructs a new {@code ListInputControlState} for managing the state of a list-based input control.
     *
     * @param value the {@code ListProperty} representing the list of values managed by this input control state.
     * @param dflt  a supplier that provides the default value for the list input control.
     * @param validate a function used to validate the current state of the list.
     *                 It takes the list as input and returns an {@code Optional<String>} containing
     *                 a validation error message if the list is invalid, or an empty {@code Optional} if valid.
     */
    @SuppressWarnings("unchecked")
    public ListInputControlState(ListProperty<S> value, Supplier<? extends List<S>> dflt, Function<? super List<S>, Optional<String>> validate) {
        super(dflt, validate);
        this.value = value;

        requiredProperty().set(validate.apply(null).isPresent());
        validProperty().set(validate.apply(value.getValue()).isEmpty());
        errorProperty().setValue("");

        value.addListener((ObservableValue<? extends ObservableList<S>> v, @Nullable ObservableList<S> o, @Nullable ObservableList<S> n) -> {
            if (o != null) {
                o.removeListener(this::listChanged);
            }
            if (n != null) {
                n.addListener(this::listChanged);
            }
            validate();
        });

        ObservableList<S> items = value.get();
        if (items != null) {
            items.addListener(this::listChanged);
        }

        reset();
    }

    private void listChanged(ListChangeListener.Change<? extends S> c) {
        validate();
    }

    @Override
    public void setValue(@Nullable List<S> arg) {
        if (arg == null) {
            value.clear();
        } else {
            value.setAll(arg);
        }
    }

    @Override
    protected Property<List<S>> valueProperty() {
        return (Property<List<S>>) (Property<?>) value;
    }

    @Override
    public List<S> getValue() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }
}
