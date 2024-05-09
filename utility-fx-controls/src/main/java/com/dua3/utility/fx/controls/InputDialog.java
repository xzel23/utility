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

import java.util.Map;

/**
 * A Dialog for inputting values.
 * <p>
 * The dialog consists of labels and input controls laid out in a grid.
 */
public class InputDialog extends Dialog<Map<String, Object>> {

    public InputDialog() {
        setResultConverter(btn -> {
            if (btn != ButtonType.OK) {
                return null;
            }
            return ((InputPane) getDialogPane()).get();
        });
    }

}
