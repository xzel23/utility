// Copyright (c) 2023 Axel Howind
//
// This software is released under the MIT License.
// https://opensource.org/licenses/MIT

package com.dua3.utility.text;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RichTextMatcher}.
 */
class RichTextMatcherTest {
    private static final Logger LOG = LogManager.getLogger(RichTextMatcherTest.class);

    @Test
    void testMatcher() {
        // Test creating a matcher
        RichText text = RichText.valueOf("Hello world!");
        Pattern pattern = Pattern.compile("world");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Test find method
        assertTrue(matcher.find());
        assertEquals(6, matcher.start());
        assertEquals(11, matcher.end());
        assertEquals("world", matcher.group());
        assertEquals(RichText.valueOf("world"), matcher.rgroup());

        // Test that find returns false when no more matches
        assertFalse(matcher.find());
    }

    @Test
    void testFindWithStartPosition() {
        RichText text = RichText.valueOf("Hello world! Hello universe!");
        Pattern pattern = Pattern.compile("Hello");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // First match
        assertTrue(matcher.find());
        assertEquals(0, matcher.start());
        assertEquals(5, matcher.end());

        // Second match using find(int)
        assertTrue(matcher.find(6));
        assertEquals(13, matcher.start());
        assertEquals(18, matcher.end());

        // second test case
        text = RichText.valueOf("One two one two one");
        pattern = Pattern.compile("(?i)one");
        matcher = RichText.matcher(pattern, text);

        // Find first match
        assertTrue(matcher.find());
        assertEquals(0, matcher.start());
        assertEquals(3, matcher.end());

        // Find second match starting from position 4
        assertTrue(matcher.find(4));
        assertEquals(8, matcher.start());
        assertEquals(11, matcher.end());

        // Find third match starting from position 12
        assertTrue(matcher.find(12));
        assertEquals(16, matcher.start());
        assertEquals(19, matcher.end());

        // No more matches
        assertFalse(matcher.find());
    }

    @Test
    void testGroupMethods() {
        RichText text = RichText.valueOf("John Doe (42)");
        Pattern pattern = Pattern.compile("(\\w+) (\\w+) \\((\\d+)\\)");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        assertTrue(matcher.find());
        assertEquals(3, matcher.groupCount());

        // Test group() and rgroup()
        assertEquals("John Doe (42)", matcher.group());
        assertEquals(RichText.valueOf("John Doe (42)"), matcher.rgroup());

        // Test group(int) and rgroup(int)
        assertEquals("John", matcher.group(1));
        assertEquals("Doe", matcher.group(2));
        assertEquals("42", matcher.group(3));

        assertEquals(RichText.valueOf("John"), matcher.rgroup(1));
        assertEquals(RichText.valueOf("Doe"), matcher.rgroup(2));
        assertEquals(RichText.valueOf("42"), matcher.rgroup(3));

        // Create a RichText with a pattern to match
        text = RichText.valueOf("Hello world! This is a test.");
        pattern = Pattern.compile("(\\w+)\\s+(\\w+)!");
        matcher = RichText.matcher(pattern, text);

        // Find the pattern
        assertTrue(matcher.find());

        // Test group()
        assertEquals("Hello world!", matcher.group());

        // Test group(int) for capturing groups
        assertEquals("Hello", matcher.group(1));
        assertEquals("world", matcher.group(2));
    }

    @Test
    void testReplaceFirst() {
        RichText text = RichText.valueOf("Hello world! Hello universe!");
        Pattern pattern = Pattern.compile("Hello");

        // Test replaceFirst with CharSequence
        RichTextMatcher matcher1 = RichText.matcher(pattern, text);
        RichText replaced = matcher1.replaceFirst("Hi");
        assertEquals("Hi world! Hello universe!", replaced.toString());

        // Test replaceFirst with RichText
        RichText boldHi = RichText.valueOf("Hi").wrap(Style.BOLD);
        RichTextMatcher matcher2 = RichText.matcher(pattern, text);
        replaced = matcher2.replaceFirst(boldHi);
        assertEquals("Hi world! Hello universe!", replaced.toString());

        // Verify that the style was applied
        assertTrue(replaced.subSequence(0, 2).runs().get(0).getStyles().contains(Style.BOLD));
    }

