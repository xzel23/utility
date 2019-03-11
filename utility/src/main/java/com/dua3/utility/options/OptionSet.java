package com.dua3.utility.options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.dua3.utility.options.Option.Value;

/**
 * A Set of possible options to configure a classes behavior, i.e. all possible
 * options
 * for configuring a CSV reader.
 */
public class OptionSet implements Iterable<Option<?>> {

	/**
	 * Tranform {@code List<T>} into {@code List<Value<T>>}.
	 * @param <T>
	 *  the item type
	 * @param choices
	 *  the items to map
	 * @return
	 *  list of values
	 */
    public static <T> List<Value<T>> wrap(Collection<T> choices) {
        return choices.stream().map(Option::value).collect(Collectors.toList());
    }

    private final Set<Option<?>> options = new LinkedHashSet<>();

    /**
     * Create a new OptionSet.
     * @param options
     *  the options to include in this set
     */
    public OptionSet(Option<?>... options) {
        for (Option<?> option : options) {
            this.options.add(option);
        }
    }

    /**
     * Create an OptionSet.
     *
     * @param options
     *                the options
     */
    public OptionSet(Iterable<Option<?>> options) {
        options.forEach(this.options::add);
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param choices
     *                     the values this option can take
     */
    @SuppressWarnings({ "unchecked" })
    public <T> void addOption(String name, Class<T> klass, T defaultValue, T... choices) {
        addOption(name, klass, defaultValue, Arrays.asList(choices));
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param choices
     *                     the values this option can take
     */
    public <T> void addOption(String name, Class<T> klass, T defaultValue, Collection<T> choices) {
        List<Value<T>> values = wrap(choices);
        options.add(Option.choiceOption(name, klass, Option.value(String.valueOf(defaultValue), defaultValue), values));
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param values
     *                     the values this option can take
     */
    @SafeVarargs
    public final <T> void addOption(String name, Class<T> klass, Value<T> defaultValue, Value<T>... values) {
        addOption(name, klass, defaultValue, Arrays.asList(values));
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param values
     *                     the values this option can take
     */
    public final <T> void addOption(String name, Class<T> klass, Value<T> defaultValue, Collection<Value<T>> values) {
        options.add(Option.choiceOption(name, klass, defaultValue, values));
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param values
     *                     the values this option can take
     */
    @SafeVarargs
    public final <T> void addOption(String name, Class<T> klass, String defaultValue, Value<T>... values) {
        addOption(name, klass, defaultValue, Arrays.asList(values));
    }

    /**
     * Add option to set.
     *
     * @param              <T>
     *                     the parameter type
     * @param name
     *                     the option's name
     * @param klass
     *                     the type of the option's parameter value
     * @param defaultValue
     *                     the option's default value
     * @param values
     *                     the values this option can take
     */
    public final <T> void addOption(String name, Class<T> klass, String defaultValue, Collection<Value<T>> values) {
        // find default by name
        Value<T> defaultChoice = null;
        for (Value<T> choice : values) {
            // use first entry as default if default not found
            if (defaultChoice == null) {
                defaultChoice = choice;
            }
            // and of course use default if found
            if (String.valueOf(choice).equals(defaultValue)) {
                defaultChoice = choice;
                break;
            }
        }

        options.add(Option.choiceOption(name, klass, defaultChoice, values));
    }

    /**
     * Get options as list.
     *
     * @return
     *         this set's options as a list
     */
    public List<Option<?>> asList() {
        return new ArrayList<>(options);
    }

    /**
     * Get option by name
     *
     * @param  name
     *              the option name
     * @return
     *              Optional containing the option
     */
    public Optional<Option<?>> getOption(String name) {
        for (Option<?> o : options) {
            if (o.getName().equals(name)) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    /**
     * Get value for an option.
     * <p>
     * If {@code overrides} contains a value for the option, this value is used.
     * Otherwise, the option's default value is used if present.
     *
     * @param  name
     *                   the option name
     * @param  overrides
     *                   the supplied option values
     * @return
     *                   the option's value, or {@code null} if no value is present
     */
    // TODO Optional?
    public Object getOptionValue(String name, OptionValues overrides) {
        Optional<Option<?>> option = getOption(name);

        // the requested option does not exist
        if (!option.isPresent()) {
            return null;
        }

        Option<?> op = option.get();
        Supplier<?> value = overrides.get(op);
        return value.get();
    }

    @Override
    public String toString() {
        try (Formatter fmt = new Formatter()) {
            fmt.format("{%n");
            for (var option : options) {
                fmt.format("  %s%n", option);
            }
            fmt.format("}%n");
            return fmt.toString();
        }
    }

    public int size() {
        return options.size();
    }

    @Override
    public Iterator<Option<?>> iterator() {
        return options.iterator();
    }

    public boolean isEmpty() {
        return options.isEmpty();
    }
}
