package com.dua3.utility.options;

import org.jspecify.annotations.Nullable;

/**
 * A builder class for configuring and creating flag-type options that represent
 * a boolean value. This is a specialized implementation of {@code AbstractOptionBuilder}
 * designed for flags, where the presence of the flag indicates a {@code true} value.
 */
public class FlagBuilder extends AbstractOptionBuilder<Boolean, FlagBuilder> {

    /**
     * Constructor.
     *
     * @param displayName the display name for the option
     */
    FlagBuilder(@Nullable ArgumentsParserBuilder argumentsParserBuilder, String displayName, String description) {
        super(argumentsParserBuilder, displayName, description, Boolean.class);
        mapper(values -> Boolean.TRUE);
        param();
        optionalParam();
        repetitions(Repetitions.ZERO_OR_ONE);
    }

    @Override
    public FlagBuilder repetitions(Repetitions repetitions) {
        return super.repetitions(repetitions);
    }
}
