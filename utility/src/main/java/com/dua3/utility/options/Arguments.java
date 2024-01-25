package com.dua3.utility.options;

import com.dua3.utility.text.TextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Class holding options and arguments; use for parsing and passing command line arguments and configuration options.
 */
public class Arguments implements Iterable<Arguments.Entry<?>> {

    /**
     * The options passed on the command line with their respective arguments.
     */
    private final List<Entry<?>> parsedOptions;
    /**
     * The positional arguments.
     */
    private final List<String> positionalArgs;

    /**
     * Constructor.
     *
     * @param parsedOptions  the options detected by the command line parser
     * @param positionalArgs the positional arguments
     */
    public Arguments(Collection<? extends Entry<?>> parsedOptions, Collection<String> positionalArgs) {
        this.parsedOptions = List.copyOf(parsedOptions);
        this.positionalArgs = List.copyOf(positionalArgs);
    }

    /**
     * Create an empty instance.
     *
     * @return empty Arguments instance
     */
    public static Arguments empty() {
        return new Arguments(Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Create an instance.
     *
     * @param args the arguments to pass to the instance.
     * @return new instance
     */
    public static Arguments of(Entry<?>... args) {
        return new Arguments(List.of(args), Collections.emptyList());
    }

    /**
     * Create an argument entry intended to be passed to {@link #of(Entry[])}.
     *
     * @param <T>    the option type
     * @param option the option for the entry
     * @param args   the arguments belonging to the option
     * @return new {@link Entry}
     */
    @SafeVarargs
    public static <T> Entry<T> createEntry(Option<T> option, T... args) {
        Entry<T> entry = new Entry<>(option);
        for (var arg : args) {
            entry.addArg(arg);
        }
        return entry;
    }

    /**
     * Get positional arguments.
     *
     * @return the positional arguments
     */
    public List<String> positionalArgs() {
        return positionalArgs;
    }

    /**
     * Get value of {@link SimpleOption}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return the parameter passed to the option, or the option's default value (if set)
     * @throws OptionException if neither is set
     */
    public <T> T getOrThrow(SimpleOption<T> option) {
        return get(option).orElseThrow(() -> new OptionException("missing required option: " + option.name()));
    }

    /**
     * Get value of {@link SimpleOption}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return Optional holding the argument passed to this option, the option's default value, or an empty Optional
     * if neither is provided
     */
    public <T> Optional<T> get(SimpleOption<T> option) {
        return stream(option).findFirst().map(list -> list.get(0)).or(option::getDefault);
    }

    /**
     * Get value of {@link ChoiceOption}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return the parameter passed to the option, or the option's default value (if set)
     * @throws OptionException if neither is set
     */
    public <T> T getOrThrow(ChoiceOption<T> option) {
        return get(option).orElseThrow(() -> new OptionException("missing required option: " + option.name()));
    }

    /**
     * Get value of {@link ChoiceOption}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return the option's value
     */
    public <T> Optional<T> get(ChoiceOption<T> option) {
        return stream(option).findFirst().map(list -> list.get(0)).or(option::getDefault);
    }

    /**
     * Test if the flag is set.
     *
     * @param flag the flag
     * @return true, if the flag is set
     */
    public boolean isSet(Flag flag) {
        return stream().anyMatch(entry -> entry.option.equals(flag));
    }

    /**
     * Execute action if {@link Flag} is set.
     *
     * @param flag   the flag
     * @param action the action to execute
     */
    public void ifSet(Flag flag, Runnable action) {
        if (isSet(flag)) {
            action.run();
        }
    }

    /**
     * Get the stream of parsed options.
     *
     * @return stream of parsed options and respective arguments
     */
    public Stream<Entry<?>> stream() {
        return parsedOptions.stream();
    }

    /**
     * Get the stream of values for an option.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return stream of lists containing the arguments for each appearance of the given option
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<List<T>> stream(Option<T> option) {
        return parsedOptions.stream()
                .filter(entry -> entry.option.equals(option))
                .map(entry -> ((Entry<T>) entry).getParams());
    }

    /**
     * Execute an action if {@link SimpleOption} is present.
     *
     * @param option the option
     * @param action the action to execute
     * @param <T>    the parameter type
     */
    public <T> void ifPresent(SimpleOption<T> option, Consumer<? super T> action) {
        stream(option).map(list -> list.get(0)).forEach(action);
    }

    /**
     * Execute an action for every instance of the given {@link Option}.
     *
     * @param option the option
     * @param action the action to execute
     * @param <T>    the parameter type
     */
    public <T> void forEach(Option<T> option, Consumer<? super List<T>> action) {
        stream(option).forEach(action);
    }

    /**
     * Call the handlers for all passed options.
     */
    public void handle() {
        parsedOptions.forEach(Entry::handle);
    }

    @Override
    public Iterator<Arguments.Entry<?>> iterator() {
        return parsedOptions.iterator();
    }

    @Override
    public String toString() {
        try (Formatter fmt = new Formatter()) {
            fmt.format("Arguments{\n");
            for (Entry<?> entry : parsedOptions) {
                if (entry.option instanceof Flag) {
                    fmt.format("  %s\n", entry.option.name());
                } else {
                    fmt.format("  %s %s\n", entry.option.name(), TextUtil.joinQuoted(entry.getParams(), " "));
                }
            }
            if (!positionalArgs().isEmpty()) {
                fmt.format("  %s\n", TextUtil.joinQuoted(positionalArgs(), " "));
            }
            fmt.format("}");
            return fmt.toString();
        }
    }

    /**
     * An entry represents a single option given on the command line together with the parameters given in that option
     * invocation, converted to the option's argument type.
     *
     * @param <T> the argument type for the option
     */
    public static final class Entry<T> {
        final Option<T> option;
        final List<T> params;

        Entry(Option<T> option) {
            this.option = Objects.requireNonNull(option);
            this.params = new ArrayList<>();
        }

        static Entry<?> create(Option<?> option) {
            return new Entry<>(option);
        }

        /**
         * Adds a parameter to the entry.
         *
         * @param s the parameter to be added
         */
        public void addParameter(String s) {
            addArg(option.map(s));
        }

        /**
         * Adds an argument to the entry.
         *
         * @param v the argument to be added
         */
        public void addArg(T v) {
            params.add(v);
        }

        /**
         * Get the option that this entry belongs to
         *
         * @return the option
         */
        public Option<T> getOption() {
            return option;
        }

        /**
         * Get the parameters given for this invocation of the option.
         *
         * @return list of option parameters, converted to the target type
         */
        public List<T> getParams() {
            return Collections.unmodifiableList(params);
        }

        /**
         * Call the handler registered with the option and pass the parameters.
         */
        public void handle() {
            option.handle(params);
        }
    }

}
