package com.dua3.utility.application.imp;

/**
 * A concrete implementation of the {@link DarkModeDetectorBase} class.
 * <p>
 * This implementation does not support dark mode detection and always returns
 * {@code false} for both detection capability and dark mode state.
 */
public final class DarkModeDetectorUnsupported extends DarkModeDetectorBase {

    private static class Holder {
        private static final DarkModeDetectorUnsupported INSTANCE = new DarkModeDetectorUnsupported();
    }

    /**
     * Returns the singleton instance of the {@code DarkModeDetectorUnsupported} class.
     * This implementation does not support dark mode detection and always returns {@code false}
     * for both the detection capability and the dark mode state.
     *
     * @return the singleton instance of {@code DarkModeDetectorUnsupported}
     */
    public static DarkModeDetectorUnsupported getInstance() {
        return Holder.INSTANCE;
    }

    private DarkModeDetectorUnsupported() {}

    @Override
    public boolean isDarkModeDetectionSupported() {
        return false;
    }

    @Override
    public boolean isDarkMode() {
        return false;
    }

    @Override
    protected void monitorSystemChanges(boolean enable) {
        // nothing to do
    }
}
