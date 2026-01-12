package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A builder for {@link CheckMenuItem} instances.
 */
public class CheckMenuItemBuilder extends MenuItemBuilder<CheckMenuItem, CheckMenuItemBuilder> {
    private @Nullable ObservableValue<Boolean> selected;
    private @Nullable Consumer<Boolean> action;

    /**
     * Constructs a new instance of the CheckMenuItemBuilder class.
     *
     * @param factory the supplier that provides a new instance of CheckMenuItem
     */
    CheckMenuItemBuilder(Supplier<CheckMenuItem> factory) {
        super(factory);
    }

    @Override
    public CheckMenuItem build() {
        CheckMenuItem item = super.build();

        if (action != null) {
            item.selectedProperty().addListener((v, o, n) -> action.accept(n));
        }

        apply(selected, item.selectedProperty());
        return item;
    }

    /**
     * Set the selected state for the CheckMenuItem.
     *
     * @param selected the selected state
     * @return this CheckMenuItemBuilder instance
     */
    public CheckMenuItemBuilder selected(boolean selected) {
        this.selected = new SimpleBooleanProperty(selected);
        return self();
    }

    /**
     * Bind the selected state for the CheckMenuItem.
     *
     * @param selected the selected state observable
     * @return this CheckMenuItemBuilder instance
     */
    public CheckMenuItemBuilder selected(ObservableValue<Boolean> selected) {
        this.selected = selected;
        return self();
    }

    /**
     * Set the action for the CheckMenuItem.
     *
     * @param action the action to perform when the selected state changes, receives the new state
     * @return this CheckMenuItemBuilder instance
     */
    public CheckMenuItemBuilder action(Consumer<Boolean> action) {
        this.action = action;
        return self();
    }
}
