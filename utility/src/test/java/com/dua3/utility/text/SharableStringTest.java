// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SharableStringTest {

    @Test
    void testConstructor() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original, sharable.toString());
        assertEquals(original.length(), sharable.length());
    }

    @Test
    void testEqualsAndHashCode() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable1 = new SharableString(original);
        SharableString sharable2 = new SharableString(original);
        SharableString sharable3 = new SharableString("different");

        assertEquals(sharable1, sharable1); // Same instance
        assertEquals(sharable1, sharable2); // Different instances, same content
        assertEquals(sharable1.hashCode(), sharable2.hashCode());

        assertNotEquals(sharable1, sharable3); // Different content
        assertNotEquals(sharable1, null); // Null comparison
        assertNotEquals(sharable1, "not a SharableString"); // Different type
    }

    @Test
    void testLength() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original.length(), sharable.length());
    }

    @Test
    void testCharAt() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        for (int i = 0; i < original.length(); i++) {
            assertEquals(original.charAt(i), sharable.charAt(i));
        }

        assertThrows(IndexOutOfBoundsException.class, () -> sharable.charAt(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> sharable.charAt(original.length()));
    }

    @Test
    void testSubSequence() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original.substring(5, 15), sharable.subSequence(5, 15).toString());

        assertThrows(IndexOutOfBoundsException.class, () -> sharable.subSequence(-1, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> sharable.subSequence(5, original.length() + 1));
        assertThrows(IndexOutOfBoundsException.class, () -> sharable.subSequence(10, 5));
    }

    @Test
    void testToString() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original, sharable.toString());
    }

    @Test
    void testSharedSubSequence() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        String originalSub1 = original.substring(5, 15);
        String originalSub2 = originalSub1.substring(2, 8);

        SharableString actual = new SharableString(original);
        SharedString actualSub1 = actual.subSequence(5, 15);
        SharedString actualSub2 = actualSub1.subSequence(2, 8);

        assertEquals(original, actual.toString());
        assertEquals(originalSub1, actualSub1.toString());
        assertEquals(originalSub2, actualSub2.toString());
    }
}
