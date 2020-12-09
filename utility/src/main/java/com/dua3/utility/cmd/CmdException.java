package com.dua3.utility.cmd;

/**
 * Exception class to throw when command line arguments do not match the options defined by the command line parser.
 */
public class CmdException extends IllegalStateException {
    /**
     * Exception thrown when a parameter argument's String value could not be converted to the target type. 
     */
    public static class ConversionException extends CmdException {
        Option<?> option;String parameter;

        /**
         * Constructor.
         * @param option the option the argument belongds to
         * @param parameter the parameter value as String
         * @param e the parent exception
         */
        public ConversionException(Option<?> option, String parameter, Exception e) {
            super("invalid value passed to "+option.name()+": "+parameter, e);
            this.option = option;
            this.parameter = parameter;
        }

        /**
         * Constructor.
         * @param option the option the argument belongds to
         * @param parameter the parameter value as String
         */
        public ConversionException(Option<?> option, String parameter) {
            super("invalid value passed to "+option.name()+": "+parameter);
            this.option = option;
            this.parameter = parameter;
        }
    }
    
    CmdException(String fmt, Object... args) {
        super(String.format(fmt, args));
    }
    
    CmdException(String msg, Exception e) {
        super(msg, e);
    }
}
