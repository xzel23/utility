package com.dua3.utility.cmd;

import com.dua3.utility.lang.LangUtil;

import java.util.*;

/**
 * Command line option.
 */
public abstract class AbstractOption implements Option {
    private final String[] names;
    String description = "";
    int minArity = 0;
    int maxArity = 0;
    int minOccurrences = 0;
    int maxOccurrences = Integer.MAX_VALUE;

    protected AbstractOption(String... names) {
        LangUtil.check(names.length > 0, "at least one name must be given");
        this.names = names.clone();
    }

    protected Option arity(int minArity, int maxArity) {
        LangUtil.check(minArity >= 0, "min arity is negative");
        LangUtil.check(minArity <= maxArity, "min arity > max arity");
        LangUtil.check(this.minArity == 0 && this.maxArity == 0, "arity already set");

        this.minArity = minArity;
        this.maxArity = maxArity;
        
        return this;
    }

    protected Option occurence(int min, int max) {
        LangUtil.check(min >= 0, "minimum occurences is negative");
        LangUtil.check(min <= max, "minimum occurrences > max occurrences");
        LangUtil.check(minOccurrences == 0 && maxOccurrences == Integer.MAX_VALUE, "occurrences already set");

        this.minOccurrences = min;
        this.maxOccurrences = max;

        return this;
    }

    protected Option description(String description) {
        LangUtil.check(this.description.isEmpty(), "description already set");
        this.description = Objects.requireNonNull(description, "description must not be null");
        return this;
    }

    public String name() {
        return names[0];
    }
    
    @Override
    public Collection<String> names() {
        return Collections.unmodifiableList(Arrays.asList(names));
    }

    @Override
    public int minOccurrences() {
        return minOccurrences;
    }

    @Override
    public int maxOccurrences() {
        return maxOccurrences;
    }

    @Override
    public int minArity() {
        return minArity;
    }

    @Override
    public int maxArity() {
        return maxArity;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractOption option = (AbstractOption) o;
        return Arrays.equals(names, option.names);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(names);
    }
}
