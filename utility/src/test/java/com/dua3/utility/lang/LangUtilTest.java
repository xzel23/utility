package com.dua3.utility.lang;

import com.dua3.utility.data.Pair;
import com.dua3.utility.io.IoUtil;
import com.dua3.utility.math.MathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class LangUtilTest {

    @Test
    void testAsUnmodifiableSortedListSet_basicProperties() {
        ImmutableSortedListSet<Integer> set = LangUtil.asUnmodifiableSortedListSet(3, 1, 2, 2);
        // sorted and unique
        assertEquals(List.of(1,2,3), set);
        // implements both List and SortedSet
        assertTrue(set instanceof List);
        assertTrue(set instanceof java.util.SortedSet);
        // unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> set.add(4));
        assertThrows(UnsupportedOperationException.class, () -> set.addAll(List.of(4,5)));
    }

    @Test
    void testTriStateSelect_variants() {
        String whenTrue = "T";
        String whenFalse = "F";
        String otherwise = "N";
        assertEquals("T", LangUtil.triStateSelect(Boolean.TRUE, whenTrue, whenFalse, otherwise));
        assertEquals("F", LangUtil.triStateSelect(Boolean.FALSE, whenTrue, whenFalse, otherwise));
        assertEquals("N", LangUtil.triStateSelect(null, whenTrue, whenFalse, otherwise));

        // also works with null payloads
        assertNull(LangUtil.triStateSelect(Boolean.TRUE, null, "x", "y"));
        assertNull(LangUtil.triStateSelect(Boolean.FALSE, "x", null, "y"));
        assertNull(LangUtil.triStateSelect(null, "x", "y", null));
    }

    @Test
    void testMap_nullableOverload() {
        // non-null input applies mapping
        Integer len = LangUtil.map("abc", s -> s == null ? null : s.length());
        assertEquals(3, len);
        // null input yields null without applying function
        AtomicInteger calls = new AtomicInteger();
        Integer res = LangUtil.map(null, s -> { calls.incrementAndGet(); return 42; });
        assertNull(res);
        assertEquals(0, calls.get(), "Mapper must not be called for null input");
    }

    @Test
    void testCheckArg_predicateOverload() {
        // valid
        assertDoesNotThrow(() -> LangUtil.checkArg("x", (Integer i) -> i > 0, 5));
        // invalid -> message contains arg name and value
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> LangUtil.checkArg("x", (Integer i) -> i > 0, -1));
        assertTrue(ex.getMessage().contains("invalid argument 'x': -1"));
    }

    @Test
    void testCheckArg_supplierOverload() {
        AtomicInteger supplied = new AtomicInteger(0);
        Supplier<String> supplier = () -> { supplied.incrementAndGet(); return "bad"; };
        // true -> no exception and supplier not evaluated
        assertDoesNotThrow(() -> LangUtil.checkArg(true, supplier));
        assertEquals(0, supplied.get());
        // false -> exception with supplied message and supplier evaluated exactly once
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.checkArg(false, supplier));
        assertEquals("bad", ex.getMessage());
        assertEquals(1, supplied.get());
    }

    @Test
    void testCheckArg_formatOverload() {
        assertDoesNotThrow(() -> LangUtil.checkArg(true, "value=%d", 1));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.checkArg(false, "value=%d", 2));
        assertEquals("value=2", ex.getMessage());
    }

    @Test
    void testCheckArg_formatOverload_invalidFormatCheckedWhenAssertionsEnabled() {
        boolean assertionsEnabled = false;
        assert (assertionsEnabled = true);
        Assumptions.assumeTrue(assertionsEnabled, "Assertions are enabled, format string should be checked against arguments");
        assertThrows(AssertionError.class, () -> LangUtil.checkArg(true, "value=%f", "x"));
    }

    @Test
    void testRequireNegativeShort_withMessage() {
        short v = -5;
        assertEquals(v, LangUtil.requireNegative(v, "msg %d", v));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative((short)0, "not neg: %d", 0));
        assertEquals("not neg: 0", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative((short)1, "not neg: %d", 1));
        assertEquals("not neg: 1", ex.getMessage());
    }

    @Test
    void testRequirePositiveShort_withMessage() {
        short v = 5;
        assertEquals(v, LangUtil.requirePositive(v, "msg %d", v));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive((short)0, "not pos: %d", 0));
        assertEquals("not pos: 0", ex.getMessage());
        ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive((short)-1, "not pos: %d", -1));
        assertEquals("not pos: -1", ex.getMessage());
    }

    @Test
    void testRemoveLeadingAndTrailing() {
        List<Integer> list = new ArrayList<>(List.of(1, 1, 2, 3, 1, 1));
        LangUtil.removeLeadingAndTrailing(list, i -> i == 1);
        assertEquals(List.of(2, 3), list);

        List<Integer> list2 = new ArrayList<>(List.of(3, 3, 3, 4, 5, 3, 3));
        LangUtil.removeLeadingAndTrailing(list2, i -> i == 3);
        assertEquals(List.of(4, 5), list2);
    }

    @Test
    void testRemoveLeadingAndTrailingNoChanges() {
        List<Integer> list = new ArrayList<>(List.of(2, 3, 4));
        LangUtil.removeLeadingAndTrailing(list, i -> i == 1);
        assertEquals(List.of(2, 3, 4), list);
    }

    @Test
    void testRemoveLeadingAndTrailingEmptyList() {
        List<Integer> emptyList = new ArrayList<>();
        LangUtil.removeLeadingAndTrailing(emptyList, i -> i == 1);
        assertTrue(emptyList.isEmpty());
    }

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
    void testCheckWithInvalidFormatter() {
        boolean assertionsEnabled = false;
        assert (assertionsEnabled = true);
        Assumptions.assumeTrue(assertionsEnabled, "Assertions are enabled, format string should be checked against arguments");

        // make sure the format is checked for both passed and failed conditions
        assertThrows(AssertionError.class, () -> LangUtil.check(true, "test %f", "succeeded"));
        assertThrows(AssertionError.class, () -> LangUtil.check(false, "test %f", "succeeded"));
    }

    @Test
    void ignore() {
        assertDoesNotThrow(() -> LangUtil.ignore("test"));
    }

    @Test
    void isOneOf() {
        assertTrue(LangUtil.isOneOf(7, 3, 9, 7, 5));
        assertFalse(LangUtil.isOneOf(8, 3, 9, 7, 5));
        assertTrue(LangUtil.isOneOf(null, 3, 9, null, 5));
        assertFalse(LangUtil.isOneOf(null, 3, 9, 7, 5));
        assertTrue(LangUtil.isOneOf(5, 3, 9, null, 5));
        assertFalse(LangUtil.isOneOf(8, 3, 9, null, 5));
        assertFalse(LangUtil.isOneOf(1));
        assertFalse(LangUtil.isOneOf(null));
    }

    @Test
    void isNoneOf_basicAndNullCases() {
        // basic
        assertTrue(LangUtil.isNoneOf(8, 3, 9, 7, 5));
        assertFalse(LangUtil.isNoneOf(7, 3, 9, 7, 5));

        // null arg, null present in rest
        assertFalse(LangUtil.isNoneOf(null, 3, 9, null, 5));
        // null arg, null not present
        assertTrue(LangUtil.isNoneOf(null, 3, 9, 7, 5));

        // non-null arg, null present in rest
        assertTrue(LangUtil.isNoneOf(8, 3, 9, null, 5));
        assertFalse(LangUtil.isNoneOf(5, 3, 9, null, 5));

        // empty varargs: nothing to compare to → treated as "none of" → true
        assertTrue(LangUtil.isNoneOf(1));
        assertTrue(LangUtil.isNoneOf(null));
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
        assertEquals("", LangUtil.trimWithByteOrderMark(""));
        assertEquals("", LangUtil.trimWithByteOrderMark(new String(new char[]{0xfeff})));
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
    void testMapOptionalOptionalInt() {
        OptionalInt opt = OptionalInt.of(2);
        Optional<String> optResult = LangUtil.mapOptional(opt, i -> Integer.toString(i));
        Assertions.assertEquals("2", optResult.get());

        opt = OptionalInt.empty();
        optResult = LangUtil.mapOptional(opt, i -> Integer.toString(i));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    void testMapOptionalOptionalLong() {
        OptionalLong opt = OptionalLong.of(3L);
        Optional<String> optResult = LangUtil.mapOptional(opt, l -> Long.toString(l));
        Assertions.assertEquals("3", optResult.get());

        opt = OptionalLong.empty();
        optResult = LangUtil.mapOptional(opt, l -> Long.toString(l));
        Assertions.assertEquals(Optional.empty(), optResult);
    }

    @Test
    void testMapOptionalOptionalDouble() {
        OptionalDouble opt = OptionalDouble.of(1.0);
        Optional<String> optResult = LangUtil.mapOptional(opt, d -> Double.toString(d));
        Assertions.assertEquals("1.0", optResult.get());

        opt = OptionalDouble.empty();
        optResult = LangUtil.mapOptional(opt, d -> Double.toString(d));
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

    @ParameterizedTest
    @CsvSource({
            "com.dua3.utility.lang.LangUtilTest, resource.txt",
            "com.dua3.utility.io.IoUtil, resource2.txt"
    })
    void testGetResourceURL_ValidInput(String className, String resourceName) {
        Class<?> clazz = "com.dua3.utility.lang.LangUtilTest".equals(className) ? this.getClass() : IoUtil.class;
        assertDoesNotThrow(() -> {
            URL url = LangUtil.getResourceURL(clazz, resourceName);
            assertNotNull(url, "The resource URL should not be null for valid input.");
        });
    }

    @Test
    void testGetResourceURL_MissingResourceException() {
        MissingResourceException exception = assertThrows(
                MissingResourceException.class,
                () -> LangUtil.getResourceURL(this.getClass(), "non_existing.txt"),
                "Resource not found: non_existing.txt"
        );
        assertTrue(exception.getMessage().contains("non_existing.txt"), "Exception message should mention the missing resource.");
    }

    @ParameterizedTest
    @CsvSource({
            "null, resource.txt",
            "com.dua3.utility.lang.LangUtilTest, null",
            "null, null"
    })
    void testGetResourceURL_NullInput(String className, String resourceName) {
        Class<?> clazz = className.equals("null") ? null : this.getClass();
        String resource = resourceName.equals("null") ? null : resourceName;
        Throwable t = assertThrows(Throwable.class, () -> LangUtil.getResourceURL(clazz, resource));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError,
                "Expected exception should be NullPointerException or AssertionError.");
    }

    @Test
    void testGetResourceURL_ValidResource() {
        URL url = LangUtil.getResourceURL(this.getClass(), "resource.txt");
        assertNotNull(url, "The resource should exist and not be null");
    }

    @Test
    void testGetResourceURL_InvalidResource() {
        assertThrows(MissingResourceException.class, () -> LangUtil.getResourceURL(this.getClass(), "nonexistent.txt"),
                "Resource not found: nonexistent.txt");
    }

    @ParameterizedTest
    @CsvSource({
            "resource.txt, en_US",
            "resource.txt, fr_FR",
            "resource.txt, de_DE"
    })
    void testGetResourceURL_WithLocale(String resourceName, String localeTag) {
        Locale locale = Locale.forLanguageTag(localeTag);
        assertDoesNotThrow(() -> {
            URL url = LangUtil.getResourceURL(this.getClass(), resourceName, locale);
            assertNotNull(url, "Resource should be found for valid input with locale");
        });
    }

    @ParameterizedTest
    @CsvSource({
            "nonexistent.txt, en_US",
            "invalidResource.xyz, fr_FR"
    })
    void testGetResourceURL_InvalidResourceWithLocale(String resourceName, String localeTag) {
        Locale locale = Locale.forLanguageTag(localeTag);
        assertThrows(MissingResourceException.class, () ->
                        LangUtil.getResourceURL(this.getClass(), resourceName, locale),
                "Resource should not be found for an invalid input with locale"
        );
    }

    @Test
    void testGetResourceURL_MissingLocale() {
        Throwable t = assertThrows(Throwable.class, () ->
                LangUtil.getResourceURL(this.getClass(), "resource.txt", null));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError,
                "Expected exception should be NullPointerException or AssertionError when locale is missing.");
    }

    @Test
    void testGetResourceURL_InvalidClass() {
        Throwable t = assertThrows(Throwable.class, () -> LangUtil.getResourceURL(null, "resource.txt"),
                "A null class should result in an exception.");
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
    }

    @ParameterizedTest
    @CsvSource({
            ", resource.txt",
            "com.dua3.utility.lang.LangUtilTest, ",
            ", "
    })
    void testGetResourceURL_NullParameters(String className, String resourceName) {
        Class<?> clazz = className == null ? null : this.getClass();
        if (clazz == null || resourceName == null) {
            Throwable t = assertThrows(Throwable.class, () -> LangUtil.getResourceURL(clazz, resourceName));
            assertTrue(t instanceof NullPointerException || t instanceof AssertionError);
        } else {
            assertDoesNotThrow(() -> LangUtil.getResourceURL(clazz, resourceName));
        }
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
    void testRequireInIntervalFloat() {
        assertEquals(5.0f, LangUtil.requireInInterval(5.0f, 1.0f, 10.0f));
        assertEquals(5.0f, LangUtil.requireInInterval(5.0f, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5.0f, 6.0f, 10.0f));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(5.0f, 6.0f, Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval(Float.NaN, 6.0f, 10.0f));
    }

    @Test
    void testRequireInIntervalShort() {
        assertEquals((short) 5, LangUtil.requireInInterval((short) 5, (short) 1, (short) 10));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval((short) 5, (short) 6, (short) 10));
    }

    @Test
    void testRequireInIntervalShortFmtArgs() {
        assertEquals((short) 5, LangUtil.requireInInterval((short) 5, (short) 1, (short) 10, "Error: %s", 5));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireInInterval((short) 5, (short) 6, (short) 10, "Error: %s", 5));
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
    void testMapOptionalNonNullOrElse_WithValuePresent() {
        String input = "test";
        String result = LangUtil.mapNonNullOrElse(input, String::toUpperCase, "default");
        assertEquals("TEST", result);
    }

    @Test
    void testMapOptionalNonNullOrElse_WithNullValue() {
        String input = null;
        String result = LangUtil.mapNonNullOrElse(input, String::toUpperCase, "default");
        assertEquals("default", result);
    }

    @Test
    void testMapOptionalNonNullElseGet_WithValuePresent() {
        String input = "test";
        String result = LangUtil.mapNonNullElseGet(input, String::toUpperCase, () -> "default");
        assertEquals("TEST", result);
    }

    @Test
    void testMapOptionalNonNullElseGet_WithNullValue() {
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
    void testAddIf_withNullItemsAcceptedByPredicate() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIf(x -> true, list, "a", null, "b", null);
        assertTrue(changed);
        // when predicate allows, null is added as well
        assertEquals(Arrays.asList("a", null, "b", null), list);
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

    @Test
    void testAddIfNonNull_withNonNullItems() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIfNonNull(list, "a", "b", "c");
        assertTrue(changed);
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void testAddIfNonNull_withMixedItems() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIfNonNull(list, "a", null, "b", null, "c");
        assertTrue(changed);
        assertEquals(List.of("a", "b", "c"), list);
    }

    @Test
    void testAddIfNonNull_withEmptyItems() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIfNonNull(list);
        assertFalse(changed);
        assertTrue(list.isEmpty());
    }

    @Test
    void testAddIfNonNull_withNoItems() {
        List<String> list = new ArrayList<>();
        boolean changed = LangUtil.addIfNonNull(list, (String) null);
        assertFalse(changed);
        assertTrue(list.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.0001, -Double.MAX_VALUE, -1e-10})
    void testRequireNegativeDoubleValid(double value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, Double.POSITIVE_INFINITY, Double.NaN, 1.0})
    void testRequireNegativeDoubleInvalid(double value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(floats = {-1.0f, -0.0001f, -Float.MAX_VALUE, -1e-10f})
    void testRequireNegativeFloatValid(float value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, Float.POSITIVE_INFINITY, Float.NaN, 1.0f})
    void testRequireNegativeFloatInvalid(float value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, -123456789L, Long.MIN_VALUE})
    void testRequireNegativeLongValid(long value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, Long.MAX_VALUE})
    void testRequireNegativeLongInvalid(long value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -1000, Integer.MIN_VALUE})
    void testRequireNegativeIntValid(int value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, Integer.MAX_VALUE})
    void testRequireNegativeIntInvalid(int value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(shorts = {-1, -100, Short.MIN_VALUE})
    void testRequireNegativeShortValid(short value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, 1, Short.MAX_VALUE})
    void testRequireNegativeShortInvalid(short value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @Test
    void testRequireNegativeWithMessage() {
        double validNegative = -1.0;
        double zeroValue = 0.0;
        double positiveValue = 1.0;

        assertEquals(validNegative, LangUtil.requireNegative(validNegative, "Error: %f must be negative", validNegative));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> LangUtil.requireNegative(positiveValue, "Error: %f must be negative", positiveValue));
        assertTrue(exception.getMessage().contains(String.format(Locale.getDefault(), "%f", 1.0)));
        exception = assertThrows(IllegalArgumentException.class,
                () -> LangUtil.requireNegative(zeroValue, "Error: %f must be negative", zeroValue));
        assertTrue(exception.getMessage().contains(String.format(Locale.getDefault(), "%f", 0.0)));
    }

    @Test
    void testRequireNegativeByte() {
        byte validNegative = -10;
        byte zeroValue = 0;
        byte positiveValue = 10;

        assertEquals(validNegative, LangUtil.requireNegative(validNegative));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(zeroValue));
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(positiveValue));
    }

    @ParameterizedTest
    @ValueSource(bytes = {-1, -100, Byte.MIN_VALUE})
    void testRequireNegativeByteValid(byte value) {
        assertEquals(value, LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @ValueSource(bytes = {0, 1, Byte.MAX_VALUE})
    void testRequireNegativeByteInvalid(byte value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value));
    }

    @ParameterizedTest
    @CsvSource({"-1.0, valid negative value", "-0.0001, valid negative value", "1.0, wrong value", "0.0, zero value"})
    void testRequireNegativeDoubleWithMessage(double value, String description) {
        if (value < 0) {
            assertEquals(value, LangUtil.requireNegative(value, "Error: %f is not negative", value));
        } else {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requireNegative(value, "Error: %f is not negative", value));
            assertTrue(ex.getMessage().contains("%f".formatted(value)));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 0.0001, Double.MAX_VALUE, 1e-10})
    void testRequirePositiveDoubleValid(double value) {
        assertEquals(value, LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -1.0, Double.NEGATIVE_INFINITY, Double.NaN})
    void testRequirePositiveDoubleInvalid(double value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(floats = {1.0f, 0.0001f, Float.MAX_VALUE, 1e-10f})
    void testRequirePositiveFloatValid(float value) {
        assertEquals(value, LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.0f, -1.0f, Float.NEGATIVE_INFINITY, Float.NaN})
    void testRequirePositiveFloatInvalid(float value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, Long.MAX_VALUE})
    void testRequirePositiveLongValid(long value) {
        assertEquals(value, LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, Long.MIN_VALUE})
    void testRequirePositiveLongInvalid(long value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, Integer.MAX_VALUE})
    void testRequirePositiveIntValid(int value) {
        assertEquals(value, LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void testRequirePositiveIntInvalid(int value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(shorts = {1, Short.MAX_VALUE})
    void testRequirePositiveShortValid(short value) {
        assertEquals(value, LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @ValueSource(shorts = {0, -1, Short.MIN_VALUE})
    void testRequirePositiveShortInvalid(short value) {
        assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value));
    }

    @ParameterizedTest
    @CsvSource({"1.0, Valid positive value", "0.0, Zero value", "-1.0, Negative value"})
    void testRequirePositiveDoubleWithMessage(double value, String description) {
        if (value > 0) {
            assertEquals(value, LangUtil.requirePositive(value, "Error: %f must be positive", value));
        } else {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> LangUtil.requirePositive(value, "Error: %f must be positive", value));
            assertTrue(ex.getMessage().contains("%f".formatted(value)));
        }
    }
    @Test
    void testMapOptionalNonNullValidInput() {
        String result = LangUtil.mapNonNull("hello", String::toUpperCase);
        assertEquals("HELLO", result, "Expected the input 'hello' to be converted to 'HELLO'");
    }

    @Test
    void testMapOptionalNonNullNullInput() {
        String result = LangUtil.mapNonNull((String) null, String::toUpperCase);
        assertNull(result, "Expected null input to result in null output");
    }

    @ParameterizedTest
    @CsvSource({
            "abc, ABC",
            "123, 123",
            "'', ''",
            "null, null"
    })
    void testMapOptionalNonNullVariousCases(String input, String expected) {
        String actual = LangUtil.mapNonNull("null".equals(input) ? null : input, String::toUpperCase);
        String expectedValue = "null".equals(expected) ? null : expected;
        assertEquals(expectedValue, actual, "Expected mapped value to equal the expected result");
    }

    @Test
    void testMapOptionalNonNullWithNullMapper() {
        Throwable t = assertThrows(Throwable.class, () -> LangUtil.mapNonNull("test", null),
                "Expected an exception when null is passed as the mapper");
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError,
                "Expected exception should be NullPointerException or AssertionError.");
    }

    @ParameterizedTest
    @CsvSource({
            "null, true",
            "naturalOrder, true",
            "custom, false"
    })
    void testIsNaturalOrder(String comparatorType, boolean expectedResult) {
        Comparator<Integer> comparator = switch (comparatorType) {
            case "naturalOrder" -> Comparator.naturalOrder();
            case "custom" -> (a, b) -> -MathUtil.sign(Integer.compare(a, b));
            default -> null; // Handles the "null" case
        };
        assertEquals(expectedResult, LangUtil.isNaturalOrder(comparator));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 5, -1",
            "7, 3, 1",
            "4, 4, 0"
    })
    void testCompareValidInputs(Integer k1, Integer k2, int expectedResult) {
        Comparator<Integer> naturalComparator = Integer::compareTo;
        assertEquals(expectedResult, LangUtil.compare(naturalComparator, k1, k2));
    }

    @ParameterizedTest
    @CsvSource({
            "3, 5, -1",
            "5, 3, 1",
            "4, 4, 0"
    })
    void testCompareWithNullComparator(Integer k1, Integer k2, int expectedResult) {
        assertEquals(expectedResult, LangUtil.compare(null, k1, k2));
    }

    @Test
    void testCompareWithNullKeys() {
        Comparator<String> customComparator = Comparator.nullsFirst(String::compareTo);
        assertEquals(-1, LangUtil.compare(customComparator, null, "a"));
        assertEquals(0, LangUtil.compare(customComparator, null, null));
        assertEquals(1, LangUtil.compare(customComparator, "a", null));
    }

    @Test
    void testCompareWithNullInputs() {
        assertEquals(0, LangUtil.compare(null, null, null));
        assertEquals(0, LangUtil.compare(LangUtil.naturalOrder(), (String) null, (String) null));
        assertTrue(LangUtil.compare(LangUtil.naturalOrder(), "a", null) > 0);
        assertTrue(LangUtil.compare(LangUtil.naturalOrder(), null, "b") < 0);
        assertTrue(LangUtil.compare(null, "a", "b") < 0);
        assertTrue(LangUtil.compare(null, "b", "a") > 0);
        assertEquals(0, LangUtil.compare(null, "a", "a"));
    }

    @Test
    void testCompareWithNullInputsAndNullAwareComparator() {
        Comparator<String> comparator = Comparator.nullsFirst(Comparator.naturalOrder());

        assertEquals(0, LangUtil.compare(comparator, (String) null, (String) null));
        assertEquals(0, LangUtil.compare(comparator, "a", "a"));
        assertEquals(1, LangUtil.compare(comparator, "a", (String) null));
        assertEquals(-1, LangUtil.compare(comparator, (String) null, "a"));
    }

    @Test
    void testCompareWithCustomComparator() {
        Comparator<String> reverseComparator = (a, b) -> -MathUtil.sign(a.compareTo(b));

        Throwable t = assertThrows(Throwable.class, () -> LangUtil.compare(reverseComparator, (String) null, (String) null));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);

        t = assertThrows(Throwable.class, () -> LangUtil.compare(reverseComparator, "a", null));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);

        t = assertThrows(Throwable.class, () -> LangUtil.compare(reverseComparator, null, "b"));
        assertTrue(t instanceof NullPointerException || t instanceof AssertionError);

        assertEquals(-1, LangUtil.compare(reverseComparator, "b", "a"));
        assertEquals(1, LangUtil.compare(reverseComparator, "a", "b"));
        assertEquals(0, LangUtil.compare(reverseComparator, "c", "c"));
    }

    @Test
    void getOrThrow_returnsExistingNonNullValue() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);

        Integer result = LangUtil.getOrThrow(map, "a");
        assertEquals(1, result);
    }

    @Test
    void getOrThrow_throwsWhenKeyMissing() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", 1);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> LangUtil.getOrThrow(map, "b"));
        assertTrue(ex.getMessage().contains("no value for key: b"));
    }

    @Test
    void getOrThrow_throwsWhenValueIsNull() {
        Map<String, Integer> map = new HashMap<>();
        map.put("a", null);

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> LangUtil.getOrThrow(map, "a"));
        assertTrue(ex.getMessage().contains("no value for key: a"));
    }

    // --- ifPresent ---

    @Test
    void ifPresent_invokesConsumerWhenKeyPresentWithNonNullValue() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        AtomicReference<String> received = new AtomicReference<>();

        LangUtil.ifPresent(map, "k", received::set);

        assertEquals("v", received.get());
    }

    @Test
    void ifPresent_doesNothingWhenKeyMissing() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        AtomicInteger counter = new AtomicInteger();
        Consumer<String> consumer = s -> counter.incrementAndGet();

        LangUtil.ifPresent(map, "missing", consumer);

        assertEquals(0, counter.get(), "consumer should not be invoked for missing key");
    }

    @Test
    void ifPresent_doesNothingWhenValueIsNull() {
        Map<String, String> map = new HashMap<>();
        map.put("k", null);
        AtomicInteger counter = new AtomicInteger();
        Consumer<String> consumer = s -> counter.incrementAndGet();

        LangUtil.ifPresent(map, "k", consumer);

        assertEquals(0, counter.get(), "consumer should not be invoked for null value");
    }

    @Test
    void newWeakHashSet_basicOperations() {
        var set = LangUtil.newWeakHashSet();
        Long a = 123456789012L;
        Long b = 123456789013L;

        assertTrue(set.add(a));
        assertTrue(set.contains(a));
        assertFalse(set.contains(b));
        assertEquals(1, set.size());

        assertFalse(set.add(a), "adding same element again should not change set");
        assertEquals(1, set.size());

        assertTrue(set.remove(a));
        assertFalse(set.contains(a));
        assertEquals(0, set.size());
    }

    @Test
    void newWeakHashSet_allowsGarbageCollection() {
        var set = LangUtil.newWeakHashSet();

        Object obj = new Object();
        java.lang.ref.WeakReference<Object> ref = new java.lang.ref.WeakReference<>(obj);

        set.add(obj);
        assertEquals(1, set.size());
        assertNotNull(ref.get());

        // Drop strong reference and encourage GC
        obj = null;

        // Loop a few times to give GC a chance; WeakHashMap cleans on access
        boolean cleared = false;
        for (int i = 0; i < 100; i++) {
            System.gc();
            // touch the set so that stale entries are expunged
            set.contains(new Object());
            if (ref.get() == null) {
                cleared = true;
                if (set.size() == 0) {
                    break;
                }
            }
            try { Thread.sleep(10); } catch (InterruptedException ignored) { }
        }

        assertTrue(cleared, "referent should be collected eventually");
        assertEquals(0, set.size(), "stale entry should be removed from weak set");
    }

    @Test
    void newWeakHashSet_withInitialCapacity_behavesLikeWeakSet() {
        var set = LangUtil.newWeakHashSet(16);
        Long x = 12345678901234L;
        assertTrue(set.add(x));
        assertTrue(set.contains(x));
        assertEquals(1, set.size());

        // Now allow it to be GC'd like above
        java.lang.ref.WeakReference<Long> ref = new java.lang.ref.WeakReference<>(x);
        x = null;
        boolean cleared = false;
        for (int i = 0; i < 100; i++) {
            System.gc();
            set.size(); // touch
            if (ref.get() == null) {
                cleared = true;
                if (set.size() == 0) {
                    break;
                }
            }
            try { Thread.sleep(10); } catch (InterruptedException ignored) { }
        }
        assertTrue(cleared, "referent should be collected eventually");
        assertEquals(0, set.size(), "stale entry should be removed from weak set");
    }

    // --- applyIfNonNull ---

    @Test
    void applyIfNonNull_whenValueNonNull_executesConsumerAndReturnsSame() {
        AtomicReference<String> received = new AtomicReference<>();
        String input = "value";

        String result = LangUtil.applyIfNonNull(input, received::set);

        assertEquals("value", received.get());
        assertSame(input, result);
    }

    @Test
    void applyIfNonNull_whenValueNull_doesNotExecuteConsumerAndReturnsNull() {
        AtomicInteger counter = new AtomicInteger();
        Consumer<Object> consumer = o -> counter.incrementAndGet();

        Object result = LangUtil.applyIfNonNull(null, consumer);

        assertNull(result);
        assertEquals(0, counter.get());
    }

    // --- applyIfNotEmpty ---

    @Test
    void applyIfNotEmpty_withNonEmptyString_executesConsumerAndReturnsSame() {
        AtomicReference<CharSequence> received = new AtomicReference<>();
        String input = "abc";

        String result = LangUtil.applyIfNotEmpty(input, received::set);

        assertEquals("abc", received.get().toString());
        assertSame(input, result);
    }

    @Test
    void applyIfNotEmpty_withEmptyString_doesNotExecuteConsumerAndReturnsSame() {
        AtomicInteger counter = new AtomicInteger();
        Consumer<CharSequence> consumer = cs -> counter.incrementAndGet();

        String input = "";
        String result = LangUtil.applyIfNotEmpty(input, consumer);

        assertEquals(0, counter.get());
        assertSame(input, result);
    }

    @Test
    void applyIfNotEmpty_withNull_doesNotExecuteConsumerAndReturnsNull() {
        AtomicInteger counter = new AtomicInteger();
        Consumer<CharSequence> consumer = cs -> counter.incrementAndGet();

        CharSequence result = LangUtil.applyIfNotEmpty(null, consumer);

        assertNull(result);
        assertEquals(0, counter.get());
    }
}
