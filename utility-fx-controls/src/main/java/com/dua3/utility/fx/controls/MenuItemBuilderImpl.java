package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import javafx.scene.control.MenuItem;

import java.util.function.Supplier;

/**
 * A builder for {@link MenuItem} instances.
 */
public class MenuItemBuilderImpl extends MenuItemBuilder<MenuItem, MenuItemBuilderImpl> {
    /**
     * Constructs a new instance of the MenuItemBuilderImpl class.
     *
     * @param factory the factory method for MenuItem instances
     */
    MenuItemBuilderImpl(Supplier<MenuItem> factory) {
        super(factory);
    }
}
