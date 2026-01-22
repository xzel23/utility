package com.dua3.utility.options;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The ArgumentsParserBuilder class is used to construct an instance of ArgumentsParser with the desired configuration.
 */
public class ArgumentsParserBuilder {

    private static final String DEFAULT_ARG_DISPLAY_NAME = "arg";

    private String name = "";
    private String description = "";
    private String argsDescription = "";
    private final SequencedMap<String, Option<?>> options = new LinkedHashMap<>();
    private int minPositionalArgs = 0;
    private int maxPositionalArgs = Integer.MAX_VALUE;
    private String[] positionalArgDisplayNames = {DEFAULT_ARG_DISPLAY_NAME};

    ArgumentsParserBuilder() {
    }

    /**
     * Sets the name for the ArgumentsParser instance being built.
     *
     * @param name the name for the ArgumentsParser
     * @return the ArgumentsParserBuilder instance
     */
    public ArgumentsParserBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description for the ArgumentsParserBuilder instance being built.
     *
     * @param description the description for the ArgumentsParser
     * @return the ArgumentsParserBuilder instance
     */
    public ArgumentsParserBuilder description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the arguments description to show below the command line in help for the ArgumentsParserBuilder
     * instance being built.
     *
     * @param argsDescription the arguments description for the ArgumentsParser
     * @return the ArgumentsParserBuilder instance
     */
    public ArgumentsParserBuilder argsDescription(String argsDescription) {
        this.argsDescription = argsDescription;
        return this;
    }

    /**
     * Sets the range and display name for the positional arguments.
     *
     * @param minArgs         the minimum number of positional arguments
     * @param maxArgs         the maximum number of positional arguments
     * @param argDisplayNames the display names for the positional arguments
     * @return the ArgumentsParserBuilder instance
     * @throws IllegalArgumentException if the minArgs is less than 0, maxArgs is less than 0, or minArgs is greater than maxArgs
     */
    public ArgumentsParserBuilder positionalArgs(int minArgs, int maxArgs, String... argDisplayNames) {
        if (minArgs < 0 || maxArgs < 0 || minArgs > maxArgs) {
            throw new IllegalArgumentException("Invalid positional arguments range");
        }
        this.minPositionalArgs = minArgs;
        this.maxPositionalArgs = maxArgs;
        this.positionalArgDisplayNames = argDisplayNames;
        return this;
    }

    /**
     * Sets the range and display name for the positional arguments.
     *
     * @param minArgs        the minimum number of positional arguments
     * @param maxArgs        the maximum number of positional arguments
     * @return the ArgumentsParserBuilder instance
     * @throws IllegalArgumentException if the minArgs is less than 0, maxArgs is less than 0, or minArgs is greater than maxArgs
     */
    public ArgumentsParserBuilder positionalArgs(int minArgs, int maxArgs) {
        return positionalArgs(minArgs, maxArgs, DEFAULT_ARG_DISPLAY_NAME);
    }

    /**
     * Creates a new {@link FlagBuilder} that will add the created flag to this builder when its {@code build()} method
     * is called.
     * <p>
     * A flag is a boolean option that can either be present (true) or absent (false) in the arguments.
     *
     * @param displayName the display name of the flag
     * @param description the description of the flag, providing details about its purpose
     * @return a {@code FlagBuilder} instance for further configuration of the flag
     */
    public FlagBuilder flagBuilder(String displayName, String description) {
        return new FlagBuilder(this, displayName, description);
    }

    /**
     * Creates a new {@code OptionBuilder} that will add the created flag to this builder when its {@code build()}
     * method is called.
     *
     * @param <T>        the type of the value the option will accept
     * @param displayName the display name of the option, used for identification during argument parsing
     * @param description the description of the option, providing details about its purpose
     * @param targetType  the type of value the option will handle or accept
     * @return an {@code OptionBuilder} instance for further configuration of the option
     */
    public <T> OptionBuilder<T> optionBuilder(
            String displayName,
            String description,
            Class<T> targetType
    ) {
        return new OptionBuilder<>(this, displayName, description, targetType);
    }

