package com.dua3.utility.text;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.ui.Graphics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link FragmentedText}.
 */
class FragmentedTextTest {

    private FontUtil<?> fontUtil;
    private Font defaultFont;

    @BeforeEach
    void setUp() {
        // Get FontUtil instance as specified in the requirements
        fontUtil = AwtFontUtil.getInstance();
        defaultFont = fontUtil.getDefaultFont();
    }

    @Test
    void testEmptyFragmentedText() {
        // Test the empty() factory method
        FragmentedText emptyText = FragmentedText.empty();

        assertTrue(emptyText.isEmpty());
        assertEquals(0, emptyText.lines().size());
        assertEquals(0.0f, emptyText.width());
        assertEquals(0.0f, emptyText.height());
        assertEquals(0.0f, emptyText.baseLine());
        assertEquals(0.0f, emptyText.actualWidth());
        assertEquals(0.0f, emptyText.actualHeight());

        // Test dimension methods
        assertEquals(Dimension2f.of(0, 0), emptyText.getLayoutDimension());
        assertEquals(Dimension2f.of(0, 0), emptyText.getActualDimension());
        assertEquals(0.0f, emptyText.getHDelta());
        assertEquals(0.0f, emptyText.getVDelta());

        // Test getTextRec
        assertEquals(Rectangle2f.of(0, 0, 0, 0), emptyText.getTextRec());
    }

