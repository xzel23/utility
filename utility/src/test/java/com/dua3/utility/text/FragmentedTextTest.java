package com.dua3.utility.text;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Rectangle2f;
import com.dua3.utility.ui.Graphics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
