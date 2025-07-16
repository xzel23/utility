package com.dua3.utility.options;

/**
 * Exception class to throw when command line arguments and/or values for configuration options do not match the
 * allowed values defined by the option/parser.
 */
public class OptionException extends ArgumentsException {
    /**
     * The {@link Option} whose parameter could not be converted to the target tape.
     */
    private final transient Option<?> option;

    /**
     * Constructor.
     *
     * @param option the option that caused the exception
     * @param msg exception message
     */
    public OptionException(Option<?> option, String msg) {
        super(msg);
        this.option = option;
    }

    /**
     * Constructor.
     *
     * @param option the option that caused the exception
     * @param msg exception message
     * @param e   cause
     */
    public OptionException(Option<?> option, String msg, Exception e) {
        super(msg, e);
        this.option = option;
    }

    /**
     * Retrieves the option that caused this exception.
     *
     * @return the option that caused this exception
     */
    public Option<?> getOption() {
        return option;
    }
}