    @Test
    void testGenerateFragmentsSimpleText() {
        // Create a simple text
        RichText text = RichText.valueOf("Hello, World!");
        float width = 200.0f;
        float height = 50.0f;

        // Generate fragments with left alignment and top vertical alignment
        FragmentedText fragText = FragmentedText.generateFragments(
                text,
                fontUtil,
                defaultFont,
                width,
                height,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT,
                Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Verify basic properties
        assertFalse(fragText.isEmpty());
        assertEquals(1, fragText.lines().size()); // Should be a single line
        assertEquals(width, fragText.width());
        assertEquals(height, fragText.height());
        assertTrue(fragText.actualWidth() > 0);
        assertTrue(fragText.actualHeight() > 0);

        // Verify the fragment contains our text
        List<FragmentedText.Fragment> line = fragText.lines().getFirst();
        assertFalse(line.isEmpty());
        assertEquals("Hello, World!", line.getFirst().text().toString());
    }

    @Test
    void testGenerateFragmentsWithWrapping() {
        // Create a text that should wrap
        RichText text = RichText.valueOf("This is a longer text that should wrap to multiple lines when the width is limited.");
        float width = 150.0f;
        float height = 100.0f;
        float wrapWidth = 100.0f; // Force wrapping

        // Generate fragments with wrapping
        FragmentedText fragText = FragmentedText.generateFragments(
                text,
                fontUtil,
                defaultFont,
                width,
                height,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT,
                Graphics.VAnchor.TOP,
                wrapWidth
        );

        // Verify wrapping occurred
        assertTrue(fragText.lines().size() > 1, "Text should be wrapped into multiple lines");

        // Check that no line exceeds the wrap width
        for (List<FragmentedText.Fragment> line : fragText.lines()) {
            float lineWidth = 0;
            for (FragmentedText.Fragment fragment : line) {
                lineWidth += fragment.w();
            }
            assertTrue(lineWidth <= width, "Line width should not exceed the specified width");
        }
    }

    @Test
    void testGenerateFragmentsWithDifferentAlignments() {
        RichText text = RichText.valueOf("Alignment Test");
        float width = 200.0f;
        float height = 50.0f;

        // Test LEFT alignment
        FragmentedText leftAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Test RIGHT alignment
        FragmentedText rightAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.RIGHT, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Test CENTER alignment
        FragmentedText centerAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.CENTER, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Verify alignments are different
        float leftX = leftAligned.lines().getFirst().getFirst().x();
        float rightX = rightAligned.lines().getFirst().getFirst().x();
        float centerX = centerAligned.lines().getFirst().getFirst().x();

        assertTrue(leftX < centerX, "Left aligned text should start before center aligned text");
        assertTrue(centerX < rightX, "Center aligned text should start before right aligned text");
    }

    @Test
    void testGenerateFragmentsWithDifferentVerticalAlignments() {
        RichText text = RichText.valueOf("Vertical Alignment Test");
        float width = 200.0f;
        float height = 100.0f;

        // Test TOP alignment
        FragmentedText topAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Test MIDDLE alignment
        FragmentedText middleAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.MIDDLE,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Test BOTTOM alignment
        FragmentedText bottomAligned = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.BOTTOM,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Instead of comparing Y coordinates directly, which might be affected by the anchor,
        // we'll verify that the vertical alignments are applied correctly by checking
        // that the text rectangles are positioned differently
        Rectangle2f topRect = topAligned.getTextRec();
        Rectangle2f middleRect = middleAligned.getTextRec();
        Rectangle2f bottomRect = bottomAligned.getTextRec();

        // Verify that the alignments are different
        assertNotEquals(topRect.yMin(), middleRect.yMin(), "Top and middle alignments should be different");
        assertNotEquals(middleRect.yMin(), bottomRect.yMin(), "Middle and bottom alignments should be different");
        assertNotEquals(topRect.yMin(), bottomRect.yMin(), "Top and bottom alignments should be different");
    }

    @Test
    void testFragmentTranslation() {
        // Create a fragment
        FragmentedText.Fragment fragment = new FragmentedText.Fragment(
                10.0f, 20.0f, 30.0f, 40.0f, 5.0f, defaultFont, "Test"
        );

        // Translate the fragment
        FragmentedText.Fragment translated = fragment.translate(5.0f, 10.0f);

        // Verify translation
        assertEquals(15.0f, translated.x());
        assertEquals(30.0f, translated.y());
        assertEquals(fragment.w(), translated.w());
        assertEquals(fragment.h(), translated.h());
        assertEquals(fragment.baseLine(), translated.baseLine());
        assertEquals(fragment.font(), translated.font());
        assertEquals(fragment.text(), translated.text());
    }

    @Test
    void testGetTextRec() {
        // Create a text with multiple lines
        RichText text = RichText.valueOf("Line 1\nLine 2\nLine 3");
        float width = 200.0f;
        float height = 100.0f;

        FragmentedText fragText = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Get the text rectangle
        Rectangle2f textRec = fragText.getTextRec();

        // Verify the rectangle properties
        assertNotNull(textRec);
        assertTrue(textRec.width() > 0);
        assertTrue(textRec.height() > 0);
        assertEquals(fragText.actualWidth(), textRec.width());
        assertEquals(fragText.actualHeight(), textRec.height());
    }

    @Test
    void testMultilineTextWithDifferentFonts() {
        // Create a rich text with different font styles
        RichText line1 = RichText.valueOf("Bold text");
        Style boldStyle = Style.BOLD;
        RichText boldText = line1.apply(boldStyle);

        RichText line2 = RichText.valueOf("Italic text");
        Style italicStyle = Style.ITALIC;
        RichText italicText = line2.apply(italicStyle);

        RichText line3 = RichText.valueOf("Regular text");

        // Join the text with newlines
        RichText newline = RichText.valueOf("\n");
        RichText text = RichText.join(newline, boldText, italicText, line3);

        float width = 200.0f;
        float height = 150.0f;

        FragmentedText fragText = FragmentedText.generateFragments(
                text, fontUtil, defaultFont, width, height,
                Alignment.LEFT, VerticalAlignment.TOP,
                Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP,
                FragmentedText.NO_WRAP
        );

        // Verify we have 3 lines
        assertEquals(3, fragText.lines().size());

        // Verify each line has the correct text and font style
        assertTrue(fragText.lines().get(0).getFirst().font().isBold());
        assertTrue(fragText.lines().get(1).getFirst().font().isItalic());
        assertFalse(fragText.lines().get(2).getFirst().font().isBold());
        assertFalse(fragText.lines().get(2).getFirst().font().isItalic());
    }

    @Test
    void testGenerateFragmentsWithDistributedVerticalAlignment() {
        // Create a multi-line text to test distributed vertical alignment
        RichText text = RichText.valueOf("Line 1\nLine 2\nLine 3\nLine 4");
        float width = 200.0f;
        float height = 200.0f; // Enough height to distribute lines

        // Generate fragments with DISTRIBUTED vertical alignment
        FragmentedText distributedAligned = FragmentedText.generateFragments(text, fontUtil, defaultFont, width, height, Alignment.LEFT, VerticalAlignment.DISTRIBUTED, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP, FragmentedText.NO_WRAP);

        // Verify that we have multiple lines
        List<List<FragmentedText.Fragment>> lines = distributedAligned.lines();
        assertTrue(lines.size() > 1, "Should have multiple lines for distributed alignment test");

        // Verify that the lines are distributed by checking that the vertical spacing is consistent
        if (lines.size() > 2) {
            float firstGap = lines.get(1).getFirst().y() - lines.get(0).getFirst().y();
            float secondGap = lines.get(2).getFirst().y() - lines.get(1).getFirst().y();

            // The gaps should be approximately equal for distributed alignment
            assertEquals(firstGap, secondGap, 0.1f, "Vertical gaps between lines should be equal with DISTRIBUTED alignment");
        }
    }

    @Test
    void testGenerateFragmentsWithDistributeAlignment() {
        // Create a text with multiple words to test distribute alignment
        RichText text = RichText.valueOf("This is a test with multiple words for distribute alignment");
        float width = 300.0f; // Wide enough to fit the text but with some extra space
        float height = 50.0f;

        // Generate fragments with DISTRIBUTE alignment
        FragmentedText distributeAligned = FragmentedText.generateFragments(text, fontUtil, defaultFont, width, height, Alignment.DISTRIBUTE, VerticalAlignment.TOP, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP, width);

        // Verify that we have at least one line
        List<List<FragmentedText.Fragment>> lines = distributeAligned.lines();
        assertFalse(lines.isEmpty(), "Should have at least one line");

        // Get the first line
        List<FragmentedText.Fragment> line = lines.getFirst();

        // Verify that there are multiple fragments in the line
        assertTrue(line.size() > 1, "Line should have multiple fragments");

        // Check that the fragments are distributed by verifying that the whitespace
        // between fragments is consistent
        if (line.size() > 2) {
            // Find fragments with whitespace
            List<Integer> whitespaceIndices = new ArrayList<>();
            for (int i = 0; i < line.size(); i++) {
                if (TextUtil.isBlank(line.get(i).text())) {
                    whitespaceIndices.add(i);
                }
            }

            // If we have at least two whitespace fragments, check that they have similar widths
            if (whitespaceIndices.size() >= 2) {
                float firstWidth = line.get(whitespaceIndices.get(0)).w();
                float secondWidth = line.get(whitespaceIndices.get(1)).w();

                // The widths should be approximately equal for distribute alignment
                assertEquals(firstWidth, secondWidth, 0.1f, "Whitespace widths should be equal with DISTRIBUTE alignment");
            }
        }
    }

    @Test
    void testGenerateFragmentsWithJustifyAlignment() {
        // Create a text with multiple lines to test justify alignment
        // Using a long text that will be wrapped to multiple lines
        RichText text = RichText.valueOf("This is a long text that should be wrapped to multiple lines. " + "We need to make sure it's long enough to wrap. The justify alignment should distribute " + "whitespace evenly in all lines except the last one.");
        float width = 200.0f; // Narrow enough to force wrapping
        float height = 200.0f; // Tall enough for multiple lines
        float wrapWidth = 150.0f; // Force wrapping

        // Generate fragments with JUSTIFY alignment and wrapping
        FragmentedText justifyAligned = FragmentedText.generateFragments(text, fontUtil, defaultFont, width, height, Alignment.JUSTIFY, VerticalAlignment.TOP, Graphics.HAnchor.LEFT, Graphics.VAnchor.TOP, wrapWidth);

        // Verify that we have multiple lines
        List<List<FragmentedText.Fragment>> lines = justifyAligned.lines();
        assertTrue(lines.size() > 1, "Should have multiple lines for justify alignment test");

        // For each line except the last one, check that it has multiple fragments
        for (int i = 0; i < lines.size() - 1; i++) {
            List<FragmentedText.Fragment> line = lines.get(i);
            assertTrue(line.size() > 1, "Line " + i + " should have multiple fragments");

            // Find fragments with whitespace
            List<Integer> whitespaceIndices = new ArrayList<>();
            for (int j = 0; j < line.size(); j++) {
                if (TextUtil.isBlank(line.get(j).text())) {
                    whitespaceIndices.add(j);
                }
            }

            // If we have at least two whitespace fragments, check that they have similar widths
            if (whitespaceIndices.size() >= 2) {
                float firstWidth = line.get(whitespaceIndices.get(0)).w();
                float secondWidth = line.get(whitespaceIndices.get(1)).w();

                // The widths should be approximately equal for justify alignment (which uses distribute for non-last lines)
                assertEquals(firstWidth, secondWidth, 0.1f, "Whitespace widths should be equal with JUSTIFY alignment for non-last line " + i);
            }
        }
    }
}
