package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RichTextConverterExtTest {

    private final RichTextConverterExt<String> converter = new RichTextConverterExt<>() {
        @Override
        public RichText toRichText(String value) {
            return RichText.valueOf("rich:" + value);
        }

        @Override
        public String fromRichText(ToRichText text) {
            return "plain:" + text.toRichText();
        }
    };

    @Test
    void defaultConversionMethodsDelegateToConcreteMethods() {
        ToRichText richText = RichText.valueOf("hello");

        assertEquals("plain:hello", converter.convert(richText));
        assertEquals("plain:hello", converter.a2b().apply(richText));
        assertEquals("rich:world", converter.convertBack("world").toString());
        assertEquals("rich:world", converter.b2a().apply("world").toString());
    }
}
