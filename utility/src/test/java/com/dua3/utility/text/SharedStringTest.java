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
    void testSubSequenceEdgeCases() {
        String base = "Hello, World!";
        SharedString sharedString = new SharableString(base).subSequence(0, 13);

        // Test empty subsequence
        SharedString emptySubSequence = sharedString.subSequence(5, 5);
        assertEquals("", emptySubSequence.toString());
        assertEquals(0, emptySubSequence.length());

        // Test subsequence at start
        SharedString startSubSequence = sharedString.subSequence(0, 5);
        assertEquals("Hello", startSubSequence.toString());

        // Test subsequence at end
        SharedString endSubSequence = sharedString.subSequence(7, 13);
        assertEquals("World!", endSubSequence.toString());

        // Test single character subsequence
        SharedString singleCharSubSequence = sharedString.subSequence(0, 1);
        assertEquals("H", singleCharSubSequence.toString());
        assertEquals(1, singleCharSubSequence.length());
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
    void testHashCodeCalculation() {
        String base = "Hello, World!";
        SharedString sharedString = new SharableString(base).subSequence(0, 5);

        // First call calculates the hash
        int hash1 = sharedString.hashCode();

        // Second call should return the cached hash
        int hash2 = sharedString.hashCode();

        assertEquals(hash1, hash2);

        // Test with empty string (edge case)
        SharedString emptyString = new SharableString(base).subSequence(0, 0);
        assertEquals(0, emptyString.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        String base = "Hello, World!";

        SharedString sharedString1 = new SharableString(base).subSequence(0, 5);
        SharedString sharedString2 = new SharableString(base).subSequence(0, 5);
        SharedString sharedString3 = new SharableString(base).subSequence(0, 4);

        // Test hashCode consistency with equals
        assertEquals(sharedString1.equals(sharedString2), sharedString1.hashCode() == sharedString2.hashCode());
        assertEquals(sharedString1.equals(sharedString3), sharedString1.hashCode() == sharedString3.hashCode());
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
    void testEqualsCharacterByCharacter() {
        // Test the character-by-character comparison in equals method
        String base1 = "Hello, World!";
        String base2 = "Jello, World!"; // Different first character

        SharedString sharedString1 = new SharableString(base1).subSequence(0, 5);
        SharedString sharedString2 = new SharableString(base2).subSequence(0, 5);

        assertNotEquals(sharedString1, sharedString2);

        // Test with strings that have same length but different characters
        String base3 = "Hello";
        String base4 = "World";

        SharedString sharedString3 = new SharableString(base3).subSequence(0, 5);
        SharedString sharedString4 = new SharableString(base4).subSequence(0, 5);

        assertNotEquals(sharedString3, sharedString4);
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
