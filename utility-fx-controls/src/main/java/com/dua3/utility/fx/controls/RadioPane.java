package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.function.Function;


/**
 * A custom control pane that arranges radio buttons vertically.
 * This class extends VBox and implements InputControl to provide
 * selection and validation capabilities.
 *
 * @param <T> The type of items that will be represented as radio buttons.
 */
public class RadioPane<T> extends VBox implements InputControl<T> {

    private static final double SPACING = 4;
    private final LinkedHashMap<T, RadioButton> items = new LinkedHashMap<>();
    private final ToggleGroup group;
    private final InputControl.State<T> state;

    /**
     * Constructs a RadioPane with a given set of items, current value, and validation function.
     *
     * @param items        the collection of items to be represented as radio buttons
     * @param currentValue the item to be selected initially, nullable
     * @param validate     the validation function to validate the selected item, returning an optional error message
     */
    @SuppressWarnings("unchecked")
    public RadioPane(Collection<T> items, @Nullable T currentValue, Function<T, Optional<String>> validate) {
        this.group = new ToggleGroup();

        setSpacing(SPACING);
        ObservableList<Node> children = getChildren();
        for (var item : items) {
            RadioButton control = new RadioButton(String.valueOf(item));
            control.setUserData(item);
            control.setToggleGroup(group);
            children.add(control);
            this.items.put(item, control);
        }

        // update state when selected toggle changes
        Property<@Nullable T> property = new SimpleObjectProperty<>();
        group.selectedToggleProperty().addListener((v, o, n) -> {
            Toggle toggle = group.getSelectedToggle();
            property.setValue(toggle != null ? (T) toggle.getUserData() : null);
        });

        this.state = new State<>(property);
        state.setValidate(validate);

        // update toggle when state changes
        state.valueProperty().addListener((v, o, n) -> group.selectToggle(this.items.get(n)));

        // set initial toggle
        group.selectToggle(this.items.get(currentValue));
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public void reset() {
        state.reset();
    }

    @Override
    public Property<@Nullable T> valueProperty() {
        return state.valueProperty();
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

    @Override
    public void requestFocus() {
        if (group.getToggles().isEmpty()) {
            super.requestFocus();
        }

        Toggle t = group.getSelectedToggle();
        if (t == null) {
            t = group.getToggles().getFirst();

        }

        if (t instanceof Control c) {
            c.requestFocus();
        } else {
            super.requestFocus();
        }
    }
}
