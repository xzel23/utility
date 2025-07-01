// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharableStringTest {

    @Test
    void testEqualsAndHashCode() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original, sharable.toString());
        assertEquals(original.hashCode(), sharable.toString().hashCode());
    }

    @Test
    void testSubSequence() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original.substring(5, 15), sharable.subSequence(5, 15).toString());
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
