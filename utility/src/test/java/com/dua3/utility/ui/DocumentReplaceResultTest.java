package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentReplaceResultTest {

    @Test
    void testProperties() {
        RichText removed = RichText.valueOf("removed text");
        DocumentReplaceResult result = new DocumentReplaceResult(2, 14, removed, true);

        assertEquals(2, result.start());
        assertEquals(14, result.end());
        assertEquals(removed, result.removed());
        assertTrue(result.changed());

        DocumentReplaceResult unchanged = new DocumentReplaceResult(0, 0, RichText.emptyText(), false);
        assertEquals(0, unchanged.start());
        assertEquals(0, unchanged.end());
        assertEquals(RichText.emptyText(), unchanged.removed());
        assertFalse(unchanged.changed());
    }

    @Test
    void testEqualsAndHashCode() {
        RichText removed = RichText.valueOf("abc");
        DocumentReplaceResult r1 = new DocumentReplaceResult(1, 4, removed, true);
        DocumentReplaceResult r2 = new DocumentReplaceResult(1, 4, removed, true);
        DocumentReplaceResult r3 = new DocumentReplaceResult(1, 4, removed, false);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
        assertNotEquals(null, r1);
    }
}
