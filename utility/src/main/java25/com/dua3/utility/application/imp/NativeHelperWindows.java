package com.dua3.utility.application.imp;

/**
 * Implementation of {@link com.dua3.utility.application.NativeHelper} for Windows.
 */
public final class NativeHelperWindows implements com.dua3.utility.application.NativeHelper {

    private NativeHelperWindows() {
    }

    private static final class Holder {
        private static final NativeHelperWindows INSTANCE = new NativeHelperWindows();
    }

    /**
     * Returns the singleton instance of {@link NativeHelperWindows}.
     *
     * @return the singleton instance
     */
    public static NativeHelperWindows getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean setWindowDecorations(boolean dark) {
        return false;
    }
}
