package com.dua3.utility.options;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Class holding options and arguments; use for parsing and passing command line arguments and configuration options.
 */
public class Arguments implements Iterable<Arguments.Entry<?>> {

    public static Arguments empty() {
        return new Arguments(new ArrayDeque<>(), Collections.emptyList());
    }

    public static Arguments of(Entry<?>... args) {
        return new Arguments(new LinkedList<>(Arrays.asList(args)), Collections.emptyList());
    }

    @SafeVarargs
    public static <T> Entry<T> createEntry(Option<T> option, T... args) {
        Entry<T> entry = new Entry<>(option);
        for (var arg: args) {
            entry.addArg(arg);
        }
        return entry;
    }

    /**
     * An entry represents a single option given on the command line together with the parameters given in that option
     * invocation, converted to the option's argument type.
     * @param <T> the arguemnt type for the option
     */
    public static final class Entry<T> {
        final Option<T> option;
        final List<T> parms;

        @SuppressWarnings("unchecked")
        static Entry<?> create(Option<?> option) {
            return new Entry<>(option);
        }

        Entry(Option<T> option) {
            this.option = Objects.requireNonNull(option);
            this.parms = new LinkedList<>();
        }

        void addParameter(String s) {
            addArg(option.map(s));
        }

        void addArg(T v) {
            parms.add(v);
        }

        /**
         * Get the option that this entry belongs to
         * @return the option
         */
        public Option<T> getOption() {
            return option;
        }

        /**
         * Getz the parameters given for this invocation of the ooption.
         * @return liest of option parameters, converted to the target type
         */
        public List<T> getParms() {
            return Collections.unmodifiableList(parms);
        }
    }

    /** The options passed on the command line with their respective arguments. */
    private final Queue<Entry<?>> parsedOptions;
    /** The positional arguments. */
    private final List<String> positionalArgs;

    /**
     * Constructor.
     * @param parsedOptions the options detected by the command line parser
     * @param positionalArgs the positional arguments
     */
    Arguments(Queue<Entry<?>> parsedOptions, List<String> positionalArgs) {
        this.parsedOptions = parsedOptions;
        this.positionalArgs = new ArrayList<>(positionalArgs);
    }

    /**
     * Get positional arguments.
     * @return the positional arguments
     */
    public List<String> positionalArgs() {
        return Collections.unmodifiableList(positionalArgs);
    }

    /**
     * Get value of {@link SimpleOption}.
     * @param option the option
     * @param <T> the generic type of the option 
     * @return the parameter passed to the option, or the option's default value (if set)
     * @throws OptionException if neither is set
     */
    public <T> T getOrThrow(SimpleOption<T> option) {
        return get(option).orElseThrow(() -> new OptionException("missing required option: " + option.name()));
    }

    /**
     * Get value of {@link SimpleOption}.
     * @param option the option
     * @param <T> the generic type of the option 
     * @return Optional holding the argument passed to this option, the option's default value, or an empty Optional
     *    if neither is provided
     */
    public <T> Optional<T> get(SimpleOption<T> option) {
        return stream(option).findFirst().map(list -> list.get(0)).or(option::getDefault);
    }

    /**
     * Get value of {@link ChoiceOption}.
     * @param option the option
     * @param <T> the generic type of the option 
     * @return the option's value
     */
    public <T> T get(ChoiceOption<T> option) {
        return stream(option).findFirst().map(list -> list.get(0)).orElse(option.getDefault());
    }

    /**
     * Test if flag is set.
     * @param flag the flag
     * @return true, if the flag is set
     */
    public boolean isSet(Flag flag) {
        return stream().anyMatch(entry -> entry.option.equals(flag));
    }

    /**
     * Execute action if {@link Flag} is set.
     * @param flag the flag
     * @param action the action to execute
     */
    public void ifSet(Flag flag, Runnable action) {
        if (isSet(flag)) {
            action.run();
        }
    }

    /**
     * Get stream of parsed options.
     * @return stream of parsed options and respective arguments
     */
    public Stream<Entry<?>> stream() {
        return parsedOptions.stream();
    }

    /**
     * Get stream of values for an option.
     * @param option the option
     * @param <T> the generic type of the option 
     * @return stream of lists containing the arguments for each appearance of the given option 
     */
    @SuppressWarnings("unchecked")
    public <T> Stream<List<T>> stream(Option<T> option) {
        return parsedOptions.stream()
                .filter(entry -> entry.option.equals(option))
                .map(entry -> ((Entry<T>) entry).getParms());
    }

    /**
     * Execute an action if {@link SimpleOption} is present.
     * @param option the option
     * @param action the action to execute
     * @param <T> the parameter type
     */
    public <T> void ifPresent(SimpleOption<T> option, Consumer<? super T> action) {
        stream(option).map(list -> list.get(0)).forEach(action);
    }
        
    /**
     * Execute an action for every instance of the given {@link Option}.
     * @param option the option
     * @param action the action to execute
     * @param <T> the parameter type
     */
    public <T> void forEach(Option<T> option, Consumer<? super List<T>> action) {
        stream(option).forEach(action);
    }
        
    @Override
    public Iterator<Entry<?>> iterator() {
        return parsedOptions.iterator();
    }

}