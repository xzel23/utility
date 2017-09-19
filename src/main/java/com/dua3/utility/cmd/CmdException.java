package com.dua3.utility.cmd;

public class CmdException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public static class MissingOptionException extends CmdException {
        private static final long serialVersionUID = 1L;

        public MissingOptionException(CmdOption option) {
            super(buildMessage(option), option);
        }

        private static String buildMessage(CmdOption option) {
            return String.format("The required option %s is missing.", option.getName());
        }
    }

    public static class MissingArgumentException extends CmdException {
        private static final long serialVersionUID = 1L;

        public MissingArgumentException(CmdOption option, int n) {
            super(buildMessage(option,n), option);
        }

        private static String buildMessage(CmdOption option, int n) {
            return String.format("Option %s requires at least %d arguments, actual number of arguments is %d.",
                    option.getName(), option.getMinArgs(), n);
        }
    }

    public static class ExcessiveArgumentException extends CmdException {
        private static final long serialVersionUID = 1L;

        public ExcessiveArgumentException(CmdOption option, int n) {
            super(buildMessage(option, n), option);
        }

        private static String buildMessage(CmdOption option, int n) {
            return String.format("Option %s supports at most %d arguments, actual number of arguments is %d.",
                    option.getName(), option.getMaxArgs(), n);
        }
    }

    private final CmdOption option;
    
    public CmdException(String s, CmdOption option) {
        super(s);
        this.option = option;
    }
    
}
