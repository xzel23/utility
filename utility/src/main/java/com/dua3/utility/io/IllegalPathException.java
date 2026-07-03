package com.dua3.utility.io;

import java.io.IOException;

/**
 * Thrown to indicate that a specified file path is invalid or illegal in the current context.
 * This exception is, for example, used to signal when a relative path points outside the root node of an
 * {@link ObjectStore} instance.
 */
public class IllegalPathException extends IOException {
    /**
     * Constructs a new {@code IllegalPathException} with the specified detail message.
     *
     * @param message the detail message, providing additional information about the exception
     */
    public IllegalPathException(String message) {
        super(message);
    }

    /**
     * Constructs a new IllegalPathException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the throwable cause of the exception
     */
    public IllegalPathException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new IllegalPathException with the specified cause.
     *
     * @param cause the underlying cause of the exception
     */
    public IllegalPathException(Throwable cause) {
        super(cause);
    }
}
