package com.dua3.utility.options;

import java.util.function.Function;

/**
 * A builder class for configuring and constructing list-based options.
 * It extends {@link AbstractOptionBuilder} to provide specific functionalities
 * for handling list options. This class simplifies the configuration of
 * options associated with lists, defining their parsing behavior, constraints, and target type.
 *
 * @param <T> the type of elements in the list option
 */
public class ListOptionBuilder<T> extends AbstractOptionBuilder<T, ListOptionBuilder<T>> {
    /**
     * Constructs a new ListOptionBuilder with the specified arguments parser builder, display name,
     * description, and target type. This builder is used to configure and construct list-based options.
     *
     * @param argumentsParserBuilder the builder used to configure how arguments are parsed
     * @param displayName the display name for the option
     * @param description the description of the option
     * @param targetType the class type of the target element for the option
     */
    protected ListOptionBuilder(
            ArgumentsParserBuilder argumentsParserBuilder,
            String displayName,
            String description,
            Class<T> targetType
    ) {
        super(argumentsParserBuilder, displayName, description, targetType);
        repetitions(Repetitions.ZERO_OR_ONE);
    }

    @Override
    public ListOptionBuilder<T> mapper(Function<Object[], T> mapper) {
        return super.mapper(mapper);
    }

    /**
     * Sets the repetitions constraint for this option, specifying the minimum and maximum
     * number of times the option can be repeated.
     *
     * @param repetitions the repetition constraints specifying the minimum and maximum repetitions
     * @return this builder instance with the updated repetitions constraint
     */
    public ListOptionBuilder<T> repetitions(Repetitions repetitions) {
        return super.repetitions(repetitions);
    }

}
