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
import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.scene.Node;
import javafx.stage.FileChooser;

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
public class InputPaneBuilder
        extends PaneBuilder<InputPane, InputPaneBuilder, Map<String, Object>>
        implements InputBuilder<InputPaneBuilder> {

    private final GridBuilder pb;

    InputPaneBuilder(MessageFormatter formatter) {
        super(formatter);
        pb = new GridBuilder(null, formatter);
        setDialogSupplier(() -> {
            InputPane inputPane = new InputPane(pb.build());
            inputPane.init();
            return inputPane;
        });
    }

    @Override
    public SectionStyle getSectionStyle(int level) {
        return pb.getSectionStyle(level);
    }

    @Override
    public <T> InputPaneBuilder addInput(String id, String label, Class<T> type, Supplier<@Nullable T> dflt, InputControl<T> control, boolean hidden) {
        pb.addInput(id, label, type, dflt, control, hidden);
        return this;
    }

    @Override
    public <T> InputPaneBuilder addInput(String id, Class<T> type, Supplier<@Nullable T> dflt, InputControl<T> control) {
        pb.addInput(id, type, dflt, control);
        return this;
    }

    @Override
    public InputPaneBuilder columns(int columns) {
        pb.columns(columns);
        return this;
    }

    @Override
    public InputPaneBuilder node(Node node) {
        pb.node(node);
        return this;
    }

    @Override
    public InputPaneBuilder node(String label, Node node) {
        pb.node(label, node);
        return this;
    }

    @Override
    public InputPaneBuilder section(int level, String fmt, Object... args) {
        pb.section(level, fmt, args);
        return this;
    }

    @Override
    public InputPaneBuilder text(String fmt, Object... args) {
        pb.text(fmt, args);
        return this;
    }

    @Override
    public InputPaneBuilder labeledText(String fmtLabel, String fmtText, Object... args) {
        pb.labeledText(fmtLabel, fmtText, args);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputConstant(String id, String label, Supplier<@Nullable T> value, Class<T> cls) {
        pb.inputConstant(id, label, value, cls);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputConstant(String id, String label, T value) {
        pb.inputConstant(id, label, value);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputHidden(String id, Supplier<T> value, Class<T> cls) {
        pb.inputHidden(id, value, cls);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputHidden(String id, T value) {
        pb.inputHidden(id, value);
        return this;
    }

    @Override
    public InputPaneBuilder inputString(String id, String label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        pb.inputString(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputPaneBuilder inputInteger(String id, String label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        pb.inputInteger(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputPaneBuilder inputDecimal(String id, String label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        pb.inputDecimal(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputPaneBuilder inputCheckBox(String id, String label, Supplier<@Nullable Boolean> dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        pb.inputCheckBox(id, label, dflt, text, validate);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputComboBox(String id, String label, Supplier<@Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        pb.inputComboBox(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputComboBoxEx(
            String id,
            String label,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Supplier<@Nullable T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<@Nullable T, Optional<String>> validate) {
        pb.inputComboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputRadioList(String id, String label, Supplier<@Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        pb.inputRadioList(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public InputPaneBuilder inputSlider(String id, String label, Supplier<@Nullable Double> dflt, double min, double max) {
        pb.inputSlider(id, label, dflt, min, max);
        return this;
    }

    @Override
    public InputPaneBuilder inputOptions(String id, String label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.inputOptions(id, label, dflt, options);
        return this;
    }

    @Override
    public InputPaneBuilder inputOptions(String id, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.inputOptions(id, dflt, options);
        return this;
    }

    @Override
    public InputPaneBuilder inputFile(String id, String label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate) {
        pb.inputFile(id, label, dflt, mode, existingOnly, filter, validate);
        return this;
    }

    @Override
    public <T> InputPaneBuilder inputControl(String id, InputControl<T> control, Class<T> type, Supplier<@Nullable T> dflt) {
        return null;
    }

    @Override
    public <T> InputPaneBuilder inputControl(String id, String label, InputControl<T> control, Class<T> type, Supplier<@Nullable T> dflt) {
        return null;
    }

}
