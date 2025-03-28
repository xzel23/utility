package com.dua3.utility.options;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@link Option} implementation that only allows a single argument out of a limited set of possible values.
 *
 * @param <T> the value type
 */
public final class ChoiceOption<T> extends Option<T> {

    private final Supplier<? extends Collection<? extends T>> values;
    private Supplier<? extends @Nullable T> defaultSupplier = () -> null;

    /**
     * Constructor.
     *
     * @param valueMapper the mapper that maps strings to values
     * @param formatter   the formatter that creates strings from values
     * @param values      list of valid strings
     * @param names       the option names
     */
    private ChoiceOption(Function<String, ? extends T> valueMapper,
                         Function<? super T, String> formatter,
                         Supplier<? extends Collection<? extends T>> values,
                         String... names) {
        super(valueMapper, formatter, names);
        occurrence(0, 1);
        arity(1, 1);
        this.values = values;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> E valueOf(Class<? extends E> cls, String s) {
        try {
            return (E) cls.getMethod("valueOf", String.class).invoke(null, s);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Enum<E>> Collection<E> enumValues(Class<? extends E> cls) {
        try {
            return List.of((E[]) cls.getMethod("values").invoke(null));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Create a choice option of enum-type with the enum values as possible option values.
     *
     * @param <E>   the enum type
     * @param cls   the enum class
     * @param names the option names
     * @return choice option
     */
    public static <E extends Enum<E>> ChoiceOption<E> create(Class<? extends E> cls,
                                                             String... names) {
        Function<String, E> parser = s -> valueOf(cls, s);
        Function<E, String> formatter = Object::toString;
        Supplier<Collection<E>> values = () -> enumValues(cls);
        return new ChoiceOption<>(parser, formatter, values, names);
    }

    /**
     * Create a choice option that maps strings in a list to values.
     *
     * @param <T>         the option type
     * @param valueMapper the mapper that maps strings to values
     * @param formatter   the formatter that creates strings from values
     * @param values      list of valid strings
     * @param names       the option names
     * @return choice option
     */
    public static <T extends @Nullable Object> ChoiceOption<T> create(Function<String, ? extends T> valueMapper,
                                                                      Function<? super T, String> formatter,
                                                                      Supplier<? extends Collection<? extends T>> values,
                                                                      String... names) {
        return new ChoiceOption<>(valueMapper, formatter, values, names);
    }

    /**
     * Get possible values.
     *
     * @return collection holding the possible values
     */
    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.get());
    }

    /**
     * Get the collection of choices.
     *
     * @return collection holding the possible choices
     */
    public Collection<Choice<T>> choices() {
        return values().stream().map(this::choice).toList();
    }

    /**
     * Get choices.
     *
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
    public ChoiceOption<T> displayName(String displayName) {
        super.displayName(displayName);
        return this;
    }

    @Override
    public ChoiceOption<T> handler(Consumer<Collection<T>> handler) {
        super.handler(handler);
        return this;
    }

    /**
     * Set default value.
     *
     * @param defaultValue the default value
     * @return this option
     */
    public ChoiceOption<T> defaultValue(@Nullable T defaultValue) {
        return defaultSupplier(() -> defaultValue);
    }

    /**
     * Set default value.
     *
     * @param defaultSupplier the default value
     * @return this option
     */
    public ChoiceOption<T> defaultSupplier(Supplier<? extends @Nullable T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
        return this;
    }

    /**
     * Mark option as required.
     *
     * @return this option
     */
    public ChoiceOption<T> required() {
        occurrence(1, 1);
        return this;
    }

    @Override
    public ChoiceOption<T> argName(String argName) {
        super.argName(argName);
        return this;
    }

    /**
     * Get the default value.
     *
     * @return Optional holding the default value.
     */
    public Optional<T> getDefault() {
        return Optional.ofNullable(defaultSupplier.get());
    }

    /**
     * A Choice is for a {@link ChoiceOption}, basically an object holding a combination of a value and its
     * string representation for a selectable value in a choice option.
     *
     * @param value the value to use
     * @param text the text to show
     * @param <T> the value type
     */
    public record Choice<T extends @Nullable Object>(T value, String text) {
        @Override
        public String toString() {
            return text;
        }
    }

}
