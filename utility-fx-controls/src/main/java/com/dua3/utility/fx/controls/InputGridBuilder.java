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

import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.controls.InputGrid.Meta;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.stage.FileChooser;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
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
public class InputGridBuilder
        implements InputBuilder<InputGridBuilder> {

    private static final String INPUT_WITH_ID_ALREADY_DEFINED = "Input with id '%s' already defined";
    private final LinkedHashMap<String, InputGrid.Meta<?>> data = new LinkedHashMap<>();
    private final @Nullable Window parentWindow;
    private final MessageFormatter formatter;
    private int columns = 1;

    /**
     * Creates a new {@code InputGridBuilder} instance.
     *
     * @param parentWindow the parent window to which the input grid will be associated; can be {@code null}
     * @param formatter the {@code MessageFormatter} used for formatting messages
     */
    InputGridBuilder(@Nullable Window parentWindow, MessageFormatter formatter) {
        this.parentWindow = parentWindow;
        this.formatter = formatter;
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return formatter;
    }

    @Override
    public String format(String message, Object... args) {
        return formatter.format(message, args);
    }

    /**
     * Builds and returns an InputGrid with the current data and column configuration.
     *
     * @return the constructed InputGrid
     */
    public InputGrid build() {
        InputGrid grid = new InputGrid();

        grid.setContent(data.sequencedValues(), columns);
        grid.init();

        return grid;
    }

    @Override
    public <T> InputGridBuilder add(String id, String label, Class<T> type, Supplier<T> dflt, InputControl<T> control, boolean hidden) {
        return doAdd(id, format(label), type, dflt, control, hidden);
    }

    static class ControlWrapper implements InputControl<Void> {

        private final Node node;

        private final Property<Void> value = new SimpleObjectProperty<>();
        private final BooleanProperty valid = new SimpleBooleanProperty(true);
        private final ReadOnlyStringProperty error = new SimpleStringProperty("");

        ControlWrapper(Node node) {
            this.node = node;
        }

        @Override
        public Node node() {
            return node;
        }

        @Override
        public Property<Void> valueProperty() {
            return value;
        }

        @Override
        public void reset() { /* nop */ }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        @Override
        public ReadOnlyStringProperty errorProperty() {
            return error;
        }
    }

    @Override
    public <T> InputGridBuilder add(String id, Class<T> type, Supplier<T> dflt, InputControl<T> control) {
        return doAdd(id, null, type, dflt, control, false);
    }

    private <T> InputGridBuilder doAdd(String id, @Nullable String label, Class<T> type, Supplier<@Nullable T> dflt, InputControl<T> control, boolean hidden) {
        Meta<T> meta = new Meta<>(id, label, type, dflt, control, hidden);
        Meta<?> prev = data.put(id, meta);
        LangUtil.check(prev == null, INPUT_WITH_ID_ALREADY_DEFINED, id);
        return this;
    }

    @Override
    public InputGridBuilder addNode(String id, @Nullable String label, Node node) {
        Meta<Void> meta = new Meta<>(id, label, Void.class, () -> null, new ControlWrapper(node), false);
        Meta<?> prev = data.put(id, meta);
        LangUtil.check(prev == null, INPUT_WITH_ID_ALREADY_DEFINED, id);
        return this;
    }

    @Override
    public InputGridBuilder addNode(String id, Node node) {
        Meta<Void> meta = new Meta<>(id, null, Void.class, () -> null, new ControlWrapper(node), false);
        Meta<?> prev = data.put(id, meta);
        LangUtil.check(prev == null, INPUT_WITH_ID_ALREADY_DEFINED, id);
        return this;
    }

    @Override
    public InputGridBuilder columns(int columns) {
        this.columns = LangUtil.requirePositive(columns);
        return this;
    }

    @Override
    public InputGridBuilder description(String fmt, Object... args) {
        Label node = new Label(format(fmt, args));
        return addNode("$ignored$" + System.identityHashCode(node), node);
    }

    @Override
    public InputGridBuilder text(String fmtLabel, String fmtText, Object... args) {
        Label node = new Label(format(fmtText, args));
        return addNode("$ignored$" + System.identityHashCode(node) + "#", format(fmtLabel, args), node);
    }

    @Override
    public <T> InputGridBuilder constant(String id, String label, Supplier<T> value, Class<T> cls) {
        Property<T> property = new SimpleObjectProperty<>(value.get());
        TextField tf = new TextField();
        tf.setDisable(true);
        tf.textProperty().bind(property.map(String::valueOf));
        InputControl<T> ic = new SimpleInputControl<>(tf, property, value, v -> Optional.empty());
        return add(id, label, cls, value, ic, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> InputGridBuilder constant(String id, String label, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return constant(id, label, dflt, cls);
    }

    @Override
    public <T> InputGridBuilder hidden(String id, Supplier<T> value, Class<T> cls) {
        Property<T> property = new SimpleObjectProperty<>(value.get());
        InputControl<T> ic = new SimpleInputControl<>(new Label(), property, value, v -> Optional.empty());
        return add(id, "", cls, value, ic, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> InputGridBuilder hidden(String id, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return hidden(id, dflt, cls);
    }

    @Override
    public InputGridBuilder string(String id, String label, Supplier<String> dflt, Function<String, Optional<String>> validate) {
        return add(id, label, String.class, dflt, InputControl.stringInput(dflt, validate), false);
    }

    @Override
    public InputGridBuilder integer(String id, String label, Supplier<Long> dflt, Function<Long, Optional<String>> validate) {
        return add(id, label, Long.class, dflt, InputControl.integerInput(dflt, validate), false);
    }

    @Override
    public InputGridBuilder decimal(String id, String label, Supplier<Double> dflt, Function<Double, Optional<String>> validate) {
        return add(id, label, Double.class, dflt, InputControl.decimalInput(dflt, validate), false);
    }

    @Override
    public InputGridBuilder checkBox(String id, String label, Supplier<Boolean> dflt, String text, Function<Boolean, Optional<String>> validate) {
        return add(id, label, Boolean.class, dflt, InputControl.checkBoxInput(dflt, text, validate), false);
    }

    @Override
    public <T> InputGridBuilder comboBox(String id, String label, Supplier<T> dflt, Class<T> cls, Collection<T> items, Function<T, Optional<String>> validate) {
        return add(id, label, cls, dflt, InputControl.comboBoxInput(items, dflt, validate), false);
    }

    @Override
    public <T> InputGridBuilder comboBoxEx(
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
        return add(id, label, cls, dflt, InputControl.comboBoxExInput(items, dflt, edit, add, remove, format, validate), false);
    }

    @Override
    public <T> InputGridBuilder radioList(String id, String label, Supplier<T> dflt, Class<T> cls, Collection<T> items,
                                          Function<T, Optional<String>> validate) {
        return add(id, label, cls, dflt, new RadioPane<>(items, null, validate), false);
    }

    @Override
    public InputGridBuilder slider(String id, String label, Supplier<Double> dflt, double min, double max) {
        return add(id, label, Double.class, dflt, Controls.slider().min(min).max(max).setDefault(dflt).build(), false);
    }

    @Override
    public InputGridBuilder options(String id, String label, Supplier<Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return add(id, label, Arguments.class, dflt, new OptionsPane(options, dflt), false);
    }

    @Override
    public InputGridBuilder options(String id, Supplier<Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return add(id, Arguments.class, dflt, new OptionsPane(options, dflt));
    }

    @Override
    public InputGridBuilder chooseFile(String id, String label, Supplier<Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<Path, Optional<String>> validate) {
        return add(id, label, Path.class, dflt, new FileInput(parentWindow, mode, existingOnly, dflt, filter, validate), false);
    }

    @Override
    public InputGridBuilder node(String id, Node node) {
        return doAdd(id, null, Void.class, () -> null, new ControlWrapper(node), false);
    }

    @Override
    public InputGridBuilder node(String id, String label, Node node) {
        return doAdd(id, label, Void.class, () -> null, new ControlWrapper(node), false);
    }
}
