package com.dua3.utility.cmd;

import java.util.Optional;

/**
 * Exception class to throw when command line arguments do not match the options defined by the command line parser.
 */
public class CmdException extends IllegalStateException {
    public static class ConversionException extends CmdException {
        Option<?> option;String parameter;

        public ConversionException(Option<?> option, String parameter, Exception e) {
            super("invalid value passed to "+option.name()+": "+parameter, e);
            this.option = option;
            this.parameter = parameter;
        }

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
