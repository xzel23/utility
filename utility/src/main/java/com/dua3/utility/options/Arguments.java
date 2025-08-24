package com.dua3.utility.options;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import org.jspecify.annotations.Nullable;

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
    public Arguments(Collection<Entry<?>> options, Collection<String> args) {
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
            Collection<Entry<?>> options,
            Collection<String> args,
            int minArgs,
            int maxArgs
    ) {
        LangUtil.checkArg(minArgs >= 0, "minArgs must be non-negative: %d", minArgs);
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
     * @param value  the value set for option
     * @return new {@link Entry}
     */
    public static <T> Entry<T> createEntry(Option<T> option, @Nullable T value) {
        return new Entry<>(option, value);
    }

    /**
     * Validates a variable number of options.
     *
     * @param allOptions the options to be validated
     * @param <O>        the type of the option
     */
    @SafeVarargs
    public final <O extends Option<?>> void validate(O... allOptions) {
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
        options.forEach(entry -> hist.compute(entry.option, (opt, i) -> i == null ? 1 : i + 1));

        record OptionOccurences(Option<?> option, int occurrences) {}
        allOptions.stream()
                .map(option -> new OptionOccurences(option, hist.getOrDefault(option, 0)))
                .forEach(oo -> {
                    Option<?> option = oo.option();
                    int occurrences = oo.occurrences();

                    Repetitions repetitions = option.repetitions();
                    int minOccurrences = repetitions.min();
                    int maxOccurrences = repetitions.max();

                    // check min occurrences
                    if (minOccurrences == 1) {
                        LangUtil.check(1 <= occurrences,
                                () -> new OptionException(
                                        option,
                                        "missing required option '%s'".formatted(option.displayName()
                                        )));
                    } else {
                        LangUtil.check(minOccurrences <= occurrences,
                                () -> new OptionException(
                                        option,
                                        "option '%s' must be specified at least %d time(s), but was only %d times".formatted(
                                                option.displayName(), minOccurrences, occurrences
                                        )));
                    }

                    // check max occurrences
                    LangUtil.check(maxOccurrences >= occurrences,
                            () -> new OptionException(
                                    option,
                                    "option '%s' must be specified at most %d time(s), but was %d times".formatted(
                                            option.displayName(), maxOccurrences, occurrences
                                    )));
                });

        if (args.size() < minArgs) {
            throw new ArgumentsException("missing argument (at least " + minArgs + " arguments must be given)");
        }

        if (args.size() > maxArgs) {
            throw new ArgumentsException("too many arguments (at most " + maxArgs + " arguments can be given)");
        }
    }

    /**
     * Checks whether the current arguments instance is empty.
     * An instance is considered empty if both the options and arguments collections are empty.
     *
     * @return true if both options and args are empty, false otherwise
     */
    public boolean isEmpty() {
        return options.isEmpty() && args.isEmpty();
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
     * Get value of {@link Option}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return Optional holding the argument passed to this option, the option's default value, or an empty Optional
     * if neither is provided
     */
    public <T> Optional<T> get(Option<T> option) {
        return stream(option).findFirst().or(option::getDefault);
    }

    /**
     * Get value of {@link Option}.
     *
     * @param option the option
     * @param <T>    the generic type of the option
     * @return the parameter passed to the option, or the option's default value (if set)
     * @throws OptionException if neither is set
     */
    public <T> T getOrThrow(Option<T> option) {
        return get(option).orElseThrow(() -> new OptionException(option, "missing required option: " + option.displayName()));
    }

    /**
     * Test if the flag is set.
     *
     * @param flag the flag
     * @return true, if the flag is set
     */
    public boolean isSet(Option<Boolean> flag) {
        return Optional.ofNullable(
                        stream()
                                .filter(entry -> entry.option.isEquivalent(flag))
                                .map(Entry::getValue)
                                .map(Boolean.class::cast)
                                .reduce(null, (@Nullable Boolean a, @Nullable Boolean b) -> {
                                    if (a == null) {
                                        return b;
                                    }
                                    if (b == null) {
                                        return a;
                                    }
                                    return a || b;
                                })
                )
                .or(flag::getDefault)
                .orElse(false);
    }

    /**
     * Execute action if the boolean option is set to {@code true}.
     *
     * @param flag   the flag
     * @param action the action to execute
     */
    public void ifSet(Option<Boolean> flag, Runnable action) {
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
    public <T> Stream<T> stream(Option<T> option) {
        return options.stream()
                .filter(entry -> entry.option.isEquivalent(option))
                .map(entry -> ((Entry<T>) entry).getValue());
    }

    /**
     * Execute an action if {@link Option} is present.
     *
     * @param option the option
     * @param action the action to execute
     * @param <T>    the parameter type
     */
    public <T> void ifPresent(Option<T> option, Consumer<? super T> action) {
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
                if (entry.option.isFlag()) {
                    fmt.format("  %s\n", entry.option.switches().getFirst());
                } else {
                    fmt.format("  %s \"%s\"\n", entry.option.switches().getFirst(), entry.getValue());
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
        final @Nullable T value;

        Entry(Option<T> option, @Nullable T value) {
            this.option = option;
            this.value = value;
        }

        /**
         * Creates a new entry that represents an option and its associated value.
         *
         * @param <U> the type of the value associated with the option
         * @param option the option to associate with the entry; must not be null
         * @param value the value associated with the option; may be null
         * @return a new entry containing the specified option and value
         */
        public static <U> Entry<U> create(Option<U> option, @Nullable U value) {
            return new Entry<>(option, value);
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
        public @Nullable T getValue() {
            return value;
        }

        /**
         * Invokes the handler associated with the option and passes the value of the option to it.
         * This method is used to process the option value using the predefined behavior specified by the handler.
         */
        public void handle() {
            option.handler().accept(value);
        }
    }

}
