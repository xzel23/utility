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
 * @param argRepetitions The allowed repetitions for the argument (e.g., exact, zero or one).
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
    public enum Required {
        REQUIRED,
        OPTIONAL;

        public Repetitions toRepetitions() {
            return this == REQUIRED ? Repetitions.EXACTLY_ONE : Repetitions.ZERO_OR_ONE;
        }
    }

    public Param(
            String displayName,
            String description,
            String argName,
            Class<T> targetType,
            Converter<String, T> converter,
            List<T> allowedValues,
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
                return Optional.of("invalid value for %s (%s): %s".formatted(displayName, argName, v));
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
                return Optional.of("invalid value for %s (%s): %s".formatted(displayName, argName, v));
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
                return Optional.of("invalid value for %s (%s): %s".formatted(displayName, argName, v));
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

    private static URI toUri(String s) {
        try {
            return new URI(s);
        } catch (Exception e) {
            throw new ArgumentsException("invalid URI: %s".formatted(s), e);
        }
    }

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

    public static <T> Param<T> ofConstants(
            String displayName,
            String description,
            String argName,
            Required required,
            Class<T> targetType,
            Converter<String, T> converter,
            List<T> allowedValues
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

    private static <T> Converter<String[], List<T>> listConverter(Converter<String, T> elementConverter) {
        return Converter.create(
                s -> mapList(s, elementConverter.a2b()),
                v -> formatList(v, elementConverter.b2a())
        );
    }

    private static <T> List<T> mapList(String[] strings, Function<String, T> elementMapper) {
        List<T> elements = new ArrayList<>(strings.length);
        for (String part : strings) {
            elements.add(elementMapper.apply(part));
        }
        return Collections.unmodifiableList(elements);
    }

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
            sb.append(TextUtil.quoteIfNeeded(s));
            sb.append(closeQuote);
            d = delimiter;
        }
        return sb.toString();
    }
}
