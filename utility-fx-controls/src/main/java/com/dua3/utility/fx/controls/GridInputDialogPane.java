package com.dua3.utility.fx.controls;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.stage.Screen;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class GridInputDialogPane extends InputDialogPane<Map<String, Object>> {
    private final Grid grid;

    public GridInputDialogPane(Grid grid) {
        this.grid = grid;

        valid.bind(grid.validProperty());

        // get the screen the window will be on (fallback to primary)
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Allow the grid to expand naturally
        grid.setMaxWidth(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);  // allow content to expand horizontally
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Let the scroll pane compute its preferred size based on content
        scrollPane.setPrefViewportWidth(USE_PREF_SIZE);
        scrollPane.setPrefViewportHeight(USE_COMPUTED_SIZE);

        // Set content in dialog
        setContent(scrollPane);

        // Make dialog width adapt to content
        setMinWidth(USE_PREF_SIZE);
        setMaxWidth(screenBounds.getWidth() * 0.9);
        setMaxHeight(screenBounds.getHeight() * 0.9);

        // After showing, force the dialog window to resize to fit content
        ChangeListener<Parent> listener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends @Nullable Parent> observable, @Nullable Parent oldValue, @Nullable Parent newValue) {
                if (newValue != null) {
                    // Request layout first to ensure proper size calculation
                    scrollPane.requestLayout();
                    getScene().getWindow().sizeToScene();
                    scrollPane.parentProperty().removeListener(this);
                }
            }
        };

        scrollPane.parentProperty().addListener(listener);
    }

    @Override
    public void init() {
        grid.init();
    }

    @Override
    public Map<String, @Nullable Object> get() {
        return grid.get();
    }
}
