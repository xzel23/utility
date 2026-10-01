package com.dua3.utility.io;

import java.io.IOException;

/**
 * Signals that an object is not a data object.
 */
public class NotADataObjectException extends IOException {
    /**
     * Constructs a new {@code NotADataObjectException} with the specified detail message.
     *
     * @param message the detail message providing information about the exception
     */
    public NotADataObjectException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code NotADataObjectException}  with the specified detail message and cause.
     *
     * @param message the detail message explaining why the exception was thrown
     * @param cause   the underlying cause of the exception, or null if not available
     */
    public NotADataObjectException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code NotADataObjectException}  with the specified cause.
     *
     * @param cause the cause of this exception, which is saved for later retrieval by the {@link Throwable#getCause()} method
     */
    public NotADataObjectException(Throwable cause) {
        super(cause);
    }
}
