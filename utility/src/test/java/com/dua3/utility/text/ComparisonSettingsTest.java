package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComparisonSettingsTest {

    @Test
    void testDefaultSettings() {
        ComparisonSettings settings = ComparisonSettings.defaultSettings();
        assertNotNull(settings);
        assertFalse(settings.ignoreCase());
        assertFalse(settings.ignoreFontFamily());
        assertFalse(settings.ignoreFontSize());
        assertFalse(settings.ignoreTextColor());
        assertFalse(settings.ignoreUnderline());
        assertFalse(settings.ignoreStrikeThrough());
        assertFalse(settings.ignoreFontWeight());
        assertFalse(settings.ignoreItalic());
        assertEquals("Arial", settings.fontMapper().apply("Arial"));
    }

    @Test
    void testBuilderCustomSettings() {
        Function<String, String> mapper = name -> name == null ? null : name.toUpperCase();
        ComparisonSettings settings = ComparisonSettings.builder()
                .setFontMapper(mapper)
                .setIgnoreCase(true)
                .setIgnoreFontFamily(true)
                .setIgnoreFontSize(true)
                .setIgnoreTextColor(true)
                .setIgnoreUnderline(true)
                .setIgnoreStrikeThrough(true)
                .setIgnoreBold(true)
                .setIgnoreItalic(true)
                .build();

        assertEquals("ARIAL", settings.fontMapper().apply("arial"));
        assertTrue(settings.ignoreCase());
        assertTrue(settings.ignoreFontFamily());
        assertTrue(settings.ignoreFontSize());
        assertTrue(settings.ignoreTextColor());
        assertTrue(settings.ignoreUnderline());
        assertTrue(settings.ignoreStrikeThrough());
        assertTrue(settings.ignoreFontWeight());
        assertTrue(settings.ignoreItalic());
    }
}
