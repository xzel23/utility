package test.com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.lang.WrappedException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
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

    enum TestEnum {
        A ("First Letter"),
        B ("Second Letter");
        
        private final String s;
        
        TestEnum(String s) {
            this.s = s;
        }
        
        @Override
        public String toString() {
            return s;
        }
    }
    
    @Test
    void enumConstant() {
        assertFalse(LangUtil.enumConstant(TestEnum.class, "A").isPresent());
        assertFalse(LangUtil.enumConstant(TestEnum.class, "B").isPresent());
        assertEquals(TestEnum.A, LangUtil.enumConstant(TestEnum.class, "First Letter").get());
        assertEquals(TestEnum.B, LangUtil.enumConstant(TestEnum.class, "Second Letter").get());
        assertFalse(LangUtil.enumConstant(TestEnum.class, "Third Letter").isPresent());
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
        assertEquals("test", LangUtil.trimWithByteOrderMark(new String(new char[] { 0xfeff, 't', 'e', 's', 't' })));
        assertEquals("test\n"+(char)0xfeff+"test", LangUtil.trimWithByteOrderMark(new String(new char[] { 0xfeff, 't', 'e', 's', 't', '\n', 0xfeff, 't', 'e', 's', 't', '\n' })));
    }

    @Test
    void putAllIfAbsent() {
        Map<Integer, String> map = new HashMap<>();
        LangUtil.putAll(map, Pair.of(1,"a"), Pair.of(3, "c"), Pair.of(2, "b"), Pair.of(5, "e"));

        LangUtil.putAllIfAbsent(map, Pair.of(1,"x"), Pair.of(3, "y"), Pair.of(5, "b"), Pair.of(4, "d"));
        
        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "b");
        expected.put(3, "c");
        expected.put(4, "d");
        expected.put(5, "e");

        assertEquals(expected, map);
    }

    @Test
    void putAll() {
        Map<Integer, String> map = new HashMap<>();
        LangUtil.putAll(map, Pair.of(1,"a"), Pair.of(3, "c"), Pair.of(2, "b"));

        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "b");
        expected.put(3, "c");

        assertEquals(expected, map);
    }

    @Test
    void map() {
        Map<Integer,String> map = LangUtil.map(Pair.of(1,"a"), Pair.of(3, "c"), Pair.of(2, "b"));

        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "b");
        expected.put(3, "c");

        assertEquals(expected, map);
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    @Test
    void testEquals() {
        Integer[] a = {1,2,3,4,5};
        Integer[] b = {1,2,3,4,5,6,7};
        Integer[] c = {1,2,0,4,5};
        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        Integer[] d = {};

        assertTrue(LangUtil.equals(Arrays.stream(a), Arrays.stream(a)));
        //noinspection RedundantOperationOnEmptyContainer
        assertTrue(LangUtil.equals(Arrays.stream(d), Arrays.stream(d)));

        assertFalse(LangUtil.equals(Arrays.stream(a), Arrays.stream(b)));
        assertFalse(LangUtil.equals(Arrays.stream(a), Arrays.stream(c)));
        //noinspection RedundantOperationOnEmptyContainer
        assertFalse(LangUtil.equals(Arrays.stream(a), Arrays.stream(d)));
    }

    @Test
    void consumeIfPresent() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");

        AtomicReference<String> ref = new AtomicReference<>();
        LangUtil.consumeIfPresent(map, 2, ref::set);
        assertEquals("b", ref.get());

        LangUtil.consumeIfPresent(map, 4, v -> ref.set("x"));
        assertEquals("b", ref.get());
    }

    @Test
    void testConsumeIfPresentBiConsumer() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "a");
        map.put(2, "b");
        map.put(3, "c");

        AtomicReference<Integer> ref1 = new AtomicReference<>();
        AtomicReference<String> ref2 = new AtomicReference<>();
        LangUtil.consumeIfPresent(map, 2, (k,v) -> { ref1.set(k); ref2.set(v); });
        assertEquals(2, ref1.get());
        assertEquals("b", ref2.get());

        LangUtil.consumeIfPresent(map, 4, (k,v) -> { ref1.set(k); ref2.set(v); });
        assertEquals(2, ref1.get());
        assertEquals("b", ref2.get());
    }

    static class Foo implements Supplier<Integer> {
        private static final AtomicInteger n = new AtomicInteger(0);
        private final int value;

        Foo(int value) {
            this.value = value;
        }
        
        @Override
        public Integer get() {
            n.incrementAndGet();
            return value;
        }
    }
    
    @Test
    void cache() {
        Supplier<Integer> giveMe5 = LangUtil.cache(new Foo(5));
        
        for (int i=1; i<100; i++) {
            assertEquals(5, giveMe5.get());
        }
        assertTrue(Foo.n.get() < 50); // ideally n==1, but GC might kick in
    }

    @Test
    void enumSet() {
        assertTrue(LangUtil.enumSet(StandardOpenOption.class).isEmpty());
        assertEquals(EnumSet.of(StandardOpenOption.CREATE), LangUtil.enumSet(StandardOpenOption.class, StandardOpenOption.CREATE));
    }

    @Test
    void enumSetCollection() {
        assertTrue(LangUtil.enumSet(StandardOpenOption.class, Collections.emptyList()).isEmpty());
        assertEquals(EnumSet.of(StandardOpenOption.CREATE), LangUtil.enumSet(StandardOpenOption.class, Collections.singletonList(StandardOpenOption.CREATE)));
    }

    @Test
    void enumValues() {
        StandardOpenOption[] expected = StandardOpenOption.values();
        StandardOpenOption[] actual = LangUtil.enumValues(StandardOpenOption.class);
        assertArrayEquals(expected, actual);
    }

    @Test
    void getLocaleSuffix() {
        assertEquals("_de_DE", LangUtil.getLocaleSuffix(Locale.GERMANY));
        // Indonesian is a special case as until Java 16, the suffix differed from the language tag! 
        // changed with JDK-8267552
        // NOTE: GraalVM 21.3 reports a Java version of 17.0.1 so remove everything following the dot
        int javaVersion = Integer.parseInt(System.getProperty("java.version").replaceFirst("\\..*", ""));
        if (javaVersion<17) {
            assertEquals("_in", LangUtil.getLocaleSuffix(Locale.forLanguageTag("id")));
        } else {
            assertEquals("_id", LangUtil.getLocaleSuffix(Locale.forLanguageTag("id")));
        }
    }
    
    @Test
    void testSurroundingItems() {
        // assert correct items are present in output
        assertEquals(
                Arrays.asList(7,8,9,10,11,12,16,17,18,19,20,21), 
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26), 
                n -> n%9==0, 2, 3));

        // assert position is passed correctly
        assertEquals(
                Arrays.asList(0,7,8,9,10,11,12,-12,16,17,18,19,20,21,-21),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26),
                        n -> n%9==0, 2, 3, (count,pos) -> -pos));

        // assert item count is passed correctly
        assertEquals(
                Arrays.asList(-6,7,8,9,10,11,12,-3,16,17,18,19,20,21,-5),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26),
                        n -> n%9==0, 2, 3, (count,pos) -> -count));

        // assert correct items are present in output, first item matches
        assertEquals(
                Arrays.asList(1,2,3,4,8,9,10,11,12,13,17,18,19,20,21,22),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26),
                        n -> (n+8)%9==0, 2, 3));

        // assert position is passed correctly, first item matches
        assertEquals(
                Arrays.asList(1,2,3,4,-4,8,9,10,11,12,13,-13,17,18,19,20,21,22,-22),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26),
                        n -> (n+8)%9==0, 2, 3, (count,pos) -> -pos));

        // assert item count is passed correctly, first item matches
        assertEquals(
                Arrays.asList(1,2,3,4,-3,8,9,10,11,12,13,-3,17,18,19,20,21,22,-4),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26),
                        n -> (n+8)%9==0, 2, 3, (count,pos) -> -count));

        // assert correct items are present in output, last item matches
        assertEquals(
                Arrays.asList(7,8,9,10,11,12,16,17,18,19,20,21,25,26,27),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27),
                        n -> n%9==0, 2, 3));

        // assert position is passed correctly, last item matches
        assertEquals(
                Arrays.asList(0,7,8,9,10,11,12,-12,16,17,18,19,20,21,-21,25,26,27),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27),
                        n -> n%9==0, 2, 3, (count,pos) -> -pos));

        // assert item count is passed correctly, last item matches
        assertEquals(
                Arrays.asList(-6,7,8,9,10,11,12,-3,16,17,18,19,20,21,-3,25,26,27),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27),
                        n -> n%9==0, 2, 3, (count,pos) -> -count));

        // assert correct items are present in output, multiple subsequent matches
        assertEquals(
                Arrays.asList(3,4,5,6,7,8,9,10),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12),
                        n -> n>=5&&n<=7, 2, 3));

        // assert position is passed correctly, multiple subsequent matches
        assertEquals(
                Arrays.asList(0,3,4,5,6,7,8,9,10,-10),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12),
                        n -> n>=5&&n<=7, 2, 3, (count,pos) -> -pos));

        // assert item count is passed correctly, multiple subsequent matches
        assertEquals(
                Arrays.asList(-2,3,4,5,6,7,8,9,10,-2),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12),
                        n -> n>=5&&n<=7, 2, 3, (count,pos) -> -count));

        // assert item count is passed correctly, after/before overlap
        assertEquals(
                Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,-1),
                LangUtil.surroundingItems(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12),
                        n -> n==3 || n==8, 3, 3, (count,pos) -> -count));
    }
    
    @Test
    void testOrElse() {
        Object a = "a";
        Object b = "b";
        assertSame(null, LangUtil.orElse(null, null));
        assertSame(b, LangUtil.orElse(null, b));
        assertSame(a, LangUtil.orElse(a, null));
        assertSame(a, LangUtil.orElse(a, b));    
    }
    
    @Test
    void testOrElseGet() {
        Object a = "a";
        assertSame(null, LangUtil.orElse(null, null));
        assertEquals("b", LangUtil.orElseGet(null, () -> "b"));
        assertEquals("a", LangUtil.orElseGet("a", () -> "b"));    
    }
    
}