package com.dua3.utility.options;

import com.dua3.cabe.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Command line or configuration option.
 * @param <T> the type of option values
 */
public abstract class Option<T> {
    private final Function<String, ? extends T> mapper;
    private final Function<?super T, String> formatter;
    
    private final String[] names;
    
    String displayName = "";
    String description = "";
    int minArity = 0;
    int maxArity = 0;
    int minOccurrences = 0;
    int maxOccurrences = Integer.MAX_VALUE;
    Consumer<Collection<T>> handler = values -> {};

    protected Option(Function<String, ? extends T> mapper,
                     Function<?super T, String> formatter,
                     String... names) {
        LangUtil.check(names.length > 0, "at least one name must be given");

        this.mapper = Objects.requireNonNull(mapper);
        this.formatter = Objects.requireNonNull(formatter);
        this.names = names.clone();
    }

    protected Option<T> arity(int minArity, int maxArity) {
        LangUtil.check(minArity >= 0, "min arity is negative");
        LangUtil.check(minArity <= maxArity, "min arity > max arity");
        LangUtil.check(this.minArity == 0 && this.maxArity == 0, "arity already set");

        this.minArity = minArity;
        this.maxArity = maxArity;
        
        return this;
    }

    protected Option<T> occurrence(int min, int max) {
        LangUtil.check(min >= 0, "minimum occurrences is negative");
        LangUtil.check(min <= max, "minimum occurrences > max occurrences");
        LangUtil.check(minOccurrences == 0 || minOccurrences == min, "minOccurrences already set");
        LangUtil.check(maxOccurrences == Integer.MAX_VALUE || maxOccurrences == max, "maxOccurrences already set");

        this.minOccurrences = min;
        this.maxOccurrences = max;

        return this;
    }

    /**
     * Set description.
     * @param description the description
     * @return this option
     */
    protected Option<T> description(String description) {
        LangUtil.check(this.description.isEmpty(), "description already set");
        this.description = Objects.requireNonNull(description, "description must not be null");
        return this;
    }

    /**
     * Set display name
     * @param displayName display name
     * @return this option
     */
    protected Option<T> displayName(String displayName) {
        LangUtil.check(this.displayName.isEmpty(), "displayName already set");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        return this;
    }

    /**
     * Set handler for this option.
     * @param handler the handler to call in {@link Arguments#handle()} for each invocation of this option
     * @return this option
     */
    public Option<T> handler(Consumer<Collection<T>> handler) {
        this.handler = Objects.requireNonNull(handler);
        return this;
    }

    /**
     * Get this option's handler.
     * @return the handler for this option (always non-null, options are initialised with a no-op handler)
     */
    public Consumer<Collection<T>> handler() {
        return handler;
    }

    void handle(Collection<T> values) {
        handler.accept(values);
    }

    /**
     * Map String to option type.
     * @param s the String
     * @return {@code s} mapped to the option type
     */
    public T map(String s) {
        try {
            return mapper.apply(s);
        } catch (Exception e) {
            throw new OptionException.ParameterConversionException(this, s, e);
        }
    }

    /**
     * Get option name.
     * @return the name of this option
     */
    public String name() {
        return names[0];
    }

    /**
     * Get all names for this option
     * @return collection containing all names for this option
     */
    public Collection<String> names() {
        return List.of(names);
    }

    /**
     * Get minimum number of occurrences of this option.
     * @return minimum occurrences
     */
    public int minOccurrences() {
        return minOccurrences;
    }

    /**
     * Get maximum number of occurrences of this option.
     * @return maximum occurrences
     */
    public int maxOccurrences() {
        return maxOccurrences;
    }

    /**
     * Get minimum arity of this option.
     * @return minimum arity
     */
    public int minArity() {
        return minArity;
    }

    /**
     * Get maximum arity of this option.
     * @return maximum arity
     */
    public int maxArity() {
        return maxArity;
    }

    /**
     * Get description for this option.
     * @return description
     */
    public String description() {
        return description;
    }

    /**
     * Get display name of this option.
     * @return display name
     */
    public String displayName() {
        return displayName.isEmpty() ? names[0] : displayName;
    }

    /**
     * Format option value to {@link String}.
     * @param v the value
     * @return String representation of value
     */
    public String format(T v) {
        return formatter.apply(v);
    }
    
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option<?> option = (Option<?>) o;
        return Arrays.equals(names, option.names);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(names);
    }

}
