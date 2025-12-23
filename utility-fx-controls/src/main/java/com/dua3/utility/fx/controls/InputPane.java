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
 * Lightweight pane for collecting user input using a {@link Grid} layout.
 * <p>
 * Wraps a {@link Grid} and exposes its validation state and collected values,
 * allowing the pane to be embedded in dialogs or other containers.
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

    /**
     * Initializes the associated {@code Grid} instance contained in this {@code InputPane}.
     * <p>
     * This method delegates to {@link Grid#init()} to perform the setup process for
     * the grid. It clears existing input controls, sets up new controls and layouts
     * them within the grid, initializes their validation bindings, and configures
     * the overall state of the grid.
     */
    public void init() {
        grid.init();
    }

    /**
     * Sets the header text displayed at the top of the pane.
     *
     * @param s the text to be displayed as the header
     */
    public void setHeaderText(String s) {
        setTop(new Label(s));
    }

    /**
     * Provides a read-only property that indicates the validation state of the input pane.
     * The property reflects whether the data entered in the contained grid layout is valid.
     *
     * @return a {@code ReadOnlyBooleanProperty} representing the current validation state
     */
    public ReadOnlyBooleanProperty validProperty() {
        return valid;
    }
}
