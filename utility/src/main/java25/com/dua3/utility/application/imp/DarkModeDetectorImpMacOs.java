package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

/**
 * Dark mode detector implementation for macOS using the Java Foreign Function & Memory (FFM) API.
 *
 * <p>This class uses CoreFoundation to check if the system appearance is set to Dark.
 * It reads the preference key "AppleInterfaceStyle" from the ".GlobalPreferences" domain
 * and compares it against the string "Dark".</p>
 *
 * <p>Access is provided via getInstance() which lazily initializes a singleton. If initialization
 * fails (for example on non-macOS platforms or when native access is disallowed), a
 * {@link DarkModeDetectorImpUnsupported} instance is returned instead.</p>
 *
 * <p>Note: The implementation performs native calls and may require enabling native access
 * depending on the runtime configuration. This class resides in the Java 25 sourceset
 * and relies on the FFM API available in modern Java versions.</p>
 *
 * <p>You can optionally call startPolling(long) to receive change notifications via the
 * base class callback onChangeDetected(boolean). The polling thread is a daemon thread.</p>
 *
 * <p>Thread-safety: isDarkMode() uses a confined Arena per call; CoreFoundation symbol
 * handles are initialized once and reused, making it safe to call concurrently.</p>
 *
 * <p>Platform: macOS only.</p>
 */
public class DarkModeDetectorImpMacOs extends DarkModeDetectorBase {

    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorImpMacOs.class);

    /** Lazy-loaded singleton */
    private static class Holder {
        private static final DarkModeDetector INSTANCE = createInstance();

        private static DarkModeDetector createInstance() {
            try {
                return new DarkModeDetectorImpMacOs();
            } catch (Exception e) {
                LOG.error("DarkModeDetectorImpMacOs initialization failed", e);
                return new DarkModeDetectorImpUnsupported();
            }
        }
    }

    /**
         * Returns the lazily initialized singleton instance of the macOS dark mode detector.
         * If initialization fails, an {@link DarkModeDetectorImpUnsupported} instance is returned.
         *
         * @return the dark mode detector instance
         */
        public static DarkModeDetector getInstance() {
        return Holder.INSTANCE;
    }

    private final Linker linker = Linker.nativeLinker();
    private final Arena sharedArena = Arena.ofShared();

    // CoreFoundation handles
    private final MethodHandle cfPreferencesCopyAppValue;
    private final MethodHandle cfStringCompare;
    private final MethodHandle cfStringCreateWithCString;
    private final MethodHandle cfRelease;

    private static final int CF_STRING_ENCODING_UTF8 = 0x08000100;

    /** Defer all CF calls until first isDarkMode() invocation */
    private DarkModeDetectorImpMacOs() {
        try {
            SymbolLookup cf = SymbolLookup.libraryLookup(
                    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
                    sharedArena
            );

            cfPreferencesCopyAppValue = linker.downcallHandle(
                    cf.findOrThrow("CFPreferencesCopyAppValue"),
                    FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS)
            );

            cfStringCompare = linker.downcallHandle(
                    cf.findOrThrow("CFStringCompare"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS, ADDRESS, ValueLayout.JAVA_INT)
            );

            cfStringCreateWithCString = linker.downcallHandle(
                    cf.findOrThrow("CFStringCreateWithCString"),
                    FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS, ValueLayout.JAVA_INT)
            );

            cfRelease = linker.downcallHandle(
                    cf.findOrThrow("CFRelease"),
                    FunctionDescriptor.ofVoid(ADDRESS)
            );

        } catch (Throwable e) {
            throw new IllegalStateException("Failed to initialize DarkModeDetectorImpMacOs", e);
        }
    }

    @Override
    public boolean isDarkModeDetectionSupported() {
        return true;
    }

    /** Returns true if macOS is currently in Dark Mode */
    @Override
    public boolean isDarkMode() {
        try (Arena arena = Arena.ofConfined()) {
            // CFString key
            MemorySegment keyCF = (MemorySegment) cfStringCreateWithCString.invoke(
                    MemorySegment.NULL,
                    arena.allocateFrom("AppleInterfaceStyle"),
                    CF_STRING_ENCODING_UTF8
            );

            // CFString for application ID - use .GlobalPreferences for system-wide settings
            MemorySegment appIDCF = (MemorySegment) cfStringCreateWithCString.invoke(
                    MemorySegment.NULL,
                    arena.allocateFrom(".GlobalPreferences"),
                    CF_STRING_ENCODING_UTF8
            );

            MemorySegment value = (MemorySegment) cfPreferencesCopyAppValue.invoke(keyCF, appIDCF);

            boolean dark = false;
            if (!value.equals(MemorySegment.NULL)) {
                MemorySegment darkCF = (MemorySegment) cfStringCreateWithCString.invoke(
                        MemorySegment.NULL,
                        arena.allocateFrom("Dark"),
                        CF_STRING_ENCODING_UTF8
                );

                int result = (int) cfStringCompare.invoke(value, darkCF, 0);
                dark = result == 0;

                cfRelease.invoke(darkCF);
                cfRelease.invoke(value);
            }

            cfRelease.invoke(appIDCF);
            cfRelease.invoke(keyCF);

            return dark;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to detect Dark Mode", e);
        }
    }

/**
     * Starts a daemon thread that periodically checks the system appearance and
     * notifies listeners when a change is detected via {@link #onChangeDetected(boolean)}.
     *
     * @param intervalMs polling interval in milliseconds
     */
    public void startPolling(long intervalMs) {
        Thread t = new Thread(() -> {
            boolean last = isDarkMode();
            while (true) {
                try {
                    Thread.sleep(intervalMs);
                    boolean current = isDarkMode();
                    if (current != last) {
                        last = current;
                        onChangeDetected(current);
                    }
                } catch (InterruptedException e) {
                    LOG.debug("DarkModePoller interrupted", e);
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable e) {
                    LOG.error("Error detecting Dark Mode", e);
                }
            }
        }, "DarkModePoller");

        t.setDaemon(true);
        t.start();
    }
}
