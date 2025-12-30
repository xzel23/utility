package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.MenuItemBuilder;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * A builder for {@link Menu} instances.
 */
public class MenuBuilder extends MenuItemBuilder<Menu, MenuBuilder> {
    private List<MenuItem> items = List.of();

    MenuBuilder(Supplier<Menu> factory) {
        super(factory);
    }

    @Override
    public Menu build() {
        Menu menu = super.build();
        menu.getItems().setAll(items);
        return menu;
    }

    /**
     * Set the menu items.
     *
     * @param items the menu items
     * @return this MenuBuilder instance
     */
    public MenuBuilder items(MenuItem... items) {
        this.items = Arrays.asList(items);
        return self();
    }
}
