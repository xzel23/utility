package com.dua3.utility.options;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A Set of possible options to configure a classes behavior, i.e. all possible options
 * for configuring a CSV reader.
 */
public class OptionSet {

    private static class StaticValue<T> implements Supplier<T> {

        private final String name;
        private final T value;

        public StaticValue(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static <T> Supplier<T> value(String name, T value) {
        return new StaticValue<>(name, value);
    }

    public static <T> Supplier<T> value(T value) {
        return new StaticValue<>(String.valueOf(value), value);
    }

    public static <T> Supplier<T>[] wrap(T[] choices) {
        @SuppressWarnings("unchecked")
        Supplier<T>[] values = new Supplier[choices.length];
        for (int i = 0; i < choices.length; i++) {
            values[i] = value(choices[i]);
        }
        return values;
    }

    private final Set<Option<?>> options = new LinkedHashSet<>();

    public OptionSet() {
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public <T> void addOption(String name, Class<T> klass, T defaultChoice, T... choices) {
    	Supplier<T> defaultValue = value(String.valueOf(defaultChoice), defaultChoice);
        Supplier[] values = wrap(choices);
        options.add(new Option<>(name, klass, defaultValue, values));
    }

    @SafeVarargs
    public final <T> void addOption(String name, Class<T> klass, Supplier<T> defaultValue, Supplier<T>... values) {
        options.add(new Option<>(name, klass, defaultValue, values));
    }

    public List<Option<?>> asList() {
        return new ArrayList<>(options);
    }

    public Optional<Option<?>> getOption(String name) {
        for (Option<?> o : options) {
            if (o.getName().equals(name)) {
                return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    public Object getOptionValue(String name, Options overrides) {
        Optional<Option<?>> option = getOption(name);

        // the requested option does not exist
        if (!option.isPresent()) {
            return null;
        }

        Option<?> op = option.get();
        Supplier<?> value = overrides.get(op);
        return value.get();
    }

}
