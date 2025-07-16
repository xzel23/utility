package com.dua3.utility.options;

import com.dua3.utility.data.Converter;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.TextUtil;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Represents a parameter with a set of attributes, behaviors, and validation rules. It provides a way to define parameters
 * of various types with optional constraints, default values, and validation logic. It is generic and accepts a type
 * parameter specifying the type of the parameter value.
 *
 * @param <T> The type of the parameter value.
 * @param displayName A human-readable name for the parameter.
 * @param description A detailed description of the parameter.
 * @param argName The name used to identify the argument in a command or configuration.
 * @param targetType The class type of the parameter's value.
 * @param converter A converter used to transform a string array representation of the argument into the specified type.
 * @param allowedValues A list of allowed values for the parameter, limiting what it can accept.
 * @param validate A function used to validate the parameter value. It returns an optional error message if validation fails.
 * @param argRepetitions The allowed repetitions for the argument (e.g., exact, zero, or one).
 */
public record Param<T>(
        String displayName,
        String description,
        String argName,
        Class<T> targetType,
        Converter<String[], @Nullable T> converter,
        List<T> allowedValues,
        Function<T, Optional<String>> validate,
        Repetitions argRepetitions
) {
    /**
     * An enumeration defining whether a value is required or optional.
     */
    public enum Required {
        /**
         * Indicates that a value is required.
         */
        REQUIRED,
        /**
         * Indicates that a value is optional.
         */
        OPTIONAL;

        /**
         * Converts the current {@code Required} instance to a corresponding {@link Repetitions} value.
         *
         * @return {@link Repetitions#EXACTLY_ONE} if the current value is {@code REQUIRED},
         *         otherwise {@link Repetitions#ZERO_OR_ONE}.
         */
        public Repetitions toRepetitions() {
            return this == REQUIRED ? Repetitions.EXACTLY_ONE : Repetitions.ZERO_OR_ONE;
        }
    }

    /**
     * Represents a parameter with metadata and validation logic for use in a system.
     *
     * @param displayName the name of the parameter to be displayed to users
     * @param description a brief description of what the parameter is for
     * @param argName the argument name to be used when passing this parameter
     * @param targetType the class type of the parameter's expected value
     * @param converter a converter that transforms string input into the target type
     * @param allowedValues a list of permissible values for the parameter; can be null if no restrictions apply
     * @param validate a validation function that checks the parameter value and returns an optional validation error message
     * @param required specifies if this parameter is mandatory or optional
     */
    public Param(
            String displayName,
            String description,
            String argName,
            Class<T> targetType,
            Converter<String, T> converter,
            List<@Nullable T> allowedValues,
            Function<T, Optional<String>> validate,
            Required required
    ) {
        this(
                displayName,
                description,
                argName,
                targetType,
                new SingleElementConverter<>(converter),
                allowedValues,
                validate,
                required.toRepetitions()
        );
    }

    private static final class SingleElementConverter<T> implements Converter<String[], @Nullable T> {
        public static final String[] ZERO_LENGTH_STRING_ARRAY = {};
        private final Function<String[], @Nullable T> a2b;
        private final Function<@Nullable T, String[]> b2a;

        private SingleElementConverter(Converter<String, @Nullable T> elementConverter) {
            this.a2b = strings -> switch (strings.length) {
                case 0 -> null;
                case 1 -> elementConverter.convert(strings[0]);
                default -> throw new IllegalArgumentException("Expected array length 0 or 1, got %d".formatted(strings.length));
            };

            this.b2a = v -> v == null ? ZERO_LENGTH_STRING_ARRAY : new String[]{elementConverter.convertBack(v)};
        }

        @Override
        public Function<String[], @Nullable T> a2b() {
            return a2b;
        }

        @Override
        public Function<@Nullable T, String[]> b2a() {
            return b2a;
        }
    }

    /**
     * Creates a {@code Param<String>} instance configured to handle string parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a descriptive text explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<String>} instance representing the configured string parameter
     */
    public static Param<String> ofString(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description, argName,
                String.class,
                Converter.identity(),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a {@code Param<String>} instance configured to handle string parameter values
     * with an additional validation predicate.
     *
     * @param displayName the name to display for the parameter
     * @param description a description explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @param validatPredicate a predicate for validating the parameter value
     * @return a {@code Param<String>} instance representing the configured string parameter
     */
    public static Param<String> ofString(
            String displayName,
            String description,
            String argName,
            Required required,
            Predicate<String> validatPredicate
    ) {
        Function<String, Optional<String>> validator = v -> {
            if (validatPredicate.test(v)) {
                return Optional.empty();
            } else {
                return Optional.of(getInvalidValueMessage(displayName, argName, v));
            }
        };

        return new Param<>(
                displayName,
                description, argName,
                String.class,
                Converter.identity(),
                List.of(),
                validator,
                required
        );
    }

    /**
     * Constructs and returns a message indicating an invalid value for a given argument.
     *
     * @param displayName the display name of the related entity or field
     * @param argName the name of the argument that has the invalid value
     * @param v the invalid value causing the issue
     * @return a formatted string containing the invalid value message
     */
    private static String getInvalidValueMessage(String displayName, String argName, Object v) {
        return "invalid value for %s (%s): %s".formatted(displayName, argName, v);
    }

    /**
     * Creates a {@code Param<String>} instance configured to handle string parameter values
     * with an additional validation regex pattern.
     *
     * @param displayName the name to display for the parameter
     * @param description a description explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @param regexValid a regular expression used to validate the parameter value
     * @return a {@code Param<String>} instance representing the configured string parameter
     */
    public static Param<String> ofString(
            String displayName,
            String description,
            String argName,
            Required required,
            String regexValid
    ) {
        Predicate<String> predicate = Pattern.compile(regexValid).asMatchPredicate();
        return ofString(
                displayName,
                description,
                argName,
                required,
                predicate
        );
    }

    /**
     * Creates a {@code Param<Boolean>} instance configured to handle boolean parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a descriptive text explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<Boolean>} instance representing the configured boolean parameter
     */
    public static Param<Boolean> ofBoolean(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description, argName,
                Boolean.class,
                Converter.stringConverter(Boolean.class),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a {@code Param<Integer>} instance configured to handle integer parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a description explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<Integer>} instance representing the configured integer parameter
     */
    public static Param<Integer> ofInt(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                Integer.class,
                Converter.stringConverter(Integer.class),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a new integer parameter with validation.
     *
     * @param displayName The name of the parameter to be displayed.
     * @param description A brief description of the parameter.
     * @param argName The name of the argument associated with the parameter.
     * @param required Specifies whether the parameter is required.
     * @param validatPredicate A predicate to validate the integer value of the parameter.
     * @return A new {@code Param<Integer>} instance based on the provided inputs.
     */
    public static Param<Integer> ofInt(
            String displayName,
            String description,
            String argName,
            Required required,
            Predicate<Integer> validatPredicate
    ) {
        Function<Integer, Optional<String>> validator = v -> {
            if (validatPredicate.test(v)) {
                return Optional.empty();
            } else {
                return Optional.of("invalid value for %s (%s): %d".formatted(displayName, argName, v));
            }
        };

        return new Param<>(
                displayName,
                description,
                argName,
                Integer.class,
                Converter.stringConverter(Integer.class),
                List.of(),
                validator,
                required
        );
    }

    /**
     * Creates a {@code Param<Long>} instance configured to handle long parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a descriptive text explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<Long>} instance representing the configured long parameter
     */
    public static Param<Long> ofLong(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                Long.class,
                Converter.stringConverter(Long.class),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a {@code Param<Double>} instance configured to handle double parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a descriptive text explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<Double>} instance representing the configured double parameter
     */
    public static Param<Double> ofDouble(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                Double.class,
                Converter.stringConverter(Double.class),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a {@code Param<Path>} instance configured to handle {@code Path} parameter values.
     *
     * @param displayName the name to display for the parameter
     * @param description a descriptive text explaining the purpose or usage of the parameter
     * @param argName the name of the argument associated with the parameter
     * @param required a flag indicating whether the parameter is mandatory or optional
     * @return a {@code Param<Path>} instance representing the configured {@code Path} parameter
     */
    public static Param<Path> ofPath(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                Path.class,
                Converter.stringConverter(Paths::get),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a new instance of {@link Param} for a {@link Path}.
     *
     * @param displayName the human-readable name of the parameter
     * @param description the detailed description of the parameter
     * @param argName the argument name associated with this parameter
     * @param required an enum indicating if the parameter is mandatory
     * @param validatPredicate a predicate to validate the {@link Path} value
     * @return a new {@link Param} instance configured for a {@link Path}
     */
    public static Param<Path> ofPath(
            String displayName,
            String description,
            String argName,
            Required required,
            Predicate<Path> validatPredicate
    ) {
        Function<Path, Optional<String>> validator = v -> {
            if (validatPredicate.test(v)) {
                return Optional.empty();
            } else {
                return Optional.of(getInvalidValueMessage(displayName, argName, v));
            }
        };

        return new Param<>(
                displayName,
                description,
                argName,
                Path.class,
                Converter.stringConverter(Paths::get),
                List.of(),
                validator,
                required
        );
    }

    /**
     * Creates a parameter of type URI with the specified configurations.
     *
     * @param displayName the name of the parameter to be displayed
     * @param description the description of the parameter
     * @param argName the argument name for the parameter
     * @param required indicates whether the parameter is required
     * @return a Param instance of type URI configured with the given attributes
     */
    public static Param<URI> ofUri(
            String displayName,
            String description,
            String argName,
            Required required
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                URI.class,
                Converter.stringConverter(Param::toUri),
                List.of(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a new parameter of type URI with the specified properties.
     *
     * @param displayName  the display name for the parameter
     * @param description  a brief description of the parameter
     * @param argName      the argument name associated with this parameter, used in user inputs
     * @param required     the requirement indicator specifying whether the parameter is mandatory
     * @param validatPredicate  a predicate to validate the provided URI parameter value
     * @return a new instance of {@code Param<URI>} configured with the specified properties
     */
    public static Param<URI> ofUri(
            String displayName,
            String description,
            String argName,
            Required required,
            Predicate<URI> validatPredicate
    ) {
        Function<URI, Optional<String>> validator = v -> {
            if (validatPredicate.test(v)) {
                return Optional.empty();
            } else {
                return Optional.of(getInvalidValueMessage(displayName, argName, v));
            }
        };

        return new Param<>(
                displayName,
                description,
                argName,
                URI.class,
                Converter.stringConverter(Param::toUri),
                List.of(),
                validator,
                required
        );
    }

    /**
     * Converts the given string into a URI object. If the string cannot be converted
     * into a valid URI, an ArgumentsException is thrown.
     *
     * @param s the string to convert into a URI
     * @return the URI object corresponding to the given string
     * @throws ArgumentsException if the string is not a valid URI
     */
    private static URI toUri(String s) {
        try {
            return new URI(s);
        } catch (Exception e) {
            throw new ArgumentsException("invalid URI: %s".formatted(s), e);
        }
    }

    /**
     * Creates a parameter instance with a list of predefined constant values.
     *
     * @param <T>          The type of the parameter's value.
     * @param displayName  The user-friendly name of the parameter.
     * @param description  A detailed description of the parameter.
     * @param argName      The argument name to be used in external representations, such as CLI or configuration.
     * @param required     Indicates whether the parameter is mandatory.
     * @param targetType   The class type of the allowed parameter values.
     * @param allowedValues A list of allowed constant values for this parameter.
     * @return A parameter instance with the specified options and constraints.
     */
    public static <T> Param<T> ofConstants(
            String displayName,
            String description,
            String argName,
            Required required,
            Class<T> targetType,
            List<T> allowedValues
    ) {
        return ofConstants(
                displayName,
                description,
                argName,
                required,
                targetType,
                Converter.stringConverter(targetType),
                allowedValues
        );
    }

    /**
     * Creates a parameter that accepts only a predefined set of constant values.
     *
     * @param <T> the type of the parameter value
     * @param displayName the display name of the parameter
     * @param description a description of the parameter
     * @param argName the argument name used for the parameter
     * @param required indicates whether the parameter is required or optional
     * @param targetType the class type of the parameter value
     * @param converter a converter to transform the input string into the target type
     * @param allowedValues a list of allowed constant values for the parameter
     * @return a new instance of {@code Param<T>} initialized with the specified values
     */
    public static <T> Param<T> ofConstants(
            String displayName,
            String description,
            String argName,
            Required required,
            Class<T> targetType,
            Converter<String, T> converter,
            List<@Nullable T> allowedValues
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                targetType,
                converter,
                allowedValues,
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a parameter of an enum type with the specified attributes.
     *
     * @param <T>        the type of the enum
     * @param displayName the human-readable name of the parameter
     * @param description a brief description of the parameter's purpose
     * @param argName     the argument name to be used for the parameter
     * @param required    the requirement status of the parameter
     * @param targetType  the enum class type to which this parameter belongs
     * @return a {@code Param<T>} object configured with the specified details
     */
    public static <T extends Enum<T>> Param<T> ofEnum(
            String displayName,
            String description,
            String argName,
            Required required,
            Class<T> targetType
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                targetType,
                Converter.stringConverter(targetType),
                LangUtil.asUnmodifiableList(LangUtil.enumValues(targetType)),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a new instance of {@code Param} for an object parameter.
     *
     * @param <T> the type of the parameter's value
     * @param displayName the display name of the parameter
     * @param description a description providing details about the parameter
     * @param argName the argument name used in command-line or user input
     * @param required specifies whether the parameter is required
     * @param targetType the class type of the parameter's value
     * @param converter the converter used to transform the input string into the target type
     * @return a new instance of {@code Param} configured for an object parameter
     */
    public static <T> Param<T> ofObject(
            String displayName,
            String description,
            String argName,
            Required required,
            Class<T> targetType,
            Converter<String, T> converter
    ) {
        return new Param<>(
                displayName,
                description,
                argName,
                targetType,
                converter,
                Collections.emptyList(),
                v -> Optional.empty(),
                required
        );
    }

    /**
     * Creates a parameter for a list of strings, taking in display name, description,
     * argument name, and repetition rules.
     *
     * @param displayName The display name of the parameter.
     * @param description A description of the parameter.
     * @param argName The name of the argument.
     * @param repetitions Specifies the allowed repetition rules for the parameter.
     * @return A parameter representing a list of strings.
     */
    @SuppressWarnings("unchecked")
    public static Param<List<String>> ofStrings(
            String displayName, String description, String argName,
            Repetitions repetitions
    ) {
        return ofList(
                displayName,
                description,
                argName,
                Converter.identity(),
                repetitions
        );
    }

    /**
     * Creates a {@link Param} instance that represents a parameter expecting a list
     * of values with specified validation constraints.
     *
     * @param <T> The type of elements within the list.
     * @param displayName The name to display for the parameter in user interfaces or messages.
     * @param description A textual description of the parameter to explain its purpose.
     * @param argName The argument name used to represent this parameter.
     * @param elementConverter A converter that transforms input strings into elements of type {@code T}.
     * @param repetitions A constraint specifying the minimum and maximum number of elements allowed in the list.
     * @return A {@link Param} object configured to represent a list of values of type {@code T}, with specified constraints.
     */
    @SuppressWarnings("unchecked")
    public static <T> Param<List<T>> ofList(
            String displayName,
            String description,
            String argName,
            Converter<String, T> elementConverter,
            Repetitions repetitions
    ) {
        Class<List<T>> targetClass = (Class<List<T>>) (Class<?>) List.class;
        return new Param<>(
                displayName,
                description,
                argName,
                targetClass,
                listConverter(elementConverter),
                Collections.emptyList(),
                v -> LangUtil.isBetween(((List<?>)v).size(), repetitions.min(), repetitions.max())
                        ? Optional.empty()
                        : Optional.of("expected %d to %d arguments for option %s, got %d".formatted(repetitions.min(), repetitions.max(), displayName, v.size())),
                repetitions
        );
    }

    /**
     * Creates a Converter that transforms a String array into a List of a specified type
     * and vice versa, using a provided element converter.
     *
     * @param elementConverter the Converter used to handle transformation of individual elements
     *                         between their String representation and the target type
     * @return a Converter that processes String arrays into Lists of the target type and
     *         Lists of the target type back into String arrays
     */
    private static <T> Converter<String[], List<T>> listConverter(Converter<String, T> elementConverter) {
        return Converter.create(
                s -> mapList(s, elementConverter.a2b()),
                v -> formatList(v, elementConverter.b2a())
        );
    }

    /**
     * Maps an array of strings to a list of elements of type T using the provided mapping function.
     *
     * @param <T> the type of elements in the resulting list
     * @param strings an array of strings to be mapped
     * @param elementMapper a function that defines how each string in the array is mapped to an element of type T
     * @return an unmodifiable list of elements of type T resulting from the transformation of the input array
     */
    private static <T> List<T> mapList(String[] strings, Function<String, T> elementMapper) {
        List<T> elements = new ArrayList<>(strings.length);
        for (String part : strings) {
            elements.add(elementMapper.apply(part));
        }
        return Collections.unmodifiableList(elements);
    }

    /**
     * Formats a list of elements into an array of strings using the provided formatting function.
     *
     * @param <T>              the type of elements in the list
     * @param elements         the list of elements to be formatted
     * @param elementFormatter a function that formats each element of the list into a string
     * @return an array of formatted strings representing the elements in the list
     */
    private static <T> String[] formatList(List<T> elements, Function<T, String> elementFormatter) {
        return elements.stream().map(elementFormatter).toArray(String[]::new);
    }

    /**
     * Determines whether the parameter has a list of allowed values defined.
     *
     * @return true if there are defined allowed values for the parameter; false otherwise
     */
    public boolean hasAllowedValues() {
        return !allowedValues.isEmpty();
    }

    /**
     * Converts the given value into a formatted text representation using a default delimiter, open quote, and close quote.
     *
     * @param value the value to be formatted and converted into its textual representation
     * @return a string representation of the given value, formatted with default settings
     */
    public String getText(T value) {
        return getText(value, " ", "", "");
    }

    /**
     * Converts the given value into a formatted textual representation using the specified delimiter,
     * open quote, and close quote. The value is split into parts, each wrapped with the specified
     * quotes, and joined using the provided delimiter.
     *
     * @param value the value to be formatted and converted into its textual representation
     * @param delimiter the string used to separate each formatted part of the value
     * @param openQuote the string used to open quotes around each part of the value
     * @param closeQuote the string used to close quotes around each part of the value
     * @return a formatted string representation of the given value with the specified delimiter
     *         and quotes applied
     */
    public String getText(T value, String delimiter, String openQuote, String closeQuote) {
        String[] strings = converter.convertBack(value);
        StringBuilder sb = new StringBuilder();
        String d = "";
        for (String s : strings) {
            sb.append(d);
            sb.append(openQuote);
            sb.append(s);
            sb.append(closeQuote);
            d = delimiter;
        }
        return sb.toString();
    }
}
