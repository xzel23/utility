package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class RichTextConverterTest {

    @Test
    void putFontPropertiesCopiesFontAttributes() {
        FontData fontData = FontData.get(
                "Courier New", 12.0f, true, true, true, true, true,
                10.0, 2.0, 12.0, 5.0
        );
        Font font = Font.getFont(fontData, Color.RED, Color.BLUE);
        Map<String, Object> properties = new HashMap<>();

        RichTextConverter.putFontProperties(properties, font);

        assertEquals(List.of("Courier New"), properties.get(Style.FONT_FAMILIES));
        assertEquals("monospace", properties.get(Style.FONT_CLASS));
        assertEquals(12.0f, properties.get(Style.FONT_SIZE));
        assertEquals(Color.RED, properties.get(Style.COLOR));
        assertEquals(Color.BLUE, properties.get(Style.BACKGROUND_COLOR));
        assertEquals(Style.FONT_STYLE_VALUE_ITALIC, properties.get(Style.FONT_STYLE));
        assertEquals(Style.FONT_WEIGHT_VALUE_BOLD, properties.get(Style.FONT_WEIGHT));
        assertEquals(Style.TEXT_DECORATION_UNDERLINE_VALUE_LINE, properties.get(Style.TEXT_DECORATION_UNDERLINE));
        assertEquals(Style.TEXT_DECORATION_LINE_THROUGH_VALUE_LINE, properties.get(Style.TEXT_DECORATION_LINE_THROUGH));
    }

    @Test
    void putFontPropertiesRemovesTransparentBackground() {
        FontData fontData = FontData.get(
                "Arial", 12.0f, false, false, false, false, false,
                10.0, 2.0, 12.0, 5.0
        );
        Font font = Font.getFont(fontData, Color.BLACK);
        Map<String, Object> properties = new HashMap<>();
        properties.put(Style.BACKGROUND_COLOR, Color.YELLOW);

        RichTextConverter.putFontProperties(properties, font);

        assertFalse(properties.containsKey(Style.BACKGROUND_COLOR));
        assertNull(properties.get(Style.FONT_CLASS));
        assertEquals(Style.FONT_STYLE_VALUE_NORMAL, properties.get(Style.FONT_STYLE));
        assertEquals(Style.FONT_WEIGHT_VALUE_NORMAL, properties.get(Style.FONT_WEIGHT));
        assertEquals(Style.TEXT_DECORATION_UNDERLINE_VALUE_NO_LINE, properties.get(Style.TEXT_DECORATION_UNDERLINE));
        assertEquals(Style.TEXT_DECORATION_LINE_THROUGH_VALUE_NO_LINE, properties.get(Style.TEXT_DECORATION_LINE_THROUGH));
    }

    @ParameterizedTest
    @MethodSource("fontClassArguments")
    void putFontPropertiesDeterminesFontClass(String family, boolean monospaced, String expectedClass) {
        FontData fontData = FontData.get(
                family, 12.0f, monospaced, false, false, false, false,
                10.0, 2.0, 12.0, 5.0
        );
        Map<String, Object> properties = new HashMap<>();

        RichTextConverter.putFontProperties(properties, Font.getFont(fontData, Color.BLACK));

        if (expectedClass == null) {
            assertFalse(properties.containsKey(Style.FONT_CLASS));
        } else {
            assertEquals(expectedClass, properties.get(Style.FONT_CLASS));
        }
    }

    private static Stream<Arguments> fontClassArguments() {
        return Stream.of(
                Arguments.of("Courier New", true, Style.FONT_CLASS_VALUE_MONOSPACE),
                Arguments.of("serif", false, Style.FONT_CLASS_VALUE_SERIF),
                Arguments.of("sans-serif", false, Style.FONT_CLASS_VALUE_SANS_SERIF)
        );
    }
}
