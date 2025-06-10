package com.dua3.utility.options;

import com.dua3.utility.lang.LangUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Formatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A parser that parses command line args into an {@link Arguments} instance.
 */
public class ArgumentsParser {

    private static final String POSITIONAL_MARKER = "--";

    private final Map<String, Option<?>> options;

    private final int minPositionalArgs;

    private final int maxPositionalArgs;

    private final String[] positionalArgDisplayNames;

    private final String name;

    private final String description;

    private final String argsDescription;

    private final Collection<Option<?>> validationOverridingOptions;

    /**
     * Returns a new instance of ArgumentsParserBuilder.
     *
     * @return a new instance of ArgumentsParserBuilder
     */
    public static ArgumentsParserBuilder builder() {
        return new ArgumentsParserBuilder();
    }

    /**
     * Represents a parser for command line arguments.
     *
     * @param name                        the name of the parser
     * @param description                 the description of the parser
     * @param argsDescription             the arguments description of the parser
     * @param options                     the map of options to be parsed
     * @param minPositionalArgs           the minimum number of positional arguments
     * @param maxPositionalArgs           the maximum number of positional arguments
     * @param positionalArgDisplayNames   the display names for positional arguments
     * @param validationOverridingOptions options that disable validation when present
     */
    ArgumentsParser(
            String name,
            String description,
            String argsDescription,
            Map<String, Option<?>> options,
            int minPositionalArgs,
            int maxPositionalArgs,
            String[] positionalArgDisplayNames,
            Option<?>[] validationOverridingOptions) {
        this.name = name;
        this.description = description;
        this.argsDescription = argsDescription;
        this.options = Map.copyOf(options);
        this.minPositionalArgs = minPositionalArgs;
        this.maxPositionalArgs = maxPositionalArgs;
        this.positionalArgDisplayNames = positionalArgDisplayNames;
        this.validationOverridingOptions = Set.of(validationOverridingOptions);
    }

    /**
     * Parse the command line arguments and return an instance of {@link Arguments}.
     *
     * @param args the command line arguments to parse
     * @return an instance of Arguments containing the parsed options and positional arguments
     * @throws OptionException if an error is encountered during parsing
     */
    public Arguments parse(String... args) {
        List<String> argList = List.of(args);

        Queue<Arguments.Entry<?>> parsedOptions = new ArrayDeque<>();
        List<String> positionalArgs = new ArrayList<>();
        Arguments.Entry<?> currentEntry = null;

        boolean parsingPositional = false;
        boolean remainingAllPositional = false;

        for (int idx = 0; idx < args.length; idx++) {
            // get next arg
            String arg = argList.get(idx);

            // shortcut if positional marker has been encountered
            if (remainingAllPositional) {
                positionalArgs.add(arg);
                continue;
            }

            // check for positional marker
            if (arg.equals(POSITIONAL_MARKER)) {
                LangUtil.check(positionalArgs.isEmpty(), () -> new ArgumentsException("positional args found before positional marker '" + POSITIONAL_MARKER + "'"));
                remainingAllPositional = true;
                continue;
            }

            // if the maximum number of args is consumed, reset the current entry
            if (currentEntry != null && currentEntry.getParams().size() == currentEntry.getOption().maxArity()) {
                currentEntry = null;
            }

            // is the argument the start of a new option?
            Option<?> option = options.get(arg);
            if (option != null) {
                // start processing of the next option
                currentEntry = Arguments.Entry.create(option);
                parsedOptions.add(currentEntry);

                if (currentEntry.getOption().maxArity() == 0) {
                    currentEntry = null;
                }
            } else {
                // add an option to the current entry or positional args
                if (currentEntry != null) {
                    currentEntry.addParameter(arg);
                } else {
                    if (!parsingPositional) {
                        parsingPositional = true;
                    }
                    positionalArgs.add(arg);
                }
            }
        }

        Arguments arguments = new Arguments(parsedOptions, positionalArgs, minPositionalArgs, maxPositionalArgs);

        // only validate if no validation overriding options are present
        if (parsedOptions.stream().map(Arguments.Entry::getOption).noneMatch(validationOverridingOptions::contains)) {
            arguments.validate(options.values());
        }

        return arguments;
    }

    /**
     * Get a help message listing all available options.
     *
     * @return help message
     */
    public String help() {
        Formatter fmt = new Formatter();
        help(fmt);
        return fmt.toString();
    }

    /**
     * Output option help.
     *
     * @param fmt the {@link Formatter} used for output
     */
    public void help(Formatter fmt) {
        // print title
        if (!name.isEmpty()) {
            fmt.format("\n%s\n", name);
            fmt.format("%s\n", "-".repeat(name.length()));
            fmt.format("\n");
        }

        // print description
        if (!description.isEmpty()) {
            fmt.format("%s\n", description);
            fmt.format("\n");
        }

        // print command line example
        String cmdText = name.isEmpty() ? "<program>" : name;
        if (hasOptions()) {
            cmdText += " <options>";
        }
        cmdText += getArgText(minPositionalArgs, maxPositionalArgs, positionalArgDisplayNames);
        fmt.format("%s\n\n", cmdText);

        if (!argsDescription.isEmpty()) {
            fmt.format("%s\n", argsDescription.indent(2));
            fmt.format("\n");
        }

        // print options
        if (hasOptions()) {
            fmt.format("  <options>:\n");
            options.values().stream().sorted(Comparator.comparing(Option::name)).distinct().forEach(option -> {
                // get argument text
                String argText = getArgText(option.minArity(), option.maxArity(), option.argNames().toArray(String[]::new));

                // print option names and arguments
                fmt.format("    %s%s\n", String.join("|", option.names()), argText);

                // print option description
                if (!option.description().isEmpty()) {
                    fmt.format("%s", option.description().indent(12));
                }

                fmt.format("\n");
            });
        }
    }