    /**
     * Adds a flag option to the ArgumentsParserBuilder with a specified display name,
     * description, and switches. A flag is a boolean option that is either true
     * (when the flag switch is present) or false (when it is absent).
     *
     * @param displayName the display name of the flag, used for identification and help documentation
     * @param description the description of the flag, providing details about its purpose
     * @param firstSwitch the primary switch used to enable this flag (e.g., "--flag")
     * @param moreSwitches additional switches that can also be used to enable this flag
     * @return an {@code Option<Boolean>} instance representing the flag
     */
    public Option<Boolean> addFlag(
            String displayName,
            String description,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Boolean.class)
                .mapper(args -> Boolean.TRUE)
                .defaultSupplier(() -> Boolean.FALSE)
                .param()
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a flag option to the ArgumentsParserBuilder with a specified display name,
     * description, and switches. A flag is a boolean option that is either true
     * (when the flag switch is present) or false (when it is absent).
     *
     * @param displayName the display name of the flag, used for identification and help documentation
     * @param description the description of the flag, providing details about its purpose
     * @param handler     the handler to call
     * @param firstSwitch the primary switch used to enable this flag (e.g., "--flag")
     * @param moreSwitches additional switches that can also be used to enable this flag
     * @return an {@code Option<Boolean>} instance representing the flag
     */
    public Option<Boolean> addFlag(
            String displayName,
            String description,
            Consumer<Boolean> handler,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Boolean.class)
                .mapper(args -> Boolean.TRUE)
                .defaultSupplier(() -> Boolean.FALSE)
                .handler(handler)
                .param()
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a string-based option to the ArgumentsParserBuilder with various configuration options.
     *
     * @param displayName     the display name of the option, used for identification during argument parsing
     * @param description     the description of the option, providing details about its purpose
     * @param repetitions     specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName         the name of the argument that will appear in usage or help messages
     * @param defaultSupplier a supplier that provides a default value for the option when it is not explicitly specified
     * @param firstSwitch     the primary flag or switch associated with this option (e.g., "--option")
     * @param moreSwitches    additional flags or switches that can also trigger this option
     * @return an {@code Option<String>} instance representing the configured string-based option
     */
    public Option<String> addStringOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable String> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, String.class)
                .repetitions(repetitions)
                .param(Param.ofString(displayName, description, argName, Param.Required.REQUIRED))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a string-based option to the ArgumentsParserBuilder with various configuration options.
     *
     * @param displayName     the display name of the option, used for identification during argument parsing
     * @param description     the description of the option, providing details about its purpose
     * @param repetitions     specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName         the name of the argument that will appear in usage or help messages
     * @param handler         the handler to call
     * @param firstSwitch     the primary flag or switch associated with this option (e.g., "--option")
     * @param moreSwitches    additional flags or switches that can also trigger this option
     * @return an {@code Option<String>} instance representing the configured string-based option
     */
    public Option<String> addStringOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<String> handler,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, String.class)
                .repetitions(repetitions)
                .param(Param.ofString(displayName, description, argName, Param.Required.REQUIRED))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an integer option to the ArgumentsParserBuilder with specified configuration options.
     *
     * @param displayName     the display name of the option, which is used for identification and displayed in help documentation
     * @param description     the description of the option, providing details about its purpose
     * @param repetitions     specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName         the name of the argument that will appear in usage or help messages
     * @param defaultSupplier a supplier that provides a default value for the option when it is not explicitly specified; can be null
     * @param firstSwitch     the primary switch or flag associated with this option (e.g., "--option")
     * @param moreSwitches    additional switches or flags that can also trigger this option
     * @return an {@code Option<Integer>} instance representing the configured integer-based option
     */
    public Option<Integer> addIntegerOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable Integer> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Integer.class)
                .repetitions(repetitions)
                .param(Param.ofInt(displayName, description, argName, Param.Required.REQUIRED))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an integer option to the ArgumentsParserBuilder with specified configuration options.
     *
     * @param displayName     the display name of the option, which is used for identification and displayed in help documentation
     * @param description     the description of the option, providing details about its purpose
     * @param repetitions     specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName         the name of the argument that will appear in usage or help messages
     * @param handler         the handler to call
     * @param firstSwitch     the primary switch or flag associated with this option (e.g., "--option")
     * @param moreSwitches    additional switches or flags that can also trigger this option
     * @return an {@code Option<Integer>} instance representing the configured integer-based option
     */
    public Option<Integer> addIntegerOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<Integer> handler,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Integer.class)
                .repetitions(repetitions)
                .param(Param.ofInt(displayName, description, argName, Param.Required.REQUIRED))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a {@code Path}-based option to the ArgumentsParserBuilder with the specified configuration.
     *
     * @param displayName     the display name of the option, used for identification and displayed in help documentation
     * @param description     the description of the option, providing details about its purpose
     * @param repetitions     specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName         the name of the argument that will appear in usage or help messages
     * @param defaultSupplier a supplier that provides a default value for the option when it is not explicitly specified; can be null
     * @param firstSwitch     the primary switch or flag associated with this option (e.g., "--option")
     * @param moreSwitches    additional switches or flags that can also trigger this option
     * @return an {@code Option<Path>} instance representing the configured {@code Path}-based option
     */
    public Option<Path> addPathOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable Path> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Path.class)
                .repetitions(repetitions)
                .param(Param.ofPath(displayName, description, argName, Param.Required.REQUIRED, Objects::nonNull))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a {@code Path}-based option to the ArgumentsParserBuilder with the specified configuration.
     *
     * @param displayName  the display name of the option, used for identification and displayed in help documentation
     * @param description  the description of the option, providing details about its purpose
     * @param repetitions  specifies how many times the option can or must occur (e.g., REQUIRED, OPTIONAL, etc.)
     * @param argName      the name of the argument that will appear in usage or help messages
     * @param handler      the handler to call
     * @param firstSwitch  the primary switch or flag associated with this option (e.g., "--option")
     * @param moreSwitches additional switches or flags that can also trigger this option
     * @return an {@code Option<Path>} instance representing the configured {@code Path}-based option
     */
    public Option<Path> addPathOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<Path> handler,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, Path.class)
                .repetitions(repetitions)
                .param(Param.ofPath(displayName, description, argName, Param.Required.REQUIRED, Objects::nonNull))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a new URI option to the configuration.
     *
     * @param displayName     the display name of the option
     * @param description     a brief description of what the option does
     * @param repetitions     the repetitions constraint for the option
     * @param argName         the name of the argument representing the URI
     * @param defaultSupplier a supplier that provides a default URI value, can be null
     * @param firstSwitch     the primary switch for the option
     * @param moreSwitches    additional switches for the option
     * @return an {@code Option<URI>} representing the configured URI option
     */
    public Option<URI> addUriOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable URI> defaultSupplier,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, URI.class)
                .repetitions(repetitions)
                .param(Param.ofUri(displayName, description, argName, Param.Required.REQUIRED, Objects::nonNull))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a new URI option to the configuration.
     *
     * @param displayName     the display name of the option
     * @param description     a brief description of what the option does
     * @param repetitions     the repetitions constraint for the option
     * @param argName         the name of the argument representing the URI
     * @param handler         the handler to call
     * @param firstSwitch     the primary switch for the option
     * @param moreSwitches    additional switches for the option
     * @return an {@code Option<URI>} representing the configured URI option
     */
    public Option<URI> addUriOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<URI> handler,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, URI.class)
                .repetitions(repetitions)
                .param(Param.ofUri(displayName, description, argName, Param.Required.REQUIRED, Objects::nonNull))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an enumerated option to the current context.
     *
     * @param <T>               the type of the enum, extending {@link Enum}
     * @param displayName       the display name of the option
     * @param description       the description of the option
     * @param repetitions       the number of times the option can or must be specified
     * @param argName           the argument name expected for the option
     * @param defaultSupplier   a supplier providing the default enum value if none is specified
     * @param targetClass       the enum class representing the possible values
     * @param firstSwitch       the primary switch for the option
     * @param moreSwitches      additional switches for the option
     * @return an {@link Option} object representing the added enumerated option
     */
    public <T extends Enum<T>> Option<T> addEnumOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable T> defaultSupplier,
            Class<T> targetClass,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(Param.ofEnum(displayName, description, argName, Param.Required.REQUIRED, targetClass))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an enumerated option to the current context.
     *
     * @param <T>               the type of the enum, extending {@link Enum}
     * @param displayName       the display name of the option
     * @param description       the description of the option
     * @param repetitions       the number of times the option can or must be specified
     * @param argName           the argument name expected for the option
     * @param handler           the handler to call
     * @param targetClass       the enum class representing the possible values
     * @param firstSwitch       the primary switch for the option
     * @param moreSwitches      additional switches for the option
     * @return an {@link Option} object representing the added enumerated option
     */
    public <T extends Enum<T>> Option<T> addEnumOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<T> handler,
            Class<T> targetClass,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(Param.ofEnum(displayName, description, argName, Param.Required.REQUIRED, targetClass))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an object option to be parsed with specific parameters and switches.
     * This method allows setting a display name, description, argument name, default value,
     * type, and converter for the option. It also specifies the first switch and additional
     * switches associated with this option.
     *
     * @param displayName     the name displayed for the option in usage or help output
     * @param description     a brief explanation of the purpose of the option
     * @param repetitions     the number of times the option may appear or is required;
     *                        it determines if the option is mandatory or optional
     * @param argName         the name used to indicate the argument for the option
     * @param defaultSupplier a supplier that provides a default value if the option is not specified;
     *                        null indicates no default value is provided
     * @param targetClass     the class type of the option's target value
     * @param converter       a converter function used to transform the string argument into the target type
     * @param firstSwitch     the primary switch that triggers this option
     * @param moreSwitches    additional switches that can also be used to trigger this option
     * @param <T> the type of the resulting option value
     * @return an Option object representing the configured option with the specified parameters and switches
     */
    public <T> Option<T> addObjectOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Supplier<@Nullable T> defaultSupplier,
            Class<T> targetClass,
            Converter<String, T> converter,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(Param.ofObject(displayName, description, argName, Param.Required.REQUIRED, targetClass, converter))
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an object option to be parsed with specific parameters and switches.
     * This method allows setting a display name, description, argument name, default value,
     * type, and converter for the option. It also specifies the first switch and additional
     * switches associated with this option.
     *
     * @param displayName     the name displayed for the option in usage or help output
     * @param description     a brief explanation of the purpose of the option
     * @param repetitions     the number of times the option may appear or is required;
     *                        it determines if the option is mandatory or optional
     * @param argName         the name used to indicate the argument for the option
     * @param handler         the handler to call
     * @param targetClass     the class type of the option's target value
     * @param converter       a converter function used to transform the string argument into the target type
     * @param firstSwitch     the primary switch that triggers this option
     * @param moreSwitches    additional switches that can also be used to trigger this option
     * @param <T> the type of the resulting option value
     * @return an Option object representing the configured option with the specified parameters and switches
     */
    public <T> Option<T> addObjectOption(
            String displayName,
            String description,
            Repetitions repetitions,
            String argName,
            Consumer<T> handler,
            Class<T> targetClass,
            Converter<String, T> converter,
            String firstSwitch,
            String... moreSwitches
    ) {
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(Param.ofObject(displayName, description, argName, Param.Required.REQUIRED, targetClass, converter))
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds a new record option with the specified configuration parameters.
     * This method dynamically constructs an option based on the components
     * defined in the target record class. Each record component is mapped to
     * a parameter that can be supplied when using the option.
     *
     * @param <T> the type of the record
     * @param displayName     the display name of the option
     * @param description     a description explaining the purpose of the option
     * @param repetitions     the number of times the option can be specified
     * @param defaultSupplier a supplier of the default value for the record instance
     * @param targetClass     the class representing the record type
     * @param firstSwitch     the primary command-line switch associated with this option
     * @param moreSwitches    additional command-line switches associated with this option
     * @return an {@link Option} representing the configured option for the specified record type
     * @throws IllegalArgumentException if a record component's type is unsupported
     */
    public <T extends Record> Option<T> addRecordOption(
            String displayName,
            String description,
            Repetitions repetitions,
            Supplier<@Nullable T> defaultSupplier,
            Class<T> targetClass,
            String firstSwitch,
            String... moreSwitches
    ) {
        RecordParams<T> recordParams = getRecordParams(displayName, targetClass);
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(recordParams.params())
                .mapper(recordParams.mapper())
                .defaultSupplier(defaultSupplier)
                .build(firstSwitch, moreSwitches);
    }

    private static <T extends Record> RecordParams<T> getRecordParams(String displayName, Class<T> targetClass) {
        RecordComponent[] recordComponents = targetClass.getRecordComponents();
        Param<?>[] params = new Param<?>[recordComponents.length];
        Class<?>[] constructorArgTypes = new Class<?>[params.length];
        handleRecordComponents(displayName, recordComponents, params, constructorArgTypes);

        Constructor<T> constructor;
        try {
            constructor = targetClass.getDeclaredConstructor(constructorArgTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("constructor not found for record class: " + targetClass.getName(), e);
        }

        Function<Object[], T> mapper = elements -> {
            try {
                return constructor.newInstance(elements);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("could not create a new instance of " + targetClass, e);
            }
        };
        return new RecordParams<>(params, mapper);
    }

    private record RecordParams<T>(Param<?>[] params, Function<Object[], T> mapper) {}

    private static void handleRecordComponents(String displayName, RecordComponent[] recordComponents, Param<?>[] params, Class<?>[] constructorArgTypes) {
        for (int i = 0, nRecordComponents = recordComponents.length; i < nRecordComponents; i++) {
            RecordComponent component = recordComponents[i];
            Class<?> type = component.getType();
            Param<?> param;
            String componentName = component.getName();
            String componentDescription = "Set the value of " + componentName;
            if (type == int.class || type == Integer.class) {
                param = Param.ofInt(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type == long.class || type == Long.class) {
                param = Param.ofLong(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type == double.class || type == Double.class) {
                param = Param.ofDouble(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type == String.class) {
                param = Param.ofString(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type == Path.class) {
                param = Param.ofPath(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type == URI.class) {
                param = Param.ofUri(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED
                );
            } else if (type.isEnum()) {
                param = Param.ofEnum(
                        componentName,
                        componentDescription,
                        componentName,
                        Param.Required.REQUIRED,
                        type.asSubclass(Enum.class)
                );
            } else {
                throw new IllegalArgumentException(
                        "Unsupported record component type '%s' for component '%s' in option '%s'"
                                .formatted(type.getName(), componentName, displayName)
                );
            }

            params[i] = param;
            constructorArgTypes[i] = type;
        }
    }

    /**
     * Adds a new record option with the specified configuration parameters.
     * This method dynamically constructs an option based on the components
     * defined in the target record class. Each record component is mapped to
     * a parameter that can be supplied when using the option.
     *
     * @param <T> the type of the record
     * @param displayName     the display name of the option
     * @param description     a description explaining the purpose of the option
     * @param repetitions     the number of times the option can be specified
     * @param handler         the handler to call
     * @param targetClass     the class representing the record type
     * @param firstSwitch     the primary command-line switch associated with this option
     * @param moreSwitches    additional command-line switches associated with this option
     * @return an {@link Option} representing the configured option for the specified record type
     * @throws IllegalArgumentException if a record component's type is unsupported
     */
    public <T extends Record> Option<T> addRecordOption(
            String displayName,
            String description,
            Repetitions repetitions,
            Consumer<T> handler,
            Class<T> targetClass,
            String firstSwitch,
            String... moreSwitches
    ) {
        RecordParams<T> recordParams = getRecordParams(displayName, targetClass);
        return new OptionBuilder<>(this, displayName, description, targetClass)
                .repetitions(repetitions)
                .param(recordParams.params())
                .mapper(recordParams.mapper())
                .handler(handler)
                .build(firstSwitch, moreSwitches);
    }

    /**
     * Adds an option to the ArgumentsParserBuilder instance being built.
     *
     * @param option the option to be added
     * @param <O>    the type of option to be added
     * @return the added option
     */
    public <O extends Option<?>> O addOption(O option) {
        List<String> switches = option.switches();
        LangUtil.check(!switches.isEmpty(), "no switches for option '%s'", option.displayName());
        for (String swtch : switches) {
            LangUtil.check(options.putIfAbsent(swtch, option) == null, "duplicate switch for option '%s': %s", option.displayName(), swtch);
        }
        return option;
    }

    /**
     * Builds the parser and returns a new instance of ArgumentsParser.
     *
     * @param validationOverridingOptions options that inhibit validation if present on the command line
     * @return a new instance of ArgumentsParser
     */
    public ArgumentsParser build(Option<?>... validationOverridingOptions) {
        return new ArgumentsParser(
                name,
                description,
                argsDescription,
                options,
                minPositionalArgs,
                maxPositionalArgs,
                positionalArgDisplayNames,
                validationOverridingOptions
        );
    }

}
