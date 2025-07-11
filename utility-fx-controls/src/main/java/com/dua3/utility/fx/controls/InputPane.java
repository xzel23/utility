package com.dua3.utility.fx.controls;

import javafx.scene.Node;

import java.util.Collections;
import java.util.Map;


/**
 * The InputPane class extends InputDialogPane to provide a customized input dialog pane containing an InputGrid.
 * It initializes the input grid and ensures data retrieval and validation status binding.
 */
public class InputPane extends InputDialogPane<Map<String, Object>> {

    private final InputGrid inputGrid;

    /**
     * Constructs an InputPane with the specified InputGrid.
     * This constructor initializes the InputGrid, binds its validation property,
     * and sets it as the content of the InputPane.
     *
     * @param inputGrid the InputGrid instance to be used in the InputPane
     */
    public InputPane(InputGrid inputGrid) {
        this.inputGrid = inputGrid;
        valid.bind(inputGrid.validProperty());
        setContent(inputGrid);
    }

    @Override
    public Map<String, Object> get() {
        Node content = getContent();
        if (content instanceof InputGrid ig) {
            return ig.get();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public void init() {
        inputGrid.init();
    }
}