    @Test
    void testReplaceAll() {
        RichText text = RichText.valueOf("Hello world! Hello universe!");
        Pattern pattern = Pattern.compile("Hello");

        // Test replaceAll with CharSequence
        RichTextMatcher matcher1 = RichText.matcher(pattern, text);
        RichText replaced = matcher1.replaceAll("Hi");
        assertEquals("Hi world! Hi universe!", replaced.toString());

        // Test replaceAll with RichText
        RichText boldHi = RichText.valueOf("Hi").wrap(Style.BOLD);
        RichTextMatcher matcher2 = RichText.matcher(pattern, text);
        replaced = matcher2.replaceAll(boldHi);
        assertEquals("Hi world! Hi universe!", replaced.toString());

        // Verify that the style was applied to both replacements
        assertTrue(replaced.subSequence(0, 2).runs().get(0).getStyles().contains(Style.BOLD));

        // Print the entire replaced text for debugging
        LOG.debug("Replaced text: {}", replaced);

        // Find the position of the second "Hi"
        int secondHiPos = replaced.toString().indexOf("Hi", 3);
        LOG.debug("Second Hi position: {}", secondHiPos);

        // The second replacement should be at the found position
        RichText secondReplacement = replaced.subSequence(secondHiPos, secondHiPos + 2);
        LOG.debug("Second replacement: {}", secondReplacement);
        LOG.debug("Second replacement runs: {}", secondReplacement.runs().size());

        // Check if the style was applied to the second replacement
        if (secondReplacement.runs().size() > 0) {
            LOG.debug("Second replacement styles: {}", secondReplacement.runs().get(0).getStyles());
            assertTrue(secondReplacement.runs().get(0).getStyles().contains(Style.BOLD));
        } else {
            LOG.debug("Second replacement has no runs");
            fail("Second replacement has no runs");
        }
    }

    @Test
    void testNoMatch() {
        RichText text = RichText.valueOf("Hello world!");
        Pattern pattern = Pattern.compile("xyz");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Test that find returns false when no match
        assertFalse(matcher.find());

        // Test that replace methods return the original text when no match
        assertEquals(text, matcher.replaceFirst("replacement"));
        assertEquals(text, matcher.replaceAll("replacement"));
    }

    @Test
    void testMatcherWithStyledText() {
        // Create styled text
        RichText boldHello = RichText.valueOf("Hello").wrap(Style.BOLD);
        RichText italicWorld = RichText.valueOf("World").wrap(Style.ITALIC);
        RichText text = RichText.join(RichText.valueOf(" "), boldHello, italicWorld);

        // Test that matcher works with styled text
        Pattern pattern = Pattern.compile("Hello");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        assertTrue(matcher.find());
        assertEquals(0, matcher.start());
        assertEquals(5, matcher.end());

        // Test that rgroup preserves styles
        RichText match = matcher.rgroup();
        assertEquals("Hello", match.toString());
        assertTrue(match.runs().get(0).getStyles().contains(Style.BOLD));
    }

    @Test
    void testStartAndEndMethods() {
        // Create a RichText with a pattern to match
        RichText text = RichText.valueOf("Hello world! This is a test.");
        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)!");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Find the pattern
        assertTrue(matcher.find());

        // Test start() and end()
        assertEquals(0, matcher.start());
        assertEquals(12, matcher.end());

        // Test start(int) and end(int) for capturing groups
        assertEquals(0, matcher.start(1)); // "Hello"
        assertEquals(5, matcher.end(1));
        assertEquals(6, matcher.start(2)); // "world"
        assertEquals(11, matcher.end(2));
    }

    @Test
    void testGroupCount() {
        // Create a RichText with a pattern to match
        RichText text = RichText.valueOf("Hello world! This is a test.");
        Pattern pattern = Pattern.compile("(\\w+)\\s+(\\w+)!");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Test groupCount()
        assertEquals(2, matcher.groupCount());
    }

    @Test
    void testMultipleMatches() {
        // Create a RichText with multiple matches
        RichText text = RichText.valueOf("One two three four five");
        Pattern pattern = Pattern.compile("(\\w+)");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Find first match
        assertTrue(matcher.find());
        assertEquals("One", matcher.group());
        assertEquals(0, matcher.start());
        assertEquals(3, matcher.end());

        // Find second match
        assertTrue(matcher.find());
        assertEquals("two", matcher.group());
        assertEquals(4, matcher.start());
        assertEquals(7, matcher.end());

        // Find third match
        assertTrue(matcher.find());
        assertEquals("three", matcher.group());
        assertEquals(8, matcher.start());
        assertEquals(13, matcher.end());

        // Find fourth match
        assertTrue(matcher.find());
        assertEquals("four", matcher.group());
        assertEquals(14, matcher.start());
        assertEquals(18, matcher.end());

        // Find fifth match
        assertTrue(matcher.find());
        assertEquals("five", matcher.group());
        assertEquals(19, matcher.start());
        assertEquals(23, matcher.end());

        // No more matches
        assertFalse(matcher.find());
    }

    @Test
    void testRgroupMethods() {
        // Create a styled RichText
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("world");
        builder.pop(Style.BOLD);
        builder.append("! This is a test.");
        RichText text = builder.toRichText();

        // Create a pattern that matches across style boundaries
        Pattern pattern = Pattern.compile("(Hello) (world)!");
        RichTextMatcher matcher = RichText.matcher(pattern, text);

        // Find the pattern
        assertTrue(matcher.find());

        // Test rgroup()
        RichText fullMatch = matcher.rgroup();
        assertEquals("Hello world!", fullMatch.toString());

        // Test rgroup(int) for capturing groups
        RichText group1 = matcher.rgroup(1);
        RichText group2 = matcher.rgroup(2);

        assertEquals("Hello", group1.toString());
        assertEquals("world", group2.toString());

        // Verify that styles are preserved
        assertFalse(group1.stylesAt(0).contains(Style.BOLD));
        assertTrue(group2.stylesAt(0).contains(Style.BOLD));
    }
}
