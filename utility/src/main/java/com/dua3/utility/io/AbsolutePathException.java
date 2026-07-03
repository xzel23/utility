package com.dua3.utility.io;

/**
 * Exception thrown to indicate that a URI is expected to be relative, but is not.
 */
public class AbsolutePathException extends IllegalPathException {
    /**
     * Constructs a new {@code UriNotRelativeException} with the specified detail message.
     *
     * @param s the detail message, which provides additional information about the error
     */
    public AbsolutePathException(String s) {
        super(s);
    }

    /**
     * Constructs a new {@code UriNotRelativeException} with the specified detail message
     * and cause.
     *
     * @param message the detail message, which provides further information about the exception
     * @param cause   the cause of the exception, which can be retrieved later using {@link Throwable#getCause()}
     */
    public AbsolutePathException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new UriNotRelativeException with the specified cause.
     *
     * @param cause the cause of this exception, typically the underlying exception
     *              that resulted in a URI being non-relative
     */
    public AbsolutePathException(Throwable cause) {
        super(cause);
    }
}
