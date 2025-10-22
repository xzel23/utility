package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
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

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false); // allow vertical scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        setContent(scrollPane);
    }

    @Override
    public Map<String, @Nullable Object> get() {
        Node content = getContent();
        if (content instanceof Grid ig) {
            return ig.get();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public void init() {
        grid.init();
    }
}
