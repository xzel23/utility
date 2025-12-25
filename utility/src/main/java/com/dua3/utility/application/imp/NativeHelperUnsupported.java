package com.dua3.utility.application.imp;

/**
 * Fallback implementation of {@link com.dua3.utility.application.NativeHelper} for unsupported platforms.
 */
public final class NativeHelperUnsupported implements com.dua3.utility.application.NativeHelper {

    private NativeHelperUnsupported() {
    }

    private static final class Holder {
        private static final NativeHelperUnsupported INSTANCE = new NativeHelperUnsupported();
    }

    /**
     * Returns the singleton instance of {@link NativeHelperUnsupported}.
     *
     * @return the singleton instance
     */
    public static NativeHelperUnsupported getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean setWindowDecorations(boolean dark) {
        return false;
    }
}
