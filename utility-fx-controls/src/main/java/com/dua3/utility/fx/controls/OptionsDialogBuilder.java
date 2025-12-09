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

import com.dua3.utility.fx.controls.abstract_builders.DialogBuilder;
import com.dua3.utility.text.MessageFormatter;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builder for {@link OptionsDialog} instances.
 * <p>
 * Provides a fluent API to configure an options dialog with a set of
 * {@link com.dua3.utility.options.Option Option}s and current values.
 */
public class OptionsDialogBuilder extends DialogBuilder<OptionsDialog, OptionsDialogBuilder, Arguments> {

    private Collection<Option<?>> options = new ArrayList<>();
    private Arguments currentValues = Arguments.empty();
    private final List<ButtonDef<Arguments>> buttons = new ArrayList<>();

    OptionsDialogBuilder(@Nullable Window parentWindow, MessageFormatter formatter) {
        super(formatter, parentWindow);
        setDialogSupplier(OptionsDialog::new);
    }

    @Override
    public OptionsDialog build() {
        OptionsDialog dlg = super.build();

        dlg.setOptions(options, currentValues);

        return dlg;
    }

    /**
     * Set options.
     *
     * @param options the options to set
     * @return this builder instance
     */
    public OptionsDialogBuilder options(Collection<Option<?>> options) {
        this.options = options;
        return this;
    }

    /**
     * Set current values.
     *
     * @param currentValues the currentValues to set
     * @return this builder instance
     */
    public OptionsDialogBuilder currentValues(Arguments currentValues) {
        this.currentValues = currentValues;
        return this;
    }

    @Override
    public final List<ButtonDef<Arguments>> getButtonDefs() {
        return buttons;
    }
}
