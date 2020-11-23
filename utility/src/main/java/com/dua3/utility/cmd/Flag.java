package com.dua3.utility.cmd;

/**
 * A flag class. 
 * 
 * A flag can be given at most once on a command line. It can be queried by calling {@link CmdArgs#isSet(Flag)}.
 */
public class Flag extends AbstractOption {

    /**
     * Construct a new flag with the given name(s).
     * @param names names for the flag, at least one.
     */
    public Flag(String[] names) {
        super(names);
        occurence(0,1);
        arity(0,0);
    }

    @Override
    public Flag description(String description) {
        super.description(description);
        return this;
    }
}
