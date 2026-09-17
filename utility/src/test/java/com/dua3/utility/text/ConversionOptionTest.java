package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversionOptionTest {

    @Test
    void testAnsiConversionOption() {
        AtomicBoolean applied = new AtomicBoolean(false);
        AnsiConversionOption option = new AnsiConversionOption(c -> applied.set(true));

        assertNotNull(option.action());
        option.apply(AnsiConverter.create());
        assertTrue(applied.get());

        AnsiConversionOption sameOption = new AnsiConversionOption(option.action());
        assertEquals(option, sameOption);
        assertEquals(option.hashCode(), sameOption.hashCode());
    }

    @Test
    void testHtmlConversionOption() {
        AtomicBoolean applied = new AtomicBoolean(false);
        HtmlConversionOption option = new HtmlConversionOption(c -> applied.set(true));

        assertNotNull(option.action());
        option.apply(HtmlConverter.create());
        assertTrue(applied.get());

        HtmlConversionOption sameOption = new HtmlConversionOption(option.action());
        assertEquals(option, sameOption);
        assertEquals(option.hashCode(), sameOption.hashCode());
    }
}
