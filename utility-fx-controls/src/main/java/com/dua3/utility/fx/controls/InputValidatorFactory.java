package com.dua3.utility.fx.controls;

import com.dua3.utility.text.MessageFormatter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A factory class for creating input validation functions. The factory provides mechanisms
 * to validate input strings based on different criteria, such as non-empty checks and
 * regular expression matching.
 * <p>
 * The factory uses a {@link MessageFormatter} for formatting error messages when validation fails.
 * The {@link MessageFormatter#format(String, Object...)} method is passed the format {@code fmt},
 * the current value, and the arguments passed as {@code args}.
 */
public class InputValidatorFactory {

    private final MessageFormatter formatter;

    /**
     * Constructs an instance of {@code InputValidatorFactory}.
     * The factory utilizes the provided {@link MessageFormatter} to format error messages
     * for input validation.
     *
     * @param formatter the {@link MessageFormatter} used for formatting error messages when validation fails
     */
    public InputValidatorFactory(MessageFormatter formatter) {
        this.formatter = formatter;
    }

    /**
     * Creates a validation function that checks if a string is non-empty.
     * If the string is empty, the provided error message format and arguments are used to create an error message.
     *
     * @param fmt the format string for the error message if validation fails
     * @param args optional arguments for formatting the error message
     * @return a function that takes a string and returns an {@code Optional<String>} containing
     *         the error message if the string is empty, or an empty {@code Optional} if it is valid
     */
    public Function<String, Optional<String>> nonEmpty(String fmt, Object... args) {
        return s -> validate(Predicate.not(String::isEmpty), s, fmt, args);
    }

    /**
     * Creates a validation function that checks if an input string matches a given regular expression pattern.
     * If the input matches the pattern, the function returns {@code Optional.empty()}.
     * Otherwise, it returns an {@code Optional} containing a formatted error message.
     *
     * @param pattern the regular expression pattern to match against the input string
     * @param fmt the format string for the error message to be returned on validation failure
     * @param args optional arguments to include in the formatted error message
     * @return a function that takes a string as input and returns an {@code Optional<String>} containing
     *         a validation error message if the input does not match the regular expression;
     *         otherwise {@code Optional.empty()} is returned
     */
    public Function<String, Optional<String>> regexp(String pattern, String fmt, Object... args) {
        return s -> validate(Pattern.compile(pattern).asMatchPredicate(), s, fmt, args);
    }

    /**
     * Validates a given value against a specified predicate and returns an error message
     * if the predicate test fails.
     *
     * @param <T>       the type of the value to be validated
     * @param predicate the predicate to test the value against
     * @param value     the value to be validated
     * @param fmt       the format string for the error message
     * @param args      optional arguments for formatting the error message
     * @return an {@code Optional} containing the formatted error message if validation fails,
     *         or an empty {@code Optional} if validation succeeds
     */
    private <T> Optional<String> validate(Predicate<T> predicate, T value, String fmt, Object... args) {
        if (predicate.test(value)) {
            return Optional.empty();
        } else {
            return Optional.of(format(fmt, value, args));
        }
    }

    /**
     * Formats a string using the provided format and arguments, with the first argument treated
     * specially as the primary value to be formatted.
     *
     * @param fmt      the format string containing placeholders to be replaced
     * @param firstArg the first argument to be included in the formatted output
     * @param args     additional arguments to be included in the formatted output
     * @return the formatted string with placeholders in the format string replaced by the corresponding arguments
     */
    private String format(String fmt, Object firstArg, Object... args) {
        Object[] fmtArgs = new Object[args.length + 1];
        fmtArgs[0] = firstArg;
        System.arraycopy(args, 0, fmtArgs, 1, args.length);
        return formatter.format(fmt, fmtArgs);
    }
}
