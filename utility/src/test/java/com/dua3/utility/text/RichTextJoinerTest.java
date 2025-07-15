package com.dua3.utility.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link RichTextJoiner}.
 */
class RichTextJoinerTest {

    @Test
    void testConstructorWithCharSequenceDelimiter() {
        String delimiter = ", ";
        RichTextJoiner joiner = new RichTextJoiner(delimiter);

        RichText result = Stream.of(RichText.valueOf("one"), RichText.valueOf("two"), RichText.valueOf("three")).collect(joiner);

        assertEquals("one, two, three", result.toString());
    }

    @Test
    void testConstructorWithCharSequenceDelimiterPrefixSuffix() {
        String delimiter = " | ";
        String prefix = "[";
        String suffix = "]";
        RichTextJoiner joiner = new RichTextJoiner(delimiter, prefix, suffix);

        RichText result = Stream.of(RichText.valueOf("one"), RichText.valueOf("two"), RichText.valueOf("three")).collect(joiner);

        assertEquals("[one | two | three]", result.toString());
    }

    @Test
    void testConstructorWithRichTextDelimiter() {
        RichText delimiter = RichText.valueOf(", ");
        RichTextJoiner joiner = new RichTextJoiner(delimiter);

        RichText result = Stream.of(RichText.valueOf("one"), RichText.valueOf("two"), RichText.valueOf("three")).collect(joiner);

        assertEquals("one, two, three", result.toString());
    }

    @Test
    void testConstructorWithRichTextDelimiterPrefixSuffix() {
        RichText delimiter = RichText.valueOf(" | ");
        RichText prefix = RichText.valueOf("[");
        RichText suffix = RichText.valueOf("]");
        RichTextJoiner joiner = new RichTextJoiner(delimiter, prefix, suffix);

        RichText result = Stream.of(RichText.valueOf("one"), RichText.valueOf("two"), RichText.valueOf("three")).collect(joiner);

        assertEquals("[one | two | three]", result.toString());
    }

    @Test
    void testEmptyStream() {
        RichTextJoiner joiner = new RichTextJoiner(", ");

        RichText result = Stream.<RichText>empty().collect(joiner);

        assertEquals("", result.toString());
        assertEquals(0, result.length());
    }

    @Test
    void testSingleElement() {
        RichTextJoiner joiner = new RichTextJoiner(", ");

        RichText result = Stream.of(RichText.valueOf("single")).collect(joiner);

        assertEquals("single", result.toString());
    }

    @Test
    void testSupplier() {
        RichTextJoiner joiner = new RichTextJoiner(", ");

        RichTextJoiner.AccumulationType accu = joiner.supplier().get();

        assertNotNull(accu);
        assertTrue(accu.texts().isEmpty());
        assertEquals(0, accu.counter().get());
    }

    @Test
    void testAccumulator() {
        RichTextJoiner joiner = new RichTextJoiner(", ");
        RichTextJoiner.AccumulationType accu = new RichTextJoiner.AccumulationType();
        RichText text1 = RichText.valueOf("test1");
        RichText text2 = RichText.valueOf("test2");

        joiner.accumulator().accept(accu, text1);
        joiner.accumulator().accept(accu, text2);

        assertEquals(2, accu.texts().size());
        assertEquals(text1, accu.texts().get(0));
        assertEquals(text2, accu.texts().get(1));
        assertEquals(text1.length() + text2.length(), accu.counter().get());
    }

    @Test
    void testCombiner() {
        RichTextJoiner joiner = new RichTextJoiner(", ");
        RichTextJoiner.AccumulationType accu1 = new RichTextJoiner.AccumulationType();
        RichTextJoiner.AccumulationType accu2 = new RichTextJoiner.AccumulationType();

        RichText text1 = RichText.valueOf("test1");
        RichText text2 = RichText.valueOf("test2");

        accu1.texts().add(text1);
        accu1.counter().addAndGet(text1.length());

        accu2.texts().add(text2);
        accu2.counter().addAndGet(text2.length());

        RichTextJoiner.AccumulationType combined = joiner.combiner().apply(accu1, accu2);

        assertSame(accu1, combined);
        assertEquals(2, combined.texts().size());
        assertEquals(text1, combined.texts().get(0));
        assertEquals(text2, combined.texts().get(1));
        assertEquals(text1.length() + text2.length(), combined.counter().get());
    }

    @Test
    void testFinisher() {
        RichTextJoiner joiner = new RichTextJoiner(", ");
        RichTextJoiner.AccumulationType accu = new RichTextJoiner.AccumulationType();

        RichText text1 = RichText.valueOf("test1");
        RichText text2 = RichText.valueOf("test2");

        accu.texts().add(text1);
        accu.texts().add(text2);
        accu.counter().addAndGet(text1.length() + text2.length());

        RichText result = joiner.finisher().apply(accu);

        assertEquals("test1, test2", result.toString());
    }

    @Test
    void testFinisherWithEmptyAccumulator() {
        RichTextJoiner joiner = new RichTextJoiner(", ");
        RichTextJoiner.AccumulationType accu = new RichTextJoiner.AccumulationType();

        RichText result = joiner.finisher().apply(accu);

        assertEquals("", result.toString());
        assertEquals(0, result.length());
    }

    @Test
    void testCharacteristics() {
        RichTextJoiner joiner = new RichTextJoiner(", ");

        Set<java.util.stream.Collector.Characteristics> characteristics = joiner.characteristics();

        assertTrue(characteristics.isEmpty());
    }

    @Test
    void testWithStyledText() {
        RichTextJoiner joiner = new RichTextJoiner(", ");

        RichTextBuilder builder1 = new RichTextBuilder();
        builder1.push(Style.BOLD);
        builder1.append("bold");
        builder1.pop(Style.BOLD);

        RichTextBuilder builder2 = new RichTextBuilder();
        builder2.push(Style.ITALIC);
        builder2.append("italic");
        builder2.pop(Style.ITALIC);

        RichText result = Stream.of(builder1.toRichText(), builder2.toRichText()).collect(joiner);

        assertEquals("bold, italic", result.toString());

        // Verify that styles are preserved
        // Check "bold" has bold style
        for (int i = 0; i < 4; i++) {
            List<Style> styles = result.stylesAt(i);
            assertEquals(1, styles.size());
            assertEquals(Style.BOLD, styles.get(0));
        }

        // Check ", " has no style
        for (int i = 4; i < 6; i++) {
            List<Style> styles = result.stylesAt(i);
            assertTrue(styles.isEmpty());
        }

        // Check "italic" has italic style
        for (int i = 6; i < 12; i++) {
            List<Style> styles = result.stylesAt(i);
            assertEquals(1, styles.size());
            assertEquals(Style.ITALIC, styles.get(0));
        }
    }
}
