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
import javafx.scene.control.DialogPane;

/**
 * Builder for {@link PromptPane} instances.
 * <p>
 * Provides a fluent API to configure a lightweight pane for prompt-style
 * text input that can be embedded or used inside dialogs.
 */
public class PromptPaneBuilder extends PaneBuilder<PromptPane, PromptPaneBuilder, String> {
    PromptPaneBuilder(MessageFormatter formatter) {
        super(formatter, DialogPane::setHeaderText);
        setDialogSupplier(PromptPane::new);
    }

    @Override
    public PromptPane build() {
        PromptPane pane = super.build();
        pane.setGraphic(null);
        return pane;
    }
}
