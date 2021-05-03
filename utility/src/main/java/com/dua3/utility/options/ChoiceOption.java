package com.dua3.utility.options;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An {@link Option} implementation that only allows a single argument that out of a limited set of possible values.
 * @param <T>
 */
public final class ChoiceOption<T> extends Option<T> {

    public static class Choice<T> {
        private final T value;
        private final String text;

        Choice(T value, String text) {
            this.value=value;
            this.text=text;
        }

        T value() {
            return value;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private final Supplier<? extends T> defaultValue;
    private final Function<? super T,String> formatter;
    private Supplier<? extends Collection<? extends T>> values;
    
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E valueOf(Class<E> cls, String s) {
        try {
            return (E) cls.getMethod("valueOf", String.class).invoke(s);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    };

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Collection<E> enumValues(Class<E> cls) {
        try {
            return Arrays.asList((E[]) cls.getMethod("values").invoke(null));
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    };

    public static <E extends Enum<E>> ChoiceOption<E> create(Class<E> cls, Supplier<? extends E> defaultValue, String... names) {
        Function<String,E> parser = s -> ChoiceOption.valueOf(cls, s);
        Function<E,String> formatter = Object::toString;
        Supplier<Collection<E>> values = () -> enumValues(cls);
        return new ChoiceOption<E>(parser, formatter, values, defaultValue, names);
    }

    public static <T> ChoiceOption<T> create(Function<String, ? extends T> parser, Function<? super T,String> formatter, Supplier<? extends Collection<? extends T>> values, Supplier<? extends T> defaultValue, String... names) {
        return new ChoiceOption<>(parser, formatter, values, defaultValue, names);
    }

    private ChoiceOption(Function<String,? extends T> parser, Function<? super T,String> formatter, Supplier<? extends Collection<? extends T>> values, Supplier<? extends T> defaultValue,  String... names) {
        super(parser, names);
        occurence(0,1);
        arity(1,1);
        this.values = Objects.requireNonNull(values);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.formatter = Objects.requireNonNull(formatter);
        this.values = Objects.requireNonNull(values);
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.get());
    }

    public Collection<Choice<T>> choices() {
        return values().stream().map(v -> new Choice<>(v, formatter.apply(v))).collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public ChoiceOption<T> description(String description) {
        super.description(description);
        return this;
    }

    public T getDefault() {
        return defaultValue.get();
    }
}
