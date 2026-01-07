package com.dua3.utility.options;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Abstract builder for creating Option instances.
 * Provides a fluent API for configuring and building Option objects.
 *
 * @param <T> the type of the option value
 * @param <B> the concrete builder type (for fluent interface)
 */
public abstract class AbstractOptionBuilder<T, B extends AbstractOptionBuilder<T, B>> {
    private final @Nullable ArgumentsParserBuilder parserBuilder;
    private final String displayName;
    private final String description;
    private final Class<T> targetType;
    private @Nullable Function<Object[], @Nullable T> mapper;
    private Supplier<@Nullable T> defaultSupplier = () -> null;
    private Consumer<T> handler = stream -> {};
    private @Nullable List<Param<?>> requiredParams;
    private @Nullable List<Param<?>> optionalParams;
    private Repetitions repetitions = Repetitions.ZERO_OR_ONE;

    /**
     * Constructs an instance of {@code AbstractOptionBuilder} with the specified arguments.
     *
     * @param parserBuilder the {@code ArgumentsParserBuilder} instance to associate with this builder,
     *                      or {@code null} if no parser builder is needed.
     * @param displayName   a user-friendly name for the option.
     * @param description   a detailed description of the option's purpose or behavior.
     * @param targetType    the {@code Class} object representing the target type to which the option will be mapped.
     */
    protected AbstractOptionBuilder(@Nullable ArgumentsParserBuilder parserBuilder, String displayName, String description, Class<T> targetType) {
        this.parserBuilder = parserBuilder;
        this.displayName = displayName;
        this.description = description;
        this.targetType = targetType;
    }

    /**
     * Returns this builder instance cast to the concrete builder type.
     *
     * @return this builder instance
     */
    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    /**
     * Sets the mapper function for converting arguments to the target type.
     *
     * @param mapper the mapper function
     * @return this builder instance
     */
    protected B mapper(Function<Object[], T> mapper) {
        this.mapper = mapper;
        return self();
    }

    /**
     * Sets the handler for processing option values.
     *
     * @param handler the handler function
     * @return this builder instance
     */
    public final B handler(Consumer<T> handler) {
        this.handler = handler;
        return self();
    }

    /**
     * Sets the parameters of this option.
     * <p>
     * This method sets all of the parameters as required.
     *
     * @param param the required parameter
     * @return this builder instance
     */
    protected B param(Param<?>... param) {
        LangUtil.check(requiredParams == null, "required parameters have already been set for the option %s", displayName);
        this.requiredParams = LangUtil.asUnmodifiableList(param);
        return self();
    }

    /**
     * Sets the parameters of this option.
     * <p>
     * This method sets all of the parameters as optional.
     *
     * @param param the required parameter
     * @return this builder instance
     */
    protected B optionalParam(Param<?>... param) {
        LangUtil.check(optionalParams == null, "optional parameters have already been set for the option %s", displayName);
        this.optionalParams = LangUtil.asUnmodifiableList(param);
        return self();
    }

    /**
     * Sets the repetitions constraint for this option.
     *
     * @param repetitions the repetition constraints specifying minimum and maximum repetitions
     * @return this builder instance
     */
    protected B repetitions(Repetitions repetitions) {
        this.repetitions = repetitions;
        return self();
    }

    /**
     * Retrieves the repetitions constraint for this option.
     * <p>
     * The repetitions constraint specifies the minimum and maximum number of times
     * the option can be provided. This is represented as a {@link Repetitions} record.
     *
     * @return the {@link Repetitions} constraint indicating allowed repetitions for this option
     */
    public Repetitions repetitions() {
        return repetitions;
    }

    /**
     * Sets the default supplier for the option. The default supplier provides a value
     * when no explicit value is supplied for the option.
     *
     * @param defaultSupplier the supplier function to provide a default value,
     *                        or null if no default value is needed
     * @return this builder instance
     */
    protected B defaultSupplier(Supplier<@Nullable T> defaultSupplier) {
        this.defaultSupplier = defaultSupplier;
        return self();
    }

    /**
     * Determines whether any parameters (required or optional) have been set for this option.
     * <p>
     * The method checks if the required or optional parameters associated with the option
     * are non-null and returns true if either is set.
     *
     * @return {@code true} if either required or optional parameters are set, otherwise {@code false}.
     */
    protected boolean isParameterSet() {
        return requiredParams != null || optionalParams != null;
    }

    /**
     * Builds and returns an {@link Option} with the specified switches and configurations.
     * Validates the provided switches and automatically configures the option if applicable.
     *
     * @param firstSwitch the primary switch for the option
     * @param moreSwitches additional switches associated with the option
     * @return a configured {@link Option} instance based on the provided parameters and configurations
     * @throws IllegalStateException if the required parameters or mapper function are not set
     */
    @SuppressWarnings("unchecked")
    public Option<T> build(String firstSwitch, String... moreSwitches) {
        LangUtil.check(isParameterSet(), "parameters not set");

        LinkedHashSet<String> switches = LinkedHashSet.newLinkedHashSet(moreSwitches.length + 1);
        switches.add(firstSwitch);
        switches.addAll(LangUtil.asUnmodifiableList(moreSwitches));
        LangUtil.check(switches.size() == 1 + moreSwitches.length, "duplicate switches detected for option %s", displayName);

        // automatically create a mapper for options that take a single required parameter
        if (mapper == null && requiredParams != null && requiredParams.size() == 1 && optionalParams == null) {
            var param = requiredParams.getFirst();
            if (param.argRepetitions() == Repetitions.EXACTLY_ONE && param.targetType() == targetType) {
                mapper = list -> {
                    LangUtil.check(list.length == 1, "internal error: wrong number of arguments for required parameter " + param.displayName());
                    return targetType.cast(list[0]);
                };
            }
        }

        // automatically create a mapper for options that return String
        if (mapper == null && targetType == String.class || targetType == String[].class) {
            mapper = list -> {
                if (list == null) {
                    return null;
                }

                List<Param<?>> allParams = getAllParams();

                List<String> result = new ArrayList<>(allParams.size());
                for (int j = 0; j < allParams.size(); j++) {
                    Param<?> p = allParams.get(j);
                    Converter<String[], Object> converter = (Converter<String[], Object>) p.converter();
                    String[] args = converter.convertBack(list[j]);
                    assert args != null;
                    result.addAll(LangUtil.asUnmodifiableList(args));
                }

                // T is either String or String[]
                if (targetType == String.class) {
                    return (T) String.join(" ", result);
                } else {
                    return (T) result;
                }
            };
        }

        LangUtil.check(mapper != null, "mapper function not set");

        Option<T> option = new Option<>(
                displayName,
                description,
                repetitions,
                targetType,
                switches.toArray(String[]::new),
                mapper,
                defaultSupplier,
                handler,
                LangUtil.orElse(requiredParams, Collections.emptyList()),
                LangUtil.orElse(optionalParams, Collections.emptyList())
        );

        if (parserBuilder != null) {
            parserBuilder.addOption(option);
        }

        return option;
    }

    /**
     * Retrieves all parameters associated with this option, including both required and optional parameters.
     * If there are no required or optional parameters or both are null, an empty list is returned.
     *
     * @return a non-null list containing all required and optional parameters for this option
     */
    private List<Param<?>> getAllParams() {
        List<Param<?>> allParams = new ArrayList<>();
        if (requiredParams != null) {
            allParams.addAll(requiredParams);
        }
        if (optionalParams != null) {
            allParams.addAll(optionalParams);
        }
        return allParams;
    }
}
