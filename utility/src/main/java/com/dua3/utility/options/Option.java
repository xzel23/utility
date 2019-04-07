package com.dua3.utility.options;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.FileType;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An Option that changes the behavior of other classes. This class is intended
 * to be used to create settings dialogs at runtime.
 *
 * @param <T> the type of this option's values
 */
public abstract class Option<T> {

    /** Logger instance. */
    private static final Logger LOG = Logger.getLogger(Option.class.getName());

    public interface Value<T> extends Supplier<T>, Comparable<Value<T>> {
        @Override
        default int compareTo(Value<T> other) {
            return text().compareTo(other.text());
        }

        default Value<T> makeStatic() {
            return new StaticValue<>(toString(), get());
        }
        
        default String text() {
            return String.valueOf(get());
        }
    }

    /**
     * A value with a substituted, human-readable name.
     */
    private static class StaticValue<T> implements Value<T> {

        private final String name;
        private final T value;

        public StaticValue(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String text() {
            return name;
        }

        @Override
        public StaticValue<T> makeStatic() {
            return this;
        }

        @Override
        public String toString() {
            return text();
        }
        
        @Override
        public boolean equals(Object obj) {
        	if (obj ==null || obj.getClass()!=getClass()) {
        		return false;
        	}
        	
        	StaticValue<?> other = (StaticValue<?>) obj;
        	return Objects.equals(other.name, name) && Objects.equals(other.value, value);
        }
        
        @Override
        public int hashCode() {
        	return Objects.hash(name, value);
        }
    }

    /**
     * Create a named value.
     *
     * @param        <T>
     *               the parameter type
     * @param  name
     *               the name to use
     * @param  value
     *               the value
     * @return
     *               the named value
     */
    public static <T> Value<T> value(String name, T value) {
        return new StaticValue<>(name, value);
    }

    /**
     * Create a value.
     *
     * @param        <T>
     *               the parameter type
     * @param  value
     *               the value
     * @return
     *               the named value
     */
    public static <T> Value<T> value(T value) {
        return new StaticValue<>(String.valueOf(value), value);
    }

    public static class StringOption extends Option<String> {
        StringOption(String name, Value<String> defaultValue) {
            super(name, String.class, defaultValue);
        }
    }

    public static class SimpleOption<T> extends Option<T> {
        SimpleOption(String name, Class<T> cls, Value<T> defaultValue) {
            super(name, cls, defaultValue);
        }
    }

    public static class FileOption extends Option<File> {
        private final List<String> extensions;
        FileOption(String name, Value<File> defaultValue, String... extensions) {
            super(name, File.class, defaultValue);
            this.extensions = List.of(extensions);
        }
    }

    public static class ChoiceOption<T> extends Option<T> {
        private final List<Value<T>> choices;

        ChoiceOption(
                String name,
                Class<T> klass,
                Value<T> defaultValue,
                Collection<Value<T>> choices) {
            super(name, klass, defaultValue);

            // make sure this.choices does not contain a duplicate for defaultValue
            if (choices.contains(defaultValue)) {
                this.choices = new ArrayList<>(choices);
            } else {
                List<Value<T>> allChoices = new ArrayList<>(choices.size() + 1);
                allChoices.add(defaultValue);
                allChoices.addAll(choices);
                this.choices = allChoices;
            }
        }

        public List<Value<T>> getChoices() {
            return Collections.unmodifiableList(choices);
        }
    }

    public static StringOption stringOption(String name) {
        return new StringOption(name, () -> "");
    }

    public static StringOption stringOption(String name, String defaultValue) {
        return new StringOption(name, () -> defaultValue);
    }

    public static StringOption stringOption(String name, Value<String> defaultValue) {
        return new StringOption(name, defaultValue);
    }

    public static Option<Integer> intOption(String name) {
        return new SimpleOption<>(name, Integer.class, () -> 0);
    }

    public static Option<Integer> intOption(String name, int defaultValue) {
        return new SimpleOption<>(name, Integer.class, () -> defaultValue);
    }

    public static Option<Integer> intOption(String name, Value<Integer> defaultValue) {
        return new SimpleOption<>(name, Integer.class, defaultValue);
    }

    public static Option<Double> doubleOption(String name) {
        return new SimpleOption<>(name, Double.class, () -> 0.0);
    }

    public static Option<Double> doubleOption(String name, double defaultValue) {
        return new SimpleOption<>(name, Double.class, () -> defaultValue);
    }

    public static Option<Double> doubleOption(String name, Value<Double> defaultValue) {
        return new SimpleOption<>(name, Double.class, defaultValue);
    }

