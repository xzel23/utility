package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test class for TextAttributes
 * Testing the 'of' methods in the TextAttributes class
 */
class TextAttributesTest {

    /**
     * Test the of method with Pair array as parameter
     */
    @Test
    void testOfWithPairArray() {
        @SuppressWarnings("unchecked")
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

    @Test
    void none() {
        TextAttributes ta = TextAttributes.none();
        assertNotNull(ta);
        assertTrue(ta.isEmpty());
    }

    @Test
    void equalsAndHashCode() {
        TextAttributes ta1 = TextAttributes.of(new Pair<>("key", "value"));
        TextAttributes ta2 = TextAttributes.of(new Pair<>("key", "value"));
        TextAttributes ta3 = TextAttributes.of(new Pair<>("key", "value2"));
        TextAttributes ta4 = TextAttributes.of(new Pair<>("key1", "value"));
        assertEquals(ta1.hashCode(), ta2.hashCode());
        assertEquals(ta1.hashCode(), ta1.hashCode());
        assertNotEquals(ta1.hashCode(), ta3.hashCode());
        assertNotEquals(ta1.hashCode(), ta4.hashCode());
        assertNotEquals(ta3.hashCode(), ta4.hashCode());
        assertEquals(ta1, ta2);
    }

    @ParameterizedTest
    @MethodSource("textAttributesArguments")
    void testToFontDef(TextAttributes ta) {
        FontDef fd = TextAttributes.getFontDef(ta);
        assertNotNull(fd);
        assertEquals(ta.get(Style.FONT_FAMILIES), fd.getFamilies());
        assertEquals(ta.get(Style.FONT_SIZE), fd.getSize());
        assertEquals(ta.get(Style.COLOR), fd.getColor());
        assertEquals(Optional.ofNullable(ta.get(Style.FONT_WEIGHT)).map(s -> s.equals(Style.FONT_WEIGHT_VALUE_BOLD)).orElse(null), fd.getBold());
        assertEquals(Optional.ofNullable(ta.get(Style.TEXT_DECORATION_UNDERLINE)).map(s -> s.equals(Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)).orElse(null), fd.getUnderline());
        assertEquals(Optional.ofNullable(ta.get(Style.TEXT_DECORATION_LINE_THROUGH)).map(s -> s.equals(Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)).orElse(null), fd.getStrikeThrough());
        assertEquals(Optional.ofNullable(ta.get(Style.FONT_STYLE)).map(s -> s.equals(Style.FONT_STYLE_VALUE_ITALIC)).orElse(null), fd.getItalic());
    }

    private static Stream<TextAttributes> textAttributesArguments() {
        return Stream.of(
                TextAttributes.none(),
                TextAttributes.of(Pair.of(Style.FONT_FAMILIES, List.of("Arial"))),
                TextAttributes.of(Pair.of(Style.FONT_SIZE, 17.0f)),
                TextAttributes.of(Pair.of(Style.COLOR, Color.BLUE)),
                TextAttributes.of(Pair.of(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD)),
                TextAttributes.of(Pair.of(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_NORMAL)),
                TextAttributes.of(Pair.of(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE)),
                TextAttributes.of(Pair.of(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE)),
                TextAttributes.of(Pair.of(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)),
                TextAttributes.of(Pair.of(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE)),
                TextAttributes.of(Pair.of(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC)),
                TextAttributes.of(Pair.of(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_NORMAL)),
                TextAttributes.of(Pair.of(Style.FONT_FAMILIES, List.of("Arial")),
                        Pair.of(Style.FONT_SIZE, 17.0f),
                        Pair.of(Style.COLOR, Color.BLUE),
                        Pair.of(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD),
                        Pair.of(Style.TEXT_DECORATION_UNDERLINE, Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE),
                        Pair.of(Style.TEXT_DECORATION_LINE_THROUGH, Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE),
                        Pair.of(Style.FONT_STYLE, Style.FONT_STYLE_VALUE_ITALIC)
                )
        );
    }
}
