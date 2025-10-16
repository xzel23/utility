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

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.fx.controls.Grid.Meta;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Builder for Alert Dialogs.
 * <p>
 * Provides a fluent interface to create Alerts.
 */
public class GridBuilder implements InputBuilder<GridBuilder> {

    private static final FxFontUtil FU = FxFontUtil.getInstance();
    private static final String INPUT_WITH_ID_ALREADY_DEFINED = "Input with id '%s' already defined";

    private static final SectionStyle[] DEFAULT_SECTION_STYLES = {
            new SectionStyle(1.0f, 0.5f, true, 1.5f),
            new SectionStyle(0.5f, 0.25f, true, 1.25f),
            new SectionStyle(0.5f, 0.125f, true, 1.0f)
    };

    private static final SectionStyle DEFAULT_SECTION_STYLE = new SectionStyle(0.0f, 0.0f, false, 1.0f);

    private static class ControlWrapper implements InputControl<Void> {

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

    private final Set<String> ids = new HashSet<>();
    private final List<Meta<?>> data = new ArrayList<>();
    private final @Nullable Window owner;
    private final MessageFormatter messageFormatter;
    private final SectionStyle[] sectionStyles;
    private final Font defaultFont;

    private int columns = 1;

    /**
     * Creates a new {@code InputGridBuilder} instance.
     *
     * @param owner the parent window to which the input grid will be associated; can be {@code null}
     * @param messageFormatter the {@code MessageFormatter} used for formatting messages
     */
    GridBuilder(@Nullable Window owner, MessageFormatter messageFormatter, SectionStyle... sectionStyles) {
        this.owner = owner;
        this.messageFormatter = messageFormatter;
        this.sectionStyles = sectionStyles.length == 0 ? DEFAULT_SECTION_STYLES : sectionStyles;
        this.defaultFont = FU.convert(new Label().getFont());
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    @Override
    public String format(String message, Object... args) {
        return messageFormatter.format(message, args);
    }

    @Override
    public SectionStyle getSectionStyle(int level) {
        return level < sectionStyles.length ? sectionStyles[level] : DEFAULT_SECTION_STYLE;
    }

    /**
     * Builds and returns an InputGrid with the current data and column configuration.
     *
     * @return the constructed InputGrid
     */
    public Grid build() {
        Grid grid = new Grid();

        grid.setContent(data, columns);
        grid.init();

        return grid;
    }

    @Override
    public <T> GridBuilder addInput(String id, String label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean hidden) {
        return doAdd(id, format(label), type, dflt, control, hidden);
    }

    @Override
    public <T> GridBuilder addInput(String id, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control) {
        return doAdd(id, null, type, dflt, control, false);
    }

    private <T> GridBuilder doAdd(@Nullable String id, @Nullable String label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean hidden) {
        // check for duplicate IDs
        if (id != null && !ids.add(id)) {
            throw new IllegalArgumentException(String.format(INPUT_WITH_ID_ALREADY_DEFINED, id));
        }

        // add
        data.add(new Meta<>(id, label, type, dflt, control, hidden));

        return this;
    }

    @Override
    public GridBuilder columns(int columns) {
        this.columns = LangUtil.requirePositive(columns);
        return this;
    }

    @Override
    public GridBuilder section(int level, String fmt, Object... args) {
        String title = format(fmt, args);

        SectionStyle style = getSectionStyle(level);

        // add spacing before the title
        if (style.vspaceBefore() > 0) {
            node(FxUtil.vspace(style.vspaceBefore() * defaultFont.getHeight()));
        }

        // add title
        FontDef fd = new FontDef();
        if (style.makeBold()) {
            fd.setBold(true);
        }
        if (style.scale() != 1.0) {
            fd.setSize(defaultFont.getSizeInPoints() * style.scale());
        }

        Label label = new Label(title);
        label.setFont(FU.convert(FU.deriveFont(defaultFont, fd)));
        node(label);

        // add spacing after the title
        if (style.vspaceAfter() > 0) {
            node(FxUtil.vspace(style.vspaceAfter() * FU.deriveFont(defaultFont, fd).getHeight()));
        }

        return this;
    }

    @Override
    public GridBuilder text(String fmt, Object... args) {
        Label node = new Label(format(fmt, args));
        return node(node);
    }

    @Override
    public GridBuilder node(Node node) {
        return doAdd(null, null, Void.class, () -> null, new ControlWrapper(node), false);
    }

    @Override
    public GridBuilder node(String label, Node node) {
        return doAdd(null, label, Void.class, () -> null, new ControlWrapper(node), false);
    }

    @Override
    public GridBuilder labeledText(String fmtLabel, String fmtText, Object... args) {
        Label node = new Label(format(fmtText, args));
        return node(format(fmtLabel, args), node);
    }

    @Override
    public <T> GridBuilder inputConstant(String id, String label, Supplier<@Nullable T> value, Class<T> cls) {
        Property<@Nullable T> property = new SimpleObjectProperty<>(value.get());
        TextField tf = new TextField();
        tf.setDisable(true);
        tf.textProperty().bind(property.map(v -> Objects.toString(v, "")));
        InputControl<T> ic = new SimpleInputControl<>(tf, property, value, v -> Optional.empty());
        return addInput(id, label, cls, value, ic, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> GridBuilder inputConstant(String id, String label, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return inputConstant(id, label, dflt, cls);
    }

    @Override
    public <T> GridBuilder inputHidden(String id, Supplier<@Nullable T> value, Class<T> cls) {
        Property<@Nullable T> property = new SimpleObjectProperty<>(value.get());
        InputControl<T> ic = new SimpleInputControl<>(new Label(), property, value, v -> Optional.empty());
        return addInput(id, "", cls, value, ic, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> GridBuilder inputHidden(String id, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return inputHidden(id, dflt, cls);
    }

    @Override
    public GridBuilder inputString(String id, String label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return addInput(id, label, String.class, dflt, InputControl.stringInput(dflt, validate), false);
    }

    @Override
    public GridBuilder inputInteger(String id, String label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        return addInput(id, label, Long.class, dflt, InputControl.integerInput(dflt, validate), false);
    }

    @Override
    public GridBuilder inputDecimal(String id, String label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        return addInput(id, label, Double.class, dflt, InputControl.decimalInput(dflt, validate), false);
    }

    @Override
    public GridBuilder inputCheckBox(String id, String label, BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        return addInput(id, label, Boolean.class, dflt::getAsBoolean, InputControl.checkBoxInput(dflt, text, validate), false);
    }

    @Override
    public <T> GridBuilder inputComboBox(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, InputControl.comboBoxInput(items, dflt, validate), false);
    }

    @Override
    public <T> GridBuilder inputComboBoxEx(
            String id,
            String label,
            @Nullable UnaryOperator<T> edit,
            @Nullable Supplier<T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Function<T, String> format,
            Supplier<? extends @Nullable T> dflt,
            Class<T> cls,
            Collection<T> items,
            Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, InputControl.comboBoxExInput(items, dflt, edit, add, remove, format, validate), false);
    }

    @Override
    public <T> GridBuilder inputRadioList(String id, String label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items,
                                          Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, new RadioPane<>(items, null, validate), false);
    }

    @Override
    public GridBuilder inputSlider(String id, String label, Supplier<@Nullable Double> dflt, double min, double max) {
        return addInput(id, label, Double.class, dflt, Controls.slider().min(min).max(max).setDefault(dflt).build(), false);
    }

    @Override
    public GridBuilder inputOptions(String id, String label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return addInput(id, label, Arguments.class, dflt, new OptionsPane(options, dflt), false);
    }

    @Override
    public GridBuilder inputOptions(String id, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return addInput(id, Arguments.class, dflt, new OptionsPane(options, dflt));
    }

    @Override
    public GridBuilder inputFile(String id, String label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate) {
        return addInput(id, label, Path.class, dflt, new FileInput(owner, mode, existingOnly, dflt, filter, validate), false);
    }

    @Override
    public <T> GridBuilder inputControl(String id, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        return doAdd(id, null, type, dflt, control, false);
    }

    @Override
    public <T> GridBuilder inputControl(String id, String label, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        return doAdd(null, label, type, dflt, control, false);
    }

}
