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
import com.dua3.utility.fx.controls.Grid.Meta;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontDef;
import com.dua3.utility.text.MessageFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A builder class for constructing a grid-based input form structure. This class provides
 * methods to add various types of input controls, sections, labels, and other nodes into
 * a grid layout with specified configuration.
 * <p>
 * Implements the {@code InputBuilder} interface to provide a fluent API for defining
 * inputs and layout properties.
 * <p>
 * The grid can include labeled inputs, sections with custom styles, and constant or hidden
 * inputs. Additional functionalities include text and node additions, along with support
 * for validation and default values for inputs.
 */
public class GridBuilder implements InputBuilder<GridBuilder> {

    private static final Logger LOG = LogManager.getLogger(GridBuilder.class);

    private static final FxFontUtil FU = FxFontUtil.getInstance();
    private static final String INPUT_WITH_ID_ALREADY_DEFINED = "Input with id '%s' already defined";

    private static final SectionStyle[] DEFAULT_SECTION_STYLES = {new SectionStyle(1.0f, 0.5f, true, 1.5f), new SectionStyle(0.5f, 0.25f, true, 1.25f), new SectionStyle(0.5f, 0.125f, true, 1.0f)};

    private static final SectionStyle DEFAULT_SECTION_STYLE = new SectionStyle(0.0f, 0.0f, false, 1.0f);
    private final Set<String> ids = new HashSet<>();
    private final List<Meta<?>> data = new ArrayList<>();
    private final @Nullable Window owner;
    private final MessageFormatter messageFormatter;
    private final SectionStyle[] sectionStyles;
    private final Font defaultFont;
    private int columns = 1;
    private double minRowHeight = 2.5;
    private LayoutUnit minRowHeightUnit = LayoutUnit.EM;
    private LabelPlacement labelPlacement = LabelPlacement.BEFORE;
    private MarkerSymbols markerSymbols;
    private double markerWidth;
    private double prefWidth = -1;
    private double prefHeight = -1;
    private double minWidth = -1;
    private double minHeight = -1;
    private double maxWidth = -1;
    private double maxHeight = -1;
    private Function<Map<String, Object>, Map<String, Optional<String>>> validate = ignored -> Collections.emptyMap();

    /**
     * Creates a new {@code InputGridBuilder} instance.
     *
     * @param owner            the parent window to which the input grid will be associated; can be {@code null}
     * @param messageFormatter the {@code MessageFormatter} used for formatting messages
     */
    GridBuilder(@Nullable Window owner, MessageFormatter messageFormatter, SectionStyle... sectionStyles) {
        this.owner = owner;
        this.messageFormatter = messageFormatter;
        this.sectionStyles = sectionStyles.length == 0 ? DEFAULT_SECTION_STYLES : sectionStyles;
        this.defaultFont = FU.convert(new Label().getFont());
        this.markerSymbols = MarkerSymbols.defaultSymbols();
        this.markerWidth = markerSymbols.calculateWidth(defaultFont, 0.0) + 2.0;
    }

    @Override
    public MessageFormatter getMessageFormatter() {
        return messageFormatter;
    }

    @Override
    public SectionStyle getSectionStyle(int level) {
        return level < sectionStyles.length ? sectionStyles[level] : DEFAULT_SECTION_STYLE;
    }

    @Override
    public GridBuilder markerSymbols(MarkerSymbols markerSymbols) {
        this.markerSymbols = markerSymbols;
        this.markerWidth = markerSymbols.calculateWidth(defaultFont, 0.0) + 2.0;
        return this;
    }

    @Override
    public GridBuilder prefWidth(double value) {
        this.prefWidth = value;
        return this;
    }

    @Override
    public GridBuilder prefHeight(double value) {
        this.prefHeight = value;
        return this;
    }

    @Override
    public GridBuilder minWidth(double value) {
        this.minWidth = value;
        return this;
    }

    @Override
    public GridBuilder minHeight(double value) {
        this.minHeight = value;
        return this;
    }

    @Override
    public GridBuilder maxWidth(double value) {
        this.maxWidth = value;
        return this;
    }

    @Override
    public GridBuilder maxHeight(double value) {
        this.maxHeight = value;
        return this;
    }

    @Override
    public <T> GridBuilder addInput(String id, MessageFormatter.MessageFormatterArgs label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean visible) {
        return doAdd(id, format(label), type, dflt, control, visible);
    }

    @Override
    public <T> GridBuilder addInput(String id, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control) {
        return doAdd(id, null, type, dflt, control, true);
    }

    @Override
    public GridBuilder columns(int columns) {
        this.columns = LangUtil.requirePositive(columns);
        return this;
    }

    @Override
    public GridBuilder minRowHeight(double height, LayoutUnit unit) {
        this.minRowHeight = height;
        this.minRowHeightUnit = unit;
        return this;
    }

