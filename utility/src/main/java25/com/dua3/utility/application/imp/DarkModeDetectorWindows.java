package com.dua3.utility.application.imp;

import com.dua3.utility.application.DarkModeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.foreign.ValueLayout.ADDRESS;

/**
 * Dark mode detector implementation for Microsoft Windows using the Java Foreign Function & Memory (FFM) API.
 *
 * <p>This implementation reads the Windows Registry keys under
 * HKCU\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize
 * for the values "AppsUseLightTheme" (preferred) and "SystemUsesLightTheme".
 * A value of 0 indicates Dark mode, 1 indicates Light mode. If neither key
 * is found, the implementation falls back to returning false (Light).</p>
 *
 * <p>Change monitoring is implemented using the native RegNotifyChangeKeyValue() API which blocks
 * until a change occurs. A single daemon thread is created only while listeners are registered.</p>
 */
public final class DarkModeDetectorWindows extends DarkModeDetectorBase {

    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorWindows.class);

    private static final String SUBKEY_PERSONALIZE = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String VALUE_APPS = "AppsUseLightTheme";
    private static final String VALUE_SYSTEM = "SystemUsesLightTheme";

    // Access rights and flags
    private static final int KEY_NOTIFY = 0x0010;
    private static final int KEY_READ = 0x20019; // STANDARD_RIGHTS_READ | KEY_QUERY_VALUE | KEY_ENUMERATE_SUB_KEYS | KEY_NOTIFY
    private static final int RRF_RT_REG_DWORD = 0x00000010;
    private static final int REG_NOTIFY_CHANGE_LAST_SET = 0x00000004;
    private static final int WAIT_OBJECT_0 = 0;
    private static final int WAIT_FAILED = 0xffffffff;
    private static final int INFINITE = 0xffffffff;

    // FFM linker and shared arena for long-lived symbols
    private final Linker linker = Linker.nativeLinker();
    private final Arena shared = Arena.ofShared();

    // advapi32 handles
    private final MethodHandle regOpenCurrentUser;
    private final MethodHandle regOpenKeyExW;
    private final MethodHandle regGetValueW;
    private final MethodHandle regNotifyChangeKeyValue;
    private final MethodHandle regCloseKey;
    private final MethodHandle createEventW;
    private final MethodHandle setEvent;
    private final MethodHandle waitForMultipleObjects;
    private final MethodHandle closeHandle;

    private static final Duration RESTART_BACKOFF = Duration.ofSeconds(1);

    private final Object watcherLock = new Object();
    private final AtomicBoolean watcherRunning = new AtomicBoolean(false);
    private Thread watcherThread;
    private MemorySegment cancellationEvent = MemorySegment.NULL;
    private volatile Boolean lastState = null;

    private static class Holder {
        private static final DarkModeDetector INSTANCE = createInstance();

        private static DarkModeDetector createInstance() {
            try {
                return new DarkModeDetectorWindows();
            } catch (Exception t) {
                LOG.error("DarkModeDetectorWindows initialization failed", t);
                return DarkModeDetectorUnsupported.getInstance();
            }
        }
    }

    /**
     * Get the singleton instance of DarkModeDetectorWindows.
     *
     * @return the singleton instance
     */
    public static DarkModeDetector getInstance() {
        return Holder.INSTANCE;
    }

    private DarkModeDetectorWindows() {
        try {
            SymbolLookup advapi = SymbolLookup.libraryLookup("Advapi32", shared);

            regOpenCurrentUser = linker.downcallHandle(
                    advapi.findOrThrow("RegOpenCurrentUser"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ADDRESS)
            );
            regOpenKeyExW = linker.downcallHandle(
                    advapi.findOrThrow("RegOpenKeyExW"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS, ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ADDRESS)
            );
            regGetValueW = linker.downcallHandle(
                    advapi.findOrThrow("RegGetValueW"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS, ADDRESS, ADDRESS, ValueLayout.JAVA_INT, ADDRESS, ADDRESS, ADDRESS)
            );
            regNotifyChangeKeyValue = linker.downcallHandle(
                    advapi.findOrThrow("RegNotifyChangeKeyValue"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ADDRESS, ValueLayout.JAVA_INT)
            );
            regCloseKey = linker.downcallHandle(
                    advapi.findOrThrow("RegCloseKey"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS)
            );
            SymbolLookup kernel32 = SymbolLookup.libraryLookup("Kernel32", shared);
            createEventW = linker.downcallHandle(
                    kernel32.findOrThrow("CreateEventW"),
                    FunctionDescriptor.of(ADDRESS, ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ADDRESS)
            );
            setEvent = linker.downcallHandle(
                    kernel32.findOrThrow("SetEvent"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS)
            );
            waitForMultipleObjects = linker.downcallHandle(
                    kernel32.findOrThrow("WaitForMultipleObjects"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
            );
            closeHandle = linker.downcallHandle(
                    kernel32.findOrThrow("CloseHandle"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ADDRESS)
            );
        } catch (Exception t) {
            throw new IllegalStateException("Failed to initialize Windows registry FFM handles", t);
        }
    }

    @Override
    public boolean isDarkModeDetectionSupported() {
        return true;
    }

    @Override
    public boolean isDarkMode() {
        try (Arena arena = Arena.ofConfined()) {
            Integer apps = readDwordFromPersonalize(arena, VALUE_APPS);
            if (apps != null) {
                return apps == 0; // 0 => Dark, 1 => Light
            }
            Integer system = readDwordFromPersonalize(arena, VALUE_SYSTEM);
            if (system != null) {
                return system == 0;
            }
        } catch (Throwable t) {
            LOG.warn("FFM Windows registry query failed: {}", t.toString());
        }
        return false; // default to Light if unknown
    }

    @Override
    protected void monitorSystemChanges(boolean enable) {
        if (enable) {
            startWatcher();
        } else {
            stopWatcher();
        }
    }

    private void startWatcher() {
        synchronized (watcherLock) {
            if (!watcherRunning.compareAndSet(false, true)) {
                return;
            }

            MemorySegment event = createEvent(true);
            if (event.equals(MemorySegment.NULL)) {
                watcherRunning.set(false);
                LOG.warn("Could not create Windows dark mode watcher cancellation event");
                return;
            }

            cancellationEvent = event;
            watcherThread = new Thread(() -> watchLoop(event), "DarkModeDetectorWindows-Watcher");
            watcherThread.setDaemon(true);
            watcherThread.start();
        }
    }

    private void stopWatcher() {
        MemorySegment event;
        if (watcherRunning.compareAndSet(true, false)) {
            synchronized (watcherLock) {
                event = cancellationEvent;
            }
            signalEventQuiet(event);
            Thread t = watcherThread;
            if (t != null) {
                t.interrupt();
            }
        }
    }

    private void watchLoop(MemorySegment cancelEvent) {
        LOG.debug("Starting Windows dark mode registry watcher thread");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment notificationEvent = createEvent(false);
            if (notificationEvent.equals(MemorySegment.NULL)) {
                LOG.warn("Could not create Windows dark mode watcher notification event");
                return;
            }
            try {
                MemorySegment eventHandles = arena.allocate(ADDRESS, 2);
                eventHandles.set(ADDRESS, 0, cancelEvent);
                eventHandles.set(ADDRESS, ADDRESS.byteSize(), notificationEvent);

                while (watcherRunning.get()) {
                    MemorySegment hku = MemorySegment.NULL;
                    MemorySegment personalizeKey = MemorySegment.NULL;
                    try {
                        hku = openCurrentUser(arena);
                        if (hku.equals(MemorySegment.NULL)) {
                            sleepBeforeRestart();
                            continue;
                        }
                        MemorySegment phkResult = arena.allocate(ADDRESS);
                        int rcOpenKey = (int) regOpenKeyExW.invokeExact(hku, toWideString(arena, SUBKEY_PERSONALIZE), 0, KEY_READ | KEY_NOTIFY, phkResult);
                        if (rcOpenKey != 0) {
                            sleepBeforeRestart();
                            continue;
                        }
                        personalizeKey = phkResult.get(ADDRESS, 0);

                        boolean current = isDarkMode();
                        Boolean prev = lastState;
                        if (prev == null || prev != current) {
                            lastState = current;
                            onChangeDetected(current);
                        }

                        int rcNotify = (int) regNotifyChangeKeyValue.invokeExact(
                                personalizeKey, 0, REG_NOTIFY_CHANGE_LAST_SET, notificationEvent, 1
                        );
                        if (rcNotify != 0) {
                            LOG.trace("RegNotifyChangeKeyValue failed: {}", rcNotify);
                            sleepBeforeRestart();
                            continue;
                        }

                        int waitResult = (int) waitForMultipleObjects.invokeExact(2, eventHandles, 0, INFINITE);
                        if (waitResult == WAIT_OBJECT_0) {
                            break;
                        }
                        if (waitResult != WAIT_OBJECT_0 + 1) {
                            LOG.trace("WaitForMultipleObjects returned {}", waitResult == WAIT_FAILED ? "WAIT_FAILED" : waitResult);
                            sleepBeforeRestart();
                        }
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Throwable t) {
                        LOG.trace("Watcher error: {}", t.toString());
                        try {
                            sleepBeforeRestart();
                        } catch (InterruptedException _) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } finally {
                        closeKeyQuiet(personalizeKey);
                        closeKeyQuiet(hku);
                    }
                }
            } finally {
                closeHandleQuiet(notificationEvent);
            }
        } finally {
            synchronized (watcherLock) {
                if (cancellationEvent.equals(cancelEvent)) {
                    cancellationEvent = MemorySegment.NULL;
                    watcherThread = null;
                    watcherRunning.set(false);
                }
            }
            closeHandleQuiet(cancelEvent);
        }
        LOG.debug("Stopping Windows dark mode registry watcher thread");
    }

    private MemorySegment createEvent(boolean manualReset) {
        try {
            return (MemorySegment) createEventW.invokeExact(MemorySegment.NULL, manualReset ? 1 : 0, 0, MemorySegment.NULL);
        } catch (Throwable t) {
            LOG.debug("Could not create Windows event", t);
            return MemorySegment.NULL;
        }
    }

    private void signalEventQuiet(MemorySegment event) {
        try {
            if (event != null && !event.equals(MemorySegment.NULL)) {
                int result = (int) setEvent.invokeExact(event);
                if (result == 0) {
                    LOG.debug("Could not signal Windows event");
                }
            }
        } catch (Throwable t) {
            LOG.debug("Could not signal Windows event", t);
        }
    }

    private void closeHandleQuiet(MemorySegment handle) {
        try {
            if (handle != null && !handle.equals(MemorySegment.NULL)) {
                int result = (int) closeHandle.invokeExact(handle);
                if (result == 0) {
                    LOG.debug("Could not close Windows handle");
                }
            }
        } catch (Throwable t) {
            LOG.debug("Could not close Windows handle", t);
        }
    }

    private static void sleepBeforeRestart() throws InterruptedException {
        Thread.sleep(RESTART_BACKOFF.toMillis());
    }

    private MemorySegment openCurrentUser(Arena arena) throws Throwable {
        MemorySegment phKey = arena.allocate(ADDRESS);
        int rc = (int) regOpenCurrentUser.invokeExact(KEY_READ, phKey);
        if (rc != 0) {
            return MemorySegment.NULL;
        }
        return phKey.get(ADDRESS, 0);
    }

    private Integer readDwordFromPersonalize(Arena arena, String valueName) throws Throwable {
        MemorySegment hku = openCurrentUser(arena);
        if (hku.equals(MemorySegment.NULL)) {
            return null;
        }
        try {
            // Using RegGetValueW with base HKCU and subkey path
            MemorySegment pvData = arena.allocate(ValueLayout.JAVA_INT);
            MemorySegment pcbData = arena.allocate(ValueLayout.JAVA_INT);
            pcbData.set(ValueLayout.JAVA_INT, 0, Integer.BYTES);

            int rc = (int) regGetValueW.invokeExact(
                    hku,
                    toWideString(arena, SUBKEY_PERSONALIZE),
                    toWideString(arena, valueName),
                    RRF_RT_REG_DWORD,
                    MemorySegment.NULL,
                    pvData,
                    pcbData
            );
            if (rc == 0) {
                return pvData.get(ValueLayout.JAVA_INT, 0);
            } else {
                return null;
            }
        } finally {
            closeKeyQuiet(hku);
        }
    }

    private static MemorySegment toWideString(Arena arena, String s) {
        // UTF-16LE null-terminated
        char[] chars = (s + "\0").toCharArray();
        MemorySegment seg = arena.allocate(ValueLayout.JAVA_CHAR, chars.length);
        for (int i = 0; i < chars.length; i++) {
            seg.set(ValueLayout.JAVA_CHAR, i * ValueLayout.JAVA_CHAR.byteSize(), chars[i]);
        }
        return seg;
    }

    private void closeKeyQuiet(MemorySegment hKey) {
        try {
            if (hKey != null && !hKey.equals(MemorySegment.NULL)) {
                int result = (int) regCloseKey.invokeExact(hKey);
                if (result != 0) {
                    LOG.debug("Failed to close Windows registry key {}: {}", hKey, result);
                }
            }
        } catch (Throwable _) {
            LOG.debug("Failed to close Windows registry key: {}", hKey);
            // ignore
        }
    }
}
