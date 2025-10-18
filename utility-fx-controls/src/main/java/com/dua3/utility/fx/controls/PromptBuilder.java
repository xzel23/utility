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

import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.controls.abstract_builders.DialogBuilder;
import com.dua3.utility.text.MessageFormatter;
import org.jspecify.annotations.Nullable;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public final class PromptBuilder extends DialogBuilder<PromptDialog, PromptBuilder, String> {
    private String defaultValue = "";
    private PromptMode promptMode = PromptMode.TEXT;
    private Predicate<? super @Nullable String> validate = (@Nullable String s) -> s != null && !s.isEmpty();

    /**
     * Constructs a new PromptBuilder instance for creating prompt dialogs.
     *
     * @param parentWindow the parent window of the dialog, or null if there is no parent
     * @param formatter    the message formatter to format strings for the dialog
     */
    PromptBuilder(@Nullable Window parentWindow, MessageFormatter formatter) {
        super(formatter, parentWindow);
        setDialogSupplier(this::createDialog);
        setButtons(
            new InputDialogPane.ButtonDef<>(
                    ButtonType.OK,
                    (bt, r) -> true,
                    idp -> {},
                    InputDialogPane::validProperty
            ),
            new InputDialogPane.ButtonDef<>(
                    ButtonType.CANCEL,
                    (bt, r) -> true,
                    idp -> {},
                    idp -> FxUtil.ALWAYS_TRUE
            )
        );
    }

    /**
     * Sets the default value for the prompt dialog.
     *
     * @param fmt the format string or the default value if no arguments are provided
     * @param args optional arguments for formatting the default value string
     * @return the current instance of PromptBuilder for method chaining
     */
    public PromptBuilder defaultValue(String fmt, Object... args) {
        this.defaultValue = format(fmt, args);
        return this;
    }

    /**
     * Sets the prompt mode for the prompt dialog.
     *
     * @param promptMode the mode to be used for the prompt input, such as TEXT or PASSWORD
     * @return the current instance of PromptBuilder for method chaining
     */
    public PromptBuilder mode(PromptMode promptMode) {
        this.promptMode = promptMode;
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
    public PromptDialog build() {
        PromptDialog dlg = super.build();
        dlg.setGraphic(null);
        return dlg;
    }

    /**
     * Creates a new instance of {@code PromptDialog} configured with the specified
     * prompt mode, default value, and validation logic.
     * <p>
     * The dialog is initialized with an OK button whose enabled state is dynamically
     * bound to the validity of the content text based on the provided validation predicate.
     *
     * @return a {@code PromptDialog} instance with the applied configurations
     */
    private PromptDialog createDialog() {
        PromptDialog dlg = new PromptDialog(promptMode, defaultValue);
        Optional.ofNullable(dlg.getDialogPane().lookupButton(ButtonType.OK))
                .ifPresent(btn -> {
                    btn.setDisable(!validate.test(dlg.getContentText()));
                    dlg.contentTextProperty().addListener((observable, oldValue, newValue) ->
                            btn.setDisable(!validate.test(newValue))
                    );
                });
        return dlg;
    }
}
