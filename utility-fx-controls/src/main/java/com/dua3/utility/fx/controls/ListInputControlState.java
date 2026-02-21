package com.dua3.utility.fx.controls;

import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * InputControlState for list-based input controls.
 *
 * @param <S> the type of elements in the list
 */
public class ListInputControlState<S> extends InputControlState<ObservableList<S>> {

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
    public ListInputControlState(ListProperty<S> value, Supplier<? extends Collection<S>> dflt, Function<List<S>, Optional<String>> validate) {
        super(toObservableListSupplier(dflt), validate);
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

    /**
     * Converts a supplier of a {@code Collection<S>} into a supplier of an {@code ObservableList<S>}.
     * If the supplied collection is already an {@code ObservableList}, it is returned directly.
     * Otherwise, a new observable list is created from the collection.
     * If the supplied collection is {@code null}, an empty {@code ObservableList} is returned.
     *
     * @param <S> the type of elements in the collection and observable list
     * @param dflt a supplier that provides a {@code Collection<S>} or {@code null},
     *             which will be converted to an {@code ObservableList<S>}
     * @return a {@code Supplier<ObservableList<S>>} that provides an observable list
     *         derived from the supplied collection
     */
    @SuppressWarnings("unchecked")
    private static <S> Supplier<ObservableList<S>> toObservableListSupplier(Supplier<? extends @Nullable Collection<S>> dflt) {
        return () -> (ObservableList<S>) switch (dflt.get()) {
            case ObservableList<?> ol -> ol;
            case Collection<?> col -> FXCollections.observableArrayList(col);
            case null -> FXCollections.observableArrayList();
        };
    }

    /**
     * Sets the value of this input control state with the provided {@link SequencedCollection}.
     * If the given collection is {@code null}, the current value will be cleared.
     * Otherwise, the elements of the provided collection will replace the current value.
     *
     * @param arg the {@code SequencedCollection} containing the new elements to set,
     *            or {@code null} to clear the current value
     */
    public void setItems(Collection<S> arg) {
        value.setAll(arg);
    }

    @Override
    public void setValue(@Nullable ObservableList<S> arg) {
        if (arg == null) {
            value.clear();
        } else {
            value.setAll(arg);
        }
    }

    @Override
    protected ListProperty<S> valueProperty() {
        return value;
    }

    @Override
    public ObservableList<S> getValue() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }
}
