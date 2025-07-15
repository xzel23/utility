package com.dua3.utility.text;

import com.dua3.utility.data.Pair;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link AttributedCharacter}.
 */
class AttributedCharacterTest {

    @Test
    void testCreate() {
        char testChar = 'A';
        TextAttributes attributes = TextAttributes.none();

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        assertEquals(testChar, ac.character());
        assertEquals(attributes, ac.attributes());
    }

    @Test
    void testCharacter() {
        char testChar = 'Z';
        TextAttributes attributes = TextAttributes.none();

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        assertEquals(testChar, ac.character());
    }

    @Test
    void testAttributes() {
        char testChar = 'B';
        TextAttributes attributes = TextAttributes.of(Pair.of("key1", "value1"), Pair.of("key2", "value2"));

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        assertEquals(attributes, ac.attributes());
        assertEquals("value1", ac.attributes().get("key1"));
        assertEquals("value2", ac.attributes().get("key2"));
    }

    @Test
    void testGetStyles() {
        char testChar = 'C';
        Style style1 = Style.create("style1", Map.of());
        Style style2 = Style.create("style2", Map.of());
        List<Style> styles = List.of(style1, style2);

        TextAttributes attributes = TextAttributes.of(Pair.of(RichText.ATTRIBUTE_NAME_STYLE_LIST, styles));

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        List<Style> retrievedStyles = ac.getStyles();
        assertEquals(2, retrievedStyles.size());
        assertTrue(retrievedStyles.contains(style1));
        assertTrue(retrievedStyles.contains(style2));
    }

    @Test
    void testGetStylesEmpty() {
        char testChar = 'D';
        TextAttributes attributes = TextAttributes.none();

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        List<Style> retrievedStyles = ac.getStyles();
        assertTrue(retrievedStyles.isEmpty());
    }

    @Test
    void testGetStylesWithEmptyList() {
        char testChar = 'E';
        TextAttributes attributes = TextAttributes.of(Pair.of(RichText.ATTRIBUTE_NAME_STYLE_LIST, Collections.emptyList()));

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        List<Style> retrievedStyles = ac.getStyles();
        assertTrue(retrievedStyles.isEmpty());
    }

    @Test
    void testImplementation() {
        char testChar = 'F';
        TextAttributes attributes = TextAttributes.of(Pair.of("testKey", "testValue"));

        AttributedCharacter ac = AttributedCharacter.create(testChar, attributes);

        // Test the implementation details
        assertEquals(testChar, ac.character());
        assertEquals(attributes, ac.attributes());
        assertEquals("testValue", ac.attributes().get("testKey"));
    }
}
