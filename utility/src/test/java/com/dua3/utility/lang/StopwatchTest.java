package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StopwatchTest {
    @Test
    void testStopwatch() throws InterruptedException {
        Stopwatch s = new Stopwatch("StopwatchTest");
        Thread.sleep(1000);
        Duration t1 = s.elapsed();
        Thread.sleep(1000);
        Duration t2 = s.elapsedSplit(false);
        Thread.sleep(1000);
        Duration t3 = s.elapsedSplit(true);
        Thread.sleep(1000);
        Duration t4 = s.elapsed();
        Duration t5 = s.elapsedSplit(false);
        Thread.sleep(1000);
        Duration t6 = s.elapsed();
        Thread.sleep(1000);
        String st = s.toString();

        assertTrue(t1.getSeconds() == 1);
        assertTrue(t2.getSeconds() == 2);
        assertTrue(t3.getSeconds() == 3);
        assertTrue(t4.getSeconds() == 4);
        assertTrue(t5.getSeconds() == 1);
        assertTrue(t6.getSeconds() == 5);

        assertTrue(st.matches("\\[StopwatchTest\\] current split: 0:00:0[34][.,][0-9]{3} total: 0:00:0[67][.,][0-9]{3}"), "format mismatch: " + st);
    }
}
