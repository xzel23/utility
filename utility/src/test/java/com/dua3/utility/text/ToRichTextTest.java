package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToRichTextTest {

    private record SampleItem(String name, int value) implements ToRichText {
        @Override
        public RichText toRichText() {
            return new RichTextBuilder().append(name).append("=").append(String.valueOf(value)).toRichText();
        }
    }

    @Test
    void testAppendTo() {
        SampleItem item = new SampleItem("Item", 42);
        RichTextBuilder builder = new RichTextBuilder();
        item.appendTo(builder);

        assertEquals("Item=42", builder.toRichText().toString());
    }

    @Test
    void testAppendToWithRange() {
        SampleItem item = new SampleItem("Item", 42);
        RichTextBuilder builder = new RichTextBuilder();
        item.appendTo(builder, 0, 4);

        assertEquals("Item", builder.toRichText().toString());

        RichTextBuilder builderRange = new RichTextBuilder();
        item.appendTo(builderRange, 5, 7);
        assertEquals("42", builderRange.toRichText().toString());
    }

    @Test
    void testAppendToWithInvalidRangeThrows() {
        SampleItem item = new SampleItem("Item", 42);
        RichTextBuilder builder = new RichTextBuilder();
        assertThrows(IndexOutOfBoundsException.class, () -> item.appendTo(builder, -1, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> item.appendTo(builder, 0, 10));
        assertThrows(IndexOutOfBoundsException.class, () -> item.appendTo(builder, 5, 2));
    }
}
