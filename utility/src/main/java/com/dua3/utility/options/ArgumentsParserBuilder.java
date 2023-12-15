package com.dua3.utility.options;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.lang.LangUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class ArgumentsParserBuilder {

    private static final String POSITIONAL_MARKER = "--";
    private static final String DEFAULT_ARG_DISPLAY_NAME = "arg";

    private String name = "";
    private String description = "";
    private final Map<String, Option<?>> options = new LinkedHashMap<>();
    private int minPositionalArgs = 0;
    private int maxPositionalArgs = Integer.MAX_VALUE;
    private String positionalArgDisplayName = DEFAULT_ARG_DISPLAY_NAME;

    ArgumentsParserBuilder() {
    }

    public ArgumentsParserBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ArgumentsParserBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public ArgumentsParserBuilder setPositionalArgs(int minArgs, int maxArgs, String argDisplayName) {
        if (minArgs < 0 || maxArgs < 0 || minArgs > maxArgs) {
            throw new IllegalArgumentException("Invalid positional arguments range");
        }
        this.minPositionalArgs = minArgs;
        this.maxPositionalArgs = maxArgs;
        this.positionalArgDisplayName = argDisplayName;
        return this;
    }

    public ArgumentsParserBuilder setPositionalArgs(int minArgs, int maxArgs) {
        return setPositionalArgs(minArgs, maxArgs, DEFAULT_ARG_DISPLAY_NAME);
    }

    public Flag flag(String... names) {
        return addOption(Flag.create(names));
    }

    public <T> SimpleOption<T> simpleOption(Class<? extends T> type, String... names) {
        return simpleOption(s -> DataUtil.convert(s, type, true), names);
    }

    public <T> SimpleOption<T> simpleOption(Function<String, ? extends T> mapper, String... names) {
        return addOption(SimpleOption.create(mapper, names));
    }

    public <E extends Enum<E>> ChoiceOption<E> choiceOption(Class<? extends E> enumClass, String... names) {
        return addOption(ChoiceOption.create(enumClass, names));
    }

    public <T> StandardOption<T> option(Class<? extends T> type, String... names) {
        return option(s -> DataUtil.convert(s, type, true), names);
    }

    public <T> StandardOption<T> option(Function<String, ? extends T> mapper, String... names) {
        return addOption(StandardOption.create(mapper, names));
    }

    public <O extends Option<?>> O addOption(O option) {
        for (String name : option.names()) {
            LangUtil.check(options.putIfAbsent(name, option) == null, "duplicate option name: %s", name);
        }
        return option;
    }

    /**
     * Builds the parser and returns a new instance of ArgumentsParser.
     *
     * @return a new instance of ArgumentsParser
     */
    public ArgumentsParser build() {
        return new ArgumentsParser(name, description, options, minPositionalArgs, maxPositionalArgs, positionalArgDisplayName);
    }

}
