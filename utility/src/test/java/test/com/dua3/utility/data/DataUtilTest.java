package test.com.dua3.utility.data;

import com.dua3.utility.data.DataUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataUtilTest {

    @Test
    void testConvert() {
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
        assertEquals(LocalDate.of(2019,6,30), DataUtil.convert("2019-06-30", LocalDate.class));

        // String to LocalDateTime
        assertEquals(LocalDateTime.of(2019,6,30,14, 53), DataUtil.convert("2019-06-30T14:53", LocalDateTime.class));
    }

    @Test
    void testConvertToArray() {
        assertArrayEquals(new Integer[]{5,-7,13}, DataUtil.convertToArray(Arrays.asList("5", "-7", "13"), Integer.class));
    }

    @Test
    void convertCollection() {
        assertEquals(Arrays.asList(5,-7,13), DataUtil.convertCollection(Arrays.asList("5", "-7", "13"), Integer.class, ArrayList::new));
        assertEquals(ArrayList.class, DataUtil.convertCollection(Arrays.asList("5", "-7", "13"), Integer.class, ArrayList::new).getClass());
        assertEquals(new HashSet<>(Arrays.asList(5,-7,13)), DataUtil.convertCollection(Arrays.asList("5", "-7", "13"), Integer.class, HashSet::new));
        assertEquals(HashSet.class, DataUtil.convertCollection(Arrays.asList("5", "-7", "13"), Integer.class, HashSet::new).getClass());
    }

    @Test
    void testCollect() {
        assertEquals(Arrays.asList(1,2,3), DataUtil.collect(Arrays.asList(1,2,3).iterator()));
    }

    @Test
    void testCollectArray() {
        assertArrayEquals(new Integer[]{1,2,3}, DataUtil.collectArray(Arrays.asList(1,2,3).iterator()));
    }
}
