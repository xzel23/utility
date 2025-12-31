package com.dua3.utility.application.imp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Implementation of {@link com.dua3.utility.application.NativeHelper} for macOS.
 */
public final class NativeHelperMacOs implements com.dua3.utility.application.NativeHelper {

    private static final Logger LOG = LogManager.getLogger(NativeHelperMacOs.class);

    private static final String NSAPPEARANCE_NAME_LIGHT = "NSAppearanceName";
    private static final String NSAPPEARANCE_NAME_DARK = "NSAppearanceNameDark";
    private static final String NSAPPEARANCE_NAME_FALLBACK = "NSAppearanceNameAqua";
    private static final int CF_STRING_ENCODING_UTF8 = 0x08000100;

    /**
     * Returns the singleton instance of {@link NativeHelperWindows}.
     *
     * @return the singleton instance
     */
    public static NativeHelperMacOs getInstance() {
        return Holder.INSTANCE;
    }

    private final Linker linker = Linker.nativeLinker();
    private final Arena sharedArena = Arena.ofShared();

    // CoreFoundation handles
    private final MethodHandle cfStringCreateWithCString;
    private final MethodHandle cfRelease;

    // AppKit handles
    private final MethodHandle selRegisterName;
    private final MethodHandle objcGetClass;
    private final MethodHandle objcMsgSend;
    private final MethodHandle objcMsgSendId;
    private final MethodHandle objcMsgSendCount;
    private final MethodHandle objcMsgSendIdx;

