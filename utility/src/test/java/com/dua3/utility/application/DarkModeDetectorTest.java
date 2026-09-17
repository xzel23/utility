package com.dua3.utility.application;

import com.dua3.utility.application.imp.DarkModeDetectorBase;
import com.dua3.utility.application.imp.DarkModeDetectorInstance;
import com.dua3.utility.application.imp.DarkModeDetectorUnsupported;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DarkModeDetectorTest {

    @Test
    void testDarkModeDetectorInstanceGet() {
        DarkModeDetector detector = DarkModeDetectorInstance.get();
        assertNotNull(detector);
    }

    @Test
    void testDarkModeDetectorUnsupported() {
        DarkModeDetectorUnsupported instance = DarkModeDetectorUnsupported.getInstance();
        assertNotNull(instance);
        assertSame(instance, DarkModeDetectorUnsupported.getInstance());
        assertFalse(instance.isDarkModeDetectionSupported());
        assertFalse(instance.isDarkMode());

        Consumer<Boolean> dummyListener = flag -> {};
        instance.addListener(dummyListener);
        instance.removeListener(dummyListener);
    }

    @Test
    void testDarkModeDetectorBase() {
        AtomicBoolean monitoringActive = new AtomicBoolean(false);
        class TestDarkModeDetector extends DarkModeDetectorBase {
            @Override
            public boolean isDarkModeDetectionSupported() {
                return true;
            }

            @Override
            public boolean isDarkMode() {
                return true;
            }

            @Override
            protected void monitorSystemChanges(boolean enable) {
                monitoringActive.set(enable);
            }

            public void fireChange(boolean dark) {
                onChangeDetected(dark);
            }
        }

        TestDarkModeDetector detector = new TestDarkModeDetector();
        assertTrue(detector.isDarkModeDetectionSupported());
        assertTrue(detector.isDarkMode());
        assertFalse(monitoringActive.get());

        List<Boolean> events = new ArrayList<>();
        Consumer<Boolean> listener = events::add;

        detector.addListener(listener);
        assertTrue(monitoringActive.get(), "Adding first listener should enable monitoring");

        detector.fireChange(true);
        detector.fireChange(false);
        assertEquals(List.of(true, false), events);

        detector.removeListener(listener);
        assertFalse(monitoringActive.get(), "Removing all listeners should disable monitoring");
    }
}
