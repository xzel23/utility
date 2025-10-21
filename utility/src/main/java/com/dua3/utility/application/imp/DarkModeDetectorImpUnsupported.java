package com.dua3.utility.application.imp;

/**
 * A concrete implementation of the {@link DarkModeDetectorBase} class.
 *
 * This implementation does not support dark mode detection and always returns
 * {@code false} for both detection capability and dark mode state.
 */
public class DarkModeDetectorImpUnsupported extends DarkModeDetectorBase {

    private static class Holder {
        private static final DarkModeDetectorImpUnsupported INSTANCE = new DarkModeDetectorImpUnsupported();
    }

    public static DarkModeDetectorImpUnsupported getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean isDarkModeDetectionSupported() {
        return false;
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }
}
