package com.dua3.utility.options;

import com.dua3.utility.lang.LangUtil;

import java.util.*;
import java.util.function.Function;

/**
 * Command line or configuration option.
 */
public abstract class Option<T> {
    private final Function<String, ? extends T> mapper;
    private final String[] names;
    
    String displayName = "";
    String description = "";
    int minArity = 0;
    int maxArity = 0;
    int minOccurrences = 0;
    int maxOccurrences = Integer.MAX_VALUE;

    protected Option(Function<String, ? extends T> mapper, String... names) {
        LangUtil.check(names.length > 0, "at least one name must be given");

        this.mapper = Objects.requireNonNull(mapper);
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

    protected Option<T> occurence(int min, int max) {
        LangUtil.check(min >= 0, "minimum occurences is negative");
        LangUtil.check(min <= max, "minimum occurrences > max occurrences");
        LangUtil.check(minOccurrences == 0 && maxOccurrences == Integer.MAX_VALUE, "occurrences already set");

        this.minOccurrences = min;
        this.maxOccurrences = max;

        return this;
    }

    protected Option<T> description(String description) {
        LangUtil.check(this.description.isEmpty(), "description already set");
        this.description = Objects.requireNonNull(description, "description must not be null");
        return this;
    }

    protected Option<T> displayName(String displayName) {
        LangUtil.check(this.displayName.isEmpty(), "displayName already set");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
        return this;
    }

    protected T map(String s) {
        try {
            return mapper.apply(s);
        } catch (Exception e) {
            throw new OptionException.ConversionException(this, s, e);
        }
    }
    
    public String name() {
        return names[0];
    }
    
    public Collection<String> names() {
        return List.of(names);
    }

    public int minOccurrences() {
        return minOccurrences;
    }

    public int maxOccurrences() {
        return maxOccurrences;
    }

    public int minArity() {
        return minArity;
    }

    public int maxArity() {
        return maxArity;
    }

    public String description() {
        return description;
    }

    public String displayName() {
        return displayName.isEmpty() ? names[0] : displayName;
    }
    
    @Override
    public boolean equals(Object o) {
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
