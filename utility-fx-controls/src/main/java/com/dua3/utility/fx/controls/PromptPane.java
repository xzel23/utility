package com.dua3.utility.fx.controls;

import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class PromptPane extends InputDialogPane<String> {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(PromptPane.class);

    private final TextField text;

    public PromptPane() {
        text = new TextField();

        // NOTE: the following code can be changed to this when minimal JavaFX version is bumped to 21:
        // valid.bind(text.textProperty().map(s -> s != null && !s.isEmpty()));
        BooleanBinding binding = new BooleanBinding() {
            {
                super.bind(text.textProperty());
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
