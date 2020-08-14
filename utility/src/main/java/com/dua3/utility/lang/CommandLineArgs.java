package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;

import java.util.*;

/**
 * Command line arguments class.
 */
public class CommandLineArgs {
    /** The options passed on the command line with their respective arguments. */
    private final Queue<Pair<CommandLineParser.Option, List<String>>> parsedOptions;
    /** The positional arguments. */
    private final List<String> positionalArgs;

    /**
     * Constructor.
     * @param parsedOptions the options detected by the command line parser
     * @param positionalArgs the positional arguments
     */
    CommandLineArgs(Queue<Pair<CommandLineParser.Option, List<String>>> parsedOptions, List<String> positionalArgs) {
        this.parsedOptions = Objects.requireNonNull(parsedOptions);
        this.positionalArgs = new ArrayList<>(positionalArgs);
    }

    /**
     * Process parsed options (i. e. call each option's processor instance).
     */
    public void process() {
        parsedOptions.forEach(this::process);
    }

    /**
     * Get positional arguments.
     * @return the positional arguments
     */
    public List<String> positionalArgs() {
        return Collections.unmodifiableList(positionalArgs);
    }

    /**
     * Add positional argument.
     * @param arg the argument
     */
    public void addPositionalArg(String arg) {
        positionalArgs.add(Objects.requireNonNull(arg, "arg must not be null"));
    }

    /**
     * Get options passed on commandline.
     * @return the options passed on the command line
     */
    public List<Pair<CommandLineParser.Option, List<String>>> parsedOptions() {
        return new ArrayList<>(parsedOptions);
    }

    /**
     * Process single option entry
     * @param entry pair consisting of an option and its arguments passed on the command line
     */
    private void process(Pair<CommandLineParser.Option, List<String>> entry) {
        CommandLineParser.Option option = entry.first;
        List<String> args = entry.second;
        
        LangUtil.check(args.size()>=option.minArity, 
                "option requires at least %d arguments but was used with %d: %s", 
                option.minArity, args.size(), option.name());

        LangUtil.check(args.size()<=option.maxArity, 
                "option requires at most %d arguments but was used with %d: %s", 
                option.maxArity, args.size(), option.name());

        option.processor.accept(args);
    }

}
