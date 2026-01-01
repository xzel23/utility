package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ControlBuilder;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Builder for {@link ComboBox}.
 *
 * @param <T> the type of the items in the ComboBox
 */
public class ComboBoxBuilder<T> extends ControlBuilder<ComboBox<T>, ComboBoxBuilder<T>> {
    private @Nullable ObservableList<T> items;
    private final Property<@Nullable T> value = new SimpleObjectProperty<>();
    private @Nullable Consumer<? super T> onChange;

    /**
     * Constructs a new ComboBoxBuilder.
     *
     * @param factory the factory for creating ComboBox instances
     */
    ComboBoxBuilder(Supplier<ComboBox<T>> factory) {
        super(factory);
    }

    @Override
    public ComboBox<T> build() {
        ComboBox<T> comboBox = super.build();
        if (items != null) {
            comboBox.setItems(items);
        }
        apply(value, comboBox.valueProperty());
        if (onChange != null) {
            comboBox.valueProperty().addListener((obs, oldVal, newVal) -> onChange.accept(newVal));
        }
        return comboBox;
    }

    /**
     * Sets the items for the ComboBox.
     *
     * @param items the items to set
     * @return this builder
     */
    public ComboBoxBuilder<T> items(Collection<T> items) {
        this.items = items instanceof ObservableList<T> ol ? ol : FXCollections.observableArrayList(items);
        return self();
    }

    /**
     * Sets the items for the ComboBox.
     *
     * @param items the items to set
     * @return this builder
     */
    @SafeVarargs
    public final ComboBoxBuilder<T> items(T... items) {
        this.items = FXCollections.observableArrayList(items);
        return self();
    }

    /**
     * Sets the initial value for the ComboBox.
     *
     * @param value the initial value
     * @return this builder
     */
    public ComboBoxBuilder<T> value(T value) {
        this.value.setValue(value);
        return self();
    }

    /**
     * Binds the ComboBox value property to an external property.
     *
     * @param property the property to bind to
     * @return this builder
     */
    @SuppressWarnings("unchecked")
    public ComboBoxBuilder<T> bind(Property<? super T> property) {
        this.value.bindBidirectional((Property<@Nullable T>) property);
        return self();
    }

    /**
     * Sets the onChange consumer for the ComboBox.
     *
     * @param onChange the consumer to be called when the value changes
     * @return this builder
     */
    public ComboBoxBuilder<T> onChange(Consumer<? super T> onChange) {
        this.onChange = onChange;
        return self();
    }
}
