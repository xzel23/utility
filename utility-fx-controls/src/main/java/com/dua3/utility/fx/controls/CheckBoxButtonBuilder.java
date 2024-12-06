package com.dua3.utility.fx.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The CheckBoxButtonBuilder class is a utility class for constructing CheckBox instances
 * with properties configured using a fluent API.
 * It extends the ButtonBuilderBase by supporting to bind the checkbox's selected state to a boolean property.
 */
public class CheckBoxButtonBuilder extends ButtonBuilderBase<CheckBox, CheckBoxButtonBuilder> {
    private @Nullable Property<Boolean> selected = null;

    /**
     * Constructor.
     *
     * @param factory the factory method for CheckBox instances
     */
    CheckBoxButtonBuilder(Supplier<? extends CheckBox> factory) {
        super(factory);
    }

    /**
     * Bind the button's selected state to an {@link Property<Boolean>}.
     * @param selected the property to bind this button's selected state to
     * @return this CheckBoxBuilder instance
     */
    public CheckBoxButtonBuilder bindSelected(Property<Boolean> selected) {
        this.selected = selected;
        return self();
    }

    @Override
    public CheckBox build() {
        CheckBox button = super.build();
        if (selected != null) {
            Bindings.bindBidirectional(selected, button.selectedProperty());
        }
        return button;
    }
}
