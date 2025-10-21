package com.dua3.utility.samples;

import com.dua3.utility.application.ApplicationUtil;
import com.dua3.utility.application.DarkModeDetector;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Small text-mode program that prints the current dark mode setting and
 * registers a listener to print whenever the setting changes.
 *
 * Note: If you run on Java 22+ with FFM-based platform detectors (macOS/Windows),
 * you may need to enable native access, e.g. by using:
 *   --enable-native-access=ALL-UNNAMED
 */
public final class DarkModeSample {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DarkModeSample() {
        // no instances
    }

    public static void main(String[] args) throws InterruptedException {
        DarkModeDetector detector = ApplicationUtil.darkModeDetector();

        System.out.println("Dark Mode Sample");
        System.out.println("=================");
        System.out.println("Detector class    : " + detector.getClass().getName());
        System.out.println("Supported         : " + detector.isDarkModeDetectionSupported());

        boolean dark = detector.isDarkMode();
        System.out.println(now() + " Current dark mode: " + (dark ? "Dark" : "Light"));

        detector.addListener(isDark -> {
            System.out.println(now() + " Dark mode changed: " + (isDark ? "Dark" : "Light"));
        });

        System.out.println();
        System.out.println("Listening for changes. Press Ctrl+C to exit.");

        // Keep process alive until killed. Use a latch to wait indefinitely but remain interruptible.
        CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(latch::countDown));
        // Wait essentially forever, but allow interruption to terminate cleanly.
        while (!Thread.currentThread().isInterrupted()) {
            if (latch.await(365, TimeUnit.DAYS)) {
                break;
            }
        }
    }

    private static String now() {
        return "[" + LocalDateTime.now().format(TS) + "]";
    }
}
