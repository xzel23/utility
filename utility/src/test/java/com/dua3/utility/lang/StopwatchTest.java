package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertEquals(1, t1.getSeconds());
        assertEquals(2, t2.getSeconds());
        assertEquals(3, t3.getSeconds());
        assertEquals(4, t4.getSeconds());
        assertEquals(1, t5.getSeconds());
        assertEquals(5, t6.getSeconds());

        assertTrue(st.matches("\\[StopwatchTest\\] current split: 0:00:0[34][.,][0-9]{3} total: 0:00:0[67][.,][0-9]{3}"), "format mismatch: " + st);
    }
}
