package com.dua3.utility.cmd;

/**
 * Exception class to throw when command line arguments do not match the options defined by the command line parser.
 */
public class CmdException extends IllegalStateException {
    CmdException(String fmt, Object... args) {
        super(String.format(fmt, args));
    }
}
