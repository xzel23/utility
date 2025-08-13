package com.dua3.utility.options;

import com.dua3.utility.data.ConversionException;
import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a command-line or configurable option with various attributes.
 * An Option can define its display name, description, value type, default supplier,
 * associated switches, and the parameters it expects. It can also be a flag,
 * a simple parameter, or a list parameter, depending on its configuration.
 *
 * @param <T> the type of value associated with this Option
 */
public class Option<T extends @Nullable Object> {
    private final String displayName;
    private final String description;
    private final Repetitions repetitions;
    private final Class<T> targetType;
    private final String[] switches;
    private final Function<Object[], T> mapper;
    private final Consumer<T> handler;
    private final List<Param<?>> param;
    private final int requiredArgCount;
    private final Supplier<? extends @Nullable T> defaultSupplier;

    /**
     * Creates an {@code Option} representing a flag, which is a boolean option that can either
     * be enabled or disabled. Flags do not have additional parameters and can be specified
     * using one or more switches (e.g., command-line arguments).
     *
     * @param displayName the descriptive name of the flag, typically used for documentation purposes
     * @param description a brief description explaining the purpose or functionality of the flag
     * @param firstSwitch the primary switch associated with the flag (e.g., "--flag" or "-f")
     * @param moreSwitches additional switches that can be used as aliases for the flag
     * @return an {@code Option<Boolean>} instance configured as a flag
     */
    public static Option<Boolean> createFlag(
            String displayName,
            String description,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new FlagBuilder(null, displayName, description)
                .repetitions(Repetitions.ZERO_OR_ONE)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Creates an {@code Option} representing a customizable and reusable parameter
     * with an associated name, description, and default value.
     *
     * @param <T> the type of the parameter value
     * @param displayName the human-readable name of the option, typically used for display and documentation
     * @param description a brief text describing the purpose or functionality of the option
     * @param param the parameter instance associated with this option
     * @param defaultSupplier a supplier providing the default value for the option; it can supply {@code null}
     * @param firstSwitch the primary switch associated with the option (e.g., command-line flag or alias)
     * @param moreSwitches additional switches or aliases for the option
     * @return an {@code Option<T>} instance configured with the specified properties
     */
    public static <T> Option<T> createSimpleOption(
            String displayName,
            String description,
            Param<T> param,
            Supplier<@Nullable T> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<T>(null, displayName, description, param.targetType())
                .param(param)
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Creates a selection option that associates display attributes, type information,
     * allowed values, a converter for parsing input, and default supplier logic.
     *
     * @param <T> The type of the option's selectable values.
     * @param displayName The name displayed for the option.
     * @param description A description providing details about the option.
     * @param targetType The type of values that the option accepts.
     * @param argName The name used for the argument associated with this option.
     * @param allowedValues A list of predefined values that can be selected for this option.
     *                       Can include null if the list permits nullable values.
     * @param converter A converter that transforms a string input into the expected type.
     * @param defaultSupplier A supplier function to provide a default value if no specific value is provided.
     * @param firstSwitch The first switch string to use for this option (e.g., "--flag").
     * @param moreSwitches Additional switches that can be used as aliases for the option.
     * @return A configured {@link Option} instance encapsulating the provided selection data and behavior.
     */
    public static <T> Option<T> createSelectionOption(
            String displayName,
            String description,
            Class<T> targetType,
            String argName,
            List<@Nullable T> allowedValues,
            Converter<String, @Nullable T> converter,
            Supplier<@Nullable T> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        Param<T> param = Param.ofConstants(
                displayName,
                description,
                argName,
                Param.Required.REQUIRED,
                targetType,
                converter,
                allowedValues
        );
        return new OptionBuilder<T>(null, displayName, description, targetType)
                .param(param)
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Creates an {@link Option} for handling enum-type parameters. This method simplifies
     * the creation and configuration of options that are based on enumerations.
     *
     * @param <E> The type of the enum.
     * @param displayName The user-facing name of the option.
     * @param description A description of the option's purpose or usage.
     * @param targetType The enum class type to which this option corresponds.
     * @param argName The argument name used to represent the option in a command-line or configuration context.
     * @param defaultSupplier A supplier that provides the default enum value for this option. Can be {@code null}.
     * @param firstSwitch The primary switch name(s) or flag(s) for the option.
     * @param moreSwitches Additional switch names or flags that the option can be identified with.
     * @return An {@link Option} configured for the specified enum type.
     */
    public static <E extends Enum<E>> Option<E> createEnumOption(
            String displayName,
            String description,
            Class<E> targetType,
            String argName,
            Supplier<@Nullable E> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        Param<E> param = Param.ofEnum(
                displayName,
                description,
                argName,
                Param.Required.REQUIRED,
                targetType
        );
        return new OptionBuilder<>(null, displayName, description, targetType)
                .param(param)
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Constructs an Option with the specified properties.
     * This constructor allows defining an option with various characteristics,
     * including its display name, description, repetitions, target type, switches,
     * value mapping logic, default value supplier, handling behavior, and required/optional arguments.
     *
     * @param displayName the human-readable name of the option, typically used for display or documentation
     * @param description a brief description explaining the purpose or functionality of the option
     * @param repetitions an instance specifying the minimum and maximum number of times the option can be repeated
     * @param targetType the expected target type of the option's value
     * @param switches an array of strings representing the command-line switches or aliases associated with the option
     * @param mapper a function responsible for mapping the raw input arguments into the target type
     * @param defaultSupplier a supplier providing the default value for the option; may return {@code null}
     * @param handler a consumer for handling the value passed to the option
     * @param requiredArgs a list of parameters representing the required arguments for this option
     * @param optionalArgs a list of parameters representing the optional arguments for this option
     */
    protected Option(
            String displayName,
            String description,
            Repetitions repetitions,
            Class<T> targetType,
            String[] switches,
            Function<Object[], T> mapper,
            Supplier<@Nullable T> defaultSupplier,
            Consumer<T> handler,
            List<Param<?>> requiredArgs,
            List<Param<?>> optionalArgs
    ) {
        this.displayName = displayName;
        this.description = description;
        this.repetitions = repetitions;
        this.targetType = targetType;
        this.switches = switches;
        this.mapper = mapper;
        this.defaultSupplier = defaultSupplier;
        this.handler = handler;

        this.param = new ArrayList<>();
        param.addAll(requiredArgs);
        this.requiredArgCount = param.size();
        param.addAll(optionalArgs);
    }

    /**
     * Retrieves the list of required parameters associated with this option.
     * The returned list is unmodifiable to ensure that the required parameters
     * cannot be altered.
     *
     * @return an unmodifiable list of required parameters {@code Param<?>} for this option
     */
    public List<Param<?>> getRequiredParameter() {
        return Collections.unmodifiableList(param.subList(0, requiredArgCount));
    }

    /**
     * Retrieves a list of optional parameters defined for this option.
     * Optional parameters are those that are not required and follow
     * the required parameters in the definition.
     *
     * @return an unmodifiable list of optional {@code Param<?>} instances
     *         representing the optional arguments for this option
     */
    public List<Param<?>> getOptionalParameter() {
        return Collections.unmodifiableList(param.subList(requiredArgCount, param.size()));
    }

    /**
     * Retrieves the default value for this {@code Option}, if it has been configured.
     * The default value is provided by the associated supplier, which may return
     * {@code null} if no default is set.
     *
     * @return an {@code Optional} containing the default value if it exists,
     *         or an empty {@code Optional} if no default value is provided
     */
    public Optional<T> getDefault() {
        return Optional.ofNullable(defaultSupplier.get());
    }

    /**
     * Retrieves the default value of this option as an {@code Optional<String>}.
     * If the default value is {@code null}, an empty {@code Optional} is returned.
     * Otherwise, the default value is formatted into a {@code String} and returned as an {@code Optional}.
     *
     * @return an {@code Optional<String>} containing the formatted default value, or an empty {@code Optional} if no default value is set
     */
    public Optional<String> getDefaultString() {
        T d = defaultSupplier.get();
        return d == null ? Optional.empty() : Optional.of(format(d));
    }

    /**
     * Retrieves the {@code Repetitions} instance associated with this {@code Option}.
     * <p>
     * The {@code Repetitions} object specifies the minimum and maximum
     * number of times this option can be repeated when used.
     *
     * @return the {@code Repetitions} instance defining the repetition constraints for this option
     */
    public Repetitions repetitions() {
        return repetitions;
    }

    /**
     * Determines whether this option is mandatory. An option is considered required
     * if the minimum number of repetitions specified by the {@code repetitions} field
     * is greater than zero.
     *
     * @return true if the option is required (minimum repetitions > 0), false otherwise
     */
    public boolean isRequired() {
        return repetitions.min() > 0;
    }

    /**
     * Retrieves the target type associated with this option.
     *
     * @return the {@code Class} instance representing the target type of the option's value
     */
    public Class<T> getTargetType() {
        return targetType;
    }

    /**
     * Retrieves the handler consumer associated with this option.
     * The handler is responsible for processing the value passed to the option.
     *
     * @return the consumer that handles the option's value
     */
    public Consumer<T> handler() {
        return handler;
    }

    /**
     * Maps the given list of argument strings to an {@link Arguments.Entry} instance.
     * <p>
     * This method validates the number of arguments against the expected range,
     * processes each argument using the associated parameter's converter, and
     * creates an entry after applying the mapper function. If the arguments
     * cannot be converted or an instance fails to be created, appropriate
     * exceptions are thrown.
     *
     * @param args the list of strings representing the arguments to be mapped;
     *             the size of the list must match the expected range of arguments
     *             for the option
     * @return an {@link Arguments.Entry} instance constructed using the mapped
     *         argument values and the associated mapper
     * @throws AssertionError if the number of arguments is not within the expected range
     * @throws OptionException if any argument fails to convert to its target type
     * @throws IllegalStateException if an instance of the target type cannot be created
     */
    public Arguments.Entry<T> map(List<String> args) throws ArgumentsException {
        LangUtil.check(
                LangUtil.isBetween(args.size(), minArgs(), maxArgs()),
                () -> new OptionException(this, String.format("wrong argument count for option %s: %d", displayName, args.size()))
        );

        @Nullable Object[] builderArgs = new Object[params().size()];

        int idxArg = 0;
        List<Param<?>> params = params();
        for (int i = 0; i < params.size(); i++) {
            Param<?> p = params.get(i);
            int to = Math.min(args.size(), idxArg + p.argRepetitions().max());
            String[] paramArgs = args.subList(idxArg, to).toArray(String[]::new);
            try {
                builderArgs[i] = p.converter().convert(paramArgs);
            } catch (ConversionException e) {
                throw new OptionException(
                        this,
                        String.format(
                                "could not convert the supplied arguments %s to %s for parameter '%s' of option '%s': %s",
                                Arrays.toString(paramArgs),
                                p.targetType(),
                                p.displayName(),
                                displayName,
                                e.getMessage()
                        ),
                        e
                );
            }
            idxArg += to - idxArg;
        }

        try {
            T value = mapper.apply(builderArgs);
            if (hasAllowedValues() && !allowedValues().contains(value)) {
                throw new ArgumentsException(
                        "The value '" + format(value) + "' is not allowed for this option."
                                + " Allowed values are: " + allowedValues().stream().map(this::format)
                );
            }
            return Arguments.Entry.create(this, value);
        } catch (RuntimeException e) {
            throw new IllegalStateException("could not create an instance of type " + targetType, e);
        }
    }

    /**
     * Retrieves the display name of this option.
     * The display name is a human-readable, descriptive name typically used for documentation and display purposes.
     *
     * @return the display name of the option as a {@code String}
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Retrieves the list of parameters associated with this {@code Option}.
     * The parameters define the characteristics and expected input for the option.
     *
     * @return a list of {@code Param<?>} instances representing the parameters for this option
     */
    public List<Param<?>> params() {
        return param;
    }

    /**
     * Retrieves the list of switches associated with this option.
     * Switches are command-line arguments or aliases that can be used to invoke or configure the option.
     *
     * @return an unmodifiable list of switches as strings for this option
     */
    public List<String> switches() {
        return LangUtil.asUnmodifiableList(switches);
    }

    /**
     * Computes and returns the minimum number of arguments required
     * based on the parameters provided.
     *
     * @return the minimum number of arguments required; returns 0 if there are no parameters
     */
    public int minArgs() {
        if (param.isEmpty()) {
            return 0;
        } else {
            return Math.toIntExact(Math.min(Integer.MAX_VALUE, params().stream()
                    .mapToLong(p -> p.argRepetitions().min())
                    .sum()));
        }
    }

    /**
     * Computes the maximum number of arguments by summing up the maximum repetitions
     * of arguments across all parameters. If there are no parameters, it returns 0.
     *
     * @return the computed maximum number of arguments, constrained by Integer.MAX_VALUE
     */
    public int maxArgs() {
        if (param.isEmpty()) {
            return 0;
        } else {
            return Math.toIntExact(Math.min(Integer.MAX_VALUE, params().stream()
                    .mapToLong(p -> p.argRepetitions().max())
                    .sum()));
        }
    }

    /**
     * Retrieves the description of this option.
     *
     * @return the description of the option as a {@code String}
     */
    public String description() {
        return description;
    }

    /**
     * Formats the given value into its string representation.
     * This method uses {@code String.valueOf()} for conversion.
     *
     * @param v the value to be formatted; may be {@code null}
     * @return the string representation of the given value
     */
    public String format(T v) {
        return String.valueOf(v);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!(o instanceof Option<?> that)) return false;
        return requiredArgCount == that.requiredArgCount
                && Objects.equals(param, that.param)
                && Objects.equals(defaultSupplier, that.defaultSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(param, requiredArgCount, defaultSupplier);
    }

    /**
     * Determines if this option is equivalent to the provided option.
     * Two options are considered equivalent if they have the same target type
     * and their associated names are identical.
     *
     * @param other the option to compare this option against; may be null
     * @return true if the options are equivalent, false otherwise
     */
    public final boolean isEquivalent(@Nullable Option<?> other) {
        if (this == other) return true;
        if (other == null) return false;

        return Objects.equals(displayName, other.displayName)
                && Objects.equals(targetType, other.targetType);
    }

    /**
     * Determines if the current configuration can be considered as a flag.
     * <p>
     * A configuration qualifies as a flag if the target type is Boolean,
     * the allowed number of repetitions is ZERO_OR_ONE, and the maximum
     * number of arguments is zero.
     *
     * @return true if the configuration is a flag; false otherwise
     */
    public boolean isFlag() {
        return targetType == Boolean.class
                && repetitions.equals(Repetitions.ZERO_OR_ONE)
                && maxArgs() == 0;
    }

    /**
     * Determines if the current object has allowed values based on its arguments and parameters.
     * <p>
     * If an option has allowed values, a list of possible values is assigned to the option and
     * only values contained within that list may be used.
     *
     * @return true if the object has exactly one minimum argument, one maximum argument,
     *         and the first parameter of the object has allowed values; false otherwise.
     */
    public boolean hasAllowedValues() {
        return minArgs() == 1 && maxArgs() == 1 && params().getFirst().hasAllowedValues();
    }

    /**
     * Retrieves a list of allowed values for the current parameter, if available.
     * If no allowed values are defined, returns an empty list.
     *
     * @return a list of allowed values of type T, or an empty list if no allowed values are defined
     */
    @SuppressWarnings("unchecked")
    public List<T> allowedValues() {
        if (!hasAllowedValues()) {
            return Collections.emptyList();
        }
        return (List<T>) params().getFirst().allowedValues();
    }
}
