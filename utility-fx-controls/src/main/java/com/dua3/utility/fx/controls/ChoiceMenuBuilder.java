package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * A builder for menus with a choice of items.
 *
 * @param <T> the type of the values
 */
public class ChoiceMenuBuilder<T> extends MenuItemBuilder<Menu, ChoiceMenuBuilder<T>> {
    private final Property<T> property;
    private final Collection<T> values;
    private @Nullable ObservableValue<Boolean> enabled;

    /**
     * Constructs a new instance of the ChoiceMenuBuilder class.
     *
     * @param factory  the factory method for Menu instances
     * @param property the property to bind the choices to
     * @param values   the values to choose from
     */
    ChoiceMenuBuilder(Supplier<Menu> factory, Property<T> property, Collection<T> values) {
        super(factory);
        this.property = property;
        this.values = values;
    }

    @Override
    public ChoiceMenuBuilder<T> enabled(ObservableValue<Boolean> enabled) {
        this.enabled = enabled;
        return super.enabled(enabled);
    }

    @Override
    public Menu build() {
        Menu menu = super.build();
        ToggleGroup group = new ToggleGroup();
        for (T value : values) {
            RadioMenuItem item = new RadioMenuItem(String.valueOf(value));
            item.setToggleGroup(group);
            item.setSelected(value.equals(property.getValue()));
            item.setOnAction(evt -> property.setValue(value));
            if (enabled != null) {
                item.disableProperty().bind(enabled.map(b -> !b));
            }
            menu.getItems().add(item);
        }
        return menu;
    }
}
