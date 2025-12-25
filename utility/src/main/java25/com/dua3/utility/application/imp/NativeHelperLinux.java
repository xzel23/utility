package com.dua3.utility.application.imp;

/**
 * Implementation of {@link com.dua3.utility.application.NativeHelper} for Linux.
 */
public final class NativeHelperLinux implements com.dua3.utility.application.NativeHelper {

    private NativeHelperLinux() {
    }

    private static final class Holder {
        private static final NativeHelperLinux INSTANCE = new NativeHelperLinux();
    }

    /**
     * Returns the singleton instance of {@link NativeHelperLinux}.
     *
     * @return the singleton instance
     */
    public static NativeHelperLinux getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean setWindowDecorations(boolean dark) {
        return false;
    }
}
