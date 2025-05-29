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

    /**
     * Exception thrown when a parameter argument's String value could not be converted to the target type.
     */
    public static class ParameterConversionException extends OptionException {
        /**
         * The string value of the parameter that could not be converted.
         */
        private final String parameter;

        /**
         * Constructor.
         *
         * @param option    the option the argument belongs to
         * @param parameter the parameter value as String
         * @param e         the parent exception
         */
        public ParameterConversionException(Option<?> option, String parameter, Exception e) {
            super(option, "invalid value passed to " + option.name() + ": " + parameter, e);
            this.parameter = parameter;
        }

        /**
         * Constructor.
         *
         * @param option    the option the argument belongs to
         * @param parameter the parameter value as String
         */
        public ParameterConversionException(Option<?> option, String parameter) {
            super(option, "invalid value passed to " + option.name() + ": " + parameter);
            this.parameter = parameter;
        }

        /**
         * Retrieves the parameter associated with this exception.
         *
         * @return the parameter associated with this exception
         */
        public String getParameter() {
            return parameter;
        }
    }
}
