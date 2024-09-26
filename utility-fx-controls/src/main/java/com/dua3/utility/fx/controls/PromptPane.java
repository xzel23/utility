package com.dua3.utility.fx.controls;

import javafx.beans.binding.BooleanBinding;
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

        // NOTE: the following code can be changed to this when minimal JavaFX version is bumped to 21:
        // valid.bind(text.textProperty().map(s -> s != null && !s.isEmpty()));
        BooleanBinding binding = new BooleanBinding() {
            {
                bind(text.textProperty());
            }

            @Override
            protected boolean computeValue() {
                String textValue = text.getText();
                return textValue != null && !textValue.isEmpty();
            }
        };
        valid.bind(binding);

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
