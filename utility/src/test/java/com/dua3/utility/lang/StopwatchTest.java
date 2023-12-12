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
public class StopwatchTest {

    // A helper function to imitate a delay in the testing environment
    static void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    public void testAutoCloseableStopWatchClose() {
        AtomicBoolean closed = new AtomicBoolean(false);
        Stopwatch.AutoCloseableStopWatch stopWatch = new Stopwatch.AutoCloseableStopWatch("stopWatch", sw -> closed.set(true));

        stopWatch.close();

        assertTrue(closed.get(), "Expected stopWatch onClose method to be invoked upon close()");
    }

    // This test case ensures that the correct name is recorded while creating a new AutoCloseableStopWatch instance.
    @Test
    public void testAutoCloseableStopWatchConstructor_withName() {
        String expectedName = "StopwatchUnderTest";
        Stopwatch.AutoCloseableStopWatch stopWatch = Stopwatch.create(expectedName, sw -> {
        });

        assertEquals(expectedName, stopWatch.getName(), "Expected the Stopwatch name to match the one supplied in the constructor.");
    }
    // This test case ensures that the correct name is recorded while creating a new AutoCloseableStopWatch instance.

    @Test
    public void testAutoCloseableStopWatchConstructor_withNameSupplier() {
        String expectedName = "StopwatchUnderTest";
        Stopwatch.AutoCloseableStopWatch stopWatch = Stopwatch.create(() -> expectedName, sw -> {
        });

        assertEquals(expectedName, stopWatch.getName(), "Expected the Stopwatch name to match the one supplied in the constructor.");
    }

    @Test
    public void testStandardFormat() {
        Duration duration = Duration.of(2, ChronoUnit.HOURS).plusMinutes(23).plusSeconds(18).plus(456, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.STANDARD.format(duration);
        assertEquals("2:23:18.456", result);
    }

    @Test
    public void testHoursMinutesSecondsMillisFormat() {
        Duration duration = Duration.of(1, ChronoUnit.HOURS).plusMinutes(10).plusSeconds(45).plus(123, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.HOURS_MINUTES_SECONDS_MILLIS.format(duration);
        assertEquals("1:10:45.123", result);
    }

    @Test
    public void testMinutesSecondsMillisFormat() {
        Duration duration = Duration.of(40, ChronoUnit.MINUTES).plusSeconds(30).plus(220, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.MINUTES_SECONDS_MILLIS.format(duration);
        assertEquals("40m:30.220s", result);
    }

    @Test
    public void testSecondsMillisFormat() {
        Duration duration = Duration.of(90, ChronoUnit.SECONDS).plus(220, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.SECONDS_MILLIS.format(duration);
        assertEquals("90.220s", result);
    }

    @Test
    public void testMillisFormat() {
        Duration duration = Duration.of(1000, ChronoUnit.MILLIS);
        String result = Stopwatch.Format.MILLIS.format(duration);
        assertEquals("1000.000ms", result);
    }
}