package com.dua3.utility.lang;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.*;

class LangUtilTest {

    @Test
    void check() {
        assertDoesNotThrow(() -> LangUtil.check(true));
        assertThrows(LangUtil.FailedCheckException.class, () -> LangUtil.check(false));
    }

    @Test
    void testCheckWithSupplier() {
        assertDoesNotThrow(() -> LangUtil.check(true, () -> new IOException("Test")));
        assertThrows(IOException.class, () -> LangUtil.check(false, () -> new IOException("test")), "test");
    }

    @Test
    void testCheckWithFormatter() {
        assertDoesNotThrow(() -> LangUtil.check(true, "test %", "succeeded"));
        assertThrows(LangUtil.FailedCheckException.class, () -> LangUtil.check(false, "test %s", "succeeded"), "test succeeded");
    }

    @Test
    void checkIndex() {
        assertThrows(IndexOutOfBoundsException.class, () -> LangUtil.checkIndex(-1,3), "index: -1");
        assertDoesNotThrow(() -> LangUtil.checkIndex(0,3));
        assertDoesNotThrow(() -> LangUtil.checkIndex(1,3));
        assertDoesNotThrow(() -> LangUtil.checkIndex(2,3));
        assertThrows(IndexOutOfBoundsException.class, () -> LangUtil.checkIndex(3,3), "index: 3");
    }

    @Test
    void ignore() {
        assertDoesNotThrow(() -> LangUtil.ignore("test"));
    }

    @Test
    void isOneOf() {
        assertTrue(LangUtil.isOneOf(7, 3, 9, 7, 5));
        assertFalse(LangUtil.isOneOf(8, 3, 9, 7, 5));
    }

    @Test
    void enumConstant() {
        // TODO
    }

    @Test
    void testEnumConstant() {
        // TODO
    }

    @Test
    void isByteOrderMark() {
        assertTrue(LangUtil.isByteOrderMark((char) 0xfeff));
        assertFalse(LangUtil.isByteOrderMark(' '));
    }

    @Test
    void uncheckedConsumer() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedConsumer(t -> { throw new IOException("test"); }).accept(null), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedConsumer(t -> { throw new IllegalStateException("test"); }).accept(null), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedConsumer(t -> { throw new Exception("test"); }).accept(null), "test");
    }

    @Test
    void uncheckedSupplier() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedSupplier(() -> { throw new IOException("test"); }).get(), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedSupplier(() -> { throw new IllegalStateException("test"); }).get(), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedSupplier(() -> { throw new Exception("test"); }).get(), "test");
    }

    @Test
    void uncheckedFunction() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedFunction(x -> { throw new IOException("test"); }).apply(null), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedFunction(x -> { throw new IllegalStateException("test"); }).apply(null), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedFunction(x -> { throw new Exception("test"); }).apply(null), "test");
    }

    @Test
    void uncheckedRunnable() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedRunnable(() -> { throw new IOException("test"); }).run(), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedRunnable(() -> { throw new IllegalStateException("test"); }).run(), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedRunnable(() -> { throw new Exception("test"); }).run(), "test");
    }

    @Test
    void trimWithByteOrderMark() {
        // TODO
    }

    @Test
    void putAllIfAbsent() {
        // TODO
    }

    @Test
    void putAll() {
        // TODO
    }

    @Test
    void map() {
        // TODO
    }

    @Test
    void testEquals() {
        // TODO
    }

    @Test
    void consumeIfPresent() {
        // TODO
    }

    @Test
    void testConsumeIfPresent() {
        // TODO
    }

    @Test
    void msgs() {
        // TODO
    }

    @Test
    void cache() {
        // TODO
    }

    @Test
    void testCache() {
        // TODO
    }

    @Test
    void getResourceURL() {
        // TODO
    }

    @Test
    void getResourceAsString() {
        // TODO
    }

    @Test
    void getResource() {
        // TODO
    }

    @Test
    void setLogLevel() {
        // TODO
    }

    @Test
    void testSetLogLevel() {
        // TODO
    }

    @Test
    void testSetLogLevel1() {
        // TODO
    }

    @Test
    void enumSet() {
        // TODO
    }

    @Test
    void testEnumSet() {
        // TODO
    }

    @Test
    void getLocaleSuffix() {
        // TODO
    }

    @Test
    void testGetResourceURL() {
        // TODO
    }
}
