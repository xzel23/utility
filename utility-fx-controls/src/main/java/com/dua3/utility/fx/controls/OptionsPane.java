package com.dua3.utility.fx.controls;

import com.dua3.utility.i18n.I18N;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.options.Param;
import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.Option;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * OptionsPane is a custom JavaFX GridPane used as a control element for managing
 * a collection of options represented by instances of the {@link Option} class.
 * It implements the {@link InputControl} interface which allows it to handle input
 * and provide output in the form of {@link Arguments}.
 */
public class OptionsPane extends GridPane implements InputControl<Arguments> {

    /**
     * Logger
     */
    protected static final Logger LOG = LogManager.getLogger(OptionsPane.class);
    private static final Insets INSETS = new Insets(2);
    private final InputControlState<Arguments> state;
    private final Supplier<? extends Collection<Option<?>>> options;
    private final Supplier<@Nullable Arguments> dflt;
    private final Map<Option<?>, InputControl<?>> items = new LinkedHashMap<>();

    /**
     * Create new OptionsPane.
     *
     * @param optionSet     the available options
     * @param currentValues the current values
     * @see Option
     * @see Arguments
     */
    public OptionsPane(Collection<Option<?>> optionSet, Arguments currentValues) {
        this(() -> optionSet, () -> currentValues);
    }

    /**
     * Constructs a new OptionsPane with the given suppliers for options and default arguments.
     *
     * @param options A supplier providing a collection of options.
     * @param dflt    A supplier providing the default arguments.
     * @see Option
     * @see Arguments
     */
    public OptionsPane(Supplier<? extends Collection<Option<?>>> options, Supplier<Arguments> dflt) {
        this.options = options;
        this.dflt = dflt;
        Property<@Nullable Arguments> value = new SimpleObjectProperty<>();
        this.state = new InputControlState<>(value, dflt);
    }

    @Override
    public InputControlState<Arguments> state() {
        return state;
    }

    @Override
    public Node node() {
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Arguments get() {
        Deque<Arguments.Entry<?>> entries = new ArrayDeque<>();
        for (var entry : items.entrySet()) {
            Option option = entry.getKey();
            Object value = entry.getValue().valueProperty().getValue();
            if (value != null) {
                entries.add(Arguments.createEntry(option, value));
            }
        }
        return Arguments.of(entries.toArray(Arguments.Entry[]::new));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void set(@Nullable Arguments arg) {
        for (var item : items.entrySet()) {
            Option option = item.getKey();
            InputControl control = item.getValue();
            Stream<List<?>> stream = Objects.requireNonNullElseGet(arg, Arguments::empty).stream(option);
            Optional<?> value = stream.filter(list -> !list.isEmpty())
                    .reduce((first, second) -> second)
                    .map(List::getLast);
            control.set(value.orElse(null));
        }
    }

    @Override
    public void init() {
        setAlignment(Pos.BASELINE_LEFT);
        getChildren().clear();
        getRowConstraints().clear();

        Collection<Option<?>> optionSet = options.get();
        Arguments values = dflt.get();

        int row = 0;
        for (Option<?> option : optionSet) {
            RowConstraints rc = new RowConstraints();
            rc.setValignment(VPos.BASELINE);
            getRowConstraints().add(rc);

            Label label = new Label(option.displayName());

            var control = createControl(Objects.requireNonNullElseGet(values, Arguments::empty), option);
            items.put(option, control);

            addToGrid(label, 0, row);
            addToGrid(control.node(), 1, row);

            row++;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> InputControl<T> createControl(Arguments values, Option<T> option) {
        List<Param<?>> params = option.params();
        return switch (params.size()) {
            case 0 -> createFlagControl(values, option);
            case 1 -> createSimpleControl(option, values, (Param<T>) params.getFirst());
            default -> throw new IllegalArgumentException("option has more than one parameter");
        };
    }

    private static <T> Optional<String> validateNonNull(Option<T> option, @Nullable Object v) {
        if (v == null) {
            return Optional.of(I18N.getInstance().format("dua3_fx.options_pane.no_value", option.displayName()));
        }
        return Optional.empty();
    }

    private <T> InputControl<T> createSimpleControl(Option<T> option, Arguments values, Param<T> param) {
        Supplier<@Nullable T> defaultSupplier = () -> values.get(option).orElse(null);
        if (param.hasAllowedValues()) {
            return InputControl.comboBoxInput(
                    param.allowedValues(),
                    defaultSupplier,
                    v -> validateNonNull(option, v)
            );
        } else {
            StringConverter<T> converter = new StringConverter<>() {
                @Override
                public @Nullable String toString(@Nullable T v) {
                    return Objects.toString(v, null);
                }

                @Override
                public @Nullable T fromString(@Nullable String s) {
                    return option.map(LangUtil.asUnmodifiableList(s)).getValue();
                }
            };
            return InputControl.stringInput(supplyDefault(option, values), nopValidator(), converter);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> InputControl<T> createFlagControl(Arguments values, Option<T> option) {
        BooleanSupplier defaultSupplier = option.getTargetType() == Boolean.class
                ? () -> values.get(option).map(Boolean.class::cast).orElse(Boolean.FALSE)
                : () -> values.get(option).isPresent();
        return (InputControl<T>) InputControl.checkBoxInput(
                defaultSupplier,
                option.displayName(),
                v -> validateNonNull(option, v)
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> @Nullable T getValue(Option<T> option, Arguments values) {
        return values.get(option).orElseGet(() -> option.getDefault().orElse(null));
    }

    private static <T> Function<T, Optional<String>> nopValidator() {
        return s -> Optional.empty();
    }

    private static <T> Supplier<@Nullable T> supplyDefault(Option<? extends T> option, Arguments values) {
        return () -> getValue(option, values);
    }

    private void addToGrid(@Nullable Node node, int c, int r) {
        if (node != null) {
            add(node, c, r);
            setMargin(node, INSETS);
        }
    }

    @Override
    public void reset() {
        items.forEach((item, control) -> control.reset());
    }

    @Override
    public Property<@Nullable Arguments> valueProperty() {
        return state.valueProperty();
    }

    @Override
    public ReadOnlyBooleanProperty requiredProperty() {
        return state.requiredProperty();
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return state.validProperty();
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return state.errorProperty();
    }

}
