package com.dua3.utility.fx.controls;

import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SimpleInputControl for list-based input controls.
 *
 * @param <C> the type of the control
 * @param <S> the type of elements in the list
 */
public final class ListInputControl<C extends Control, S> extends SimpleInputControl<C, ObservableList<S>> {

    /**
     * Constructs a ListInputControl instance used for managing list-based inputs.
     *
     * @param control the underlying GUI control associated with this input control
     * @param value the property representing the list of values to bind to this control
     * @param dflt a supplier providing the default list of values for this control
     * @param validate a function used to validate the list of values and optionally return an error message
     */
    public ListInputControl(C control, ListProperty<S> value, Supplier<? extends Collection<S>> dflt, Function<List<S>, Optional<String>> validate) {
        super(control, new ListInputControlState<>(value, dflt, validate));
    }

}
