package com.dua3.utility.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MagicTest {

    @Test
    void testMagicNumberOfStringPattern() {
        Magic.MagicNumber pdfMagic = Magic.MagicNumber.of("application/pdf", "%PDF");
        assertEquals("application/pdf", pdfMagic.mimeType());
        // '%PDF' bytes: 0x25 0x50 0x44 0x46 in big endian
        long pdfLong = 0x2550444600000000L;
        assertTrue(pdfMagic.matches(pdfLong));
        assertFalse(pdfMagic.matches(0x1234567800000000L));

        // Pattern > 8 characters throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> Magic.MagicNumber.of("text/plain", "123456789"));

        // Pattern with non-ASCII characters that take multiple bytes throws IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> Magic.MagicNumber.of("text/plain", "ü"));
    }

    @Test
    void testMagicNumberOfNumeric() {
        Magic.MagicNumber custom = Magic.MagicNumber.of("custom/type", 0xCAFEBABE00000000L, 0xFFFFFFFF00000000L);
        assertEquals("custom/type", custom.mimeType());
        assertTrue(custom.matches(0xCAFEBABE12345678L));
        assertFalse(custom.matches(0xDEADBEEF12345678L));
    }

    @Test
    void testMagicGetMimeType() {
        Magic.MagicNumber png = Magic.MagicNumber.of("image/png", 0x89504E4700000000L, 0xFFFFFFFF00000000L);
        Magic.MagicNumber jpeg = Magic.MagicNumber.of("image/jpeg", 0xFFD8FF0000000000L, 0xFFFFFF0000000000L);

        Magic magic = new Magic(List.of(png, jpeg));

        assertEquals("image/png", magic.getMimeType(0x89504E4712345678L));
        assertEquals("image/jpeg", magic.getMimeType(0xFFD8FFE000000000L));
        assertEquals(Magic.UNKNOWN_MIME_TYPE, magic.getMimeType(0x0000000000000000L));
    }
}
