package com.dua3.utility.text;

import com.dua3.utility.math.geometry.Rectangle2f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.SequencedCollection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontUtilTest {

    @Test
    void testGetInstance() {
        FontUtil fontUtil = FontUtil.getInstance();
        assertNotNull(fontUtil);
    }

    @ParameterizedTest
    @EnumSource(FontUtil.FontTypes.class)
    void testGetFamilies(FontUtil.FontTypes type) {
        FontUtil fontUtil = FontUtil.getInstance();
        SequencedCollection<String> families = fontUtil.getFamilies(type);
        assertNotNull(families);
        assertFalse(families.isEmpty());
    }

    @Test
    void testGetFamiliesDefault() {
        FontUtil fontUtil = FontUtil.getInstance();
        SequencedCollection<String> families = fontUtil.getFamilies();
        assertNotNull(families);
        assertFalse(families.isEmpty());
        assertEquals(fontUtil.getFamilies(FontUtil.FontTypes.ALL), families);
    }

    @Test
    void testGetDefaultFont() {
        FontUtil fontUtil = FontUtil.getInstance();
        Font defaultFont = fontUtil.getDefaultFont();
        assertNotNull(defaultFont);
    }

    @Test
    void testGetFontFromSpecAndFontDef() {
        FontUtil fontUtil = FontUtil.getInstance();
        Font fontFromSpec = fontUtil.getFont("Arial-bold-14");
        assertNotNull(fontFromSpec);
        assertEquals("Arial", fontFromSpec.getFamily());
        assertEquals(14.0, fontFromSpec.getSizeInPoints(), 0.1);
        assertTrue(fontFromSpec.isBold());

        FontDef fontDef = new FontDef();
        fontDef.setFamily("serif");
        fontDef.setSize(16.0f);
        fontDef.setItalic(true);
        Font fontFromDef = fontUtil.getFont(fontDef);
        assertNotNull(fontFromDef);
        assertEquals("serif", fontFromDef.getFamily());
        assertEquals(16.0, fontFromDef.getSizeInPoints(), 0.1);
        assertTrue(fontFromDef.isItalic());
    }

    @Test
    void testTextDimensions() {
        FontUtil fontUtil = FontUtil.getInstance();
        Font font = fontUtil.getDefaultFont();

        Rectangle2f dim = fontUtil.getTextDimension("Hello World", font);
        assertNotNull(dim);
        assertTrue(dim.width() > 0);
        assertTrue(dim.height() > 0);

        assertEquals(dim.width(), fontUtil.getTextWidth("Hello World", font), 0.001);
        assertEquals(dim.height(), fontUtil.getTextHeight("Hello World", font), 0.001);
    }

    @Test
    void testRichTextDimension() {
        FontUtil fontUtil = FontUtil.getInstance();
        Font font = fontUtil.getDefaultFont();

        RichText plainRt = RichText.valueOf("Plain single line");
        Rectangle2f plainDim = fontUtil.getRichTextDimension(plainRt, font);
        assertTrue(plainDim.width() > 0);
        assertTrue(plainDim.height() > 0);

        RichText multiLineRt = RichText.valueOf("Line 1\nLine 2");
        Rectangle2f multiLineDim = fontUtil.getRichTextDimension(multiLineRt, font);
        assertTrue(multiLineDim.height() > plainDim.height());

        RichText emptyRt = RichText.emptyText();
        Rectangle2f emptyDim = fontUtil.getRichTextDimension(emptyRt, font);
        assertNotNull(emptyDim);

        CharSequence cs = "Normal CharSequence";
        Rectangle2f csDim = fontUtil.getRichTextDimension(cs, font);
        assertEquals(fontUtil.getTextDimension(cs, font), csDim);
    }
}
