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

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

/**
 * A Dialog for inputting values.
 * <p>
 * The dialog consists of labels and input controls laid out in a grid.
 */
public class InputDialog extends Dialog<InputResult<ButtonType, String>> {

    /**
     * Constructs a new InputDialog instance.
     *
     * <p>This constructor sets up the result converter for the dialog. When the dialog
     * is closed, this converter determines the result based on the button pressed.
     * If the OK button is pressed, it retrieves the input values captured in the
     * dialog pane. Otherwise, it returns null.
     */
    public InputDialog() {
        setResultConverter(btn -> new InputResult<>(btn, ((InputPane) getDialogPane()).get()));
    }

}
