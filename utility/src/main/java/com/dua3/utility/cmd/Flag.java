package com.dua3.utility.cmd;

/**
 * A flag class. 
 * 
 * A flag can be given at most once on a command line. It can be queried by calling {@link CmdArgs#isSet(Flag)}.
 */
public class Flag extends Option<Boolean> {

    /**
     * Construct a new flag with the given name(s).
     * @param names names for the flag, at least one.
     */
    public Flag(String[] names) {
        super(Flag::mapToBoolean, names);
        occurence(0,1);
        arity(0,0);
    }

    @Override
    public Flag description(String description) {
        super.description(description);
        return this;
    }

    private static Boolean mapToBoolean(String s) {
        if (s.equalsIgnoreCase("true")) {
            return true;
        }
        if (s.equalsIgnoreCase("false")) {
            return false;
        }
        throw new IllegalArgumentException("invalid boolean value: "+s);
    }

}
