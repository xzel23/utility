package com.dua3.utility.io;

import java.io.IOException;

/**
 * Signals that a folder is not empty.
 */
public class FolderNotEmptyException extends IOException {
    /**
     * Constructs a new {@code ObjectNotFoundException} with the specified detail message.
     *
     * @param message the detail message providing information about the exception
     */
    public FolderNotEmptyException(String message) {
        super(message);
    }

    /**
     * Constructs a new ObjectNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message explaining why the exception was thrown
     * @param cause   the underlying cause of the exception, or null if not available
     */
    public FolderNotEmptyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ObjectNotFoundException with the specified cause.
     *
     * @param cause the cause of this exception, which is saved for later retrieval by the {@link Throwable#getCause()} method
     */
    public FolderNotEmptyException(Throwable cause) {
        super(cause);
    }
}
