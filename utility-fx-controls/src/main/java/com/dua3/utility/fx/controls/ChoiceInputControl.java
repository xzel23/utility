package com.dua3.utility.fx.controls;

import com.dua3.utility.options.ChoiceOption;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

import java.util.function.Supplier;

/**
 * Interface for an input field.
 *
 * @param <T> the input result type
 */
public class ChoiceInputControl<T> implements InputControl<T> {

    private final ComboBox<ChoiceOption.Choice<T>> control;
    private final ChoiceOption<T> option;
    private final Supplier<? extends T> dfltValue;
    private final Property<T> valueProperty;

    /**
     * Constructs a ChoiceInputControl with the given options and default value supplier.
     *
     * @param option the ChoiceOption containing possible values for the input control
     * @param dfltValue a Supplier that provides the default value for the input control
     */
    public ChoiceInputControl(ChoiceOption<T> option, Supplier<? extends T> dfltValue) {
        this.option = option;
        this.dfltValue = dfltValue;
        this.control = new ComboBox<>();
        this.valueProperty = new SimpleObjectProperty<>();

        control.valueProperty().addListener((v, o, n) -> valueProperty.setValue(n == null ? null : n.value()));
        valueProperty.addListener((v, o, n) -> control.getSelectionModel().select(n == null ? null : option.choice(n)));

        control.getItems().setAll(option.choices());
        control.getSelectionModel().select(option.choice(dfltValue.get()));
    }

    @Override
    public Node node() {
        return control;
    }

    @Override
    public Property<T> valueProperty() {
        return valueProperty;
    }

    @Override
    public void reset() {
        valueProperty.setValue(dfltValue.get());
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return new SimpleStringProperty("");
    }
}
