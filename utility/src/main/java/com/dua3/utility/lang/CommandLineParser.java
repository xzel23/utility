package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import com.dua3.utility.text.TextUtil;

import java.util.*;
import java.util.function.Consumer;

/**
 * A simple commandline parser class.
 */
public class CommandLineParser {

    /** Marker to pass on the command line indicating that all remaining args should be treated as positional parameters. */
    public static final String POSITIONAL_MARKER = "--";

    /** The command name. */
    private final String name;

    /** The command description. */
    private final String description;

    /** The options understood by this CommandLineParser instance, stored in a map (command line arg: option). */
    private final Map<String, Option> options = new LinkedHashMap<>();

    /** The minimum number of positional arguments. */
    int minPositionalArgs;

    /** The maximum number of positional arguments. */
    int maxPositionalArgs;
    
    /**
     * Constructor. 
     */
    public CommandLineParser() {
        this("","");
    }

    public CommandLineParser(String name, String description, int minArgs, int maxArgs) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);

        LangUtil.check(minArgs>=0);
        LangUtil.check(maxArgs>=minArgs);
        this.minPositionalArgs = minArgs;
        this.maxPositionalArgs = maxArgs;
    }

    public CommandLineParser(String name, String description, int minArgs) {
        this(name, description, minArgs, Integer.MAX_VALUE);        
    }
    
    /**
     * Constructor. 
     * @param name the command name to show in help text.
     * @param description the command description to show in help text.
     */
    public CommandLineParser(String name, String description) {
        this(name, description, 0, Integer.MAX_VALUE);
    }

    /** 
     * Define a new option.
     * @param processor the function to call for processing the arguments passed to this option
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the option
     */
    public Option option(Consumer<List<String>> processor, String... names) {
        return addOption(new Option(processor, names));
    }

    /** 
     * Add option to parser. 
     * @param option the option to add
     */
    private Option addOption(Option option) {
        LangUtil.check(option.names.length > 0, "option must have at least one name");
        for (String name : option.names) {
            LangUtil.check(options.putIfAbsent(name, option) == null, "duplicate option name: %s", name);
        }
        return option;
    }

    /**
     * Parse command line arguments.
     * @param args the command line arguments to parse.
     * @return object holding the parsed command line arguments
     */
    public CommandLineArgs parse(String... args) {
        List<String> argList = Arrays.asList(args);

        Queue<Pair<Option, List<String>>> parsedOptions = new LinkedList<>();
        List<String> positionalArgs = new LinkedList<>();
        List<String> currentList = positionalArgs;

        boolean remainingAllPositional = false;
        
        for (int idx = 0; idx < args.length; idx++) {
            String arg = argList.get(idx);
            if (remainingAllPositional) {
                positionalArgs.add(arg); 
                continue;
            }
            
            if (arg.equals(POSITIONAL_MARKER)) {
                remainingAllPositional = true;
                continue;
            }
            
            Option option = options.get(arg);
            if (option != null) {
                currentList = new LinkedList<>();
                Pair<Option, List<String>> entry = Pair.of(option, currentList);
                parsedOptions.add(entry);
            } else {
                currentList.add(arg);
            }
        }

        validate(parsedOptions);

        if (positionalArgs.size()<minPositionalArgs) {
            throw new CommandLineException("missing argument (at least "+minPositionalArgs+" arguments must be given)");
        }

        if (positionalArgs.size()>maxPositionalArgs) {
            throw new CommandLineException("too many arguments (at most "+minPositionalArgs+" arguments can be given)");
        }

        return new CommandLineArgs(parsedOptions, positionalArgs);
    }

    /**
     * Validate the parsed option, i. e. check number of occurences and arity.
     * @param parsedOptions the parsed options to validate
     * @throws CommandLineException if an error is detected
     */
    private void validate(Queue<Pair<Option, List<String>>> parsedOptions) {
        Map<CommandLineParser.Option, Integer> hist = new HashMap<>();
        parsedOptions.forEach(p -> hist.compute(p.first, (k_,i_) -> i_==null ? 1 : i_+1));

        Set<Option> allOptions = new HashSet<>(options.values());
        allOptions.stream()
                .map(option -> Pair.of(option, hist.getOrDefault(option, 0)))
                .forEach(p -> {
                    Option option = p.first;
                    int count = p.second;
                    LangUtil.check(option.minOccurences <= count,
                            () -> new CommandLineException(
                                "option '%s' must be specified at least %d time(s), but was only %d times",
                                option.name(), option.minOccurences, count
                            ));
                    LangUtil.check(option.maxOccurences >= count,
                            () -> new CommandLineException(
                                "option '%s' must be specified at most %d time(s), but was %d times",
                                option.name(), option.maxOccurences, count
                            ));
                });
    }

    /**
     * Process command line arguments.
     * The argument list is parsed and for each given option, the processor is executed. 
     * @param args the command line arguments
     * @return the positional arguments
     */
    public List<String> process(String... args) {
        CommandLineArgs commandLineArgs = parse(args);
        commandLineArgs.process();
        return commandLineArgs.positionalArgs();
    }

    /**
     * Command line option.
     */
    public static class Option {
        final String[] names;
        final Consumer<List<String>> processor;
        String description = "";
        int minArity = 0;
        int maxArity = 0;
        int minOccurences = 0;
        int maxOccurences = 1;

        private Option(Consumer<List<String>> Consumer, String... names) {
            LangUtil.check(names.length > 0, "at least one name must be given");
            this.names = names.clone();
            this.processor = Objects.requireNonNull(Consumer, "Consumer must not be null");
        }

        public Option arity(int a) {
            return arity(a,a);
        }
        
        public Option arity(int min, int max) {
            LangUtil.check(min >= 0, "min arity is negative");
            LangUtil.check(min <= max, "min arity > max arity");
            LangUtil.check(minArity == 0 && maxArity == 0, "arity already set");

            this.minArity = min;
            this.maxArity = max;

            return this;
        }

        public Option occurence(int o) {
            return occurence(o,o);
        }
        
        public Option occurence(int min, int max) {
            LangUtil.check(min >= 0, "minimum occurences is negative");
            LangUtil.check(min <= max, "minimum occurrences > max occurrences");
            LangUtil.check(minOccurences == 0 && maxOccurences == 1, "occurrences already set");

            this.minOccurences = min;
            this.maxOccurences = max;

            return this;
        }

        public Option description(String description) {
            LangUtil.check(this.description.isEmpty());

            this.description = Objects.requireNonNull(description, "description must not be null");

            return this;
        }

        public String name() {
            return names[0];
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Option option = (Option) o;
            return Arrays.equals(names, option.names);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(names);
        }
    }

    /**
     * Exception class to throw when command line arguments do not match the options defined by the command line parser.
     */
    public static class CommandLineException extends IllegalStateException {
        CommandLineException(String fmt, Object... args) {
            super(String.format(fmt, args));
        }
    }
    
    public String help() {
        Formatter fmt = new Formatter();
        help(fmt);
        return fmt.toString();
    }

    public void help(Formatter fmt) {
        if (!name.isEmpty()) {
            fmt.format("%s%n", name);
            fmt.format("%s%n", TextUtil.repeat("-", name.length()));
            fmt.format("%n");
        }
        
        if (!description.isEmpty()) {
            fmt.format("%s%n", description);
            fmt.format("%n");
        }
        
        // determine required indentation
        int indent = options.keySet().stream().mapToInt(String::length).max().orElse(0);
        
        // print options
        String format = "%"+indent+"s%s%n";
        options.values().stream().sorted(Comparator.comparing(Option::name)).forEach(option -> {
            fmt.format(format, option.names[0]+ " - ", option.description);
            for (int i=1; i<option.names.length; i++) {
                fmt.format(format, option.names[i], option.description);
            }
        });
    }
}
