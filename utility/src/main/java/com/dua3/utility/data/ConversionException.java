package com.dua3.utility.data;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * This exception is thrown to indicate that an error occurred during a
 * conversion process.
 */
public class ConversionException extends RuntimeException {

    private static @NonNull String getMessage(Class<?> sourceClass, Class<?> targetClass, @Nullable String message) {
        return "could not convert from " + sourceClass.getName() +
                " to " + targetClass.getName()
                + (message != null ? ": " + message : "");
    }

    /**
     * Constructs a new {@code ConversionException} with the specified source class, target class, and cause.
     *
     * @param sourceClass the class of the object that was being converted
     * @param targetClass the class to which the object was being converted
     * @param cause       the cause of the exception, which can be another throwable
     *                    that led to this exception
     */
    ConversionException(Class<?> sourceClass, Class<?> targetClass, Throwable cause) {
        super(getMessage(sourceClass, targetClass, null), cause);
    }

    /**
     * Constructs a new {@code ConversionException} with the specified source class, target class,
     * and detail message.
     *
     * @param sourceClass the class of the source object that failed to be converted
     * @param targetClass the class of the target type to which the source object failed to convert
     * @param message     the detail message, providing information about the reason for the failure
     */
    ConversionException(Class<?> sourceClass, Class<?> targetClass, String message) {
        super(getMessage(sourceClass, targetClass, message));
    }

    /**
     * Constructs a new {@code ConversionException} with detailed information about
     * the failed conversion, including the source and target classes, an optional
     * message, and a cause.
     *
     * @param sourceClass the class type of the source object that was being converted
     * @param targetClass the class type of the target object that the source was
     *                    being converted to
     * @param message     an optional detail message providing additional information
     *                    about the error
     * @param cause       the cause of the exception, which can be another throwable
     *                    that led to this exception
     */
    ConversionException(Class<?> sourceClass, Class<?> targetClass, String message, Throwable cause) {
        super(getMessage(sourceClass, targetClass, message), cause);
    }

    /**
     * Constructs a new {@code ConversionException} with the specified detail message.
     *
     * @param message the detail message, providing information about the reason for the exception
     */
    public ConversionException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code ConversionException} with the specified detail message
     * and cause.
     *
     * @param message the detail message, providing information about the reason
     *                for the exception
     * @param cause   the cause of the exception, which can be another throwable
     *                that led to this exception
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code ConversionException} with the specified cause.
     *
     * @param cause the cause of the exception, which can be another throwable
     *              that led to this exception
     */
    public ConversionException(Throwable cause) {
        super(cause);
    }
}
