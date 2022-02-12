package com.dua3.utility.options;

import com.dua3.cabe.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@link Option} implementation that only allows a single argument out of a limited set of possible values.
 * @param <T> the value type
 */
public final class ChoiceOption<T> extends Option<T> {

    /**
     * A Choice is for a {@link ChoiceOption}, basically an object holding a combination of a value and its
     * string representation for a selectable value in a choice option.
     *
     * @param <T> the value type
     */
    public record Choice<T>(T value, String text) {
        @Override
        public String toString() {
            return text;
        }
    }

    private Supplier<? extends T> defaultValue = () -> null;
    private final Supplier<? extends Collection<? extends T>> values;
    
    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E valueOf(Class<? extends E> cls, String s) {
        try {
            return (E) cls.getMethod("valueOf", String.class).invoke(null, s);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Collection<E> enumValues(Class<? extends E> cls) {
        try {
            return Arrays.asList((E[]) cls.getMethod("values").invoke(null));
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a choice option of enum type with the enum values as possible option values. 
     * @param <E> the enum type
     * @param cls the enum class
     * @param names the option names
     * @return choice option
     */
    public static <E extends Enum<E>> ChoiceOption<E> create(Class<? extends E> cls, 
                                                                      String... names) {
        Function<String,E> parser = s -> ChoiceOption.valueOf(cls, s);
        Function<E,String> formatter = Object::toString;
        Supplier<Collection<E>> values = () -> enumValues(cls);
        return new ChoiceOption<>(parser, formatter, values, names);
    }

    /**
     * Create a choice option where strings in a list are mapped to values. 
     * @param <T> the option type
     * @param valueMapper the mapper that maps strings to values
     * @param formatter the formatter that creates strings from values
     * @param values list of valid strings
     * @param names the option names
     * @return choice option
     */
    public static <T> ChoiceOption<T> create(Function<String, ? extends T> valueMapper, 
                                                      Function<? super T,String> formatter, 
                                                      Supplier<? extends Collection<? extends T>> values, 
                                                      String... names) {
        return new ChoiceOption<>(valueMapper, formatter, values, names);
    }

    /**
     * Constructor.
     * @param valueMapper the mapper that maps strings to values
     * @param formatter the formatter that creates strings from values
     * @param values list of valid strings
     * @param names the option names
     */
    private ChoiceOption(Function<String,? extends T> valueMapper,
                         Function<? super T,String> formatter,
                         Supplier<? extends Collection<? extends T>> values,
                         String... names) {
        super(valueMapper, formatter, names);
        occurrence(0,1);
        arity(1,1);
        this.values = Objects.requireNonNull(values);
    }

    /**
     * Get possible values.
     * @return collection holding the possible values
     */
    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.get());
    }

    /**
     * Get collection of choices.
     * @return collection holding the possible choices
     */
    public Collection<Choice<T>> choices() {
        return values().stream().map(this::choice).toList();
    }

    /**
     * Get choices.
     * @param v the choice to use in a ChoiceOption
     * @return collection holding the possible choices
     */
    public Choice<T> choice(T v) {
        return new Choice<>(v, format(v));
    }
    
    @Override
    public ChoiceOption<T> description(String description) {
        super.description(description);
        return this;
    }

    @Override
    public ChoiceOption<T> handler(Consumer<Collection<T>> handler) {
        super.handler(handler);
        return this;
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public ChoiceOption<T> defaultValue(@Nullable T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public ChoiceOption<T> defaultValue(Supplier<T> defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, "default value supplier cannot be set to null");
        return this;
    }

    /**
     * Get the default value.
     * @return Optional holding the default value.
     */
    public Optional<T> getDefault() {
        return Optional.ofNullable(defaultValue.get());
    }
    
}