    private NativeHelperMacOs() {
        try {
            SymbolLookup cf = SymbolLookup.libraryLookup(
                    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
                    sharedArena
            );
            SymbolLookup objc = SymbolLookup.libraryLookup(
                    "/usr/lib/libobjc.A.dylib",
                    sharedArena
            );

            cfStringCreateWithCString = linker.downcallHandle(
                    cf.findOrThrow("CFStringCreateWithCString"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

            cfRelease = linker.downcallHandle(
                    cf.findOrThrow("CFRelease"),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
            );

            selRegisterName = linker.downcallHandle(
                    objc.findOrThrow("sel_registerName"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            objcGetClass = linker.downcallHandle(
                    objc.findOrThrow("objc_getClass"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            objcMsgSend = linker.downcallHandle(
                    objc.findOrThrow("objc_msgSend"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            objcMsgSendId = linker.downcallHandle(
                    objc.findOrThrow("objc_msgSend"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            objcMsgSendCount = linker.downcallHandle(
                    objc.findOrThrow("objc_msgSend"),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            objcMsgSendIdx = linker.downcallHandle(
                    objc.findOrThrow("objc_msgSend"),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize NativeHelperMacOs", e);
        }
    }

    private static final class Holder {
        private static final NativeHelperMacOs INSTANCE = new NativeHelperMacOs();
    }

    private static final String defaultAppearanceName = getInstance().getDefaultAppearanceName();

    @Override
    public boolean setWindowDecorations(boolean dark) {
        String appearanceName = (dark ? NSAPPEARANCE_NAME_DARK : NSAPPEARANCE_NAME_LIGHT) + defaultAppearanceName;

        try (Arena arena = Arena.ofConfined()) {
            // 1. Get the shared application instance
            MemorySegment clsNSApp = (MemorySegment) objcGetClass.invoke(arena.allocateFrom("NSApplication"));
            MemorySegment selSharedApp = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("sharedApplication"));
            MemorySegment sharedApp = (MemorySegment) objcMsgSend.invoke(clsNSApp, selSharedApp);

            // 2. Get the 'windows' array
            MemorySegment selWindows = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("windows"));
            MemorySegment windowArray = (MemorySegment) objcMsgSend.invoke(sharedApp, selWindows);

            // 3. Get the count of windows
            MemorySegment selCount = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("count"));
            long count = (long) objcMsgSendCount.invoke(windowArray, selCount);

            // 4. Iterate and apply theme
            MemorySegment selObjectAtIndex = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("objectAtIndex:"));
            for (long i = 0; i < count; i++) {
                MemorySegment nsWindow = (MemorySegment) objcMsgSendIdx.invoke(windowArray, selObjectAtIndex, i);
                applyThemeToNativeWindow(nsWindow, appearanceName);
            }
            return true;
        } catch (Throwable t) {
            LOG.warn("Failed to iterate native macOS windows", t);
            return false;
        }
    }

    private void applyThemeToNativeWindow(MemorySegment nsWindow, @Nullable String appearanceName) {
        try (Arena arena = Arena.ofConfined()) {
            // Get NSAppearance class
            MemorySegment nsAppearanceClass = (MemorySegment) objcGetClass.invoke(arena.allocateFrom("NSAppearance"));

            // Get appearanceNamed: selector
            MemorySegment appearanceNamedSel = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("appearanceNamed:"));

            // Create CFString for appearance name
            MemorySegment appearanceNameCF = (MemorySegment) cfStringCreateWithCString.invoke(
                    MemorySegment.NULL,
                    arena.allocateFrom(appearanceName),
                    CF_STRING_ENCODING_UTF8
            );

            // Get the appearance instance: [NSAppearance appearanceNamed:appearanceName]
            MemorySegment appearance = (MemorySegment) objcMsgSendId.invoke(nsAppearanceClass, appearanceNamedSel, appearanceNameCF);

            if (appearance.equals(MemorySegment.NULL)) {
                LOG.warn("Failed to get appearance for name: {}", appearanceName);
                cfRelease.invoke(appearanceNameCF);
            }

            // Get setAppearance: selector
            MemorySegment setAppearanceSel = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("setAppearance:"));

            // Set the appearance: [nsWindow setAppearance:appearance]
            objcMsgSendId.invoke(nsWindow, setAppearanceSel, appearance);

            cfRelease.invoke(appearanceNameCF);
        } catch (Throwable e) {
            LOG.warn("Failed to set window appearance", e);
        }
    }

    /**
     * Retrieves the default appearance name of the system's user interface.
     * The method interacts with the macOS native APIs to determine the effective
     * appearance of the application (e.g., Light or Dark Mode).
     *
     * @return a string representing the appearance name with any common prefix
     *         removed. If the appearance is not determined, it falls back to "Aqua".
     */
    private String getDefaultAppearanceName() {
        try (Arena arena = Arena.ofConfined()) {
            // 1. [NSApplication sharedApplication]
            MemorySegment clsNSApp = (MemorySegment) objcGetClass.invoke(arena.allocateFrom("NSApplication"));
            MemorySegment selSharedApp = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("sharedApplication"));
            MemorySegment sharedApp = (MemorySegment) objcMsgSend.invoke(clsNSApp, selSharedApp);

            // 2. [sharedApp effectiveAppearance]
            MemorySegment selEffective = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("effectiveAppearance"));
            MemorySegment effectiveAppearance = (MemorySegment) objcMsgSend.invoke(sharedApp, selEffective);

            // 3. [effectiveAppearance name] -> returns a CFStringRef/NSString
            MemorySegment selName = (MemorySegment) selRegisterName.invoke(arena.allocateFrom("name"));
            MemorySegment nameStringPtr = (MemorySegment) objcMsgSend.invoke(effectiveAppearance, selName);

            // 4. Convert the native string to Java String, or use fallback value
            String s = convertNsStringToSting(nameStringPtr);
            if (s.startsWith(NSAPPEARANCE_NAME_DARK)) {
                return s.substring(NSAPPEARANCE_NAME_DARK.length());
            }
            if (s.startsWith(NSAPPEARANCE_NAME_LIGHT)) {
                return s.substring(NSAPPEARANCE_NAME_LIGHT.length());
            }
            return "Aqua";
        } catch (Throwable t) {
            LOG.error("Failed to query default appearance", t);
            return NSAPPEARANCE_NAME_FALLBACK;
        }
    }

    /**
     * Converts an NSString object represented by a memory segment pointer into a Java String.
     *
     * @param nsStringPtr the memory segment pointer referencing the NSString to be converted.
     *                     If the pointer is {@code MemorySegment.NULL}, an empty string is returned.
     * @return the Java String equivalent of the NSString referenced by the input pointer.
     * @throws Throwable if an error occurs during invocation of native methods.
     */
    private String convertNsStringToSting(MemorySegment nsStringPtr) throws Throwable {
        if (nsStringPtr.equals(MemorySegment.NULL)) return "";
        MemorySegment selUtf8 = (MemorySegment) selRegisterName.invoke(Arena.ofAuto().allocateFrom("UTF8String"));
        MemorySegment cStringPtr = (MemorySegment) objcMsgSend.invoke(nsStringPtr, selUtf8);
        return cStringPtr.reinterpret(Long.MAX_VALUE).getString(0);
    }
}
