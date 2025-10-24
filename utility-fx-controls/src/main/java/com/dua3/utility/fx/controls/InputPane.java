package com.dua3.utility.fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;


/**
 * The InputPane class extends InputDialogPane to provide a customized input dialog pane containing an InputGrid.
 * It initializes the input grid and ensures data retrieval and validation status binding.
 */
public class InputPane extends InputDialogPane<Map<String, Object>> {

    private final Grid grid;

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

        // get the screen the window will be on (fallback to primary)
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        ScrollPane scrollPane = new ScrollPane(new StackPane(grid));
        scrollPane.setFitToWidth(true);  // allow content to expand horizontally
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Allow the grid to expand naturally
        grid.setMaxWidth(Double.MAX_VALUE);

        // Set the scroll pane max width to screen width
        scrollPane.setMaxWidth(screenBounds.getWidth() * 0.9);

        // Set content in dialog
        setContent(scrollPane);

        // Make dialog width adapt to content
        setMinWidth(Region.USE_PREF_SIZE);
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        setMaxWidth(screenBounds.getWidth() * 0.9);

        // After showing, force the dialog window to resize to fit content
        ChangeListener<Parent> listener = new ChangeListener<Parent>() {
            @Override
            public void changed(ObservableValue<? extends @Nullable Parent> observable, @Nullable Parent oldValue, @Nullable Parent newValue) {
                if (newValue != null) {
                    getScene().getWindow().sizeToScene();
                    scrollPane.parentProperty().removeListener(this);
                }
            }
        };

        scrollPane.parentProperty().addListener(listener);
    }

    @Override
    public Map<String, @Nullable Object> get() {
        return grid.get();
    }

    @Override
    public void init() {
        grid.init();
    }
}
