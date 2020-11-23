package com.dua3.utility.cmd;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;

import java.util.*;

/**
 * A simple commandline parser class.
 */
public class CmdParser {

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
    public CmdParser() {
        this("","");
    }

    public CmdParser(String name, String description, int minArgs, int maxArgs) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);

        LangUtil.check(minArgs >= 0);
        LangUtil.check(maxArgs>=minArgs);
        this.minPositionalArgs = minArgs;
        this.maxPositionalArgs = maxArgs;
    }

    public CmdParser(String name, String description, int minArgs) {
        this(name, description, minArgs, Integer.MAX_VALUE);        
    }
    
    /**
     * Constructor. 
     * @param name the command name to show in help text.
     * @param description the command description to show in help text.
     */
    public CmdParser(String name, String description) {
        this(name, description, 0, Integer.MAX_VALUE);
    }

    /**
     * Define a new {@link Flag}.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the flag
     */
    public Flag  flag(String... names) {
        return addOption(new Flag(names));
    }

    /**
     * Define a new {@link Flag}.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the option
     */
    public SimpleOption  simpleOption(String... names) {
        return addOption(new SimpleOption(names));
    }

    /**
     * Define a new option.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the option
     */
    public StandardOption option(String... names) {
        return addOption(new StandardOption(names));
    }

    /** 
     * Add option to parser. 
     * @param option the option to add
     */
    private <O extends Option> O addOption(O option) {
        for (String name : option.names()) {
            LangUtil.check(options.putIfAbsent(name, option) == null, "duplicate option name: %s", name);
        }
        return option;
    }

    /**
     * Parse command line arguments.
     * @param args the command line arguments to parse.
     * @return object holding the parsed command line arguments
     */
    public CmdArgs parse(String... args) {
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
            throw new CmdException("missing argument (at least " + minPositionalArgs + " arguments must be given)");
        }

        if (positionalArgs.size()>maxPositionalArgs) {
            throw new CmdException("too many arguments (at most " + minPositionalArgs + " arguments can be given)");
        }

        return new CmdArgs(parsedOptions, positionalArgs);
    }

    /**
     * Validate the parsed option, i. e. check number of occurences and arity.
     * @param parsedOptions the parsed options to validate
     * @throws CmdException if an error is detected
     */
    private void validate(Queue<Pair<Option, List<String>>> parsedOptions) {
        Map<Option, Integer> hist = new HashMap<>();
        parsedOptions.forEach(p -> hist.compute(p.first, (k_,i_) -> i_==null ? 1 : i_+1));

        Set<Option> allOptions = new HashSet<>(options.values());
        allOptions.stream()
                .map(option -> Pair.of(option, hist.getOrDefault(option, 0)))
                .forEach(p -> {
                    Option option = p.first;
                    int count = p.second;
                    LangUtil.check(option.minOccurrences() <= count,
                            () -> new CmdException(
                                "option '%s' must be specified at least %d time(s), but was only %d times",
                                option.name(), option.minOccurrences(), count
                            ));
                    LangUtil.check(option.maxOccurrences() >= count,
                            () -> new CmdException(
                                "option '%s' must be specified at most %d time(s), but was %d times",
                                option.name(), option.maxOccurrences(), count
                            ));
                });
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
            fmt.format(format, option.name()+ " - ", option.description());
            for (String name: option.names()) {
                fmt.format(format, name, option.description());
            }
        });
    }
}
