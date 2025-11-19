package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.atomic.AtomicBoolean;

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
 * {@link DarkModeDetectorUnsupported} instance is returned instead.</p>
 *
 * <p>Note: The implementation performs native calls and may require enabling native access
 * depending on the runtime configuration. This class resides in the Java 25 sourceset
 * and relies on the FFM API available in modern Java versions.</p>
 *
 * <p>Thread-safety: isDarkMode() uses a confined Arena per call; CoreFoundation symbol
 * handles are initialized once and reused, making it safe to call concurrently.</p>
 *
 * <p>Platform: macOS only.</p>
 */
public final class DarkModeDetectorMacOs extends DarkModeDetectorBase {

    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorMacOs.class);

    /** Lazy-loaded singleton */
    private static class Holder {
        private static final DarkModeDetector INSTANCE = createInstance();

        private static DarkModeDetector createInstance() {
            try {
                return new DarkModeDetectorMacOs();
            } catch (Exception e) {
                LOG.error("DarkModeDetectorMacOs initialization failed", e);
                return DarkModeDetectorUnsupported.getInstance();
            }
        }
    }

    /**
     * Returns the lazily initialized singleton instance of the macOS dark mode detector.
     * If initialization fails, an {@link DarkModeDetectorUnsupported} instance is returned.
     *
     * @return the dark mode detector instance
     */
    public static DarkModeDetector getInstance() {
        return Holder.INSTANCE;
    }

    private static final int CF_STRING_ENCODING_UTF8 = 0x08000100;

    private final Linker linker = Linker.nativeLinker();
    private final Arena sharedArena = Arena.ofShared();

    // CoreFoundation handles
    private final MethodHandle cfPreferencesCopyAppValue;
    private final MethodHandle cfStringCompare;
    private final MethodHandle cfStringCreateWithCString;
    private final MethodHandle cfRelease;

    // CFNotificationCenter handles (for distributed notifications)
    private final MethodHandle cfNotificationCenterGetDistributedCenter;
    private final MethodHandle cfNotificationCenterAddObserver;
    private final MethodHandle cfNotificationCenterRemoveObserver;

    // State for observer registration
    private MemorySegment distributedCenter = MemorySegment.NULL;
    private MemorySegment notificationNameCF = MemorySegment.NULL;
    private final AtomicBoolean observerRegistered = new AtomicBoolean(false);

    /** Initialize CoreFoundation symbols and register for dark mode change notifications */
    private DarkModeDetectorMacOs() {
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

            // CFNotificationCenter
            cfNotificationCenterGetDistributedCenter = linker.downcallHandle(
                    cf.findOrThrow("CFNotificationCenterGetDistributedCenter"),
                    FunctionDescriptor.of(ADDRESS)
            );
            cfNotificationCenterAddObserver = linker.downcallHandle(
                    cf.findOrThrow("CFNotificationCenterAddObserver"),
                    // void CFNotificationCenterAddObserver(CFNotificationCenterRef center, const void *observer, CFNotificationCallback callBack, CFStringRef name, const void *object, CFNotificationSuspensionBehavior suspensionBehavior)
                    FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS, ValueLayout.JAVA_INT)
            );
            cfNotificationCenterRemoveObserver = linker.downcallHandle(
                    cf.findOrThrow("CFNotificationCenterRemoveObserver"),
                    // void CFNotificationCenterRemoveObserver(CFNotificationCenterRef center, const void *observer, CFStringRef name, const void *object)
                    FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS)
            );

            // Register for notifications immediately
            registerForAppearanceChanges();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize DarkModeDetectorMacOs", e);
        }
    }

    @Override
    public boolean isDarkModeDetectionSupported() {
        return true;
    }

    private void registerForAppearanceChanges() {
        if (observerRegistered.compareAndSet(false, true)) {
            try {
                // Create CFString for notification name in shared arena (persist across lifetime)
                notificationNameCF = (MemorySegment) cfStringCreateWithCString.invoke(
                        MemorySegment.NULL,
                        sharedArena.allocateFrom("AppleInterfaceThemeChangedNotification"),
                        CF_STRING_ENCODING_UTF8
                );

                // Obtain distributed center
                distributedCenter = (MemorySegment) cfNotificationCenterGetDistributedCenter.invoke();

                // Bind instance method as callback and create upcall stub
                MethodHandle cb = MethodHandles.lookup().bind(this,
                        "notificationCallback",
                        MethodType.methodType(void.class, MemorySegment.class, MemorySegment.class, MemorySegment.class, MemorySegment.class, MemorySegment.class));
                MemorySegment notificationCallbackStub = linker.upcallStub(
                        cb,
                        FunctionDescriptor.ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS, ADDRESS),
                        sharedArena
                );

                // Register observer with DeliverImmediately (4)
                cfNotificationCenterAddObserver.invoke(
                        distributedCenter,
                        MemorySegment.NULL,
                        notificationCallbackStub,
                        notificationNameCF,
                        MemorySegment.NULL,
                        4
                );
            } catch (Throwable e) {
                LOG.warn("Failed to register dark mode notifications", e);
            }
        }

        // Ensure we remove the observer on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(this::safeRemoveObserver, "DarkModeNotificationCleanup"));
    }

    private void safeRemoveObserver() {
        try {
            removeObserver();
        } catch (Throwable t) {
            LOG.trace("Ignoring error while removing dark mode observer", t);
        }
    }

    private void removeObserver() throws Throwable {
        if (observerRegistered.compareAndSet(true, false)) {
            if (!distributedCenter.equals(MemorySegment.NULL) && !notificationNameCF.equals(MemorySegment.NULL)) {
                cfNotificationCenterRemoveObserver.invoke(
                        distributedCenter,
                        MemorySegment.NULL,
                        notificationNameCF,
                        MemorySegment.NULL
                );
            }
            // CF objects created with Create should be released
            if (!notificationNameCF.equals(MemorySegment.NULL)) {
                cfRelease.invoke(notificationNameCF);
                notificationNameCF = MemorySegment.NULL;
            }
            // distributedCenter is not owned, do not release
        }
    }

    // CFNotification callback signature: void callback(CFNotificationCenterRef, void* observer, CFStringRef name, const void* object, CFDictionaryRef userInfo)
    @SuppressWarnings("unused")
    private void notificationCallback(MemorySegment center, MemorySegment observer, MemorySegment name, MemorySegment object, MemorySegment userInfo) {
        try {
            boolean dark = isDarkMode();
            onChangeDetected(dark);
        } catch (Exception t) {
            LOG.error("Error handling dark mode notification", t);
        }
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
            LOG.warn("Failed to detect Dark Mode", e);
            return false;
        }
    }

    @Override
    protected void monitorSystemChanges(boolean enable) {
        if (enable) {
            registerForAppearanceChanges();
        }
        // In this non-polling implementation, we do not deregister. This is done on shutdown.
    }
}
