package com.dua3.utility.cmd;

import com.dua3.utility.data.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Command line arguments class.
 */
public class CmdArgs implements Iterable<Pair<Option, List<String>>> {
    /** The options passed on the command line with their respective arguments. */
    private final Queue<Pair<Option, List<String>>> parsedOptions;
    /** The positional arguments. */
    private final List<String> positionalArgs;

    /**
     * Constructor.
     * @param parsedOptions the options detected by the command line parser
     * @param positionalArgs the positional arguments
     */
    CmdArgs(Queue<Pair<Option, List<String>>> parsedOptions, List<String> positionalArgs) {
        this.parsedOptions = parsedOptions;
        this.positionalArgs = new ArrayList<>(positionalArgs);
    }

    /**
     * Get positional arguments.
     * @return the positional arguments
     */
    public List<String> positionalArgs() {
        return Collections.unmodifiableList(positionalArgs);
    }

    /**
     * Get value of {@link SimpleOption}.
     * @param option the option
     * @return the parameter passed to the option, or the option's default value (if set)
     * @throws CmdException if neither is set
     */
    public String get(SimpleOption option) {
        String value = get(option, null);
        
        if (value == null) {
            throw new CmdException("option not set: " + option);
        }
        
        return value;
    }

    /**
     * Get value of {@link SimpleOption}.
     * @param option the option
     * @return the parameter passed to the option, the option's default value (if set), or {@code dflt}
     */
    public String get(SimpleOption option, String dflt) {
        String value = stream()
                .filter(po -> po.first.equals(option))
                .findFirst()
                .map(p -> p.second.get(0))
                .orElse(option.defaultValue());
        
        return value != null ? value : dflt;
    }

    /**
     * Test if flag is set.
     * @param flag the flag
     * @return true, if the flag is set
     */
    public boolean isSet(Flag flag) {
        return stream().anyMatch(po -> po.first.equals(flag));
    }

    /**
     * Execute action if {@link Flag} is set.
     * @param flag the flag
     * @param action the action to execute
     */
    public void ifSet(Flag flag, Runnable action) {
        if (isSet(flag)) {
            action.run();
        }
    }

    /**
     * Run action with arguments of supplied option.
     * @param option the option
     * @param action action to call
     */
    public void forEach(Option option, Consumer<List<String>> action) {
        parsedOptions.stream().filter(po -> po.first.equals(option)).forEach(po -> action.accept(po.second));
    }
    
    /**
     * Get {@link Iterator} of parsed Options and their arguments.
     * @return ietrator over {@link Pair}s consisting of Option and arguments passed to that option
     */
    @Override
    public Iterator<Pair<Option, List<String>>> iterator() {
        return parsedOptions.iterator();
    }

    /**
     * Get stream of parsed options.
     * @return stream of parsed options and respective arguments
     */
    public Stream<Pair<Option, List<String>>> stream() {
        return parsedOptions.stream();
    }

    /**
     * Get stream of values for an option.
     * @param option the option
     * @return stream of lists containing the arguments for each appearance of the given option 
     */
    public Stream<List<String>> stream(Option option) {
        return parsedOptions.stream().filter(p -> p.first.equals(option)).map(p -> p.second);
    }
}
