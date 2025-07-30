package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("HttpUrlsUsage")
class DataUtilTest {

    @Test
    void testIsSortedNaturalOrder() {
        assertTrue(DataUtil.isSorted(List.of(1, 2, 3, 4, 5))); // Sorted in ascending order
        assertFalse(DataUtil.isSorted(List.of(1, 3, 2, 5, 4))); // Not sorted
    }

    @Test
    void testIsSortedWithComparator() {
        // Custom comparator (descending order)
        assertTrue(DataUtil.isSorted(List.of(5, 4, 3, 2, 1), Comparator.reverseOrder()));
        assertFalse(DataUtil.isSorted(List.of(5, 1, 4, 2, 3), Comparator.reverseOrder()));
    }

    @Test
    void testIsSortedEmptyOrSingleElement() {
        assertTrue(DataUtil.isSorted(List.<Integer>of())); // Empty collection
        assertTrue(DataUtil.isSorted(List.of(42))); // Single element
    }

    @Test
    void testIsSortedEdgeCases() {
        assertTrue(DataUtil.isSorted(List.of(1, 1, 1, 1))); // All elements equal
        assertFalse(DataUtil.isSorted(List.of(2, 1, 1, 2))); // Multiple occurrences unordered
    }

    @Test
    void testConvert() throws IOException, URISyntaxException {
        // Object to String
        assertEquals("123", DataUtil.convert(123, String.class));

        // String to Number
        assertEquals(123, DataUtil.convert("123", Integer.class));
        assertEquals(Integer.class, DataUtil.convert("123", Integer.class).getClass());
        assertEquals(123.0, DataUtil.convert("123", Double.class));
        assertEquals(Double.class, DataUtil.convert("123", Double.class).getClass());
        assertEquals(-0.5f, DataUtil.convert("-0.5", Float.class));
        assertEquals(Float.class, DataUtil.convert("-0.5", Float.class).getClass());
        assertThrows(ConversionException.class, () -> DataUtil.convert("", Integer.class));
        assertNull(DataUtil.convert((Object) null, Integer.class));

        // Number to Number
        assertEquals(123, DataUtil.convert(123.0, Integer.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert("2147483648", Integer.class));
        assertEquals(2147483648L, DataUtil.convert("2147483648", Long.class));
        assertEquals(-2147483648, DataUtil.convert("-2147483648", Integer.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert("-2147483649", Integer.class));
        assertEquals(-2147483649L, DataUtil.convert("-2147483649", Long.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert(123.5, Long.class));
        assertEquals(Integer.class, DataUtil.convert(123.0, Integer.class).getClass());
        assertEquals(123.0, DataUtil.convert(123, Double.class));
        assertEquals(Double.class, DataUtil.convert(123, Double.class).getClass());
        assertEquals(-0.5f, DataUtil.convert(-0.5, Float.class));
        assertEquals(Float.class, DataUtil.convert(-0.5, Float.class).getClass());

        // String to Boolean
        assertEquals(true, DataUtil.convert("true", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("true", Boolean.class).getClass());
        assertEquals(true, DataUtil.convert("TRUE", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("TRUE", Boolean.class).getClass());
        assertEquals(true, DataUtil.convert("True", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("True", Boolean.class).getClass());

        assertEquals(false, DataUtil.convert("false", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("false", Boolean.class).getClass());
        assertEquals(false, DataUtil.convert("FALSE", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("FALSE", Boolean.class).getClass());
        assertEquals(false, DataUtil.convert("False", Boolean.class));
        assertEquals(Boolean.class, DataUtil.convert("False", Boolean.class).getClass());

        assertThrows(ConversionException.class, () -> DataUtil.convert("yes", Boolean.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert("no", Boolean.class));
        assertThrows(ConversionException.class, () -> DataUtil.convert("", Boolean.class));

        // String to LocalDate
        assertEquals(LocalDate.of(2019, 6, 30), DataUtil.convert("2019-06-30", LocalDate.class));

        // String to LocalDateTime
        assertEquals(LocalDateTime.of(2019, 6, 30, 14, 53), DataUtil.convert("2019-06-30T14:53", LocalDateTime.class));

        // String, URL, Path, File to URI
        assertEquals(URI.create("http://www.dua3.com"), DataUtil.convert("http://www.dua3.com", URI.class));
        assertEquals(URI.create("http://www.dua3.com"), DataUtil.convert(new URI("http://www.dua3.com").toURL(), URI.class));
        assertEquals(Paths.get(".").toUri(), DataUtil.convert(Paths.get("."), URI.class));
        assertEquals(new File(".").toURI(), DataUtil.convert(new File("."), URI.class));

        // String, URI, Path, File to URL
        assertEquals(new URI("http://www.dua3.com").toURL(), DataUtil.convert("http://www.dua3.com", URL.class));
        assertEquals(new URI("http://www.dua3.com").toURL(), DataUtil.convert(URI.create("http://www.dua3.com"), URL.class));
        assertEquals(Paths.get(".").toUri().toURL(), DataUtil.convert(Paths.get("."), URL.class));
        assertEquals(new File(".").toURI().toURL(), DataUtil.convert(new File("."), URL.class));

        // String, URI, URL, File to Path
        Path path = Paths.get(".").toAbsolutePath().normalize();
        assertEquals(path, DataUtil.convert(".", Path.class).toAbsolutePath().normalize());
        assertEquals(path, DataUtil.convert(path.toFile(), Path.class).normalize());
        assertEquals(path, DataUtil.convert(path.toUri(), Path.class).normalize());
        assertEquals(path, DataUtil.convert(path.toUri().toURL(), Path.class).normalize());

        // String, URI, URL, Path to File
        File file = path.toFile();
        assertEquals(file, DataUtil.convert(".", File.class).getCanonicalFile());
        assertEquals(file, DataUtil.convert(file.toPath(), File.class).getAbsoluteFile());
        assertEquals(file, DataUtil.convert(path.toUri(), File.class).getAbsoluteFile());
        assertEquals(file, DataUtil.convert(path.toUri().toURL(), File.class).getAbsoluteFile());

        // use BigDecimal to test conversion using valueOf() and constructor
        // these use BigDecimal constructors
        assertEquals(BigDecimal.valueOf(123), DataUtil.convert(123, BigDecimal.class, true));
        assertEquals(BigDecimal.valueOf(123), DataUtil.convert(Integer.valueOf(123), BigDecimal.class, true));
        assertEquals(BigDecimal.valueOf(123), DataUtil.convert("123", BigDecimal.class, true));
        // these use BigDecimal.valueOf()
        assertEquals(BigDecimal.valueOf(123), DataUtil.convert(123L, BigDecimal.class, true));
        assertEquals(BigDecimal.valueOf(123), DataUtil.convert(Long.valueOf(123), BigDecimal.class, true));
    }

    @Test
    void testConvertToArray() {
        assertArrayEquals(new Integer[]{5, -7, 13}, DataUtil.convertToArray(List.of("5", "-7", "13"), Integer.class));
    }

    @Test
    void testConvertArrayTypes() {
        assertArrayEquals(new Integer[]{5, -7, 13}, DataUtil.convert(new Number[]{5, -7, 13}, Integer[].class));
        assertArrayEquals(new Number[]{5, -7, 13}, DataUtil.convert(new Integer[]{5, -7, 13}, Number[].class));
    }

    @Test
    void convertCollection() {
        assertEquals(List.of(5, -7, 13), DataUtil.convertCollection(List.of("5", "-7", "13"), Integer.class, ArrayList::new));
        assertEquals(ArrayList.class, DataUtil.convertCollection(List.of("5", "-7", "13"), Integer.class, ArrayList::new).getClass());
        assertEquals(new HashSet<>(List.of(5, -7, 13)), DataUtil.convertCollection(List.of("5", "-7", "13"), Integer.class, HashSet::new));
        assertEquals(HashSet.class, DataUtil.convertCollection(List.of("5", "-7", "13"), Integer.class, HashSet::new).getClass());
    }

    @Test
    void testCollect() {
        assertEquals(List.of(1, 2, 3), DataUtil.collect(List.of(1, 2, 3).iterator()));
    }

    @Test
    void testCollectArray_Iterable() {
        assertArrayEquals(new Integer[]{1, 2, 3}, DataUtil.collectArray(List.of(1, 2, 3)));
    }

    @Test
    void testCollectArray_Iterator() {
        assertArrayEquals(new Integer[]{1, 2, 3}, DataUtil.collectArray(List.of(1, 2, 3).iterator()));
    }

    @Test
    void testFilter() {
        // Create a list and get its iterator
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Iterator<Integer> iterator = numbers.iterator();

        // Create a predicate for even numbers
        Predicate<Integer> isEven = n -> n % 2 == 0;

        // Filter the iterator
        Iterator<Integer> filteredIterator = DataUtil.filter(iterator, isEven);

        // Collect the filtered items
        List<Integer> filteredNumbers = new ArrayList<>();
        filteredIterator.forEachRemaining(filteredNumbers::add);

        // Verify the filtered list contains only even numbers
        assertEquals(List.of(2, 4, 6, 8, 10), filteredNumbers);
    }

    @Test
    void testMap() {
        // Create a list and get its iterator
        List<String> strings = List.of("1", "2", "3", "4", "5");
        Iterator<String> iterator = strings.iterator();

        // Create a mapping function to convert strings to integers
        Function<String, Integer> stringToInt = Integer::valueOf;

        // Map the iterator
        Iterator<Integer> mappedIterator = DataUtil.map(iterator, stringToInt);

        // Collect the mapped items
        List<Integer> mappedNumbers = new ArrayList<>();
        mappedIterator.forEachRemaining(mappedNumbers::add);

        // Verify the mapped list contains the correct integers
        assertEquals(List.of(1, 2, 3, 4, 5), mappedNumbers);
    }

    @Test
    void testAsFunction() {
        // Create a map
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        // Create a default value
        Integer defaultValue = -1;

        // Convert the map to a function
        Function<String, Integer> function = DataUtil.asFunction(map, defaultValue);

        // Test the function with keys in the map
        assertEquals(1, function.apply("one"));
        assertEquals(2, function.apply("two"));
        assertEquals(3, function.apply("three"));

        // Test the function with a key not in the map
        assertEquals(defaultValue, function.apply("four"));
    }

    @Test
    void testChanges() {
        // Create two maps
        Map<String, Integer> mapA = new HashMap<>();
        mapA.put("one", 1);
        mapA.put("two", 2);
        mapA.put("three", 3);

        Map<String, Integer> mapB = new HashMap<>();
        mapB.put("one", 1);  // Same as in mapA
        mapB.put("two", 20); // Different from mapA
        mapB.put("four", 4); // Not in mapA

        // Compute changes
        Map<String, Pair<Integer, Integer>> changes = DataUtil.changes(mapA, mapB);

        // Verify changes
        assertEquals(3, changes.size());

        // "one" should not be in changes as it's the same in both maps
        assertFalse(changes.containsKey("one"));

        // "two" should have changed from 2 to 20
        assertTrue(changes.containsKey("two"));
        assertEquals(Pair.of(2, 20), changes.get("two"));

        // "three" should have changed from 3 to null
        assertTrue(changes.containsKey("three"));
        assertEquals(Pair.of(3, null), changes.get("three"));

        // "four" should have changed from null to 4
        assertTrue(changes.containsKey("four"));
        assertEquals(Pair.of(null, 4), changes.get("four"));
    }

    @Test
    void testDiff() {
        // Create two maps
        Map<String, Integer> mapA = new HashMap<>();
        mapA.put("one", 1);
        mapA.put("two", 2);
        mapA.put("three", 3);

        Map<String, Integer> mapB = new HashMap<>();
        mapB.put("one", 1);  // Same as in mapA
        mapB.put("two", 20); // Different from mapA
        mapB.put("four", 4); // Not in mapA

        // Compute diff
        Map<String, Integer> diff = DataUtil.diff(mapA, mapB);

        // Verify diff
        assertEquals(3, diff.size());

        // "one" should not be in diff as it's the same in both maps
        assertFalse(diff.containsKey("one"));

        // "two" should have the value from mapB
        assertTrue(diff.containsKey("two"));
        assertEquals(Integer.valueOf(20), diff.get("two"));

        // "three" should have null as it's not in mapB
        assertTrue(diff.containsKey("three"));
        assertNull(diff.get("three"));

        // "four" should have the value from mapB
        assertTrue(diff.containsKey("four"));
        assertEquals(Integer.valueOf(4), diff.get("four"));
    }

    @Test
    void testDiffWithMapFactory() {
        // Create two maps
        Map<String, Integer> mapA = new HashMap<>();
        mapA.put("one", 1);
        mapA.put("two", 2);

        Map<String, Integer> mapB = new HashMap<>();
        mapB.put("one", 1);
        mapB.put("two", 20);

        // Compute diff with TreeMap factory
        Map<String, Integer> diff = DataUtil.diff(mapA, mapB, TreeMap::new);

        // Verify diff is a TreeMap
        assertTrue(diff instanceof TreeMap);

        // Verify diff content
        assertEquals(1, diff.size());
        assertEquals(Integer.valueOf(20), diff.get("two"));
    }

    @Test
    void testIfPresent() {
        // Create a map
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("null", null);

        // Test ifPresent with key in map
        AtomicBoolean called = new AtomicBoolean(false);
        Consumer<Integer> action = value -> called.set(true);

        DataUtil.ifPresent(map, "one", action);
        assertTrue(called.get());

        // Test ifPresent with key not in map
        called.set(false);
        DataUtil.ifPresent(map, "three", action);
        assertFalse(called.get());

        // Test ifPresent with key mapped to null
        called.set(false);
        DataUtil.ifPresent(map, "null", action);
        assertTrue(called.get());
    }

    @Test
    void testIfMapped() {
        // Create a map
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("null", null);

        // Test ifMapped with key mapped to non-null value
        AtomicBoolean called = new AtomicBoolean(false);
        Consumer<Integer> action = value -> called.set(true);

        boolean result = DataUtil.ifMapped(map, "one", action);
        assertTrue(result);
        assertTrue(called.get());

        // Test ifMapped with key not in map
        called.set(false);
        result = DataUtil.ifMapped(map, "three", action);
        assertFalse(result);
        assertFalse(called.get());

        // Test ifMapped with key mapped to null
        called.set(false);
        result = DataUtil.ifMapped(map, "null", action);
        assertFalse(result);
        assertFalse(called.get());
    }

    @Test
    void testConversionException() {
        // Test ConversionException constructor with cause
        Exception cause = new Exception("Test cause");
        ConversionException exception1 = new ConversionException(String.class, Integer.class, cause);

        assertTrue(exception1.getMessage().contains(String.class.getName()));
        assertTrue(exception1.getMessage().contains(Integer.class.getName()));
        assertEquals(cause, exception1.getCause());

        // Test ConversionException constructor with message
        String message = "Test message";
        ConversionException exception2 = new ConversionException(String.class, Integer.class, message);

        assertTrue(exception2.getMessage().contains(message));
        assertTrue(exception2.getMessage().contains(String.class.getName()));
        assertTrue(exception2.getMessage().contains(Integer.class.getName()));

        // Test ConversionException constructor with message and cause
        ConversionException exception3 = new ConversionException(String.class, Integer.class, message, cause);

        assertTrue(exception3.getMessage().contains(message));
        assertTrue(exception3.getMessage().contains(String.class.getName()));
        assertTrue(exception3.getMessage().contains(Integer.class.getName()));
        assertEquals(cause, exception3.getCause());
    }
}
