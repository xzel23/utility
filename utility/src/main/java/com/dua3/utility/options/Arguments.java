package com.dua3.utility.options;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private final List<Entry<?>> options;
    /**
     * The positional arguments.
     */
    private final List<String> args;
    private final int minArgs;
    private final int maxArgs;

    /**
     * Constructor.
     *
     * @param options  the options to set
     * @param args the arguments
     */
    public Arguments(Collection<? extends Entry<?>> options, Collection<String> args) {
        this(options, args, 0, Integer.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param options  the options to set
     * @param args the arguments
     * @param minArgs the minimum argument count
     * @param maxArgs the maximum argument count
     */
    public Arguments(
            Collection<? extends Entry<?>> options,
            Collection<String> args,
            int minArgs,
            int maxArgs
    ) {
        LangUtil.check(minArgs >= 0, "minArgs must be non-negative: %d", minArgs);
        LangUtil.check(maxArgs >= minArgs, "maxArgs must be greater than or equal to minArgs (%d): %d", minArgs, maxArgs);

        this.options = List.copyOf(options);
        this.args = List.copyOf(args);
        this.minArgs = minArgs;
        this.maxArgs = maxArgs;
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
     * Validates a variable number of options.
     *
     * @param allOptions the options to be validated
     * @param <T>        the type of the option
     */
    @SafeVarargs
    public final <T extends Option<?>> void validate(T... allOptions) {
        validate(List.of(allOptions));
    }

    /**
     * Validates a collection of options.
     *
     * @param allOptions the options to be validated
     */
    public void validate(
            Collection<? extends Option<?>> allOptions
    ) {
        // check occurrences
        Map<Option<?>, Integer> hist = new HashMap<>();
        options.forEach(entry -> hist.compute(entry.option, (k_, i_) -> i_ == null ? 1 : i_ + 1));

        allOptions.stream()
                .map(option -> Pair.ofNonNull(option, hist.getOrDefault(option, 0)))
                .forEach(p -> {
                    Option<?> option = p.first();
                    int occurrences = p.second();

                    int minOccurrences = option.minOccurrences();
                    int maxOccurrences = option.maxOccurrences();

                    // check min occurrences
                    if (minOccurrences == 1) {
                        LangUtil.check(minOccurrences <= occurrences,
                                () -> new OptionException(
                                        "missing required option '%s'".formatted(option.name()
                                        )));
                    } else {
                        LangUtil.check(minOccurrences <= occurrences,
                                () -> new OptionException(
                                        "option '%s' must be specified at least %d time(s), but was only %d times".formatted(
                                                option.name(), minOccurrences, occurrences
                                        )));
                    }

                    // check max occurrences
                    LangUtil.check(maxOccurrences >= occurrences,
                            () -> new OptionException(
                                    "option '%s' must be specified at most %d time(s), but was %d times".formatted(
                                            option.name(), maxOccurrences, occurrences
                                    )));
                });

        // check arity
        options.forEach(entry -> {
            Option<?> option = entry.option;
            int nParams = entry.params.size();
            LangUtil.check(
                    option.minArity() <= nParams,
                    () -> new OptionException(
                            "option '%s' must have at least %d parameters, but has only %d".formatted(
                                    option.name(),
                                    option.minArity(),
                                    nParams
                            )
                    )
            );
            LangUtil.check(
                    nParams <= option.maxArity(),
                    () -> new OptionException(
                            "option '%s' must have at most %d parameters, but has %d".formatted(
                                    option.name(),
                                    option.maxArity(),
                                    nParams
                            )
                    )
            );
        });

        if (args.size() < minArgs) {
            throw new OptionException("missing argument (at least " + minArgs + " arguments must be given)");
        }

        if (args.size() > maxArgs) {
            throw new OptionException("too many arguments (at most " + maxArgs + " arguments can be given)");
        }
    }

    /**
     * Get positional arguments.
     *
     * @return the positional arguments
     */
    public List<String> positionalArgs() {
        return args;
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
        return options.stream();
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
        return options.stream()
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
        options.forEach(Entry::handle);
    }

    @Override
    public Iterator<Arguments.Entry<?>> iterator() {
        return options.iterator();
    }

    @Override
    public String toString() {
        try (Formatter fmt = new Formatter()) {
            fmt.format("Arguments{\n");
            for (Entry<?> entry : options) {
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
            this.option = option;
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
