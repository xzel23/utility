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

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * A Dialog for inputting values.
 * <p>
 * The dialog consists of labels and input controls laid out in a grid.
 */
public class OptionsDialog extends Dialog<@Nullable Arguments> {

    private @Nullable OptionsPane optionPane;

    /**
     * Constructs an OptionsDialog with OK and Cancel buttons and a result converter.
     * The result converter will return the values provided by the OptionsPane.
     * If the Cancel button is pressed or if the OptionsPane has not been set, the result is null.
     */
    public OptionsDialog() {
        // buttons
        DialogPane dialogPane = getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter(btn -> {
            LangUtil.check(optionPane != null, "setOptions() not called!");

            if (btn != ButtonType.OK) {
                return null;
            }

            return optionPane.get();
        });
    }

    /**
     * Set options.
     *
     * @param optionSet     the options to set
     * @param currentValues the current values
     */
    public void setOptions(Collection<Option<?>> optionSet, Arguments currentValues) {
        LangUtil.check(optionPane == null, "setOptions() already called!");
        this.optionPane = new OptionsPane(optionSet, currentValues);
        optionPane.init();
        getDialogPane().setContent(optionPane);
    }

}
