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
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.text.MessageFormatter;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Builder for {@link InputDialog} instances.
 * <p>
 * Provides a fluent API to assemble an input dialog composed of a
 * {@link GridInputDialogPane}. Use the various {@code input*} and
 * {@code node} methods to add controls, sections, and text, and
 * {@link #columns(int)} to configure the grid layout.
 */
public class InputDialogBuilder extends DialogBuilder<InputDialog, InputDialogBuilder, InputResult> implements InputBuilder<InputDialogBuilder> {

    private final InputDialogPaneBuilder pb;
    private final List<ButtonDef<InputResult>> buttonDefs = new ArrayList<>();

    InputDialogBuilder(@Nullable Window parentWindow, MessageFormatter formatter) {
        super(formatter, parentWindow);
        this.pb = new InputDialogPaneBuilder(formatter);
        setDialogSupplier(this::createDialog);
        pb.buttons(new ButtonDef<>(ButtonType.OK, (bt, r) -> true, idp -> {}, InputDialogPane::validProperty), new ButtonDef<>(ButtonType.CANCEL, (bt, r) -> true, idp -> {}, idp -> FxUtil.ALWAYS_TRUE));
    }

    private InputDialog createDialog() {
        InputDialog dlg = new InputDialog();
        GridInputDialogPane dialogPane = pb.build();

        pb.getButtonDefs().forEach(bd -> dialogPane.addButton(bd.type(), bd.resultHandler(), bd.action(), bd.enabled()));
        dialogPane.init();

        final Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.disableProperty().bind(Bindings.not(dialogPane.validProperty()));
        }

        dlg.setDialogPane(dialogPane);
        dlg.getDialogPane().applyCss();
        dlg.getDialogPane().layout();

        return dlg;
    }

    @Override
    public SectionStyle getSectionStyle(int level) {
        return pb.getSectionStyle(level);
    }

    @Override
    public <T> InputDialogBuilder addInput(String id, MessageFormatter.MessageFormatterArgs label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean visible) {
        pb.addInput(id, label, type, dflt, control, visible);
        return this;
    }

    @Override
    public <T> InputDialogBuilder addInput(String id, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control) {
        pb.addInput(id, type, dflt, control);
        return this;
    }

    @Override
    public InputDialogBuilder columns(int columns) {
        pb.columns(columns);
        return this;
    }

    @Override
    public InputDialogBuilder node(Node node) {
        pb.node(node);
        return this;
    }

    @Override
    public InputDialogBuilder node(MessageFormatter.MessageFormatterArgs label, Node node) {
        pb.node(label, node);
        return this;
    }

    @Override
    public InputDialogBuilder section(int level, String fmt, Object... args) {
        pb.section(level, fmt, args);
        return this;
    }

    @Override
    public InputDialogBuilder text(String fmt, Object... args) {
        pb.text(fmt, args);
        return this;
    }

    @Override
    public InputDialogBuilder labeledText(String fmtLabel, String fmtText, Object... args) {
        pb.labeledText(fmtLabel, fmtText, args);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputConstant(String id, MessageFormatter.MessageFormatterArgs label, Supplier<T> value, Class<T> cls) {
        pb.inputConstant(id, label, value, cls);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputConstant(String id, MessageFormatter.MessageFormatterArgs label, T value) {
        pb.inputConstant(id, label, value);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputHidden(String id, Supplier<T> value, Class<T> cls) {
        pb.inputHidden(id, value, cls);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputHidden(String id, T value) {
        pb.inputHidden(id, value);
        return this;
    }

    @Override
    public InputDialogBuilder inputText(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        pb.inputText(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputString(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        pb.inputString(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputPassword(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        pb.inputPassword(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputInteger(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        pb.inputInteger(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputDecimal(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        pb.inputDecimal(id, label, dflt, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputCheckBox(String id, MessageFormatter.MessageFormatterArgs label, BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        pb.inputCheckBox(id, label, dflt, text, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        pb.inputComboBox(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputComboBoxEx(String id, MessageFormatter.MessageFormatterArgs label, @Nullable UnaryOperator<@Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, @Nullable T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        pb.inputComboBoxEx(id, label, edit, add, remove, format, dflt, cls, items, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputRadioList(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        pb.inputRadioList(id, label, dflt, cls, items, validate);
        return this;
    }

    @Override
    public InputDialogBuilder inputSlider(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, double min, double max) {
        pb.inputSlider(id, label, dflt, min, max);
        return this;
    }

    @Override
    public InputDialogBuilder inputOptions(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.inputOptions(id, label, dflt, options);
        return this;
    }

    @Override
    public InputDialogBuilder inputOptions(String id, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        pb.inputOptions(id, dflt, options);
        return this;
    }

    @Override
    public InputDialogBuilder inputFile(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate) {
        pb.inputFile(id, label, dflt, mode, existingOnly, filter, validate);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputControl(String id, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        pb.inputControl(id, control, type, dflt);
        return this;
    }

    @Override
    public <T> InputDialogBuilder inputControl(String id, MessageFormatter.MessageFormatterArgs label, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        pb.inputControl(id, label, control, type, dflt);
        return this;
    }

    @Override
    public final List<ButtonDef<InputResult>> getButtonDefs() {
        return buttonDefs;
    }
}
