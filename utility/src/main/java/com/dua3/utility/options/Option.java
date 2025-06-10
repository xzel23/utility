package com.dua3.utility.options;

import org.jspecify.annotations.Nullable;
import com.dua3.utility.lang.LangUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Command line or configuration option.
 *
 * @param <T> the type of option values
 */
@SuppressWarnings("MagicCharacter")
public abstract class Option<T> {
    private final Function<String, ? extends T> mapper;
    private final Function<? super T, String> formatter;

    private final String[] names;

    private String displayName = "";
    private String description = "";
    private String[] argNames = { "arg" };
    private int minArity;
    private int maxArity;
    private int minOccurrences;
    private int maxOccurrences = Integer.MAX_VALUE;
    private Consumer<Collection<T>> handler = values -> {
    };

    /**
     * Represents an option with a set of names, a mapper function to convert from String to the option's type, and a formatter function to convert the option's type to a String.
     *
     * @param mapper    the function to map a String to the option's type
     * @param formatter the function to format the option's type to a String
     * @param names     the names associated with the option
     */
    protected Option(Function<String, ? extends T> mapper,
                     Function<? super T, String> formatter,
                     String... names) {
        LangUtil.check(names.length > 0, "at least one name must be given");

        this.mapper = mapper;
        this.formatter = formatter;
        this.names = names.clone();
    }

    /**
     * Sets the arity (minimum and maximum number of arguments) for this option.
     *
     * @param minArity the minimum number of arguments required
     * @param maxArity the maximum number of arguments allowed
     * @return this option with the arity set
     * @throws IllegalArgumentException if the minimum arity is negative, the minimum arity is greater than the maximum
     *         arity, or arity has already been set
     */
    protected Option<T> arity(int minArity, int maxArity) {
        LangUtil.check(minArity >= 0, "min arity is negative");
        LangUtil.check(minArity <= maxArity, "min arity > max arity");
        LangUtil.check(this.minArity == 0 && this.maxArity == 0, "arity already set");

        this.minArity = minArity;
        this.maxArity = maxArity;

        return this;
    }

    /**
     * Sets the minimum and maximum number of occurrences for this option.
     *
     * @param min the minimum number of occurrences
     * @param max the maximum number of occurrences
     * @return the modified Option with the occurrences set
     * @throws IllegalArgumentException if the minimum occurrences is negative or greater than the maximum occurrences or the number of occurrences have already been set
     */
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
     *
     * @param description the description
     * @return this option
     */
    protected Option<T> description(String description) {
        LangUtil.check(this.description.isEmpty(), "description already set");
        this.description = description;
        return this;
    }

    /**
     * Set the display name.
     *
     * @param displayName the display name
     * @return this option
     */
    protected Option<T> displayName(String displayName) {
        LangUtil.check(!displayName.isEmpty(), "display name must not be empty");
        this.displayName = displayName;
        return this;
    }

    /**
     * Set the argument name.
     *
     * @param remainingArgNames the argument name
     * @return this option
     */
    protected Option<T> argNames(String firstArgName, String... remainingArgNames) {
        LangUtil.check(!firstArgName.isBlank() && Arrays.stream(remainingArgNames).noneMatch(String::isBlank), "argument names must not be blank");
        this.argNames = new String[1 + remainingArgNames.length];
        this.argNames[0] = firstArgName;
        System.arraycopy(remainingArgNames, 0, this.argNames, 1, remainingArgNames.length);
        return this;
    }

    /**
     * Set handler for this option.
     *
     * @param handler the handler to call in {@link Arguments#handle()} for each invocation of this option
     * @return this option
     */
    protected Option<T> handler(Consumer<Collection<T>> handler) {
        this.handler = handler;
        return this;
    }

    /**
     * Get this option's handler.
     *
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
     *
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
     *
     * @return the name of this option
     */
    public String name() {
        return names[0];
    }

    /**
     * Retrieves the display name of this option.
     *
     * @return The display name of this option.
     */
    public String displayName() {
        return displayName.isEmpty() ? names[0] : displayName;
    }

    /**
     * Retrieves the list of argument names associated with this option.
     *
     * @return a list containing the argument names
     */
    public List<String> argNames() {
        return List.of(argNames);
    }

    /**
     * Get all names for this option
     *
     * @return collection containing all names for this option
     */
    public List<String> names() {
        return List.of(names);
    }

    /**
     * Get minimum number of occurrences of this option.
     *
     * @return minimum occurrences
     */
    public int minOccurrences() {
        return minOccurrences;
    }

    /**
     * Get maximum number of occurrences of this option.
     *
     * @return maximum occurrences
     */
    public int maxOccurrences() {
        return maxOccurrences;
    }

    /**
     * Get minimum arity of this option.
     *
     * @return minimum arity
     */
    public int minArity() {
        return minArity;
    }

    /**
     * Get maximum arity of this option.
     *
     * @return maximum arity
     */
    public int maxArity() {
        return maxArity;
    }

    /**
     * Get description for this option.
     *
     * @return description
     */
    public String description() {
        return description;
    }

    /**
     * Format option value to {@link String}.
     *
     * @param v the value
     * @return String representation of value
     */
    public String format(@Nullable T v) {
        return formatter.apply(v);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Option<?> option = (Option<?>) obj;
        return Arrays.equals(names, option.names);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(names);
    }

    /**
     * Get this option's default value.
     * @return Optional containing the default value for this option or empty Optional
     */
    public abstract Optional<T> getDefault();

    /**
     * Get string representation this option's default value.
     * @return Optional containing the string representation of this option's default value or empty Optional
     */
    public Optional<String> getDefaultString() {
        return getDefault().map(this::format);
    }

    /**
     * Checks if this option is required.
     *
     * @return true if this option is required, false otherwise
     */
    public boolean isRequired() {
        return minOccurrences > 0;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", names=" + Arrays.toString(names) +
                ", argNames=" + Arrays.toString(argNames) +
                ", minArity=" + minArity +
                ", maxArity=" + maxArity +
                ", minOccurrences=" + minOccurrences +
                ", maxOccurrences=" + maxOccurrences +
                '}';
    }
}
