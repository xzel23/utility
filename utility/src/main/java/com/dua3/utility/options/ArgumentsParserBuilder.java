package com.dua3.utility.options;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * The ArgumentsParserBuilder class is used to construct an instance of ArgumentsParser with the desired configuration.
 */
public class ArgumentsParserBuilder {

    private static final String DEFAULT_ARG_DISPLAY_NAME = "arg";

    private String name = "";
    private String description = "";
    private String argsDescription = "";
    private final Map<String, Option<?>> options = new LinkedHashMap<>();
    private int minPositionalArgs = 0;
    private int maxPositionalArgs = Integer.MAX_VALUE;
    private String[] positionalArgDisplayNames = new String[]{DEFAULT_ARG_DISPLAY_NAME};

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
     * Adds a flag option to the ArgumentsParserBuilder instance being built.
     *
     * @param names the names for the flag option
     * @return the ArgumentsParserBuilder instance
     */
    public Flag flag(String... names) {
        return addOption(Flag.create(names));
    }

    /**
     * Creates a SimpleOption with the given mapper function and names.
     *
     * @param <T>    the option's argument type.
     * @param type   {@link Class} instance of the option's argument type
     * @param names  the names for the option, at least one
     * @return the created SimpleOption
     */
    public <T> SimpleOption<T> simpleOption(Class<? extends T> type, String... names) {
        return simpleOption(s -> DataUtil.convert(s, type, true), names);
    }

    /**
     * Adds a simple option to the ArgumentsParserBuilder instance being built.
     *
     * @param <T>    the option's argument type.
     * @param mapper the mapping function to the target type.
     * @param names  the names for the option, at least one.
     * @return the created SimpleOption.
     */
    public <T> SimpleOption<T> simpleOption(Function<@Nullable String, ? extends @Nullable T> mapper, String... names) {
        return addOption(SimpleOption.create(mapper, names));
    }

    /**
     * Adds a choice option with a limited set of possible values to the ArgumentsParserBuilder instance being built.
     *
     * @param <E>        the enum type
     * @param enumClass  the enum class representing the possible values
     * @param names      the names for the choice option
     * @return the ArgumentsParserBuilder instance
     */
    public <E extends Enum<E>> ChoiceOption<E> choiceOption(Class<? extends E> enumClass, String... names) {
        return addOption(ChoiceOption.create(enumClass, names));
    }

    /**
     * Adds a standard option to the ArgumentsParserBuilder instance being built.
     *
     * @param <T>    the option's argument type.
     * @param type   {@link Class} instance of the option's argument type
     * @param names  the names for the option, at least one.
     * @return the created StandardOption.
     */
    public <T> StandardOption<T> option(Class<? extends T> type, String... names) {
        return option(s -> DataUtil.convert(s, type, true), names);
    }

    /**
     * Adds a standard option to the ArgumentsParserBuilder instance being built.
     *
     * @param <T>    the option's argument type.
     * @param mapper the mapping function to the target type.
     * @param names  the names for the option, at least one.
     * @return the created StandardOption.
     */
    public <T> StandardOption<T> option(Function<@Nullable String, ? extends @Nullable T> mapper, String... names) {
        return addOption(StandardOption.create(mapper, names));
    }

    /**
     * Adds an option to the ArgumentsParserBuilder instance being built.
     *
     * @param option the option to be added
     * @param <O>    the type of option to be added
     * @return the added option
     */
    public <O extends Option<?>> O addOption(O option) {
        for (String name : option.names()) {
            LangUtil.check(options.putIfAbsent(name, option) == null, "duplicate option name: %s", name);
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
