package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This file contains test cases for the Stopwatch class
 * It tests both duration methods: elapsed and elapsedSplit.
 */
class StopwatchTest {

    private static double parseStandardSeconds(String s) {
        // format h:mm:ss.sss
        String[] parts = s.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        double seconds = Double.parseDouble(parts[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    private static double parseSecondsMillis(String s) {
        // format like "0.123s"
        String num = s.substring(0, s.length() - 1);
        return Double.parseDouble(num);
    }

    // A helper function to imitate a delay in the testing environment
    static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testElapsed() {
        // Create a new Stopwatch object
        Stopwatch stopWatch = Stopwatch.create("testElapsed");

        // Delay/Wait for a specified time
        delay(1000);

        // Verify that the elapsed method returns a duration that is greater than or equal to the delay time.
        assertTrue(stopWatch.elapsed().toMillis() >= 1000);
    }

    @Test
    void testElapsedSplit_same_delay() {
        // Create a new Stopwatch object. 
        Stopwatch stopWatch = Stopwatch.create("testElapsedSplit_same_delay");

        // Delay/Wait for 1000 milliseconds
        delay(1000);

        // Capture the elapsed time in a split while setting a new split
        stopWatch.elapsedSplit(true);

        // Delay/Wait for the same amount of time
        delay(1000);

        // Verify that the elapsedSplit method returns a duration that is greater than or equal
        // to the delay for the current split
        assertTrue(stopWatch.elapsed().toMillis() >= 2000);
        assertTrue(stopWatch.elapsedSplit(false).toMillis() >= 1000);
    }

    @Test
    void testElapsedSplit_different_delays() {
        // Create a new Stopwatch object. 
        Stopwatch stopWatch = Stopwatch.create("testElapsedSplit_different_delays");

        // Delay/Wait for 500 milliseconds
        delay(500);

        // Capture the elapsed time in a split while setting a new split
        stopWatch.elapsedSplit(true);

        // Delay/Wait for 1000 milliseconds
        delay(1000);

        // Verify that the elapsed method returns a duration that is greater than or equal
        // to the total delay time (i.e., 1500 ms)
        assertTrue(stopWatch.elapsed().toMillis() >= 1500);

        // Verify that the elapsedSplit method returns a duration that is greater than or equal
        // to the delay for the current split (i.e., 1000 ms)
        assertTrue(stopWatch.elapsedSplit(false).toMillis() >= 1000);
    }
    // This test case validates the functionality of the close() method in the AutoCloseableStopWatch class.

    @Test
    void testAutoCloseableStopWatchClose() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Stopwatch.AutoCloseableStopWatch stopWatch = new Stopwatch.AutoCloseableStopWatch("stopWatch", sw -> closed.set(true));

        stopWatch.close();

        assertTrue(closed.get(), "Expected stopWatch onClose method to be invoked upon close()");
    }

    // This test case ensures that the correct name is recorded while creating a new AutoCloseableStopWatch instance.
    @Test
    void testAutoCloseableStopWatchConstructor_withName() {
        String expectedName = "StopwatchUnderTest";
        try (Stopwatch.AutoCloseableStopWatch stopWatch = Stopwatch.create(expectedName, sw -> {})) {
            assertEquals(expectedName, stopWatch.getName(), "Expected the Stopwatch name to match the one supplied in the constructor.");
        }
    }
    // This test case ensures that the correct name is recorded while creating a new AutoCloseableStopWatch instance.

    @Test
    void testAutoCloseableStopWatchConstructor_withNameSupplier() {
        String expectedName = "StopwatchUnderTest";
        try (Stopwatch.AutoCloseableStopWatch stopWatch = Stopwatch.create(() -> expectedName, sw -> {})) {
            assertEquals(expectedName, stopWatch.getName(), "Expected the Stopwatch name to match the one supplied in the constructor.");
        }
    }

    @Test
    void testStandardFormat() {
        Duration duration = Duration.of(2, ChronoUnit.HOURS).plusMinutes(23).plusSeconds(18).plus(456, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.STANDARD.format(duration);
        assertEquals("2:23:18.456", result);
    }

    @Test
    void testHoursMinutesSecondsMillisFormat() {
        Duration duration = Duration.of(1, ChronoUnit.HOURS).plusMinutes(10).plusSeconds(45).plus(123, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.HOURS_MINUTES_SECONDS_MILLIS.format(duration);
        assertEquals("1:10:45.123", result);
    }

    @Test
    void testMinutesSecondsMillisFormat() {
        Duration duration = Duration.of(40, ChronoUnit.MINUTES).plusSeconds(30).plus(220, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.MINUTES_SECONDS_MILLIS.format(duration);
        assertEquals("40m:30.220s", result);
    }

    @Test
    void testSecondsMillisFormat() {
        Duration duration = Duration.of(90, ChronoUnit.SECONDS).plus(220, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.SECONDS_MILLIS.format(duration);
        assertEquals("90.220s", result);
    }

    @Test
    void testMillisFormat() {
        Duration duration = Duration.of(1000, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.MILLIS.format(duration);
        assertEquals("1000.000ms", result);
    }

    @Test
    void testToStringContainsNameAndFormats() {
        Stopwatch sw = Stopwatch.create("MySW");
        delay(10);
        String s = sw.toString();
        assertTrue(s.startsWith("[MySW] current split: "));
        assertTrue(s.contains(" total: "));
        // Ensure both time parts look like STANDARD format
        String[] parts = s.substring(s.indexOf(':') + 2).split(" total: ");
        String splitStr = parts[0];
        String totalStr = parts[1];
        // basic regex: h:mm:ss.sss
        assertTrue(splitStr.matches("\\d+:\\d{2}:\\d{2}\\.\\d{3}"));
        assertTrue(totalStr.matches("\\d+:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    @Test
    void testElapsedStringFormatStandard() {
        Stopwatch sw = Stopwatch.create("Test");
        delay(5);
        String s = sw.elapsedString();
        assertTrue(s.matches("\\d+:\\d{2}:\\d{2}\\.\\d{3}"));
    }

    @Test
    void testElapsedStringSplitFormattingAndReset() {
        Stopwatch sw = Stopwatch.create("TestSplit");
        delay(15);
        String beforeReset = sw.elapsedStringSplit(true); // returns old split and resets
        assertTrue(beforeReset.matches("\\d+:\\d{2}:\\d{2}\\.\\d{3}"));
        // After reset, split should start near zero
        delay(10);
        String afterReset = sw.elapsedStringSplit(false);
        double seconds = parseStandardSeconds(afterReset);
        assertTrue(seconds >= 0.005 && seconds < 1.0, "split after reset should be small, was " + seconds);
    }

    @Test
    void testLogElapsedCapturesValueAtCreation() {
        Stopwatch sw = Stopwatch.create("LogElapsed");
        delay(20);
        Object proxy = sw.logElapsed(Stopwatch.Format.SECONDS_MILLIS);
        // wait more time; toString should still reflect value at creation time
        delay(50);
        String fixed = proxy.toString();
        assertTrue(fixed.endsWith("s"));
        double fixedSeconds = parseSecondsMillis(fixed);
        double currentSeconds = sw.elapsed().toMillis() / 1000.0;
        assertTrue(fixedSeconds <= currentSeconds, "captured value should not exceed current elapsed");
    }

    @Test
    void testLogElapsedSplitCapturesSplitAtCreation() {
        Stopwatch sw = Stopwatch.create("LogSplit");
        delay(10);
        sw.elapsedSplit(true); // reset split
        delay(15);
        Object proxy = sw.logElapsedSplit(Stopwatch.Format.SECONDS_MILLIS, true);
        delay(50);
        String fixed = proxy.toString();
        double fixedSeconds = parseSecondsMillis(fixed);
        assertTrue(fixedSeconds >= 0.010 && fixedSeconds < 1.0, "captured split should be small but > 0");
        // ensure stability on repeated calls
        delay(30);
        String fixed2 = proxy.toString();
        assertEquals(fixed, fixed2);
    }
}