    /**
     * Set the label placement.
     *
     * @param labelPlacement the label placement
     * @return this GridBuilder instance
     */
    public GridBuilder labelPlacement(LabelPlacement labelPlacement) {
        this.labelPlacement = labelPlacement;
        return this;
    }

    @Override
    public GridBuilder verticalSpace(double height, LayoutUnit unit) {
        data.add(new Meta<>(null, null, Void.class, () -> null, new ControlWrapper(new Region()), true, markerWidth, height, unit));
        return this;
    }

    @Override
    public GridBuilder node(Node node) {
        return doAdd(null, null, Void.class, () -> null, new ControlWrapper(node), true);
    }

    @Override
    public GridBuilder node(MessageFormatter.MessageFormatterArgs label, Node node) {
        return doAdd(null, format(label), Void.class, () -> null, new ControlWrapper(node), true);
    }

    private String format(MessageFormatter.MessageFormatterArgs mfargs) {
        return format(mfargs.fmt(), mfargs.args());
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

    private <T> GridBuilder doAdd(@Nullable String id, @Nullable String label, Class<T> type, Supplier<? extends @Nullable T> dflt, InputControl<T> control, boolean visible) {
        // check for duplicate IDs
        if (id != null && !id.isEmpty() && !ids.add(id)) {
            throw new IllegalArgumentException(String.format(INPUT_WITH_ID_ALREADY_DEFINED, id));
        }

        // add
        Meta<T> meta = new Meta<>(id, label, type, dflt, control, visible, markerWidth);
        data.add(meta);

        return this;
    }

    @Override
    public GridBuilder text(String fmt, Object... args) {
        Label node = new Label(format(fmt, args));
        return node(node);
    }

    @Override
    public GridBuilder labeledText(String fmtLabel, String fmtText, Object... args) {
        Label node = new Label(format(fmtText, args));
        return node(MessageFormatter.args(fmtLabel, args), node);
    }

    @Override
    public <T> GridBuilder inputConstant(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable T> value, Class<T> cls) {
        Property<@Nullable T> property = new SimpleObjectProperty<>(value.get());
        TextField tf = new TextField();
        tf.setDisable(true);
        tf.textProperty().bind(property.map(v -> Objects.toString(v, "")));
        InputControl<T> ic = new SimpleInputControl<>(tf, property, value, v -> Optional.empty());
        return addInput(id, label, cls, value, ic, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> GridBuilder inputConstant(String id, MessageFormatter.MessageFormatterArgs label, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return inputConstant(id, label, dflt, cls);
    }

    @Override
    public <T> GridBuilder inputHidden(String id, Supplier<@Nullable T> value, Class<T> cls) {
        Property<@Nullable T> property = new SimpleObjectProperty<>(value.get());
        InputControl<T> ic = new SimpleInputControl<>(new Label(), property, value, v -> Optional.empty());
        return addInput(id, "", cls, value, ic, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> GridBuilder inputHidden(String id, T value) {
        Class<T> cls = (Class<T>) value.getClass();
        Supplier<T> dflt = () -> value;
        return inputHidden(id, dflt, cls);
    }

    @Override
    public GridBuilder inputText(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return addInput(id, label, String.class, dflt, InputControl.textInput(dflt, validate), true);
    }

    @Override
    public GridBuilder inputString(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return addInput(id, label, String.class, dflt, InputControl.stringInput(dflt, validate), true);
    }

    @Override
    public GridBuilder inputPassword(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable String> dflt, Function<@Nullable String, Optional<String>> validate) {
        return addInput(id, label, String.class, dflt, InputControl.passwordInput(dflt, validate), true);
    }

    @Override
    public GridBuilder inputInteger(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Long> dflt, Function<@Nullable Long, Optional<String>> validate) {
        return addInput(id, label, Long.class, dflt, InputControl.integerInput(dflt, validate), true);
    }

    @Override
    public GridBuilder inputDecimal(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, Function<@Nullable Double, Optional<String>> validate) {
        return addInput(id, label, Double.class, dflt, InputControl.decimalInput(dflt, validate), true);
    }

    @Override
    public GridBuilder inputCheckBox(String id, MessageFormatter.MessageFormatterArgs label, BooleanSupplier dflt, String text, Function<@Nullable Boolean, Optional<String>> validate) {
        return addInput(id, label, Boolean.class, dflt::getAsBoolean, InputControl.checkBoxInput(dflt, text, validate), true);
    }

    @Override
    public <T> GridBuilder inputComboBox(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, InputControl.comboBoxInput(items, dflt, validate), true);
    }

    @Override
    public <T> GridBuilder inputComboBoxEx(String id, MessageFormatter.MessageFormatterArgs label, @Nullable Function<T, @Nullable T> edit, @Nullable Supplier<@Nullable T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, InputControl.comboBoxExInput(items, dflt, edit, add, remove, format, validate), true);
    }

    @Override
    public <T> GridBuilder inputRadioList(String id, MessageFormatter.MessageFormatterArgs label, Supplier<? extends @Nullable T> dflt, Class<T> cls, Collection<T> items, Function<@Nullable T, Optional<String>> validate) {
        return addInput(id, label, cls, dflt, new RadioPane<>(items, null, validate), true);
    }

    @Override
    public GridBuilder inputSlider(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Double> dflt, double min, double max) {
        return addInput(id, label, Double.class, dflt, Controls.slider().min(min).max(max).build(), true);
    }

    @Override
    public GridBuilder inputOptions(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return addInput(id, label, Arguments.class, dflt, new OptionsPane(options, dflt), true);
    }

    @Override
    public GridBuilder inputOptions(String id, Supplier<@Nullable Arguments> dflt, Supplier<Collection<Option<?>>> options) {
        return addInput(id, Arguments.class, dflt, new OptionsPane(options, dflt));
    }

    @Override
    public GridBuilder inputFile(String id, MessageFormatter.MessageFormatterArgs label, Supplier<@Nullable Path> dflt, FileDialogMode mode, boolean existingOnly, Collection<FileChooser.ExtensionFilter> filter, Function<@Nullable Path, Optional<String>> validate) {
        return addInput(id, label, Path.class, dflt, new FileInput(owner, mode, existingOnly, dflt, filter, validate), true);
    }

    @Override
    public <T> GridBuilder inputControl(String id, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        return doAdd(id, null, type, dflt, control, true);
    }

    @Override
    public <T> GridBuilder inputControl(String id, MessageFormatter.MessageFormatterArgs label, InputControl<T> control, Class<T> type, Supplier<? extends @Nullable T> dflt) {
        return doAdd(null, format(label), type, dflt, control, true);
    }

    /**
     * Sets the validation logic for the grid inputs.
     * <p>
     * An explicit validation function is only needed if field based validation is not enough, i.e.,
     * when multiple fields have to be validated together.
     * <p>
     * The method should always return a map with the same keys so that the validation errors
     * can be displayed correctly.
     *
     * @param validate a function that takes a map of input values and returns a map where
     *                 the keys are the input field identifiers, and the values are
     *                 optional error messages indicating validation issues. An empty
     *                 optional indicates no validation error for the corresponding field.
     * @return this {@code GridBuilder} instance for method chaining
     */
    public GridBuilder validate(Function<Map<String, Object>, Map<String, Optional<String>>> validate) {
        this.validate = validate;
        return this;
    }

    /**
     * Builds and returns an InputGrid with the current data and column configuration.
     *
     * @return the constructed InputGrid
     */
    public Grid build() {
        LOG.trace("building grid with {} rows and {} columns", data.size(), columns);

        Grid grid = new Grid(markerSymbols, validate);

        setIfConfigured(minWidth, grid::setMinWidth);
        setIfConfigured(minHeight, grid::setMinHeight);
        setIfConfigured(maxWidth, grid::setMaxWidth);
        setIfConfigured(maxHeight, grid::setMaxHeight);
        setIfConfigured(prefWidth, grid::setPrefWidth);
        setIfConfigured(prefHeight, grid::setPrefHeight);

        grid.setContent(data, columns);
        grid.setMinRowHeight(minRowHeight, minRowHeightUnit);
        grid.setLabelPlacement(labelPlacement);
        grid.init();

        return grid;
    }

    private void setIfConfigured(double value, DoubleConsumer setter) {
        if (value > 0) {
            setter.accept(value);
        }
    }

    private static class ControlWrapper implements InputControl<Void> {

        private final Node node;

        private final Property<Void> value = new SimpleObjectProperty<>();
        private final BooleanProperty required = new SimpleBooleanProperty(false);
        private final BooleanProperty valid = new SimpleBooleanProperty(true);
        private final ReadOnlyStringProperty error = new SimpleStringProperty("");
        private final InputControlState<Void> state = InputControlState.voidState();

        ControlWrapper(Node node) {
            this.node = node;
        }

        @Override
        public InputControlState<Void> state() {
            return state;
        }

        @Override
        public void reset() { /* nop */ }

        @Override
        public ReadOnlyBooleanProperty requiredProperty() {
            return required;
        }

        @Override
        public ReadOnlyBooleanProperty validProperty() {
            return valid;
        }

        @Override
        public ReadOnlyStringProperty errorProperty() {
            return error;
        }

        @Override
        public Node node() {
            return node;
        }

        @Override
        public Property<Void> valueProperty() {
            return value;
        }
    }

}
