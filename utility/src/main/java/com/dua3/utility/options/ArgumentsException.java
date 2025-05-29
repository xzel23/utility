package com.dua3.utility.options;

/**
 * Exception thrown when argument processing fails.
 * <p>
 * See also {@link  com.dua3.utility.options.Arguments}, {@link OptionException}
 */
public class ArgumentsException extends IllegalStateException {

    /**
     * Constructs an ArgumentsException with no detail message.
     */
    public ArgumentsException() {
        super();
    }

    /**
     * Constructs an ArgumentsException with the specified detail message.
     *
     * @param s the detail message
     */
    public ArgumentsException(String s) {
        super(s);
    }

    /**
     * Constructs a new ArgumentsException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public ArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ArgumentsException with the specified cause.
     *
     * @param cause the cause
     */
    public ArgumentsException(Throwable cause) {
        super(cause);
    }
}
