package com.dua3.utility.options;

import com.dua3.cabe.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple option class. 
 * <p>
 * A simple option can be present at most once in an {@link Arguments} instance and takes exactly one parameter.
 * Its value can be queried by calling {@link Arguments#get(SimpleOption)}.
 * @param <T> the option's argument type.
 */
public final class SimpleOption<T> extends Option<T> {

    private Supplier<T> defaultValue = () -> null;

    public static <T> SimpleOption<T>  create(Function<String, ? extends T> mapper,
                                                       String... names) {
        return new SimpleOption<>(mapper, Object::toString, names);
    }
    
    public static <T> SimpleOption<T>  create(Function<String, ? extends T> mapper,
                                                       Function<? super T, String> formatter,
                                                       String... names) {
        return new SimpleOption<>(mapper, formatter, names);
    }
    
    /**
     * Construct a new simple option with the given name(s).
     * @param mapper the mapping function to the target type
     * @param names names for the flag, at least one.
     */
    private SimpleOption(Function<String, ? extends T> mapper,
                         Function<? super T, String> formatter,
                         String... names) {
        super(mapper, formatter, names);
        occurrence(0,1);
        arity(1,1);
    }
    
    @Override
    public SimpleOption<T> description(String description) {
        super.description(description);
        return this;
    }

    @Override
    public SimpleOption<T> handler(Consumer<Collection<T>> handler) {
        super.handler(handler);
        return this;
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public SimpleOption<T> defaultValue(@Nullable T defaultValue) {
        return defaultValue(() -> defaultValue);
    }

    /**
     * Set default value.
     * @param defaultValue the default value
     * @return this option
     */
    public SimpleOption<T> defaultValue(Supplier<T> defaultValue) {
        this.defaultValue = Objects.requireNonNull(defaultValue, "default value supplier cannot be set to null");
        return this;
    }

    /**
     * Mark option as required.
     * @return this option
     */
    public SimpleOption<T> required() {
        occurrence(1,1);
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
