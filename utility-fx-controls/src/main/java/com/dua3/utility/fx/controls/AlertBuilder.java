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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Arrays;

/**
 * Builder for Alert Dialogs.
 * Provides a fluent interface to create Alerts.
 */
public class AlertBuilder
        extends DialogBuilder<Alert, AlertBuilder, ButtonType> {
    private @Nullable String css = null;
    private @Nullable String text = null;
    private ButtonType @Nullable [] buttons;
    private @Nullable ButtonType defaultButton;

    AlertBuilder(AlertType type, @Nullable Window parentWindow) {
        super(parentWindow);
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

        if (css != null) {
            dlg.getDialogPane().getScene().getStylesheets().add(css);
        }

        if (buttons != null) {
            dlg.getButtonTypes().setAll(buttons);
        }

        if (defaultButton != null) {
            DialogPane pane = dlg.getDialogPane();
            for (ButtonType t : dlg.getButtonTypes()) {
                ((Button) pane.lookupButton(t)).setDefaultButton(t == defaultButton);
            }
        }

        if (text != null) {
            dlg.setContentText(text);
        }

        return dlg;
    }

    /**
     * Set text.
     *
     * @param fmt  the format String as defined by {@link java.util.Formatter}
     * @param args the arguments passed to the formatter
     * @return {@code this}
     */
    public AlertBuilder text(String fmt, Object... args) {
        this.text = String.format(fmt, args);
        return this;
    }

    /**
     * Define Alert Buttons.
     *
     * @param buttons the buttons to show
     * @return {@code this}
     */
    public AlertBuilder buttons(ButtonType... buttons) {
        this.buttons = Arrays.copyOf(buttons, buttons.length);
        return this;
    }

    /**
     * Define the default Buttons.
     *
     * @param button the button to use as default
     * @return {@code this}
     */
    public AlertBuilder defaultButton(ButtonType button) {
        this.defaultButton = button;
        return this;
    }

    /**
     * Set supplemental CSS.
     *
     * @param css the name of the CSS resource to load ({@link URL#toExternalForm()}
     * @return this
     */
    public AlertBuilder css(String css) {
        this.css = css;
        return this;
    }

}
