package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ButtonBaseBuilder;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.scene.control.CheckBox;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The CheckBoxButtonBuilder class is a utility class for constructing CheckBox instances
 * with properties configured using a fluent API.
 * It extends the ButtonBuilderBase by supporting to bind the checkbox's selected state to a boolean property.
 */
public class CheckBoxButtonBuilder extends ButtonBaseBuilder<CheckBox, CheckBoxButtonBuilder> {
    private final List<Property<Boolean>> selectedList = new ArrayList<>();
    private @Nullable Boolean selected = null;

    /**
     * Constructor.
     *
     * @param factory the factory method for CheckBox instances
     */
    CheckBoxButtonBuilder(Supplier<? extends CheckBox> factory) {
        super(factory);
    }

    /**
     * Bind the button's selected state to a {@link Property<Boolean>}.
     * @param selected the property to bind this button's selected state to
     * @return this CheckBoxBuilder instance
     * @deprecated use {@link #selected(Property)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public CheckBoxButtonBuilder bindSelected(Property<Boolean> selected) {
        return selected(selected);
    }

    /**
     * Bind the button's selected state to a {@link Property<Boolean>}.
     * @param selected the property to bind this button's selected state to
     * @return this CheckBoxBuilder instance
     */
    public CheckBoxButtonBuilder selected(Property<Boolean> selected) {
        selectedList.add(selected);
        return self();
    }

    /**
     * Sets the selected state of the CheckBox.
     *
     * @param selected the boolean value to set the selected state of the CheckBox
     * @return this CheckBoxButtonBuilder instance
     */
    public CheckBoxButtonBuilder selected(boolean selected) {
        this.selected = selected;
        return self();
    }

    @Override
    public CheckBox build() {
        CheckBox button = super.build();

        selectedList.forEach(property -> Bindings.bindBidirectional(property, button.selectedProperty()));

        if (selected != null) {
            button.setSelected(selected);
        }

        return button;
    }
}
