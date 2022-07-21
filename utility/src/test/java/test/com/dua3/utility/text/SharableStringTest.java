// Copyright (c) 2019 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package test.com.dua3.utility.text;

import com.dua3.utility.text.SharableString;
import com.dua3.utility.text.SharedString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SharableStringTest {

    @Test
    public void testEqualsAndHashCode() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original, sharable.toString());
        assertEquals(original.hashCode(), sharable.toString().hashCode());
    }

    @Test
    public void testSubSequence() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        SharableString sharable = new SharableString(original);

        assertEquals(original.substring(5, 15), sharable.subSequence(5, 15).toString());
    }

    @Test
    public void testSharedSubSequence() {
        String original = "0123456789abcdefghijklmnopqrstuvwxyz";
        String original_sub1 = original.substring(5, 15);
        String original_sub2 = original_sub1.substring(2, 8);

        SharableString actual = new SharableString(original);
        SharedString actual_sub1 = actual.subSequence(5, 15);
        SharedString actual_sub2 = actual_sub1.subSequence(2, 8);

        assertEquals(original, actual.toString());
        assertEquals(original_sub1, actual_sub1.toString());
        assertEquals(original_sub2, actual_sub2.toString());
    }
}
