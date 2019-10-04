package com.dua3.utility.options;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.OpenMode;

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

    /**
     * Identifier String for the option type.
     */
    public static final String OPTION_TYPE = "type";
    /**
     * Type identifier String for file options.
     */
    public static final String OPTION_TYPE_FILE = "file";
    /**
     * Type identifier String for string options.
     */
    public static final String OPTION_TYPE_STRING = "string";
    /**
     * Type identifier String for integer options.
     */
    public static final String OPTION_TYPE_INTEGER = "integer";
    /**
     * Type identifier String for double options.
     */
    public static final String OPTION_TYPE_DOUBLE = "double";
    /**
     * Logger instance.
     */
    private static final Logger LOG = Logger.getLogger(Option.class.getName());
    private static final String PATTERN_VAR_START = "\\$\\{";
    private static final String PATTERN_VAR_NAME = "(?<name>\\p{Alpha}(\\p{Alnum}|_)*)";
    private static final String PATTERN_VAR_ARG_1 = "(:((?<arg1>\\p{Alpha}(\\p{Alnum}|_)*)=(?<value1>[^,}]*)))";
    private static final String PATTERN_VAR_ARG_N = "(,((?<argn>\\p{Alpha}(\\p{Alnum}|_)*)=(?<valuen>[^,}]*)))";
    private static final String PATTERN_VAR_REMAINING_ARGS = "(?<remainingargs>" + PATTERN_VAR_ARG_N + "*)";
    private static final String PATTERN_VAR_END = "\\}";
    private static final Pattern PATTERN_VAR = Pattern.compile(
            PATTERN_VAR_START
                    + PATTERN_VAR_NAME
                    + "(" + PATTERN_VAR_ARG_1 + PATTERN_VAR_REMAINING_ARGS + ")?"
                    + PATTERN_VAR_END);
    private static final Pattern PATTERN_ARGN = Pattern.compile(PATTERN_VAR_ARG_N);
    private final String name;
    private final Class<T> klass;
    private final Value<T> defaultValue;

    protected Option(String name, Class<T> klass, Value<T> defaultValue) {
        this.name = Objects.requireNonNull(name);
        this.klass = Objects.requireNonNull(klass);
        this.defaultValue = defaultValue;
    }

    /**
     * Create a named value.
     *
     * @param <T>   the parameter type
     * @param name  the name to use
     * @param value the value
     * @return the named value
     */
    public static <T> Value<T> value(String name, T value) {
        return new StaticValue<>(name, value);
    }

    /**
     * Create a value.
     *
     * @param <T>   the parameter type
     * @param value the value
     * @return the named value
     */
    public static <T> Value<T> value(T value) {
        return new StaticValue<>(String.valueOf(value), value);
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
        return new FileOption(name, () -> null, OpenMode.READ);
    }

    public static FileOption fileOption(String name, File defaultValue) {
        return new FileOption(name, () -> defaultValue, OpenMode.READ);
    }

    public static FileOption fileOption(String name, Value<File> defaultValue) {
        return new FileOption(name, defaultValue, OpenMode.READ);
    }

    public static FileOption fileOption(String name, Value<File> defaultValue, String... extensions) {
        return new FileOption(name, defaultValue, OpenMode.READ, extensions);
    }

    public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue,
                                                   Collection<Value<T>> choices) {
        return new ChoiceOption<>(name, klass, defaultValue, choices);
    }

    @SafeVarargs
    public static <T> ChoiceOption<T> choiceOption(String name, Class<T> klass, Value<T> defaultValue,
                                                   Value<T>... choices) {
        return new ChoiceOption<>(name, klass, defaultValue, Arrays.asList(choices));
    }

    /**
     * Parse a configuration schema string.
     * <p>
     * Example: https://${SERVER}:${PORT}
     *
     * @param s the scheme to parse
     * @return {@link Pair} consisting of
     * <ul>
     *     <li>list of options
     *     <li>scheme with var arguments removed
     * </ul>
     */
    public static Pair<String, List<Option<?>>> parseScheme(String s) {
        // extract options
        List<Option<?>> list = new ArrayList<>();
        Matcher matcher = PATTERN_VAR.matcher(s);
        while (matcher.find()) {
            String name = matcher.group("name");
            Map<String, String> arguments = extractArgs(matcher);
            Option<?> option = createOption(name, arguments);
            list.add(option);
        }

        // remove arguments from scheme
        String r = PATTERN_VAR.matcher(s).replaceAll("\\$\\{${name}\\}");

        return Pair.of(r, list);
    }

    /**
     * Create Option instance (used by {@link #parseScheme(String)}).
     *
     * @param name      the option's name
     * @param arguments the option's arguments
     * @return new option instance
     */
    private static Option<?> createOption(String name, Map<String, String> arguments) {
        String type = arguments.getOrDefault(OPTION_TYPE, OPTION_TYPE_STRING);
        String dflt = arguments.get("default");
        Option<?> option;
        switch (type) {
            case OPTION_TYPE_STRING:
                option = Option.stringOption(name, dflt);
                break;
            case OPTION_TYPE_FILE:
                option = Option.fileOption(name, () -> (dflt == null ? null : new File(dflt)), arguments.get("extension"));
                break;
            case OPTION_TYPE_INTEGER:
                option = Option.intOption(name, dflt == null ? 0 : Integer.parseInt(dflt));
                break;
            case OPTION_TYPE_DOUBLE:
                option = Option.doubleOption(name, dflt == null ? 0.0 : Double.parseDouble(dflt));
                break;
            default:
                throw new IllegalStateException("unsupported type: " + type);
        }
        return option;
    }

    /**
     * Extract arguments (used by {@link #parseScheme(String)}).
     *
     * @param matcher the current matcher instance that matches a single option declaration
     * @return map of arguments for the option matched by matcher
     */
    private static Map<String, String> extractArgs(Matcher matcher) {
        Map<String, String> arguments = new HashMap<>();
        String arg = matcher.group("arg1");
        if (arg != null) {
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
        return arguments;
    }

    private static void addArgument(Map<String, String> arguments, String arg, String val) {
        var old = arguments.put(arg, val);
        if (old != null) {
            LOG.log(Level.WARNING, () -> String.format("while parsing option string: multiple values for argument '%s'", arg));
        }
    }

    public <T> Value<T> toValue(T v) {
        return value(v);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Option<?> other = (Option<?>) obj;
        return name.equals(other.name) && klass == other.klass;
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
            if (obj == null || obj.getClass() != getClass()) {
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
        private final OpenMode mode;

        FileOption(String name, Value<File> defaultValue, OpenMode mode, String... extensions) {
            super(name, File.class, defaultValue);
            this.extensions = List.of(extensions);
            this.mode = mode;
        }

        public List<String> getExtensions() {
            return Collections.unmodifiableList(extensions);
        }

        public OpenMode getMode() {
            return mode;
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            FileOption other = (FileOption) obj;
            return other.mode == mode && other.extensions.equals(extensions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mode, extensions.size());
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

        @Override
        @SuppressWarnings("unchecked")
        public Value<T> toValue(Object v) {
            for (Value<T> value : getChoices()) {
                if (value.toString().equals(v.toString())) {
                    return value;
                }
            }
            throw new IllegalArgumentException(String.format("invalid value for option %s: %s", getName(), v));
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }

            ChoiceOption<?> other = (ChoiceOption<?>) obj;
            return other.choices.equals(choices);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), choices.size());
        }
    }
}
