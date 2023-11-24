package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToRichTextTest implements ToRichText {

    static class Foo implements ToRichText {
        @Override
        public String toString() {
            return "Hello";
        }
    }

    @Test
    void testToRichText() {
        Foo foo = new Foo();
        assertEquals(RichText.valueOf("Hello"), foo.toRichText());
    }

}