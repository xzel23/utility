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
import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class InputDialogBuilder
        extends DialogBuilder<InputDialog, InputDialogBuilder, Map<String, Object>>
        implements InputBuilder<InputDialogBuilder> {

    private final InputPaneBuilder pb = new InputPaneBuilder();

    InputDialogBuilder(@Nullable Window parentWindow) {
        super(parentWindow);
        setDialogSupplier(this::createDialog);
    }

    private InputDialog createDialog() {
        InputDialog dlg = new InputDialog();
        InputPane dialogPane = pb.build();
        pb.buttons().forEach(bd -> dialogPane.addButton(bd.type(), bd.resultHandler(), bd.action(), bd.enabled()));
        dialogPane.init();

        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.disableProperty().bind(Bindings.not(dialogPane.validProperty()));
        }

        dlg.setDialogPane(dialogPane);

        return dlg;
    }

    @Override
    public <T> InputDialogBuilder add(String id, String label, Class<T> type, Supplier<T> dflt, InputControl<T> control) {
        pb.add(id, label, type, dflt, control);
        return this;
    }

    @Override
    public <T> InputDialogBuilder add(String id, Class<T> type, Supplier<T> dflt, InputControl<T> control) {
        pb.add(id, type, dflt, control);
        return this;
    }

    @Override
    public InputDialogBuilder addNode(String id, String label, Node node) {
        pb.addNode(id, label, node);
        return this;
    }

    @Override
    public InputDialogBuilder addNode(String id, Node node) {
        pb.addNode(id, node);
        return this;
    }

    @Override
    public InputDialogBuilder columns(int columns) {
        pb.columns(columns);
        return this;
    }

    @Override
    public InputDialogBuilder string(String id, String label, Supplier<String> dflt, Function<String, Optional<String>> validate) {
        pb.string(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder integer(String id, String label, Supplier<Integer> dflt, Function<Integer, Optional<String>> validate) {
        pb.integer(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder decimal(String id, String label, Supplier<Double> dflt, Function<Double, Optional<String>> validate) {
        pb.decimal(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder checkBox(String id, String label, Supplier<Boolean> dflt, String text, Function<Boolean, Optional<String>> validate) {
        pb.checkBox(id, label, dflt, text, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder comboBox(String id, String label, Supplier<T> dflt, Class<T> cls, Collection<T> items, Function<T, Optional<String>> validate) {
        pb.comboBox(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder comboBoxEx(
            String id,
            String label,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Supplier<T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<T, Optional<String>> validate) {
        pb.comboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder radioList(String id, String label, Supplier<T> dflt, Class<T> cls, Collection<T> items, Function<T, Optional<String>> validate) {
        pb.radioList(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public InputDialogBuilder slider(String id, String label, Supplier<Double> dflt, double min, double max) {
        pb.slider(id, label, dflt, min, max);
        return this;
    }

    @Override
    public InputDialogBuilder options(String id, String label, Supplier<Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.options(id, label, dflt, options);
        return this;
    }

    @Override
    public InputDialogBuilder options(String id, Supplier<Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.options(id, dflt, options);
        return this;
    }

    @Override
    public InputDialogBuilder chooseFile(String id, String label, Supplier<Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<Path, Optional<String>> validate) {
        pb.chooseFile(id, label, dflt, mode, existingOnly, filter, validate);
        return this;
    }

    @Override
    public InputDialogBuilder node(String id, Node node) {
        pb.node(id, node);
        return this;
    }

    @Override
    public InputDialogBuilder node(String id, String label, Node node) {
        pb.node(id, label, node);
        return this;
    }
}
