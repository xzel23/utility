package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.DataUtil;
import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for TextAttributes
 * Testing the 'of' methods in the TextAttributes class
 */
public class TextAttributesTest {

    /**
     * Test the of method with Pair array as parameter
     */
    @Test
    void testOfWithPairArray() {
        Pair<String, ?>[] entries = new Pair[]{
                Pair.of("Key1", "Value1"),
                Pair.of("Key2", "Value2"),
        };

        TextAttributes textAttributes = TextAttributes.of(entries);
        assertNotNull(textAttributes);
        assertFalse(textAttributes.isEmpty());
        assertEquals(2, textAttributes.size());
        assertEquals("Value1", textAttributes.get("Key1"));
        assertEquals("Value2", textAttributes.get("Key2"));
    }

    /**
     * Test the of method with iterable as argument
     */
    @Test
    void testOfWithIterable() {
        List<Pair<String, ?>> entries = new ArrayList<>();
        entries.add(Pair.of("Key1", "Value1"));
        entries.add(Pair.of("Key2", "Value2"));

        TextAttributes textAttributes = TextAttributes.of(entries);
        assertNotNull(textAttributes);
        assertFalse(textAttributes.isEmpty());
        assertEquals(2, textAttributes.size());
        assertEquals("Value1", textAttributes.get("Key1"));
        assertEquals("Value2", textAttributes.get("Key2"));
    }

    /**
     * Test the of method with map as argument
     */
    @Test
    void testOfWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Key1", "Value1");
        map.put("Key2", "Value2");

        TextAttributes textAttributes = TextAttributes.of(map);
        assertNotNull(textAttributes);
        assertFalse(textAttributes.isEmpty());
        assertEquals(2, textAttributes.size());
        assertEquals("Value1", textAttributes.get("Key1"));
        assertEquals("Value2", textAttributes.get("Key2"));
    }
}