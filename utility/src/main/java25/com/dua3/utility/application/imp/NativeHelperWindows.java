package com.dua3.utility.application.imp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

import static java.lang.foreign.ValueLayout.*;

/**
 * Implementation of NativeHelper for Windows using FFM API.
 * Automatically themes current and future windows in the process.
 */
public final class NativeHelperWindows implements com.dua3.utility.application.NativeHelper {

    private static final Logger LOG = LogManager.getLogger(NativeHelperWindows.class);

    private final Linker linker = Linker.nativeLinker();
    private final MethodHandle dwmSetWindowAttribute;
    private final MethodHandle enumWindows;
    private final MethodHandle getWindowThreadProcessId;
    private final MethodHandle getCurrentProcessId;
    private final MethodHandle isWindowVisible;
    private final MethodHandle rtlGetVersion;
    private final MethodHandle setWinEventHook;

    private static final int WINEVENT_OUTOFCONTEXT = 0x0000;
    // Event range for object creation and showing
    private static final int EVENT_OBJECT_CREATE = 0x8000;
    private static final int EVENT_OBJECT_NAMECHANGE = 0x800C;

    private MemorySegment hookHandle = MemorySegment.NULL;
    private boolean isAutoThemeEnabled = false;
    private volatile int currentDarkMode = 0;

