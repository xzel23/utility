package com.dua3.utility.options;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class OptionBuilder<T> extends AbstractOptionBuilder<T, OptionBuilder<T>> {
    /**
     * Constructs an instance of OptionBuilder with the provided parameters.
     *
     * @param argumentsParserBuilder an optional {@link ArgumentsParserBuilder} instance that can be used to parse arguments,
     *                               or null if no argument parser is required
     * @param displayName            the display name of the option being built
     * @param description            a description or detail for the option being built
     * @param targetType             the class type of the target object for this option
     */
    protected OptionBuilder(
            @Nullable ArgumentsParserBuilder argumentsParserBuilder,
            String displayName,
            String description,
            Class<T> targetType
    ) {
        super(argumentsParserBuilder, displayName, description, targetType);
    }

    /**
     * Returns a function that maps an array of objects to a list of strings.
     * The method assumes each element of the provided array is a list of objects.
     * It converts each object in the nested lists to its string representation.
     *
     * @return a function that transforms an object array into a list of strings
     */
    public static Function<Object[], List<String>> toStringListMapper() {
        return list -> Arrays.stream(list).map(x -> ((List<Object>) x)).flatMap(list2 -> list2.stream().map(String::valueOf))
                .toList();
    }

    @Override
    public OptionBuilder<T> param(Param<?>... param) {
        return super.param(param);
    }

    @Override
    public OptionBuilder<T> optionalParam(Param<?>... param) {
        return super.optionalParam(param);
    }

    @Override
    public OptionBuilder<T> mapper(Function<Object[], T> mapper) {
        return super.mapper(mapper);
    }

    @Override
    public OptionBuilder<T> repetitions(Repetitions repetitions) {
        super.repetitions(repetitions);
        return self();
    }

    @Override
    public OptionBuilder<T> defaultSupplier(Supplier<@Nullable T> defaultSupplier) {
        return super.defaultSupplier(defaultSupplier);
    }

}
