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
public class DarkModeDetectorWindows extends DarkModeDetectorBase {

    private static final Logger LOG = LogManager.getLogger(DarkModeDetectorWindows.class);

    private static final String SUBKEY_PERSONALIZE = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
    private static final String VALUE_APPS = "AppsUseLightTheme";
    private static final String VALUE_SYSTEM = "SystemUsesLightTheme";

    // Access rights and flags
    private static final int KEY_NOTIFY = 0x0010;
    private static final int KEY_READ = 0x20019; // STANDARD_RIGHTS_READ | KEY_QUERY_VALUE | KEY_ENUMERATE_SUB_KEYS | KEY_NOTIFY
    private static final int RRF_RT_REG_DWORD = 0x00000010;
    private static final int REG_NOTIFY_CHANGE_LAST_SET = 0x00000004;

    // FFM linker and shared arena for long-lived symbols
    private final Linker linker = Linker.nativeLinker();
    private final Arena shared = Arena.ofShared();

    // advapi32 handles
    private final MethodHandle regOpenCurrentUser;
    private final MethodHandle regOpenKeyExW;
    private final MethodHandle regGetValueW;
    private final MethodHandle regNotifyChangeKeyValue;
    private final MethodHandle regCloseKey;

    private static final Duration RESTART_BACKOFF = Duration.ofSeconds(1);

    private final AtomicBoolean watcherRunning = new AtomicBoolean(false);
    private Thread watcherThread;
    private volatile Boolean lastState = null;

    private static class Holder {
        private static final DarkModeDetector INSTANCE = createInstance();
        private static DarkModeDetector createInstance() {
            try {
                return new DarkModeDetectorWindows();
            } catch (Exception t) {
                LOG.error("DarkModeDetectorWindows initialization failed", t);
                return new DarkModeDetectorImpUnsupported();
            }
        }
    }

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
        if (watcherRunning.compareAndSet(false, true)) {
            watcherThread = new Thread(this::watchLoop, "DarkModeDetectorWindows-Watcher");
            watcherThread.setDaemon(true);
            watcherThread.start();
        }
    }

    private void stopWatcher() {
        if (watcherRunning.compareAndSet(true, false)) {
            Thread t = watcherThread;
            if (t != null) {
                t.interrupt();
            }
        }
    }

    private void watchLoop() {
        LOG.debug("Starting Windows dark mode registry watcher thread");
        while (watcherRunning.get()) {
            try (Arena arena = Arena.ofConfined()) {
                MemorySegment hku = openCurrentUser(arena);
                if (hku.equals(MemorySegment.NULL)) {
                    LOG.debug("RegOpenCurrentUser returned NULL");
                    Thread.sleep(RESTART_BACKOFF.toMillis());
                    continue;
                }
                MemorySegment phkResult = arena.allocate(ADDRESS);
                int rcOpenKey = (int) regOpenKeyExW.invokeExact(hku, toWideString(arena, SUBKEY_PERSONALIZE), 0, KEY_READ | KEY_NOTIFY, phkResult);
                if (rcOpenKey != 0) {
                    closeKeyQuiet(hku);
                    Thread.sleep(RESTART_BACKOFF.toMillis());
                    continue;
                }
                MemorySegment personalizeKey = phkResult.get(ADDRESS, 0);

                // Immediately read and notify if changed
                boolean current = isDarkMode();
                Boolean prev = lastState;
                if (prev == null || prev != current) {
                    lastState = current;
                    onChangeDetected(current);
                }

                // Block until change
                int rcNotify = (int) regNotifyChangeKeyValue.invokeExact(personalizeKey, 0 /* bWatchSubtree=false */, REG_NOTIFY_CHANGE_LAST_SET, MemorySegment.NULL, 0 /* fAsynchronous=false */);
                // Close handles
                closeKeyQuiet(personalizeKey);
                closeKeyQuiet(hku);

                if (rcNotify != 0) {
                    // On error, back off slightly
                    Thread.sleep(RESTART_BACKOFF.toMillis());
                }
                // Loop and wait again as long as watcherRunning
            } catch (InterruptedException _) {
                LOG.debug("Watcher interrupted");
                // Exit gracefully
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                LOG.debug("Watcher error: {}", t.toString());
                try {
                    Thread.sleep(RESTART_BACKOFF.toMillis());
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        LOG.debug("Stopping Windows dark mode registry watcher thread");
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
                regCloseKey.invokeExact(hKey);
            }
        } catch (Throwable _) {
            LOG.debug("Failed to close Windows registry key: {}", hKey);
            // ignore
        }
    }
}