    private NativeHelperWindows() {
        SymbolLookup dwm = SymbolLookup.libraryLookup("dwmapi.dll", Arena.global());
        SymbolLookup user32 = SymbolLookup.libraryLookup("user32.dll", Arena.global());
        SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32.dll", Arena.global());

        this.dwmSetWindowAttribute = linker.downcallHandle(
                dwm.findOrThrow("DwmSetWindowAttribute"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT, ADDRESS, JAVA_INT)
        );

        this.enumWindows = linker.downcallHandle(
                user32.findOrThrow("EnumWindows"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG)
        );

        this.getWindowThreadProcessId = linker.downcallHandle(
                user32.findOrThrow("GetWindowThreadProcessId"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS)
        );

        this.getCurrentProcessId = linker.downcallHandle(
                kernel32.findOrThrow("GetCurrentProcessId"),
                FunctionDescriptor.of(JAVA_INT)
        );

        this.isWindowVisible = linker.downcallHandle(
                user32.findOrThrow("IsWindowVisible"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS)
        );

        this.rtlGetVersion = linker.downcallHandle(
                SymbolLookup.libraryLookup("ntdll.dll", Arena.global()).findOrThrow("RtlGetVersion"),
                FunctionDescriptor.of(JAVA_INT, ADDRESS)
        );

        this.setWinEventHook = linker.downcallHandle(
                user32.findOrThrow("SetWinEventHook"),
                FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT)
        );
    }

    private static final class Holder {
        private static final NativeHelperWindows INSTANCE = new NativeHelperWindows();
    }

    public static NativeHelperWindows getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public boolean setWindowDecorations(boolean dark) {
        this.currentDarkMode = dark ? 1 : 0;

        // Ensure the hook is running for future windows
        ensureAutoThemeEnabled();

        try (Arena arena = Arena.ofConfined()) {
            int currentPid = (int) getCurrentProcessId.invokeExact();
            int attribute = getDwmAttributeConstant();
            List<MemorySegment> myWindows = new ArrayList<>();

            MethodHandle handle = MethodHandles.lookup().findVirtual(NativeHelperWindows.class, "processWindow",
                    MethodType.methodType(int.class, List.class, int.class, MemorySegment.class, long.class));

            MethodHandle boundHandle = MethodHandles.insertArguments(handle, 0, this, myWindows, currentPid);

            MemorySegment enumCallbackStub = linker.upcallStub(
                    boundHandle,
                    FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG),
                    arena
            );

            // Trigger enumeration for existing windows
            int result = (int) enumWindows.invokeExact(enumCallbackStub, 0L);

            MemorySegment pDarkMode = arena.allocateFrom(JAVA_INT, currentDarkMode);
            for (MemorySegment hwnd : myWindows) {
                if ((int) isWindowVisible.invokeExact(hwnd) == 1) {
                    result = (int) dwmSetWindowAttribute.invokeExact(hwnd, attribute, pDarkMode, (int) JAVA_INT.byteSize());
                    LOG.debug("Applied theme to existing HWND {}: result {}", hwnd, result);
                }
            }
            return true;
        } catch (Throwable t) {
            LOG.warn("Failed to set window decorations", t);
            return false;
        }
    }

    private void ensureAutoThemeEnabled() {
        if (isAutoThemeEnabled) return;

        try {
            int currentPid = (int) getCurrentProcessId.invokeExact();

            // MethodHandle for: void onWindowEvent(MemorySegment, int, MemorySegment, int, int, int, int)
            MethodHandle onWindowEventHandle = MethodHandles.lookup().findVirtual(NativeHelperWindows.class, "onWindowEvent",
                    MethodType.methodType(void.class, MemorySegment.class, int.class, MemorySegment.class, int.class, int.class, int.class, int.class));

            MemorySegment hookStub = linker.upcallStub(
                    onWindowEventHandle.bindTo(this),
                    FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT, ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT),
                    Arena.global()
            );

            this.hookHandle = (MemorySegment) setWinEventHook.invokeExact(
                    EVENT_OBJECT_CREATE, EVENT_OBJECT_NAMECHANGE,
                    MemorySegment.NULL, hookStub,
                    currentPid, 0, WINEVENT_OUTOFCONTEXT
            );

            if (!this.hookHandle.equals(MemorySegment.NULL)) {
                isAutoThemeEnabled = true;
                LOG.info("Agnostic window hook installed for PID {}", currentPid);
            }
        } catch (Throwable t) {
            LOG.error("Failed to install native auto-theme hook", t);
        }
    }

    /**
     * Native callback for WinEventHook.
     */
    private void onWindowEvent(MemorySegment hWinEventHook, int event, MemorySegment hwnd,
                               int idObject, int idChild, int dwEventThread, int dwmsEventTime) {
        // idObject == 0 means the event is for a Window object (OBJID_WINDOW)
        if (idObject == 0 && !hwnd.equals(MemorySegment.NULL)) {
            LOG.debug("Processing window event 0x{} for HWND {}", Integer.toHexString(event), hwnd);
            try (Arena local = Arena.ofConfined()) {
                int attr = getDwmAttributeConstant();
                MemorySegment pDarkMode = local.allocateFrom(JAVA_INT, currentDarkMode);

                // Note: We don't check IsWindowVisible here because some windows are themed before being shown.
                int res = (int) dwmSetWindowAttribute.invokeExact(hwnd, attr, pDarkMode, (int) JAVA_INT.byteSize());
                if (res == 0) {
                    LOG.debug("Auto-applied theme to new HWND {}", hwnd);
                }
            } catch (Throwable t) {
                // Keep callback silent
            }
        }
    }

    private int processWindow(List<MemorySegment> list, int targetPid, MemorySegment hwnd, long lParam) {
        try (Arena localArena = Arena.ofConfined()) {
            MemorySegment pPid = localArena.allocate(JAVA_INT);
            int threadId = (int) getWindowThreadProcessId.invokeExact(hwnd, pPid);

            if (pPid.get(JAVA_INT, 0) == targetPid) {
                list.add(hwnd);
            }
        } catch (Throwable t) {
            LOG.error("Error in processWindow", t);
        }
        return 1;
    }

    private int getDwmAttributeConstant() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment versionInfo = arena.allocate(284);
            versionInfo.set(JAVA_INT, 0, 284);

            int result = (int) rtlGetVersion.invokeExact(versionInfo);
            int build = versionInfo.get(JAVA_INT, 12);

            return (build >= 19041) ? 20 : 19;
        } catch (Throwable t) {
            return 20;
        }
    }
}