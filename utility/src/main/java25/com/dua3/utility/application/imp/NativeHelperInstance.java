package com.dua3.utility.application.imp;

import com.dua3.utility.lang.Platform;

/**
 * Implementation of {@link com.dua3.utility.application.NativeHelper} that delegates to a platform-specific implementation.
 */
public final class NativeHelperInstance implements com.dua3.utility.application.NativeHelper {

    private NativeHelperInstance() {
    }

    private static final class SingletonHolder {
        private static final com.dua3.utility.application.NativeHelper INSTANCE = switch (Platform.currentPlatform()) {
            case MACOS -> NativeHelperMacOs.getInstance();
            case WINDOWS -> NativeHelperWindows.getInstance();
            case LINUX -> NativeHelperLinux.getInstance();
            default -> NativeHelperUnsupported.getInstance();
        };
    }

    /**
     * Returns the singleton instance of {@link com.dua3.utility.application.NativeHelper}.
     *
     * @return the singleton instance
     */
    public static com.dua3.utility.application.NativeHelper get() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public boolean setWindowDecorations(boolean dark) {
        return SingletonHolder.INSTANCE.setWindowDecorations(dark);
    }
}
