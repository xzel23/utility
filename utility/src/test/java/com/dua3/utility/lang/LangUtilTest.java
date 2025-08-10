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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Properties;
import java.util.UUID;
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
        assertDoesNotThrow(() -> LangUtil.check(true, "test %s", "succeeded"));
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
        //noinspection RedundantOperationOnEmptyContainer - needed for test
        assertTrue(LangUtil.equals(Arrays.stream(d), Arrays.stream(d)));

        assertFalse(LangUtil.equals(Arrays.stream(a), Arrays.stream(b)));
        assertFalse(LangUtil.equals(Arrays.stream(a), Arrays.stream(c)));
        //noinspection RedundantOperationOnEmptyContainer - needed for test
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
        assertEquals("_id", LangUtil.getLocaleSuffix(Locale.forLanguageTag("id")));
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
    void isBetweenLong() {
        assertTrue(LangUtil.isBetween(5L, 3L, 7L));
        assertFalse(LangUtil.isBetween(8L, 3L, 7L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.isBetween(8L, 7L, 3L));
    }

    @Test
    void isBetweenDouble() {
        assertTrue(LangUtil.isBetween(5.0, 3.0, 7.0));
        assertTrue(LangUtil.isBetween(5.0, 3.0, Double.POSITIVE_INFINITY));
        assertTrue(LangUtil.isBetween(5.0, Double.NEGATIVE_INFINITY, 7.0));
        assertTrue(LangUtil.isBetween(5.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertFalse(LangUtil.isBetween(8.0, 3.0, 7.0));
        assertFalse(LangUtil.isBetween(Double.NaN, 3.0, 7.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.isBetween(8.0, 7.0, 3.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.isBetween(2.0, Double.NaN, 3.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.isBetween(2.0, 1.0, Double.NaN));
    }

    @Test
    void isBetweenComparable() {
        assertTrue(LangUtil.isBetween("b", "a", "c"));
        assertFalse(LangUtil.isBetween("z", "a", "y"));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.isBetween("b", "z", "a"));
    }

    @Test
    void formatStackTrace_ShouldReturnText() {
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
    void testMapOptionalInt() {
        OptionalInt opt = OptionalInt.of(2);
        Optional<String> optResult = LangUtil.map(opt, i -> Integer.toString(i));
        Assertions.assertEquals("2", optResult.get());

        opt = OptionalInt.empty();
        optResult = LangUtil.map(opt, i -> Integer.toString(i));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    void testMapOptionalLong() {
        OptionalLong opt = OptionalLong.of(3L);
        Optional<String> optResult = LangUtil.map(opt, l -> Long.toString(l));
        Assertions.assertEquals("3", optResult.get());

        opt = OptionalLong.empty();
        optResult = LangUtil.map(opt, l -> Long.toString(l));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    void testMapOptionalDouble() {
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
        Supplier<String> supplier = value::get;
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

    @Test
    void formatLazy_withValidFormatStringAndArguments_ShouldCreateObject() {
        Object result = LangUtil.formatLazy("%s %s", "Hello", "World");
        assertEquals("Hello World", result.toString());
    }

    @Test
    void formatLazy_withNullFormatString_ShouldReturnNull() {
        Object result = LangUtil.formatLazy(null, "Hello", "World");
        assertEquals(Objects.toString(null), result.toString());
    }

    @Test
    void testRequireNonNegativeDouble() {
        assertEquals(5.0, LangUtil.requireNonNegative(5.0));
        assertEquals(0.0, LangUtil.requireNonNegative(0.0));
        assertEquals(Double.POSITIVE_INFINITY, LangUtil.requireNonNegative(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testRequireNonNegativeFloat() {
        assertEquals(5.0f, LangUtil.requireNonNegative(5.0f));
        assertEquals(0.0f, LangUtil.requireNonNegative(0.0f));
        assertEquals(Float.POSITIVE_INFINITY, LangUtil.requireNonNegative(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(Float.NEGATIVE_INFINITY));
    }

    @Test
    void testRequireNonNegativeLong() {
        assertEquals(5L, LangUtil.requireNonNegative(5L));
        assertEquals(0L, LangUtil.requireNonNegative(0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5L));
    }

    @Test
    void testRequireNonNegativeLongFmtArgs() {
        assertEquals(5L, LangUtil.requireNonNegative(5L, "Error: %s", 5L));
        assertEquals(0L, LangUtil.requireNonNegative(0L, "Error: %s", 0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5L, "Error: %s", -5L));
    }

    @Test
    void testRequireNonNegativeInt() {
        assertEquals(5, LangUtil.requireNonNegative(5));
        assertEquals(0, LangUtil.requireNonNegative(0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5));
    }

    @Test
    void testRequireNonNegativeIntFmtArgs() {
        assertEquals(5, LangUtil.requireNonNegative(5, "Error: %s", -5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5, "Error: %s", -5));
    }

    @Test
    void testRequireNonNegativeShort() {
        assertEquals((short) 5, LangUtil.requireNonNegative((short) 5));
        assertEquals((short) 0, LangUtil.requireNonNegative((short) 0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative((short) -5));
    }

    @Test
    void testRequireNonNegativeShortFmtArgs() {
        assertEquals((short) 5, LangUtil.requireNonNegative((short) 5, "Error: %s", -5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNonNegative(-5, "Error: %s", (short) -5));
    }

    @Test
    void testRequirePositiveDouble() {
        assertEquals(5.0, LangUtil.requirePositive(5.0));
        assertEquals(Double.POSITIVE_INFINITY, LangUtil.requirePositive(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(0.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testRequirePositiveFloat() {
        assertEquals(5.0f, LangUtil.requirePositive(5.0f));
        assertEquals(Float.POSITIVE_INFINITY, LangUtil.requirePositive(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(0.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(Float.NEGATIVE_INFINITY));
    }

    @Test
    void testRequirePositiveLong() {
        assertEquals(5L, LangUtil.requirePositive(5L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5L));
    }

    @Test
    void testRequirePositiveLongFmtArgs() {
        assertEquals(5L, LangUtil.requirePositive(5L, "Error: %s", 5L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(0L, "Error: %s", 0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5L, "Error: %s", -5L));
    }

    @Test
    void testRequirePositiveInt() {
        assertEquals(5, LangUtil.requirePositive(5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5));
    }

    @Test
    void testRequirePositiveIntFmtArgs() {
        assertEquals(5, LangUtil.requirePositive(5, "Error: %s", -5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(-5, "Error: %s", -5));
    }

    @Test
    void testRequireNegativeDouble() {
        assertEquals(-5.0, LangUtil.requireNegative(-5.0));
        assertEquals(Double.NEGATIVE_INFINITY, LangUtil.requireNegative(Double.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(Double.NaN));
    }

    @Test
    void testRequireNegativeFloat() {
        assertEquals(-5.0f, LangUtil.requireNegative(-5.0f));
        assertEquals(Float.NEGATIVE_INFINITY, LangUtil.requireNegative(Float.NEGATIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(Float.NaN));
    }

    @Test
    void testRequireNegativeLong() {
        assertEquals(-5L, LangUtil.requireNegative(-5L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5L));
    }

    @Test
    void testRequireNegativeLongFmtArgs() {
        assertEquals(-5L, LangUtil.requireNegative(-5L, "Error: %s", 5L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0L, "Error: %s", 0L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5L, "Error: %s", 5L));
    }

    @Test
    void testRequireNegativeInt() {
        assertEquals(-5, LangUtil.requireNegative(-5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5));
    }

    @Test
    void testRequireNegativeIntFmtArgs() {
        assertEquals(-5, LangUtil.requireNegative(-5, "Error: %s", 5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(0, "Error: %s", 0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(5, "Error: %s", 5));
    }

    @Test
    void testRequireInIntervalDouble() {
        assertEquals(5.0, LangUtil.requireInInterval(5.0, 1.0, 10.0));
        assertEquals(5.0, LangUtil.requireInInterval(5.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5.0, 6.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5.0, 6.0, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5.0, Double.NaN, 10.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(Double.NaN, 6.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(Double.NEGATIVE_INFINITY, 6.0, 10.0));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(Double.POSITIVE_INFINITY, 6.0, 10.0));
    }

    @Test
    void testRequireInIntervalLong() {
        assertEquals(5L, LangUtil.requireInInterval(5L, 1L, 10L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5L, 6L, 10L));
    }

    @Test
    void testRequireInIntervalLongFmtArgs() {
        assertEquals(5L, LangUtil.requireInInterval(5L, 1L, 10L, "Error: %s", 5L));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5L, 6L, 10L, "Error: %s", 5L));
    }

    @Test
    void testRequireInIntervalInt() {
        assertEquals(5, LangUtil.requireInInterval(5, 1, 10));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5, 6, 10));
    }

    @Test
    void testRequireInIntervalIntFmtArgs() {
        assertEquals(5, LangUtil.requireInInterval(5, 1, 10, "Error: %s", 5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5, 6, 10, "Error: %s", 5));
    }

    @Test
    void testReverseInPlace() {
        Integer[] arr1 = {1, 2, 3, 4, 5};
        LangUtil.reverseInPlace(arr1);
        assertArrayEquals(new Integer[]{5, 4, 3, 2, 1}, arr1);

        Integer[] arr2 = {1};
        LangUtil.reverseInPlace(arr2);
        assertArrayEquals(new Integer[]{1}, arr2);

        Integer[] arr3 = {};
        LangUtil.reverseInPlace(arr3);
        assertArrayEquals(new Integer[]{}, arr3);

        Integer[] arr4 = {1, 2, 3, 4};
        LangUtil.reverseInPlace(arr4);
        assertArrayEquals(new Integer[]{4, 3, 2, 1}, arr4);
    }

    @Test
    void testReverseInPlaceWithRange() {
        Integer[] arr = {1, 2, 3, 4, 5, 6, 7};
        LangUtil.reverseInPlace(arr, 2, 5);
        assertArrayEquals(new Integer[]{1, 2, 5, 4, 3, 6, 7}, arr);

        Integer[] arr2 = {1, 2, 3, 4, 5};
        LangUtil.reverseInPlace(arr2, 0, 5);
        assertArrayEquals(new Integer[]{5, 4, 3, 2, 1}, arr2);

        Integer[] arr3 = {1, 2, 3, 4, 5};
        LangUtil.reverseInPlace(arr3, 1, 4);
        assertArrayEquals(new Integer[]{1, 4, 3, 2, 5}, arr3);
    }

    @Test
    void testReverseInPlaceInvalidRange() {
        Integer[] arr = {1, 2, 3, 4, 5};

        assertThrows(IllegalArgumentException.class, () -> LangUtil.reverseInPlace(arr, 3, 2));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.reverseInPlace(arr, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.reverseInPlace(arr, 0, 6));
    }


    @Test
    void testIsWrapperFor() {
        // Test valid wrapper-class pairings
        assertTrue(LangUtil.isWrapperFor(Integer.class, int.class));
        assertTrue(LangUtil.isWrapperFor(Double.class, double.class));
        assertTrue(LangUtil.isWrapperFor(Float.class, float.class));
        assertTrue(LangUtil.isWrapperFor(Long.class, long.class));
        assertTrue(LangUtil.isWrapperFor(Short.class, short.class));
        assertTrue(LangUtil.isWrapperFor(Byte.class, byte.class));
        assertTrue(LangUtil.isWrapperFor(Character.class, char.class));
        assertTrue(LangUtil.isWrapperFor(Boolean.class, boolean.class));

        // Test invalid wrapper-class pairings
        assertFalse(LangUtil.isWrapperFor(String.class, int.class));
        assertFalse(LangUtil.isWrapperFor(Object.class, int.class));
        assertFalse(LangUtil.isWrapperFor(Integer.class, double.class));
        assertFalse(LangUtil.isWrapperFor(Double.class, boolean.class));
        assertFalse(LangUtil.isWrapperFor(Integer.class, String.class));
        assertFalse(LangUtil.isWrapperFor(String.class, String.class));

        // Test non-primitive second argument
        assertFalse(LangUtil.isWrapperFor(Integer.class, Integer.class));
    }

    @Test
    void testNewUuidV7() {
        // Test that the generated UUID has the correct version (7)
        UUID uuid = LangUtil.newUuidV7();
        assertEquals(7, uuid.version());

        // Test that the generated UUID has the correct variant (RFC 4122 variant)
        // In Java, the variant for RFC 4122 UUIDs is represented by the constant UUID.VARIANT_RFC_4122
        // which is 2 according to the documentation, but the actual implementation might return a different value
        // The important thing is that the most significant bits of the 8th octet are '10'
        long lsb = uuid.getLeastSignificantBits();
        // Check that the variant bits (the most significant bits of the 8th octet) are '10'
        assertEquals(0x8000000000000000L, lsb & 0xC000000000000000L, "Variant bits should be '10' for RFC 4122 UUIDs");

        // Test that two UUIDs generated in sequence are different
        UUID uuid2 = LangUtil.newUuidV7();
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testToByteArray() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = LangUtil.toByteArray(uuid);

        // Ensure the resulting byte array is exactly 16 bytes
        assertEquals(16, bytes.length);

        // Convert the byte array back to a UUID
        UUID reconstructedUuid = LangUtil.fromByteArray(bytes);

        // Verify that the reconstructed UUID matches the original
        assertEquals(uuid, reconstructedUuid);
    }

    @Test
    void testToByteArrayInvalid() {
        // Validate behavior when an invalid byte array length is passed
        assertThrows(IllegalArgumentException.class, () -> LangUtil.fromByteArray(new byte[10]));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.fromByteArray(new byte[20]));
    }

    @Test
    void testFromByteArrayValid() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = LangUtil.toByteArray(uuid);

        // Convert the bytes back to UUID and verify it matches the original UUID
        UUID result = LangUtil.fromByteArray(bytes);
        assertEquals(uuid, result);
    }

    @Test
    void testFromByteArrayInvalid() {
        // Test that exceptions are thrown for invalid byte arrays
        assertThrows(IllegalArgumentException.class, () -> LangUtil.fromByteArray(new byte[8]));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.fromByteArray(new byte[24]));
    }

    @Test
    void testNewUuidV7WithTimestamp() {
        // Create a specific timestamp
        Instant timestamp = Instant.ofEpochMilli(1625097600000L); // 2021-07-01T00:00:00Z

        // Generate a UUID with the timestamp
        UUID uuid = LangUtil.newUuidV7(timestamp);

        // Test that the generated UUID has the correct version (7)
        assertEquals(7, uuid.version());

        // Test that the generated UUID has the correct variant (RFC 4122 variant)
        // In Java, the variant for RFC 4122 UUIDs is represented by the constant UUID.VARIANT_RFC_4122
        // which is 2 according to the documentation, but the actual implementation might return a different value
        // The important thing is that the most significant bits of the 8th octet are '10'
        long lsb = uuid.getLeastSignificantBits();
        // Check that the variant bits (the most significant bits of the 8th octet) are '10'
        assertEquals(0x8000000000000000L, lsb & 0xC000000000000000L, "Variant bits should be '10' for RFC 4122 UUIDs");

        // Test that the timestamp in the UUID matches the provided timestamp
        // The timestamp is stored in the most significant 48 bits of the UUID
        long uuidTimestamp = uuid.getMostSignificantBits() >>> 16;
        assertEquals(timestamp.toEpochMilli(), uuidTimestamp);

        // Test that two UUIDs generated with the same timestamp are different
        UUID uuid2 = LangUtil.newUuidV7(timestamp);
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testNewUuidV7WithLongTimestamp() {
        // Create a specific timestamp
        long timestamp = 1625097600000L; // 2021-07-01T00:00:00Z

        // Generate a UUID with the timestamp
        UUID uuid = LangUtil.newUuidV7(timestamp);

        // Test that the generated UUID has the correct version (7)
        assertEquals(7, uuid.version());

        // Test that the generated UUID has the correct variant (RFC 4122 variant)
        long lsb = uuid.getLeastSignificantBits();
        assertEquals(0x8000000000000000L, lsb & 0xC000000000000000L, "Variant bits should be '10' for RFC 4122 UUIDs");

        // Test that the timestamp in the UUID matches the provided timestamp
        long uuidTimestamp = uuid.getMostSignificantBits() >>> 16;
        assertEquals(timestamp, uuidTimestamp);

        // Test that two UUIDs generated with the same timestamp are different
        UUID uuid2 = LangUtil.newUuidV7(timestamp);
        assertNotEquals(uuid, uuid2);
    }

    @Test
    void testGetTimestampRaw() {
        // Test with UUIDv7
        Instant timestamp = Instant.ofEpochMilli(1625097600000L); // 2021-07-01T00:00:00Z
        UUID uuidV7 = LangUtil.newUuidV7(timestamp);

        long extractedTimestamp = LangUtil.getTimestampRaw(uuidV7);
        assertEquals(timestamp.toEpochMilli(), extractedTimestamp);

        // Test with UUIDv1 (using a predefined UUIDv1 for testing)
        UUID uuidV1 = UUID.fromString("c9aec320-8b57-11eb-8dcd-0242ac130003"); // Version 1 UUID
        assertDoesNotThrow(() -> LangUtil.getTimestampRaw(uuidV1));

        // Test with unsupported version (UUIDv4)
        UUID uuidV4 = UUID.randomUUID(); // Version 4 UUID
        // UUIDv4 is not time-based, so it should throw an exception
        assertThrows(UnsupportedOperationException.class, () -> LangUtil.getTimestampRaw(uuidV4));
    }

    @Test
    void testGetTimestampAsInstant() {
        // Test with UUIDv7
        Instant timestamp = Instant.ofEpochMilli(1625097600000L); // 2021-07-01T00:00:00Z
        UUID uuidV7 = LangUtil.newUuidV7(timestamp);

        Instant extractedInstant = LangUtil.getTimestampAsInstant(uuidV7);
        assertEquals(timestamp, extractedInstant);

        // Test with UUIDv1 (using a predefined UUIDv1 for testing)
        UUID uuidV1 = UUID.fromString("c9aec320-8b57-11eb-8dcd-0242ac130003"); // Version 1 UUID
        assertDoesNotThrow(() -> LangUtil.getTimestampAsInstant(uuidV1));

        // Test with unsupported version (UUIDv4)
        UUID uuidV4 = UUID.randomUUID(); // Version 4 UUID
        assertThrows(UnsupportedOperationException.class, () -> LangUtil.getTimestampAsInstant(uuidV4));
    }

    @Test
    void testMapNonNullOrElse_WithValuePresent() {
        String input = "test";
        String result = LangUtil.mapNonNullOrElse(input, String::toUpperCase, "default");
        assertEquals("TEST", result);
    }

    @Test
    void testMapNonNullOrElse_WithNullValue() {
        String input = null;
        String result = LangUtil.mapNonNullOrElse(input, String::toUpperCase, "default");
        assertEquals("default", result);
    }

    @Test
    void testMapNonNullElseGet_WithValuePresent() {
        String input = "test";
        String result = LangUtil.mapNonNullElseGet(input, String::toUpperCase, () -> "default");
        assertEquals("TEST", result);
    }

    @Test
    void testMapNonNullElseGet_WithNullValue() {
        String input = null;
        String result = LangUtil.mapNonNullElseGet(input, String::toUpperCase, () -> "default");
        assertEquals("default", result);
    }

    @Test
    void testAddIf_withAllItemsValid() {
        List<Integer> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(i -> i % 2 == 0, list, 2, 4, 6);
        assertTrue(changed);
        assertEquals(List.of(2, 4, 6), list);
    }

    @Test
    void testAddIf_withSomeItemsValid() {
        List<Integer> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(i -> i % 2 == 0, list, 3, 4, 5);
        assertTrue(changed);
        assertEquals(List.of(4), list);
    }

    @Test
    void testAddIf_withNoItemsValid() {
        List<Integer> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(i -> i % 2 == 0, list, 1, 3, 5);
        assertFalse(changed);
        assertEquals(List.of(), list);
    }

    @Test
    void testAddIf_withEmptyItems() {
        List<Integer> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(i -> i % 2 == 0, list);
        assertFalse(changed);
        assertEquals(List.of(), list);
    }

    @Test
    void testAddIf_withNullPredicate() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(Objects::nonNull, list, "a", null, "b");
        assertTrue(changed);
        assertEquals(List.of("a", "b"), list);
    }

    @Test
    void testRemoveLeading() {
        List<Integer> list = new ArrayList<>(List.of(1, 1, 2, 3, 1));
        LangUtil.removeLeading(list, i -> i == 1);
        assertEquals(List.of(2, 3, 1), list);

        List<Integer> list2 = new ArrayList<>(List.of(3, 3, 3, 4, 5));
        LangUtil.removeLeading(list2, i -> i == 3);
        assertEquals(List.of(4, 5), list2);
    }

    @Test
    void testRemoveLeadingNoChanges() {
        List<Integer> list = new ArrayList<>(List.of(2, 3, 4));
        LangUtil.removeLeading(list, i -> i == 1);
        assertEquals(List.of(2, 3, 4), list);
    }

    @Test
    void testRemoveLeadingEmptyList() {
        List<Integer> emptyList = new ArrayList<>();
        LangUtil.removeLeading(emptyList, i -> i == 1);
        assertTrue(emptyList.isEmpty());
    }
}