    public static FileOption fileOption(String name) {
        return new FileOption(name, () -> null);
    }

    public static FileOption fileOption(String name, File defaultValue) {
        return new FileOption(name, () -> defaultValue);
    }

    public static FileOption fileOption(String name, Value<File> defaultValue) {
        return new FileOption(name, defaultValue);
    }

    public static FileOption fileOption(String name, Value<File> defaultValue, String... extensions) {
        return new FileOption(name, defaultValue, extensions);
    }

    public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue,
            Collection<Value<T>> choices) {
        return new ChoiceOption<>(name, klass, defaultValue, choices);
    }

    public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue,
            Value<T>... choices) {
        return new ChoiceOption<>(name, klass, defaultValue, Arrays.asList(choices));
    }

    private final String name;
    private final Class<T> klass;
    private final Value<T> defaultValue;

    protected Option(String name, Class<T> klass, Value<T> defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.klass = Objects.requireNonNull(klass);
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return name.equals(other.name) && klass.equals(other.klass);
    }

    public Value<T> getDefault() {
        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public Class<T> getOptionClass() {
        return klass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, klass);
    }

    @Override
    public String toString() {
        return name + "[" + getDefault() + "]";
    }

    private static final String PATTERN_VAR_START = "\\$\\{";
    private static final String PATTERN_VAR_NAME = "(?<name>\\p{Alpha}(\\p{Alnum}|_)*)";
    private static final String PATTERN_VAR_ARG_1 = "(:((?<arg1>\\p{Alpha}(\\p{Alnum}|_)*)=(?<value1>[^,}]*)))";
    private static final String PATTERN_VAR_ARG_N = "(,((?<argn>\\p{Alpha}(\\p{Alnum}|_)*)=(?<valuen>[^,}]*)))";
    private static final String PATTERN_VAR_REMAINING_ARGS = "(?<remainingargs>"+PATTERN_VAR_ARG_N+"*)";
    private static final String PATTERN_VAR_END = "\\}";

    private static final Pattern PATTERN_VAR = Pattern.compile(
              PATTERN_VAR_START
            + PATTERN_VAR_NAME
            + "(" + PATTERN_VAR_ARG_1 + PATTERN_VAR_REMAINING_ARGS +")?"
            + PATTERN_VAR_END);

    private static final Pattern PATTERN_ARGN = Pattern.compile(PATTERN_VAR_ARG_N);

    /**
     * Parse a configuration schema string.
     * <p>
     * Example: https://${SERVER}:${PORT}
     *
     * @param  s
     *           the scheme to parse
     * @return
     *          {@link Pair} consisting of
     *          <ul>
     *              <li>list of options
     *              <li>scheme with var arguments removed
     *          </ul>
     */
    public static Pair<String,List<Option<?>>> parseScheme(String s) {
        // extract options
        List<Option<?>> list = new ArrayList<>();
        Matcher matcher = PATTERN_VAR.matcher(s);
        while (matcher.find()) {
            String name = matcher.group("name");

            Map<String,String> arguments = new HashMap<>();
            String arg = matcher.group("arg1");
            if (arg!=null) {
                String val = matcher.group("value1");
                addArgument(arguments, arg, val);
                String remainingArgs = matcher.group("remainingargs");
                if (!remainingArgs.isEmpty()) {
                    Matcher matcherArgs = PATTERN_ARGN.matcher(remainingArgs);
                    while (matcherArgs.find()) {
                        arg = matcherArgs.group("argn");
                        val = matcherArgs.group("valuen");
                        addArgument(arguments, arg, val);
                    }
                }
            }

            String type = arguments.getOrDefault("type", "string");
            String dflt = arguments.get("default");

            switch (type) {
                case "file":
                    list.add(Option.fileOption(name, () -> (dflt == null ? (File) null : new File(dflt)), arguments.get("extension")));
                    break;
                case "string":
                    list.add(Option.stringOption(name, dflt));
                    break;
                case "integer":
                    list.add(Option.intOption(name, dflt==null?0:Integer.parseInt(dflt)));
                    break;
                default:
                    throw new IllegalStateException("unsupported type: "+type);
            }
        }

        // remove arguments from scheme
        String r = PATTERN_VAR.matcher(s).replaceAll("\\$\\{${name}\\}");

        return Pair.of(r, list);
    }

    private static void addArgument(Map<String, String> arguments, String arg, String val) {
        var old = arguments.put(arg, val);
        if (old!=null) {
            LOG.log(Level.WARNING, String.format("while parsing option string: multiple values for argument '%s'", arg));
        }
    }
}
