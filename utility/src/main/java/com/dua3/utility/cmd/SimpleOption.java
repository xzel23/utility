package com.dua3.utility.cmd;

import com.dua3.utility.lang.LangUtil;

import java.util.Objects;

/**
 * A simple option class. 
 *
 * A simple option can be given at most once on a command line and takes exactly one parameter.
 * Its value can be queried by calling {@link CmdArgs#get(SimpleOption)}.
 */
public class SimpleOption extends AbstractOption {

    private String defaultValue = null;
    
    /**
     * Construct a new simple option with the given name(s).
     * @param names names for the flag, at least one.
     */
    public SimpleOption(String[] names) {
        super(names);
        occurence(0,1);
        arity(1,1);
    }
    
    @Override
    public SimpleOption description(String description) {
        super.description(description);
        return this;
    }
    
    public SimpleOption defaultValue(String defaultValue) {
        LangUtil.check(this.defaultValue==null, "default value has already been set");
        this.defaultValue = Objects.requireNonNull(defaultValue);
        return this;
    }

    public String defaultValue() {
        return defaultValue;
    }
    
}
