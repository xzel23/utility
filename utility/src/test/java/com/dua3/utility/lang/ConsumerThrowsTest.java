package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LangUtil.ConsumerThrows default methods.
 */
class ConsumerThrowsTest {

    @Test
    void testAndThenTry_bothExecutedInOrder() throws Exception {
        List<String> calls = new ArrayList<>();
        LangUtil.ConsumerThrows<String, IOException> first = s -> calls.add("first:" + s);
        LangUtil.ConsumerThrows<String, IOException> second = s -> calls.add("second:" + s);

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThenTry(second);
        assertDoesNotThrow(() -> composed.accept("X"));

        assertEquals(List.of("first:X", "second:X"), calls, "Both consumers should be executed in order");
    }

    @Test
    void testAndThenTry_firstThrows_preventsSecond() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        LangUtil.ConsumerThrows<String, IOException> first = s -> {throw new IOException("boom");};
        LangUtil.ConsumerThrows<String, IOException> second = s -> counter.incrementAndGet();

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThenTry(second);
        IOException ex = assertThrows(IOException.class, () -> composed.accept("X"));
        assertEquals("boom", ex.getMessage());
        assertEquals(0, counter.get(), "Second consumer must not run if first throws");
    }

    @Test
    void testAndThenTry_secondThrows_afterFirstRan() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        LangUtil.ConsumerThrows<String, IOException> first = s -> counter.incrementAndGet();
        LangUtil.ConsumerThrows<String, IOException> second = s -> {throw new IOException("kaboom");};

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThenTry(second);
        IOException ex = assertThrows(IOException.class, () -> composed.accept("X"));
        assertEquals("kaboom", ex.getMessage());
        assertEquals(1, counter.get(), "First consumer should have run before second throws");
    }

    @Test
    void testAndThenTry_nullAfter_throwsAssertionOnComposition() {
        LangUtil.ConsumerThrows<String, IOException> first = s -> {};
        Throwable t = assertThrows(Throwable.class, () -> first.andThenTry(null));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
    }

    @Test
    void testAndThen_bothExecutedInOrder() throws Exception {
        List<String> calls = new ArrayList<>();
        LangUtil.ConsumerThrows<String, IOException> first = s -> calls.add("first:" + s);
        Consumer<String> second = s -> calls.add("second:" + s);

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThen(second);
        composed.accept("Y");

        assertEquals(List.of("first:Y", "second:Y"), calls, "Both consumers should be executed in order");
    }

    @Test
    void testAndThen_firstThrows_preventsSecond() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        LangUtil.ConsumerThrows<String, IOException> first = s -> {throw new IOException("boom");};
        Consumer<String> second = s -> counter.incrementAndGet();

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThen(second);
        IOException ex = assertThrows(IOException.class, () -> composed.accept("Y"));
        assertEquals("boom", ex.getMessage());
        assertEquals(0, counter.get(), "Second consumer must not run if first throws");
    }

    @Test
    void testAndThen_secondThrows_afterFirstRan() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        LangUtil.ConsumerThrows<String, IOException> first = s -> counter.incrementAndGet();
        Consumer<String> second = s -> {throw new RuntimeException("kaboom");};

        LangUtil.ConsumerThrows<String, IOException> composed = first.andThen(second);
        RuntimeException ex = assertThrows(RuntimeException.class, () -> composed.accept("Y"));
        assertEquals("kaboom", ex.getMessage());
        assertEquals(1, counter.get(), "First consumer should have run before second throws");
    }

    @Test
    void testAndThen_nullAfter_throwsAssertionOnComposition() {
        LangUtil.ConsumerThrows<String, IOException> first = s -> {};
        Throwable t = assertThrows(Throwable.class, () -> first.andThen((Consumer<String>) null));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
    }
}
