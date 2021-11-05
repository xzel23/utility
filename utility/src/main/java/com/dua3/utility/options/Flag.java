package com.dua3.utility.options;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * A flag class, which is in principle a boolean option. 
 * 
 * A flag can be given at most once on a command line. It can be queried by calling {@link Arguments#isSet(Flag)}.
 */
public final class Flag extends Option<Boolean> {

    public static @NotNull Flag create(@NotNull String @NotNull ... names) {
        return new Flag(names);
    }
    
    /**
     * Construct a new flag with the given name(s).
     * @param names names for the flag, at least one.
     */
    private Flag(@NotNull String @NotNull [] names) {
        super(Flag::mapToBoolean, b -> Boolean.toString(b), names);
        occurence(0,1);
        arity(0,0);
    }

    @Override
    public @NotNull Flag description(@NotNull String description) {
        super.description(description);
        return this;
    }

    @Override
    public @NotNull Flag handler(@NotNull Consumer<Collection<Boolean>> handler) {
        super.handler(handler);
        return this;
    }

    private static @NotNull Boolean mapToBoolean(@NotNull String s) {
        if (s.equalsIgnoreCase("true")) {
            return true;
        }
        if (s.equalsIgnoreCase("false")) {
            return false;
        }
        throw new IllegalArgumentException("invalid boolean value: "+s);
    }
    
}
