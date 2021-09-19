package com.dua3.utility.options;

import java.util.Objects;
import java.util.Optional;
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

    public static <T> SimpleOption<T>  create(Function<String, ? extends T> mapper, String... names) {
        return new SimpleOption<>(mapper, Object::toString, names);
    }
    
    public static <T> SimpleOption<T>  create(Function<String, ? extends T> mapper, Function<? super T, String> formatter, String... names) {
        return new SimpleOption<>(mapper, formatter, names);
    }
    
    /**
     * Construct a new simple option with the given name(s).
     * @param mapper the mapping function to the target type
     * @param names names for the flag, at least one.
     */
    private SimpleOption(Function<String, ? extends T> mapper, Function<? super T, String> formatter, String... names) {
        super(mapper, formatter, names);
        occurence(0,1);
        arity(1,1);
    }
    
    @Override
    public SimpleOption<T> description(String description) {
        super.description(description);
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
    public SimpleOption<T> defaultValue(Supplier<T> defaultValue) {
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
