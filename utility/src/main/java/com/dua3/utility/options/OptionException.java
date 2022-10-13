package com.dua3.utility.options;

/**
 * Exception class to throw when command line arguments and/or values for configuration options do not match the 
 * allowed values defined by the option/parser.
 */
public class OptionException extends IllegalStateException {
    /**
     * Exception thrown when a parameter argument's String value could not be converted to the target type. 
     */
    public static class ParameterConversionException extends OptionException {
        final Option<?> option;
        final String parameter;

        /**
         * Constructor.
         * @param option the option the argument belongs to
         * @param parameter the parameter value as String
         * @param e the parent exception
         */
        public ParameterConversionException(Option<?> option, String parameter, Exception e) {
            super("invalid value passed to " + option.name() + ": " + parameter, e);
            this.option = option;
            this.parameter = parameter;
        }

        /**
         * Constructor.
         * @param option the option the argument belongs to
         * @param parameter the parameter value as String
         */
        public ParameterConversionException(Option<?> option, String parameter) {
            super("invalid value passed to " + option.name() + ": " + parameter);
            this.option = option;
            this.parameter = parameter;
        }
    }

    /**
     * Constructor.
     * @param msg exception message
     */
    public OptionException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     * @param msg exception message
     * @param e cause
     */
    public OptionException(String msg, Exception e) {
        super(msg, e);
    }
}
