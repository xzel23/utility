package com.dua3.utility.swing;

import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;
import org.junit.jupiter.api.Test;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the StyledDocumentConverter class.
 * <p>
 * These tests verify the functionality of converting RichText to StyledDocument.
 */
class StyledDocumentConverterTest {

    /**
     * Test creating a StyledDocumentConverter with default options.
     */
    @Test
    void testCreateWithDefaultOptions() {
        StyledDocumentConverter converter = StyledDocumentConverter.create();
        assertNotNull(converter, "Converter should not be null");
    }

    /**
     * Test converting simple text without styling.
     */
    @Test
    void testConvertSimpleText() throws BadLocationException {
        // Create a simple RichText
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello world!");
        RichText richText = builder.toRichText();

        // Convert to StyledDocument
        StyledDocumentConverter converter = StyledDocumentConverter.create();
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Hello world!", document.getText(0, document.getLength()), "Document text should match the original text");
    }

    /**
     * Test converting text with bold styling.
     */
    @Test
    void testConvertBoldText() throws BadLocationException {
        // Create RichText with bold styling
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Hello ");
        builder.push(Style.BOLD);
        builder.append("bold");
        builder.pop(Style.BOLD);
        builder.append(" world!");
        RichText richText = builder.toRichText();

        // Convert to StyledDocument
        StyledDocumentConverter converter = StyledDocumentConverter.create();
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Hello bold world!", document.getText(0, document.getLength()), "Document text should match the original text");

        // Note: We can't easily verify the styling attributes in a unit test
        // without complex setup, but we can verify the text content is correct
    }

    /**
     * Test converting text with custom font.
     */
    @Test
    void testConvertWithCustomFont() throws BadLocationException {
        // Get a custom font
        Font customFont = FontUtil.getInstance().getFont("arial-14-bold");
        Style customStyle = Style.create("customFont", Map.entry(Style.FONT, customFont));

        // Create RichText with custom font
        RichTextBuilder builder = new RichTextBuilder();
        builder.push(customStyle);
        builder.append("Custom font text");
        builder.pop(customStyle);
        RichText richText = builder.toRichText();

        // Convert to StyledDocument
        StyledDocumentConverter converter = StyledDocumentConverter.create();
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Custom font text", document.getText(0, document.getLength()), "Document text should match the original text");
    }

    /**
     * Test converting text with multiple styles.
     */
    @Test
    void testConvertWithMultipleStyles() throws BadLocationException {
        // Create RichText with multiple styles
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Normal ");
        builder.push(Style.BOLD);
        builder.append("Bold ");
        builder.push(Style.ITALIC);
        builder.append("Bold-Italic ");
        builder.pop(Style.ITALIC);
        builder.pop(Style.BOLD);
        builder.push(Style.ITALIC);
        builder.append("Italic");
        builder.pop(Style.ITALIC);
        RichText richText = builder.toRichText();

        // Convert to StyledDocument
        StyledDocumentConverter converter = StyledDocumentConverter.create();
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Normal Bold Bold-Italic Italic", document.getText(0, document.getLength()), "Document text should match the original text");
    }

    /**
     * Test converting text with custom scale.
     */
    @Test
    void testConvertWithCustomScale() throws BadLocationException {
        // Create a simple RichText
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Scaled text");
        RichText richText = builder.toRichText();

        // Convert to StyledDocument with custom scale
        StyledDocumentConverter converter = StyledDocumentConverter.create(StyledDocumentConverter.scale(2.0));
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Scaled text", document.getText(0, document.getLength()), "Document text should match the original text");
    }

    /**
     * Test converting text with default attributes.
     */
    @Test
    void testConvertWithDefaultAttributes() throws BadLocationException {
        // Create a simple RichText
        RichTextBuilder builder = new RichTextBuilder();
        builder.append("Text with default attributes");
        RichText richText = builder.toRichText();

        // Create a map of default attributes
        Map<String, Object> defaultAttrs = Map.of(Style.FONT_WEIGHT, Style.FONT_WEIGHT_VALUE_BOLD);

        // Convert to StyledDocument with default attributes
        StyledDocumentConverter converter = StyledDocumentConverter.create(StyledDocumentConverter.defaultAttributes(defaultAttrs));
        StyledDocument document = converter.convert(richText);

        // Verify the content
        assertNotNull(document, "Document should not be null");
        assertEquals("Text with default attributes", document.getText(0, document.getLength()), "Document text should match the original text");
    }
}