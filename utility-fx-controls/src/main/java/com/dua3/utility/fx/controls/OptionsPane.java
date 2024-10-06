package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.ChoiceOption;
import com.dua3.utility.options.Flag;
import com.dua3.utility.options.Option;
import com.dua3.utility.options.SimpleOption;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final InputControl.State<Arguments> state;
    private final Supplier<? extends Collection<Option<?>>> options;
    private final Supplier<Arguments> dflt;
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
        Property<Arguments> value = new SimpleObjectProperty<>();
        this.state = new State<>(value, dflt);
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
    public void set(Arguments arg) {
        for (var item : items.entrySet()) {
            Option option = item.getKey();
            InputControl control = item.getValue();
            Stream<List<?>> stream = arg.stream(option);
            Optional<?> value = stream.filter(list -> !list.isEmpty())
                    .reduce((first, second) -> second)
                    .map(list -> list.get(list.size() - 1));
            control.set(value.orElse(null));
        }
    }

    @Override
    public void init() {
        getChildren().clear();

        Collection<Option<?>> optionSet = options.get();
        Arguments values = dflt.get();

        int row = 0;
        for (Option<?> option : optionSet) {
            Label label = new Label(option.displayName());

            var control = createControl(values, option);
            items.put(option, control);

            addToGrid(label, 0, row);
            addToGrid(control.node(), 1, row);

            row++;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> InputControl<T> createControl(Arguments values, Option<T> option) {
        if (option instanceof ChoiceOption<T> co) {
            return new ChoiceInputControl<>(co, supplyDefault(co, values));
        } else if (option instanceof Flag f) {
            CheckBox checkBox = new CheckBox(f.displayName());
            return (InputControl<T>) new SimpleInputControl<>(checkBox, checkBox.selectedProperty(), supplyDefault(f, values), nopValidator());
        } else if (option instanceof SimpleOption<T> so) {
            StringConverter<T> converter = new StringConverter<>() {
                @Override
                public String toString(T v) {
                    return option.format(v);
                }

                @Override
                public T fromString(String s) {
                    return option.map(s);
                }
            };
            return InputControl.stringInput(supplyDefault(so, values), nopValidator(), converter);
        }

        throw new UnsupportedOperationException("unsupported input type: " + option.getClass().getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> T getValue(Option<T> option, Arguments values) {
        if (option instanceof Flag flag) {
            return (T) (Object) values.isSet(flag);
        }
        if (option instanceof SimpleOption so) {
            return (T) values.get(so).orElse(so.getDefault());
        }
        if (option instanceof ChoiceOption co) {
            return (T) values.get(co).orElse(co.getDefault());
        }
        throw new IllegalArgumentException("Unknown option type: " + option);
    }

    private static <T> Function<T, Optional<String>> nopValidator() {
        return s -> Optional.empty();
    }

    private static <T> Supplier<T> supplyDefault(Option<? extends T> option, Arguments values) {
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
    public Property<Arguments> valueProperty() {
        return state.valueProperty();
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
