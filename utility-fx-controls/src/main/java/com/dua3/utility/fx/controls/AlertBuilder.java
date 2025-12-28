// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.DialogBuilder;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.MessageFormatter;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Alert Dialogs.
 * Provides a fluent interface to create Alerts.
 */
public class AlertBuilder
        extends DialogBuilder<Alert, AlertBuilder, ButtonType> {
    private @Nullable String css = null;
    private @Nullable String text = null;
    private boolean selectableText = false;
    private final List<ButtonDef<ButtonType>> buttons = new ArrayList<>();
    private @Nullable ButtonType defaultButton;

    /**
     * Constructs an AlertBuilder for creating Alert dialogs.
     *
     * @param parentWindow the parent window for the alert dialog; can be null
     * @param type         the type of the alert, such as confirmation or error
     * @param formatter    the message formatter used for building formatted messages
     */
    AlertBuilder(@Nullable Window parentWindow, AlertType type, MessageFormatter formatter) {
        super(formatter, parentWindow);
        setDialogSupplier(() -> new Alert(type));
    }

    /**
     * Create Alert instance.
     *
     * @return Alert instance
     */
    @Override
    public Alert build() {
        Alert dlg = super.build();

        // Replace the Label in the dialog pane with a text area to allow the user to select and copy text
        if (selectableText && dlg.getDialogPane().lookup(".content.label") instanceof Label label) {
            TextArea textArea = new TextArea("");
            textArea.textProperty().bind(dlg.contentTextProperty());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setFocusTraversable(false);

            // Make it look like normal alert content
            textArea.setFont(label.getFont());
            textArea.setStyle("-fx-background-color: transparent;" +
                    "-fx-control-inner-background: transparent;" +
                    "-fx-padding: 0;" +
                    "-fx-background-insets: 0;" +
                    "-fx-border-width: 0;"
            );

            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);

            dlg.getDialogPane().setContent(textArea);
        }

        LangUtil.applyIfNotEmpty(css, dlg.getDialogPane().getScene().getStylesheets()::add);
        LangUtil.applyIfNotEmpty(text, dlg::setContentText);

        if (!buttons.isEmpty()) {
            ObservableList<ButtonType> buttonTypes = dlg.getButtonTypes();
            buttonTypes.clear();
            buttons.forEach(bd -> buttonTypes.add(bd.type()));
        }

        if (defaultButton != null) {
            DialogPane pane = dlg.getDialogPane();
            for (ButtonType t : dlg.getButtonTypes()) {
                ((Button) pane.lookupButton(t)).setDefaultButton(t == defaultButton);
            }
        }

        dlg.getDialogPane().applyCss();

        return dlg;
    }

    @Override
    public List<ButtonDef<ButtonType>> getButtonDefs() {
        return buttons;
    }

    /**
     * Set text.
     *
     * @param fmt  the format String as defined by {@link java.util.Formatter}
     * @param args the arguments passed to the formatter
     * @return {@code this}
     */
    public AlertBuilder text(String fmt, Object... args) {
        this.text = format(fmt, args);
        return self();
    }

    /**
     * Sets whether the text in the alert dialog should be selectable or not.
     * When enabled, users will be able to highlight and copy the text from the dialog.
     *
     * @param selectableText true to make the text selectable, false otherwise
     * @return the current instance of {@code AlertBuilder} for method chaining
     */
    public AlertBuilder selectableText(boolean selectableText) {
        this.selectableText = selectableText;
        return self();
    }

    /**
     * Define the default Buttons.
     *
     * @param button the button to use as default
     * @return {@code this}
     */
    public AlertBuilder defaultButton(ButtonType button) {
        this.defaultButton = button;
        return self();
    }

    /**
     * Set supplemental CSS.
     *
     * @param css the name of the CSS resource to load ({@link URL#toExternalForm()}
     * @return self();
     */
    public AlertBuilder css(String css) {
        this.css = css;
        return self();
    }

}
