package com.dua3.utility.options;

import com.dua3.cabe.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple option class. 
 *
 * A simple option can be present at most once in an {@link Arguments} instance and takes exactly one parameter.
 * Its value can be queried by calling {@link Arguments#get(SimpleOption)}.
 */
public final class SimpleOption<T> extends Option<T> {

    private Supplier<T> defaultValue = () -> null;

    public static <T> SimpleOption<T>  create(@NotNull Function<String, ? extends T> mapper,
                                                       @NotNull String... names) {
        return new SimpleOption<>(mapper, Object::toString, names);
    }
    
    public static <T> SimpleOption<T>  create(@NotNull Function<String, ? extends T> mapper,
                                                       @NotNull Function<? super T, String> formatter,
                                                       @NotNull String... names) {
        return new SimpleOption<>(mapper, formatter, names);
    }
    
    /**
     * Construct a new simple option with the given name(s).
     * @param mapper the mapping function to the target type
     * @param names names for the flag, at least one.
     */
    private SimpleOption(@NotNull Function<String, ? extends T> mapper,
                         @NotNull Function<? super T, String> formatter,
                         @NotNull String... names) {
        super(mapper, formatter, names);
        occurence(0,1);
        arity(1,1);
    }
    
    @Override
    public SimpleOption<T> description(@NotNull String description) {
        super.description(description);
        return this;
    }

    @Override
    public SimpleOption<T> handler(@NotNull Consumer<Collection<T>> handler) {
        super.handler(handler);
        return this;
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public SimpleOption<T> defaultValue(T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public SimpleOption<T> defaultValue(@NotNull Supplier<T> defaultValue) {
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
