package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Map;


public class InputPane extends InputDialogPane<Map<String, Object>> {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(InputPane.class);

    private final InputGrid inputGrid;

    public InputPane(InputGrid inputGrid) {
        this.inputGrid = inputGrid;
        valid.bind(inputGrid.validProperty());
        setContent(inputGrid);
    }

    @Override
    public Map<String, Object> get() {
        Node content = getContent();
        if (content instanceof InputGrid) {
            return ((InputGrid) content).get();
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public void init() {
        inputGrid.init();
    }
}
