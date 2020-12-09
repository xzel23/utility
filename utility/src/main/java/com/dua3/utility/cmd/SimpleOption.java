package com.dua3.utility.cmd;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.lang.LangUtil;

import java.util.Objects;
import java.util.function.Function;

/**
 * A simple option class. 
 *
 * A simple option can be given at most once on a command line and takes exactly one parameter.
 * Its value can be queried by calling {@link CmdArgs#get(SimpleOption)}.
 */
public class SimpleOption<T> extends Option<T> {

    private T defaultValue = null;
    
    /**
     * Construct a new simple option with the given name(s).
     * @param mapper the mapping function to the target type
     * @param names names for the flag, at least one.
     */
    SimpleOption(Function<String,T> mapper, String[] names) {
        super(mapper, names);
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
        LangUtil.check(this.defaultValue==null, "default value has already been set");
        this.defaultValue = Objects.requireNonNull(defaultValue);
        return this;
    }
    
    T getDefault() {
        return defaultValue;
    }
}
