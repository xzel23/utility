package com.dua3.utility.fx.controls;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * A custom dialog for prompting users to input either text or password.
 * This dialog extends the Dialog class and provides a text input field
 * based on the specified prompt mode.
 */
public class PromptDialog extends Dialog<String> {

    private final TextField textField;

    /**
     * Creates a new PromptDialog instance with a specific prompt mode and default input value.
     * This dialog allows users to input text or password depending on the specified prompt mode.
     *
     * @param promptMode the mode of the prompt, indicating whether the input field is for text or password
     * @param defaultValue the default value to prefill in the input field
     */
    public PromptDialog(PromptMode promptMode, String defaultValue) {
        this.textField = switch (promptMode) {
            case PASSWORD -> new PasswordField();
            case TEXT -> new TextField();
        };
        textField.textProperty().bindBidirectional(contentTextProperty());
        textField.setText(defaultValue);

        final DialogPane dialogPane = getDialogPane();
        BorderPane pane = new BorderPane(textField);

        textField.setMaxWidth(Double.MAX_VALUE);
        BorderPane.setAlignment(textField, Pos.CENTER);

        dialogPane.setContent(pane);

        setResultConverter(btn -> {
            ButtonData data = btn == null ? null : btn.getButtonData();
            return data == ButtonData.OK_DONE ? textField.getText() : null;
        });

        Platform.runLater(textField::requestFocus);
    }
}
