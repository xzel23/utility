package com.dua3.utility.db;

import com.dua3.utility.data.Pair;
import com.dua3.utility.options.Arguments;
import com.dua3.utility.options.SimpleOption;
import com.dua3.utility.text.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC driver information.
 */
public class JdbcDriverInfo {

    /**
     * Identifier String for the option type.
     */
    public static final String OPTION_TYPE = "type";
    /**
     * Type identifier String for file options.
     */
    public static final String OPTION_TYPE_PATH = "file";
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
    private static final Logger LOG = LoggerFactory.getLogger(JdbcDriverInfo.class);
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

    /**
     * The driver name.
     */
    public final String name;
    /**
     * The driver's class name.
     */
    public final String className;
    /**
     * URL prefix used by this driver (used to identify the correct driver to use when accessing a database URL).
     */
    public final String urlPrefix;
    /**
     * The URL scheme for JDBC connections used by this driver.
     */
    public final String urlScheme;
    /**
     * Link to the vendor's driver webpage.
     */
    public final String link;
    /**
     * This driver's options.
     */
    public final Collection<SimpleOption<?>> options;

    /**
     * Constructor.
     *
     * @param name      driver name
     * @param className driver's class name
     * @param urlPrefix URL prefix used by this driver
     * @param urlScheme URL scheme for JDBC connections
     * @param link      link to driver vendor webpage
     */
    public JdbcDriverInfo(String name, String className, String urlPrefix, String urlScheme, String link) {
        this.name = name;
        this.className = className;
        this.urlPrefix = urlPrefix;
        this.link = link;

        Pair<String, List<SimpleOption<?>>> parsed = parseScheme(urlScheme);
        this.urlScheme = parsed.first();
        this.options = parsed.second();
    }

    /**
     * Parse a configuration schema string.
     * <p>
     * Example string: {@code "https://${SERVER}:${PORT}"}
     *
     * @param s the scheme to parse
     * @return {@link Pair} consisting of
     * <ul>
     *     <li>list of options
     *     <li>scheme with var arguments removed
     * </ul>
     */
    private static Pair<String, List<SimpleOption<?>>> parseScheme(CharSequence s) {
        // extract options
        List<SimpleOption<?>> list = new ArrayList<>();
        Matcher matcher = PATTERN_VAR.matcher(s);
        while (matcher.find()) {
            String name = matcher.group("name");
            Map<String, String> arguments = extractArgs(matcher);
            SimpleOption<?> option = createOption(name, arguments);
            list.add(option);
        }

        // remove arguments from scheme
        String r = PATTERN_VAR.matcher(s).replaceAll("\\$\\{${name}\\}");

        return Pair.of(r, list);
    }

    /**
     * Extract arguments (used by {@link #parseScheme(CharSequence)}).
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

    private static void addArgument(Map<? super String, String> arguments, String arg, String val) {
        String old = arguments.put(arg, val);
        //noinspection VariableNotUsedInsideIf
        if (old != null) {
            LOG.warn("while parsing option string: multiple values for argument '{}'", arg);
        }
    }

    /**
     * Create Option instance (used by {@link #parseScheme(CharSequence)}).
     *
     * @param name      the option's name
     * @param arguments the option's arguments
     * @return new option instance
     */
    private static SimpleOption<?> createOption(String name, Map<String, String> arguments) {
        String type = arguments.getOrDefault(OPTION_TYPE, OPTION_TYPE_STRING);
        String dflt = arguments.get("default");
        return switch (type) {
            case OPTION_TYPE_STRING -> SimpleOption.create(s -> s, name).description(name).defaultValue(dflt);
            case OPTION_TYPE_PATH ->
                    SimpleOption.create(Paths::get, name).description(name).defaultValue(dflt == null ? null : Paths.get(dflt));
            case OPTION_TYPE_INTEGER ->
                    SimpleOption.create(Integer::valueOf, name).description(name).defaultValue(dflt == null ? null : Integer.valueOf(dflt));
            case OPTION_TYPE_DOUBLE ->
                    SimpleOption.create(Double::valueOf, name).description(name).defaultValue(dflt == null ? null : Double.valueOf(dflt));
            default -> throw new IllegalStateException("unsupported type: " + type);
        };
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Get driver description text.
     *
     * @return the driver description
     */
    public String description() {
        return String.format(Locale.ROOT,
                "%s%n  driver class : %s%n  URL prefix   : %s%n  URL scheme   : %s%n  vendor link  : %s%n%s%n",
                name,
                className,
                urlPrefix,
                urlScheme,
                link,
                options);
    }

    /**
     * Construct URL from URL-scheme and supplied options
     *
     * @param values the option values to set in the URL
     * @return the connection URL
     */
    public String getUrl(Arguments values) {
        return TextUtil.transform(urlScheme,
                s -> Objects.toString(
                        values.get(
                                getOption(s).orElseThrow(() -> new NoSuchElementException("No value present"))
                        ).orElseThrow(),
                        "")
        );
    }

    private Optional<SimpleOption<?>> getOption(String s) {
        return options.stream().filter(opt -> opt.names().contains(s)).findFirst();
    }
}
