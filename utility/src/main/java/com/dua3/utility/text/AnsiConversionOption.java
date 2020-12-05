package com.dua3.utility.text;

import java.util.function.Consumer;

/**
 * Options controling the conversion process.
 */
public class AnsiConversionOption {

    /**
     * Send RESET sequence at beginning.
     * @param flag set to true to enable RESET before output
     * @return the option to use
     */
    public static AnsiConversionOption reset(boolean flag) {
        return new AnsiConversionOption(c -> c.setReset(flag));
    }

    /**
     * Enable reverse video ofr output.
     * @param flag set to true to enable reverse video output
     * @return the option to use
     */
    public static AnsiConversionOption reverseVideo(boolean flag) {
        return new AnsiConversionOption(c -> c.setReverseVideo(flag));
    }
    
    private final Consumer<AnsiConverter> action;

    protected AnsiConversionOption(Consumer<AnsiConverter> action) {
        this.action = action;
    }

    void apply(AnsiConverter converter) {
        action.accept(converter);
    }
}
