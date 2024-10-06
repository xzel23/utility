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

import org.jspecify.annotations.Nullable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class PromptBuilder extends AbstractDialogBuilder<TextInputDialog, PromptBuilder, String> {
    private String defaultValue = "";
    private Predicate<? super String> validate = s -> s != null && !s.isEmpty();

    PromptBuilder(@Nullable Window parentWindow) {
        super(parentWindow);
        setDialogSupplier(this::createDialog);
    }

    /**
     * Sets the default value for the prompt dialog.
     *
     * @param fmt the format string or the default value if no arguments are provided
     * @param args optional arguments for formatting the default value string
     * @return the current instance of PromptBuilder for method chaining
     */
    public PromptBuilder defaultValue(String fmt, Object... args) {
        this.defaultValue = args.length == 0 ? fmt : String.format(fmt, args);
        return this;
    }

    /**
     * Sets the validation logic for the input prompt.
     *
     * @param validate a Predicate to validate the input string
     * @return the current instance of PromptBuilder for method chaining
     */
    public PromptBuilder validate(Predicate<? super String> validate) {
        this.validate = validate;
        return this;
    }

    @Override
    public TextInputDialog build() {
        TextInputDialog dlg = super.build();
        dlg.setGraphic(null);
        return dlg;
    }

    private TextInputDialog createDialog() {
        TextInputDialog dlg = new TextInputDialog(defaultValue);
        Optional.ofNullable(dlg.getDialogPane().lookupButton(ButtonType.OK))
                .ifPresent(btn -> {
                    btn.setDisable(!validate.test(dlg.getEditor().getText()));
                    dlg.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                            btn.setDisable(!validate.test(newValue))
                    );
                });
        return dlg;
    }
}
