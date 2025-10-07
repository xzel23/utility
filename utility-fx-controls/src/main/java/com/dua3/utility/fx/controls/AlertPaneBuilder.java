// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.PaneBuilder;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import org.jspecify.annotations.Nullable;

/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class AlertPaneBuilder
        extends PaneBuilder<InputDialogPane<Void>, AlertPaneBuilder, Void> {
    private @Nullable String text;

    AlertPaneBuilder(MessageFormatter formatter, AlertType type) {
        super(formatter);
        setDialogSupplier(() -> createPane(type));
    }

    private static InputDialogPane<Void> createPane(AlertType type) {
        return new InputDialogPane<>() {
            @Override
            public void init() {
                valid.set(true);
            }

            @Override
            public @Nullable Void get() {
                return null;
            }
        };
    }

    /**
     * Sets the text content of the alert dialog. The text can be formatted using
     * a format string and arguments.
     *
     * @param fmt the format string.
     * @param args arguments referenced by the format specifiers in the format string.
     * @return the current instance of the AlertPaneBuilder for chaining.
     */
    public AlertPaneBuilder text(String fmt, Object... args) {
        this.text = format(fmt, args);
        return this;
    }

    @Override
    public InputDialogPane<Void> build() {
        InputDialogPane<Void> inputPane = super.build();
        applyIfNotNull(DialogPane::setContentText, inputPane, text);
        return inputPane;
    }
}
