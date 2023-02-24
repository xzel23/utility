package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("HttpUrlsUsage")
class DataUtilTest {

    @Test
    void testConvert() throws IOException {
        // Object to String
        assertEquals("123", DataUtil.convert(123, String.class));

        // String to Number
        assertEquals(123, DataUtil.convert("123", Integer.class));
        assertEquals(Integer.class, DataUtil.convert("123", Integer.class).getClass());
        assertEquals(123.0, DataUtil.convert("123", Double.class));
        assertEquals(Double.class, DataUtil.convert("123", Double.class).getClass());
        assertEquals(-0.5f, DataUtil.convert("-0.5", Float.class));
        assertEquals(Float.class, DataUtil.convert("-0.5", Float.class).getClass());
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("", Integer.class));
        assertNull(DataUtil.convert((Object) null, Integer.class));

        // Number to Number
        assertEquals(123, DataUtil.convert(123.0, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("2147483648", Integer.class));
        assertEquals(2147483648L, DataUtil.convert("2147483648", Long.class));
        assertEquals(-2147483648, DataUtil.convert("-2147483648", Integer.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("-2147483649", Integer.class));
        assertEquals(-2147483649L, DataUtil.convert("-2147483649", Long.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert(123.5, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert(123.5, Long.class));
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

        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("yes", Boolean.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("no", Boolean.class));
        assertThrows(IllegalArgumentException.class, () -> DataUtil.convert("", Boolean.class));

        // String to LocalDate
        assertEquals(LocalDate.of(2019, 6, 30), DataUtil.convert("2019-06-30", LocalDate.class));

        // String to LocalDateTime
        assertEquals(LocalDateTime.of(2019, 6, 30, 14, 53), DataUtil.convert("2019-06-30T14:53", LocalDateTime.class));

        // String, URL, Path, File to URI
        assertEquals(URI.create("http://www.dua3.com"), DataUtil.convert("http://www.dua3.com", URI.class));
        assertEquals(URI.create("http://www.dua3.com"), DataUtil.convert(new URL("http://www.dua3.com"), URI.class));
        assertEquals(Paths.get(".").toUri(), DataUtil.convert(Paths.get("."), URI.class));
        assertEquals(new File(".").toURI(), DataUtil.convert(new File("."), URI.class));

        // String, URI, Path, File to URL
        assertEquals(new URL("http://www.dua3.com"), DataUtil.convert("http://www.dua3.com", URL.class));
        assertEquals(new URL("http://www.dua3.com"), DataUtil.convert(URI.create("http://www.dua3.com"), URL.class));
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
    }

    @Test
    void testConvertToArray() {
        assertArrayEquals(new Integer[]{5, -7, 13}, DataUtil.convertToArray(List.of("5", "-7", "13"), Integer.class));
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
    void testCollectArray() {
        assertArrayEquals(new Integer[]{1, 2, 3}, DataUtil.collectArray(List.of(1, 2, 3).iterator()));
    }
}
