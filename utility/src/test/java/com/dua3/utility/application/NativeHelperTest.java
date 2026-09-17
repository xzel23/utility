package com.dua3.utility.application;

import com.dua3.utility.application.imp.NativeHelperInstance;
import com.dua3.utility.application.imp.NativeHelperUnsupported;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class NativeHelperTest {

    @Test
    void testNativeHelperInstanceGet() {
        NativeHelper helper = NativeHelperInstance.get();
        assertNotNull(helper);
    }

    @Test
    void testNativeHelperUnsupported() {
        NativeHelperUnsupported instance = NativeHelperUnsupported.getInstance();
        assertNotNull(instance);
        assertSame(instance, NativeHelperUnsupported.getInstance());
        assertFalse(instance.setWindowDecorations(true));
        assertFalse(instance.setWindowDecorations(false));
    }
}
