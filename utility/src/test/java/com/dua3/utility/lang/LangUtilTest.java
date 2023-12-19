package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedConsumer(t -> {
            throw new IOException("test");
        }).accept(null), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedConsumer(t -> {
            throw new IllegalStateException("test");
        }).accept(null), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedConsumer(t -> {
            throw new Exception("test");
        }).accept(null), "test");
    }

    @Test
    void uncheckedSupplier() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedSupplier(() -> {
            throw new IOException("test");
        }).get(), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedSupplier(() -> {
            throw new IllegalStateException("test");
        }).get(), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedSupplier(() -> {
            throw new Exception("test");
        }).get(), "test");
    }

    @Test
    void uncheckedFunction() {
        // make sure IOException is converted
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedFunction(x -> {
            throw new IOException("test");
        }).apply(null), "test");
        // make sure unchecked exceptions are not converted
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedFunction(x -> {
            throw new IllegalStateException("test");
        }).apply(null), "test");
        // make sure checked exceptions are conerted to unchecked
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedFunction(x -> {
            throw new Exception("test");
        }).apply(null), "test");
    }

    @Test
    void uncheckedRunnable() {
        assertThrows(UncheckedIOException.class, () -> LangUtil.uncheckedRunnable(() -> {
            throw new IOException("test");
        }).run(), "test");
        assertThrows(IllegalStateException.class, () -> LangUtil.uncheckedRunnable(() -> {
            throw new IllegalStateException("test");
        }).run(), "test");
        assertThrows(WrappedException.class, () -> LangUtil.uncheckedRunnable(() -> {
            throw new Exception("test");
        }).run(), "test");
    }

    @Test
    void trimWithByteOrderMark() {
        assertEquals("test", LangUtil.trimWithByteOrderMark(new String(new char[]{0xfeff, 't', 'e', 's', 't'})));
        assertEquals("test\n" + (char) 0xfeff + "test", LangUtil.trimWithByteOrderMark(new String(new char[]{0xfeff, 't', 'e', 's', 't', '\n', 0xfeff, 't', 'e', 's', 't', '\n'})));
    }

    @Test
    void putAllIfAbsent() {
        Map<Integer, String> map = new HashMap<>();
        LangUtil.putAll(map, Pair.of(1, "a"), Pair.of(3, "c"), Pair.of(2, "b"), Pair.of(5, "e"));

        LangUtil.putAllIfAbsent(map, Pair.of(1, "x"), Pair.of(3, "y"), Pair.of(5, "b"), Pair.of(4, "d"));

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
        LangUtil.putAll(map, Pair.of(1, "a"), Pair.of(3, "c"), Pair.of(2, "b"));

        Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "b");
        expected.put(3, "c");

        assertEquals(expected, map);
    }

    @SuppressWarnings("ZeroLengthArrayAllocation")
    @Test
    void testEquals() {
        Integer[] a = {1, 2, 3, 4, 5};
        Integer[] b = {1, 2, 3, 4, 5, 6, 7};
        Integer[] c = {1, 2, 0, 4, 5};
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
        LangUtil.consumeIfPresent(map, 2, (k, v) -> {
            ref1.set(k);
            ref2.set(v);
        });
        assertEquals(2, ref1.get());
        assertEquals("b", ref2.get());

        LangUtil.consumeIfPresent(map, 4, (k, v) -> {
            ref1.set(k);
            ref2.set(v);
        });
        assertEquals(2, ref1.get());
        assertEquals("b", ref2.get());
    }

    @Test
    void cache() {
        Supplier<Integer> giveMe5 = LangUtil.cache(new Foo(5));

        for (int i = 1; i < 100; i++) {
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
        if (javaVersion < 17) {
            assertEquals("_in", LangUtil.getLocaleSuffix(Locale.forLanguageTag("id")));
        } else {
            assertEquals("_id", LangUtil.getLocaleSuffix(Locale.forLanguageTag("id")));
        }
    }

    @Test
    void testSurroundingItems() {
        // assert correct items are present in output
        assertEquals(
                List.of(7, 8, 9, 10, 11, 12, 16, 17, 18, 19, 20, 21),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> n % 9 == 0, 2, 3));

        // assert position is passed correctly
        assertEquals(
                List.of(0, 7, 8, 9, 10, 11, 12, -12, 16, 17, 18, 19, 20, 21, -21),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> n % 9 == 0, 2, 3, (count, pos) -> -pos));

        // assert item count is passed correctly
        assertEquals(
                List.of(-6, 7, 8, 9, 10, 11, 12, -3, 16, 17, 18, 19, 20, 21, -5),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> n % 9 == 0, 2, 3, (count, pos) -> -count));

        // assert correct items are present in output, first item matches
        assertEquals(
                List.of(1, 2, 3, 4, 8, 9, 10, 11, 12, 13, 17, 18, 19, 20, 21, 22),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> (n + 8) % 9 == 0, 2, 3));

        // assert position is passed correctly, first item matches
        assertEquals(
                List.of(1, 2, 3, 4, -4, 8, 9, 10, 11, 12, 13, -13, 17, 18, 19, 20, 21, 22, -22),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> (n + 8) % 9 == 0, 2, 3, (count, pos) -> -pos));

        // assert item count is passed correctly, first item matches
        assertEquals(
                List.of(1, 2, 3, 4, -3, 8, 9, 10, 11, 12, 13, -3, 17, 18, 19, 20, 21, 22, -4),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> (n + 8) % 9 == 0, 2, 3, (count, pos) -> -count));

        // assert correct items are present in output, last item matches
        assertEquals(
                List.of(7, 8, 9, 10, 11, 12, 16, 17, 18, 19, 20, 21, 25, 26, 27),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27),
                        n -> n % 9 == 0, 2, 3));

        // assert position is passed correctly, last item matches
        assertEquals(
                List.of(0, 7, 8, 9, 10, 11, 12, -12, 16, 17, 18, 19, 20, 21, -21, 25, 26, 27),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27),
                        n -> n % 9 == 0, 2, 3, (count, pos) -> -pos));

        // assert item count is passed correctly, last item matches
        assertEquals(
                List.of(-6, 7, 8, 9, 10, 11, 12, -3, 16, 17, 18, 19, 20, 21, -3, 25, 26, 27),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27),
                        n -> n % 9 == 0, 2, 3, (count, pos) -> -count));

        // assert correct items are present in output, multiple subsequent matches
        assertEquals(
                List.of(3, 4, 5, 6, 7, 8, 9, 10),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        n -> n >= 5 && n <= 7, 2, 3));

        // assert position is passed correctly, multiple subsequent matches
        assertEquals(
                List.of(0, 3, 4, 5, 6, 7, 8, 9, 10, -10),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        n -> n >= 5 && n <= 7, 2, 3, (count, pos) -> -pos));

        // assert item count is passed correctly, multiple subsequent matches
        assertEquals(
                List.of(-2, 3, 4, 5, 6, 7, 8, 9, 10, -2),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        n -> n >= 5 && n <= 7, 2, 3, (count, pos) -> -count));

        // assert item count is passed correctly, after/before overlap
        assertEquals(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                        n -> n == 3 || n == 8, 3, 3, (count, pos) -> -count));

        // assert empty list is returned if there are no matches
        assertEquals(
                List.of(),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> false, 2, 3));


        // assert placeholder is returned if there are no matches and a placeholder is given
        assertEquals(
                List.of(99),
                LangUtil.surroundingItems(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
                        n -> false, 2, 3, (count, pos) -> 99));
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

    @Test
    void testAsFunction() {
        Map<String, Double> m = Map.of("π", Math.PI, "e", Math.E);
        Function<String,Double> f1 = LangUtil.asFunction(m);
        assertEquals(Math.PI, f1.apply("π"));
        assertEquals(Math.E, f1.apply("e"));
        assertEquals(null, f1.apply("x"));

        Function<String,Number> f2 = LangUtil.asFunction(m, 123.0);
        assertEquals(Math.PI, f2.apply("π"));
        assertEquals(Math.E, f2.apply("e"));
        assertEquals(123.0, f2.apply("x"));
    }

    enum TestEnum {
        A("First Letter"),
        B("Second Letter");

        private final String s;

        TestEnum(String s) {
            this.s = s;
        }

        @Override
        public String toString() {
            return s;
        }
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
    void isBetween_forLong_ShouldReturnTrue() {
        assertTrue(LangUtil.isBetween(5L, 3L, 7L));
    }

    @Test
    void isBetween_forLong_ShouldReturnFalse() {
        assertFalse(LangUtil.isBetween(8L, 3L, 7L));
    }

    @Test
    void isBetween_forDouble_ShouldReturnTrue() {
        assertTrue(LangUtil.isBetween(5.0, 3.0, 7.0));
    }

    @Test
    void isBetween_forDouble_ShouldReturnFalse() {
        assertFalse(LangUtil.isBetween(8.0, 3.0, 7.0));
    }

    @Test
    public void isBetween_forComparable_ShouldReturnTrue() {
        assertTrue(LangUtil.isBetween("b", "a", "c"));
    }

    @Test
    public void isBetween_forComparable_ShouldReturnFalse() {
        assertFalse(LangUtil.isBetween("z", "a", "y"));
    }

    @Test
    public void formatStackTrace_ShouldReturnText() {
        Exception sampleException = new Exception("SampleException");
        assertFalse(LangUtil.formatStackTrace(sampleException).isEmpty());
    }

    @Test
    void defaultToString_ShouldReturnExpectedResult() {
        assertEquals("null", LangUtil.defaultToString(null));
        String nonNullObjectToString = LangUtil.defaultToString(new Object());
        assertNotNull(nonNullObjectToString);
        assertFalse(nonNullObjectToString.isEmpty());
    }
    @Test
    public void testMapOptionalInt() {
        OptionalInt opt = OptionalInt.of(2);
        Optional<String> optResult = LangUtil.map(opt, i -> Integer.toString(i));
        Assertions.assertEquals("2", optResult.get());

        opt = OptionalInt.empty();
        optResult = LangUtil.map(opt, i -> Integer.toString(i));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    public void testMapOptionalLong() {
        OptionalLong opt = OptionalLong.of(3L);
        Optional<String> optResult = LangUtil.map(opt, l -> Long.toString(l));
        Assertions.assertEquals("3", optResult.get());

        opt = OptionalLong.empty();
        optResult = LangUtil.map(opt, l -> Long.toString(l));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    public void testMapOptionalDouble() {
        OptionalDouble opt = OptionalDouble.of(1.0);
        Optional<String> optResult = LangUtil.map(opt, d -> Double.toString(d));
        Assertions.assertEquals("1.0", optResult.get());

        opt = OptionalDouble.empty();
        optResult = LangUtil.map(opt, d -> Double.toString(d));
        Assertions.assertEquals(Optional.empty(), optResult);
    }
    @Test
    void testCache() {
        // Create a supplier that supplies a new string "test" and a Consumer that does nothing
        AtomicReference<String> value = new AtomicReference<>("test");
        Supplier<String> supplier = () -> value.get();
        Consumer<String> consumer = (String s) -> value.set(null);

        LangUtil.AutoCloseableSupplier<String> cS = LangUtil.cache(supplier, consumer);
        assertEquals("test", cS.get());

        // Make sure cache is preserving the value
        assertEquals("test", cS.get());

        // Close the cache
        cS.close();

        // Make sure cache was resetted to uninitialized state
        assertNotEquals("test", cS.get());
    }

    @Test
    void testGetResourceURL() {
        URL url = LangUtil.getResourceURL(this.getClass(), "resource.txt");
        assertNotNull(url);
    }

    @Test
    void testGetResourceAsString() throws IOException {
        String s = LangUtil.getResourceAsString(this.getClass(), "resource.txt");
        assertEquals("test", s);
    }

    @Test
    void testGetResource() throws IOException {
        byte[] data = LangUtil.getResource(this.getClass(), "resource.txt");
        assertEquals("test", new String(data, StandardCharsets.UTF_8));
    }

    @Test
    void testLoadPropertiesWithPath() throws IOException, URISyntaxException {
        Path path = Paths.get(this.getClass().getResource("test.properties").toURI());
        Properties properties = LangUtil.loadProperties(path);
        assertEquals("value", properties.get("key"));
    }

    @Test
    void testLoadPropertiesWithURL() throws IOException {
        URL url = this.getClass().getResource("test.properties");
        Properties properties = LangUtil.loadProperties(url);
        assertEquals("value", properties.get("key"));
    }

    @Test
    void testLoadPropertiesWithURI() throws IOException, URISyntaxException {
        URI uri = this.getClass().getResource("test.properties").toURI();
        Properties properties = LangUtil.loadProperties(uri);
        assertEquals("value", properties.get("key"));
    }
}
