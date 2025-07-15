package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link SharedString}.
 */
class SharedStringTest {

    @Test
    void testConstructor() {
        String base = "Hello, World!";
        int start = 0;
        int end = 5;

        SharedString sharedString = new SharableString(base).subSequence(start, end);

        assertEquals("Hello", sharedString.toString());
        assertEquals(5, sharedString.length());
    }

    @Test
    void testLength() {
        String base = "Hello, World!";
        int start = 7;
        int end = 12;

        SharedString sharedString = new SharableString(base).subSequence(start, end);

        assertEquals(5, sharedString.length());
    }

    @Test
    void testCharAt() {
        String base = "Hello, World!";
        int start = 0;
        int end = 5;

        SharedString sharedString = new SharableString(base).subSequence(start, end);

        assertEquals('H', sharedString.charAt(0));
        assertEquals('e', sharedString.charAt(1));
        assertEquals('l', sharedString.charAt(2));
        assertEquals('l', sharedString.charAt(3));
        assertEquals('o', sharedString.charAt(4));
    }

    @Test
    void testCharAtOutOfBounds() {
        String base = "Hello, World!";
        int start = 0;
        int end = 5;

        SharedString sharedString = new SharableString(base).subSequence(start, end);

        assertThrows(IndexOutOfBoundsException.class, () -> sharedString.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> sharedString.charAt(5));
    }

    @Test
    void testSubSequence() {
        String base = "Hello, World!";

        SharedString sharedString = new SharableString(base).subSequence(0, 13);
        SharedString subSequence = sharedString.subSequence(7, 12);

        assertEquals("World", subSequence.toString());
        assertEquals(5, subSequence.length());
    }

    @Test
    void testSubSequenceInvalidRange() {
        String base = "Hello, World!";

        SharedString sharedString = new SharableString(base).subSequence(0, 13);

        assertThrows(IndexOutOfBoundsException.class, () -> sharedString.subSequence(5, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> sharedString.subSequence(-1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> sharedString.subSequence(0, 14));
    }

    @Test
    void testToString() {
        String base = "Hello, World!";
        int start = 7;
        int end = 12;

        SharedString sharedString = new SharableString(base).subSequence(start, end);

        assertEquals("World", sharedString.toString());
    }

    @Test
    void testHashCode() {
        String base1 = "Hello, World!";
        String base2 = "Hello, Universe!";

        SharedString sharedString1 = new SharableString(base1).subSequence(0, 5);
        SharedString sharedString2 = new SharableString(base2).subSequence(0, 5);

        assertEquals(sharedString1.hashCode(), sharedString2.hashCode());
    }

    @Test
    void testEquals() {
        String base1 = "Hello, World!";
        String base2 = "Hello, Universe!";

        SharedString sharedString1 = new SharableString(base1).subSequence(0, 5);
        SharedString sharedString2 = new SharableString(base2).subSequence(0, 5);
        SharedString sharedString3 = new SharableString(base1).subSequence(0, 4);

        assertEquals(sharedString1, sharedString1); // Same instance
        assertEquals(sharedString1, sharedString2); // Different instances, same content
        assertNotEquals(sharedString1, sharedString3); // Different content
        assertNotEquals(null, sharedString1); // Null comparison
        assertNotEquals("Hello", sharedString1); // Different type
    }

    @Test
    void testNestedSubSequence() {
        String base = "Hello, World!";

        SharedString sharedString = new SharableString(base).subSequence(0, 13);
        SharedString level1 = sharedString.subSequence(0, 5);
        SharedString level2 = level1.subSequence(1, 4);

        assertEquals("Hello", level1.toString());
        assertEquals("ell", level2.toString());
    }
}
