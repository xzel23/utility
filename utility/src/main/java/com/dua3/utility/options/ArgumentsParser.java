package com.dua3.utility.options;

import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;

import java.util.*;
import java.util.function.Function;

/**
 * A parser that parses command line args into an {@link Arguments} instance.
 */
public class ArgumentsParser {

    /** Marker to pass on the command line indicating that all remaining args should be treated as positional parameters. */
    public static final String POSITIONAL_MARKER = "--";

    /** The command name. */
    private final String name;

    /** The command description. */
    private final String description;

    /** The options understood by this CommandLineParser instance, stored in a map (command line arg: option). */
    private final Map<String, Option<?>> options = new LinkedHashMap<>();

    /** The minimum number of positional arguments. */
    final int minPositionalArgs;

    /** The maximum number of positional arguments. */
    final int maxPositionalArgs;
    
    /**
     * Constructor. 
     */
    public ArgumentsParser() {
        this("","");
    }

    /**
     * Constructor. 
     * @param name program name
     * @param description program description 
     * @param minArgs minimum number of positional arguments
     * @param maxArgs maximum number of positional arguments
     */
    public ArgumentsParser(String name, String description, int minArgs, int maxArgs) {
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);

        LangUtil.check(minArgs >= 0);
        LangUtil.check(maxArgs>=minArgs);
        this.minPositionalArgs = minArgs;
        this.maxPositionalArgs = maxArgs;
    }

    /**
     * Constructor. 
     * @param name program name
     * @param description program description 
     * @param minArgs minimum number of positional arguments
     */
    public ArgumentsParser(String name, String description, int minArgs) {
        this(name, description, minArgs, Integer.MAX_VALUE);        
    }
    
    /**
     * Constructor. 
     * @param name the command name to show in help text.
     * @param description the command description to show in help text.
     */
    public ArgumentsParser(String name, String description) {
        this(name, description, 0, Integer.MAX_VALUE);
    }

    /**
     * Define a new {@link Flag}.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the flag
     */
    public Flag  flag(String... names) {
        return addOption(Flag.create(names));
    }

    /**
     * Define a new {@link SimpleOption}.
     * @param type the class of the target type
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @param <T> the target type
     * @return the option
     */
    public <T> SimpleOption<T> simpleOption(Class<? extends T> type, String... names) {
        return simpleOption(s -> DataUtil.convert(s, type, true), names);
    }

    /**
     * Define a new {@link SimpleOption}.
     * @param mapper the mapping to the target type
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @param <T> the target type
     * @return the option
     */
    public <T> SimpleOption<T> simpleOption(Function<String,T> mapper, String... names) {
        return addOption(SimpleOption.create(mapper, names));
    }

    /**
     * Add choice option to parser.
     * @param <E> enum class
     * @param enumClass the enum class instance
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @return the option
     */
    public <E extends Enum<E>> ChoiceOption<E> choiceOption(Class<? extends E> enumClass, String... names) {
        return addOption(ChoiceOption.create(enumClass, names));
    }

    /**
     * Define a new option.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @param type the class type instance
     * @param <T> the generic type of the option 
     * @return the option
     */
    public <T> StandardOption<T> option(Class<? extends T> type, String... names) {
        return option(s -> DataUtil.convert(s, type, true), names);
    }

    /**
     * Define a new option.
     * @param names the (alternative) option names (i. e. "-h", "--help"); at least one name must be given.
     * @param mapper the mapper used to convert string arguments to the target type
     * @param <T> the generic type of the option 
     * @return the option
     */
    public <T> StandardOption<T> option(Function<String,T> mapper, String... names) {
        return addOption(StandardOption.create(mapper, names));
    }

    /** 
     * Add option to parser. 
     * @param option the option to add
     */
    private <O extends Option<?>> O addOption(O option) {
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
    public Arguments parse(String... args) {
        List<String> argList = Arrays.asList(args);

        Queue<Arguments.Entry<?>> parsedOptions = new LinkedList<>();
        List<String> positionalArgs = new LinkedList<>();
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
                LangUtil.check(positionalArgs.isEmpty(), () -> new OptionException("positional args found before positional marker '" + POSITIONAL_MARKER + "'"));
                remainingAllPositional = true;
                continue;
            }

            // if maximum number of args is consumed, reset the current entry
            if (currentEntry!=null && currentEntry.getParms().size()==currentEntry.getOption().maxArity) {
                currentEntry = null;
            }

            // is argument start of a new option?
            Option<?> option = options.get(arg);
            if (option != null) {
                // start processing of next ooption
                currentEntry = Arguments.Entry.create(option);
                parsedOptions.add(currentEntry);
                
                if (currentEntry.getOption().maxArity==0) {
                    currentEntry=null;
                }
            } else {
                // add option to current entry or positional args
                if (currentEntry!=null) {
                    currentEntry.addParameter(arg);
                } else {
                    if (!parsingPositional) {
                        LangUtil.check(positionalArgs.isEmpty(), () -> new OptionException("positional args mixed in with option parameters"));
                        parsingPositional = true;
                    }
                    positionalArgs.add(arg);
                }
            }
        }

        validate(parsedOptions);

        if (positionalArgs.size()<minPositionalArgs) {
            throw new OptionException("missing argument (at least " + minPositionalArgs + " arguments must be given)");
        }

        if (positionalArgs.size()>maxPositionalArgs) {
            throw new OptionException("too many arguments (at most " + minPositionalArgs + " arguments can be given)");
        }

        return new Arguments(parsedOptions, positionalArgs);
    }

    /**
     * Validate the parsed option, i. e. check number of occurences and arity.
     * @param parsedOptions the parsed options to validate
     * @throws OptionException if an error is detected
     */
    private void validate(Collection<Arguments.Entry<?>> parsedOptions) {
        Map<Option<?>, Integer> hist = new HashMap<>();
        parsedOptions.forEach(entry -> hist.compute(entry.option, (k_,i_) -> i_==null ? 1 : i_+1));

        Collection<Option<?>> allOptions = new HashSet<>(options.values());
        allOptions.stream()
                .map(option -> Pair.of(option, hist.getOrDefault(option, 0)))
                .forEach(p -> {
                    Option<?> option = p.first();
                    int count = p.second();
                    LangUtil.check(option.minOccurrences() <= count,
                            () -> new OptionException(String.format(
                                "option '%s' must be specified at least %d time(s), but was only %d times",
                                option.name(), option.minOccurrences(), count
                            )));
                    LangUtil.check(option.maxOccurrences() >= count,
                            () -> new OptionException(String.format(
                                "option '%s' must be specified at most %d time(s), but was %d times",
                                option.name(), option.maxOccurrences(), count
                            )));
                });
    }

    /**
     * Get a help message listing all available options.
     * @return help message
     */
    public String help() {
        Formatter fmt = new Formatter();
        help(fmt);
        return fmt.toString();
    }

    /**
     * Output option help.
     * @param fmt the {@link Formatter} used for output
     */
    public void help(Formatter fmt) {
        // print title
        if (!name.isEmpty()) {
            fmt.format("%n%s%n", name);
            fmt.format("%s%n", "-".repeat(name.length()));
            fmt.format("%n");
        }
        
        // print description
        if (!description.isEmpty()) {
            fmt.format("%s%n", description);
            fmt.format("%n");
        }
        
        // print command line example
        String cmdText = name.isEmpty() ? "<program>" : name;
        if (!options.isEmpty()) {
            cmdText += " <options>";
        }
        cmdText += getArgText(minPositionalArgs, maxPositionalArgs);
        fmt.format("%s%n%n", cmdText);
        
        // print options
        options.values().stream().sorted(Comparator.comparing(Option::name)).distinct().forEach(option -> {
            // get argument text
            String argText = getArgText(option.minArity, option.maxArity);

            // print option names
            for (String name: option.names()) {
                fmt.format("    %s%s%n", name, argText);
            }
            
            // print option description
            if (!option.description.isEmpty()) {
                fmt.format("%s", option.description.indent(12));
            }
            
            fmt.format("%n");
        });
    }

    private String getArgText(int min, int max) {
        assert min<=max;
        
        String argText = switch (min) {
            case 0 -> "";
            case 1 -> (min == max) ? " arg" : " arg1";
            case 2 -> " arg1 arg2";
            case 3 -> " arg1 arg2 arg3";
            default -> " arg1 ... arg" + min;
        };

        // handle max arity
        if (max == Integer.MAX_VALUE) {
            argText += " [arg" + (min+1) + "] ...";
        } else {
            int optionalCount = max - min;
            if (optionalCount==1) {
                argText += " [arg" + (min+1) + "]";
            } else if (optionalCount >1) {
                argText += " [arg" + (min+1) + "] ... (up to " + max + " arguments)";
            }
        }
        return argText;
    }
    
    public void errorMessage(Formatter fmt, OptionException e) {
        // print title
        if (!name.isEmpty()) {
            fmt.format("%s%n", name);
            fmt.format("%s%n", "-".repeat(name.length()));
            fmt.format("%n");
        }

        // print description
        if (!description.isEmpty()) {
            fmt.format("%s%n", description);
            fmt.format("%n");
        }
        
        fmt.format("ERROR: %s", e.getMessage());
    }

    public String errorMessage(OptionException e) {
        Formatter fmt = new Formatter();
        errorMessage(fmt, e);
        return fmt.toString();
    }
    
}
