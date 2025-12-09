package com.dua3.utility.fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;


/**
 * The InputPane class extends InputDialogPane to provide a customized input dialog pane containing an InputGrid.
 * It initializes the input grid and ensures data retrieval and validation status binding.
 */
public class InputPane extends BorderPane implements Supplier<Map<String, Object>> {

    private final Grid grid;
    private final BooleanProperty valid = new SimpleBooleanProperty();

    /**
     * Constructs an InputPane with the specified InputGrid.
     * This constructor initializes the InputGrid, binds its validation property,
     * and sets it as the content of the InputPane.
     *
     * @param grid the InputGrid instance to be used in the InputPane
     */
    public InputPane(Grid grid) {
        this.grid = grid;
        valid.bind(grid.validProperty());
        setCenter(grid);
    }

    @Override
    public Map<String, @Nullable Object> get() {
        return grid.get();
    }

    public void init() {
        grid.init();
    }

    public void setHeaderText(String s) {
        setTop(new Label(s));
    }

    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }
}
