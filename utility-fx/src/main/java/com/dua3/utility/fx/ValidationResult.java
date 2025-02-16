package com.dua3.utility.fx;

import com.dua3.utility.lang.LangUtil;
import javafx.scene.control.Control;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the result of a validation process performed on a Control.
 * Contains information about the control being validated, the level of validation result, and an optional message.
 * @param control the control the result belongs to
 * @param level the {@code Level}
 * @param message the message
 */
public record ValidationResult(Control control, Level level, String message) {
    /**
     * Creates a ValidationResult with an OK level and an empty message for the given control.
     *
     * @param c the control for which the validation result is being created
     * @return a {@code ValidationResult} indicating an OK level for the given control
     */
    public static ValidationResult ok(Control c) {
        return new ValidationResult(c, Level.OK, "");
    }

    /**
     * Creates a {@code ValidationResult} indicating an error for the specified control,
     * with the given error message.
     *
     * @param c the control associated with the validation error
     * @param message the error message describing the validation failure
     * @return a {@code ValidationResult} indicating an error with the given message
     */
    public static ValidationResult error(Control c, String message) {
        return new ValidationResult(c, Level.ERROR, message);
    }

    /**
     * Determines if the ValidationResult level is OK.
     *
     * @return true if the validation level is OK, otherwise false.
     */
    public boolean isOk() {
        return level == Level.OK;
    }

    /**
     * Merges this {@code ValidationResult} with another {@code ValidationResult}.
     * If either result is OK, the other result is returned.
     * If both results are errors, a new result is created combining the error messages.
     *
     * @param other the {@code ValidationResult} to merge with this result
     * @return a new {@code ValidationResult} that represents the combined result of the merge
     */
    public ValidationResult merge(ValidationResult other) {
        LangUtil.check(other.control() == control(), "trying to merge results for different controls");
        if (isOk()) {
            return other;
        }
        if (other.isOk()) {
            return this;
        }
        return new ValidationResult(
                control(),
                Level.ERROR,
                Stream.of(message(), other.message()).filter(s -> !s.isBlank()).collect(Collectors.joining("\n"))
        );
    }

    /**
     * Enum representing the level of a validation result.
     */
    public enum Level {
        /**
         * Represents a successful or valid result in the validation process.
         */
        OK,
        /**
         * Represents an error level validation result.
         */
        ERROR
    }
}
