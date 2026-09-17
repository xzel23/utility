package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributedCharSequenceTest {

    @Test
    void testAttributedCharsStream() {
        RichText text = RichText.valueOf("Hello");
        List<AttributedCharacter> chars = text.attributedChars().toList();

        assertEquals(5, chars.size());
        assertEquals('H', chars.get(0).character());
        assertEquals('o', chars.get(4).character());
    }

    @Test
    void testAttributedCharIteratorThrowsWhenExhausted() {
        RichText text = RichText.valueOf("A");
        Iterator<AttributedCharacter> it = text.attributedChars().iterator();

        assertTrue(it.hasNext());
        assertEquals('A', it.next().character());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }
}
