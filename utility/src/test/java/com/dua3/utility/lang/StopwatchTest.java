package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

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

        assertTrue(t1.getSeconds()==1 && t1.getNano()<500_000_000L);
        assertTrue(t2.getSeconds()==2 && t2.getNano()<500_000_000L);
        assertTrue(t3.getSeconds()==3 && t3.getNano()<500_000_000L);
        assertTrue(t4.getSeconds()==4 && t4.getNano()<500_000_000L);
        assertTrue(t5.getSeconds()==1 && t4.getNano()<500_000_000L);
        assertTrue(t6.getSeconds()==5 && t5.getNano()<500_000_000L);
        assertTrue(st.matches("\\[StopwatchTest\\] current split: 0:00:03[.,][01][0-9]{2} total: 0:00:06[.,][01][0-9]{2}"), "format mismatch: "+st);
    }
}