    /**
     * Checks if the ArgumentsParser has any options defined.
     *
     * @return true if there are options defined, false otherwise
     */
    public boolean hasOptions() {
        return !options.isEmpty();
    }

    /**
     * Output error message for the given {@link OptionException} to {@link Formatter} instance.
     *
     * @param fmt formatter
     * @param e   exception
     */
    public void errorMessage(Formatter fmt, OptionException e) {
        // print title
        if (!name.isEmpty()) {
            fmt.format("%s\n", name);
            fmt.format("%s\n", "-".repeat(name.length()));
            fmt.format("\n");
        }

        // print description
        if (!description.isEmpty()) {
            fmt.format("%s\n", description);
            fmt.format("\n");
        }

        fmt.format("ERROR: %s", e.getMessage());
    }

    /**
     * Format error message for the given {@link OptionException} to {@link String}.
     *
     * @param e exception
     * @return error message
     */
    public String errorMessage(OptionException e) {
        try (Formatter fmt = new Formatter()) {
            errorMessage(fmt, e);
            return fmt.toString();
        }
    }

    /**
     * Get all options defined for this parser.
     *
     * @return list containing all options defined for this parser
     */
    public List<Option<?>> options() {
        return List.copyOf(new LinkedHashSet<>(options.values()));
    }

    /**
     * Generates the formatted argument text based on the minimum and maximum number of arguments and the argument name.
     *
     * @param min the minimum number of arguments
     * @param max the maximum number of arguments
     * @param args the names of the arguments
     * @return the formatted argument text
     */
    private static String getArgText(int min, int max, String[] args) {
        assert min <= max : "invalid interval: min=" + min + ", max=" + max;

        boolean useNumberingForArg = max == Integer.MAX_VALUE ? args.length < min + 1 : args.length < max;
        int argNr = 1;

        // append the first min arguments
        Formatter argText = new Formatter();
        if (args.length > 0) {
            for (int i = 0; i < min; i++) {
                if (i < args.length - 1) {
                    argText.format(" <%s>", args[i]);
                } else {
                    argNr = appendArg(argText, args[args.length - 1], false, useNumberingForArg, false, argNr);
                }
            }
        } else {
            String arg = "arg";
            switch (min) {
                case 0 -> { /* nothing to do */ }
                case 1 -> argNr = appendArg(argText, "arg", false, useNumberingForArg, false, argNr);
                case 2 -> {
                    argNr = appendArg(argText, "arg", false, useNumberingForArg, false, argNr);
                    argNr = appendArg(argText, "arg", false, useNumberingForArg, false, argNr);
                }
                default -> {
                    argNr = appendArg(argText, "arg", false, useNumberingForArg, false, argNr);
                    argNr += min - 1;
                    argNr = appendArg(argText, "arg", false, useNumberingForArg, false, argNr);
                }
            }
        }

        // append remaining arguments
        if (max == Integer.MAX_VALUE) {
            String arg = args.length == 0 ? "arg" : args[Math.min(min, args.length -1)];
            for (int i = min; i < args.length - 1; i++) {
                argText.format(" [<%s>]".formatted(arg));
                arg = args[i + 1];
            }
            if (args.length == min + 1) {
                argNr = appendArg(argText, arg, true, useNumberingForArg, true, argNr);
            } else {
                argNr = appendArg(argText, arg, true, useNumberingForArg, true, argNr);
            }
        } else {
            int optionalCount = max - min;
            if (optionalCount > 0) {
                if (args.length > 1) {
                    for (int i = min; i < Math.min(args.length, max); i++) {
                        String arg = i < args.length ? args[i] : "arg";
                        argNr = appendArg(argText, arg, true, i >= args.length - 1 && useNumberingForArg, false, argNr);
                        optionalCount--;
                    }
                }
                if (optionalCount > 0) {
                    String arg = args.length == 0 ? "arg" : args[Math.min(max - 1, args.length - 1)];
                    switch (optionalCount) {
                        case 1 -> {
                            argNr = appendArg(argText, arg, true, useNumberingForArg, false, argNr);
                        }
                        case 2 -> {
                            argNr = appendArg(argText, arg, true, useNumberingForArg, false, argNr);
                            argNr = appendArg(argText, arg, true, useNumberingForArg, false, argNr);
                        }
                        default -> {
                            argNr = appendArg(argText, arg, true, useNumberingForArg, false, argNr);
                            argText.format(" ...");
                            argNr += optionalCount - 2;
                            argNr = appendArg(argText, arg, true, useNumberingForArg, false, argNr);
                        }
                    }
                }
            }
        }
        return argText.toString();
    }

    private static int appendArg(Formatter fmt, String arg, boolean useBrackets, boolean useNumbering, boolean addEllipsis, int argNr) {
        fmt.format(" ");
        if (useBrackets) {
            fmt.format("[");
        }

        if (useNumbering) {
            fmt.format("<%s%d>", arg, argNr++);
        } else {
            fmt.format("<%s>", arg);
        }

        if (addEllipsis) {
            fmt.format(" ...");
        }

        if (useBrackets) {
            fmt.format("]");
        }
        return argNr;
    }
}

