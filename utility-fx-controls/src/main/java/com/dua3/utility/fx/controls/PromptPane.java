package com.dua3.utility.fx.controls;

import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * A specialized dialog pane for prompting the user to input a string value.
 */
public class PromptPane extends InputDialogPane<String> {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(PromptPane.class);

    private final TextField text;

    /**
     * Constructs a new PromptPane. This constructor initializes
     * the text field and sets up a validation binding to ensure the input is
     * non-null and not empty.
     */
    public PromptPane() {
        text = new TextField();
        valid.bind(text.textProperty().map(s -> s != null && !s.isEmpty()));
        setContent(new StackPane(text));
    }

    @Override
    public String get() {
        return text.getText();
    }

    @Override
    public void init() {
        text.requestFocus();
    }
}
