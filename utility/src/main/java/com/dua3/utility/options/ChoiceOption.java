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

        private Choice(T value, String text) {
            this.value=value;
            this.text=text;
        }

        public T value() {
            return value;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Choice<?> choice = (Choice<?>) o;
            return Objects.equals(value, choice.value) && Objects.equals(text, choice.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, text);
        }
    }

    private final Supplier<? extends T> defaultValue;
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
        super(parser, formatter, names);
        occurence(0,1);
        arity(1,1);
        this.values = Objects.requireNonNull(values);
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.values = Objects.requireNonNull(values);
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.get());
    }

    public Collection<Choice<T>> choices() {
        return values().stream().map(this::choice).collect(Collectors.toUnmodifiableList());
    }
    
    public Choice<T> choice(T v) {
        return new Choice<>(v, format(v));
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
