package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckMenuItem;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A builder for {@link CheckMenuItem} instances.
 */
public class CheckMenuItemBuilder extends MenuItemBuilder<CheckMenuItem, CheckMenuItemBuilder> {
    private @Nullable ObservableValue<Boolean> selected;

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
     * Set the selected state for the CheckMenuItem.
     *
     * @param selected the selected state property
     * @return this CheckMenuItemBuilder instance
     */
    public CheckMenuItemBuilder selected(Property<Boolean> selected) {
        this.selected = selected;
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
     * Bind the selected state for the CheckMenuItem.
     *
     * @param selected the selected state observable
     * @return this CheckMenuItemBuilder instance
     * @deprecated use {@link #selected(ObservableValue)} instead
     */
    @Deprecated(since = "21", forRemoval = true)
    public CheckMenuItemBuilder bindSelected(ObservableValue<Boolean> selected) {
        return selected(selected);
    }
}
