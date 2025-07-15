package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontDefTest {
    @Test
    void testNewInstance() {
        FontDef fontDef = new FontDef();
        assertNull(fontDef.getBold());
        assertNull(fontDef.getColor());
        assertNull(fontDef.getFamily());
        assertNull(fontDef.getItalic());
        assertNull(fontDef.getSize());
        assertNull(fontDef.getStrikeThrough());
        assert (fontDef.isEmpty());
    }

    @Test
    void testCreateWithBasicColor() {
        Color col = Color.valueOf("red");
        FontDef fontDef = FontDef.color(col);
        assertEquals(fontDef.getColor(), col);
    }

    @Test
    void testCreateWithValidColor() {
        Color expectedColor = Color.BLUE;
        FontDef fontDef = FontDef.color(expectedColor);
        assertEquals(expectedColor, fontDef.getColor());
    }

    @Test
    void testCreateWithNullColor() {
        FontDef fontDef = FontDef.color(null);
        assertNull(fontDef.getColor());
    }

    @Test
    void testCreateWithOnlyColorSet() {
        Color expectedColor = Color.GREEN;
        FontDef fontDef = FontDef.color(expectedColor);
        assertEquals(expectedColor, fontDef.getColor());
        assertNull(fontDef.getSize());
        assertNull(fontDef.getFamily());
        assertNull(fontDef.getBold());
        assertNull(fontDef.getItalic());
        assertNull(fontDef.getStrikeThrough());
        assertFalse(fontDef.isEmpty());
    }

    @Test
    void testCreateWithRedColor() {
        FontDef fontDef = FontDef.color(Color.RED);
        assertEquals(Color.RED, fontDef.getColor());
    }

    @Test
    void testCreateWithFamily() {
        String family = "Times New Roman";
        FontDef fontDef = FontDef.family(family);
        assertEquals(family, fontDef.getFamily());
    }

    @Test
    void testCreateWithSize() {
        Float size = 12.0f;
        FontDef fontDef = FontDef.size(size);
        assertEquals(size, fontDef.getSize());
    }

    @Test
    void testCreateWithBold() {
        boolean bold = true;
        FontDef fontDef = FontDef.bold(bold);
        assertEquals(bold, fontDef.getBold());
    }

    @Test
    void testParseFontspec() {
        String fontspec = "TimesNewRoman-bold-italic-12-red";
        FontDef result = FontDef.parseFontspec(fontspec);
        assertEquals("TimesNewRoman", result.getFamily());
        assertTrue(result.getBold());
        assertTrue(result.getItalic());
        assertEquals(12, result.getSize());
        assertEquals(Color.valueOf("red"), result.getColor());
    }

    @Test
    void testParseFontspecWithInvalidColor() {
        String fontspec = "TimesNewRoman-bold-italic-12-undefinedColor";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    void testParseFontspecWithInvalidSize() {
        String fontspec = "TimesNewRoman-bold-italic-undefinedSize-red";
        Assertions.assertThrows(IllegalArgumentException.class, () -> FontDef.parseFontspec(fontspec));
    }

    @Test
    void testParseCssBasic() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Arial", fd.getFamily());
        assertEquals(List.of("Arial"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithQuotedFont() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: \"Times New Roman\"; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Times New Roman", fd.getFamily());
        assertEquals(List.of("Times New Roman"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithMultipleFamilies() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial, Helvetica, SansSerif; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertEquals("Arial", fd.getFamily());
        assertEquals(List.of("Arial", "Helvetica", "SansSerif"), fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithInherit() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: inherit; font-weight: bold; font-style: italic; }";

        FontDef fd = FontDef.parseCssFontDef(fontdef);

        assertEquals(10.5f, fd.getSize()); // 14px = 10.5pt
        assertEquals(Color.WHITE, fd.getColor());
        assertNull(fd.getFamily());
        assertNull(fd.getFamilies());
        assertTrue(fd.getBold());
        assertTrue(fd.getItalic());
    }

    @Test
    void testParseCssWithInheritAfterFamily() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: Arial, inherit; font-weight: bold; font-style: italic; }";

        assertThrows(IllegalArgumentException.class, () -> FontDef.parseCssFontDef(fontdef));
    }

    @Test
    void testParseCssWithInheritBeforeFamily() {
        String fontdef = "{ font-size: 14px; color: #FFFFFF; font-family: inherit, inherit; font-weight: bold; font-style: italic; }";

        assertThrows(IllegalArgumentException.class, () -> FontDef.parseCssFontDef(fontdef));
    }

    @Test
    void testGenerateFontspec() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial, Helvetica, SansSerif");
        fd.setBold(true);
        fd.setItalic(true);

        // assuming the fontspec() method returns a font specification in the format 'family-bold/regular/italic-14.0-#FFFFFF'
        String expectedFontSpec = "Arial-bold-italic-*-*-14.0-#ffffff";
        String actualFontSpec = fd.fontspec();

        // put your appropriate assertions here
        assertEquals(expectedFontSpec, actualFontSpec);
    }

    @Test
    void testGenerateCssStyle() {
        FontDef fd = new FontDef();

        fd.setSize(14.0f);
        fd.setColor(Color.WHITE);
        fd.setFamily("Arial");
        fd.setBold(true);
        fd.setItalic(true);

        String expected = "font-family: Arial; font-size: 14.0pt; font-weight: bold; font-style: italic; color: #ffffff;";
        String actual = fd.getCssStyle();

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("fontArguments")
    void testConvertFontToFontDef(Font font) {
        // Test with a Font set
        FontDef fd = font.toFontDef();
        assertNotNull(fd);

        assertEquals(font.getFamily(), fd.getFamily());
        assertEquals(font.getSizeInPoints(), fd.getSize());
        assertEquals(font.getColor(), fd.getColor());
        assertEquals(font.isBold(), fd.getBold());
        assertEquals(font.isItalic(), fd.getItalic());
        assertEquals(font.isUnderline(), fd.getUnderline());
        assertEquals(font.isStrikeThrough(), fd.getStrikeThrough());
    }

    private static Stream<Font> fontArguments() {
        return Stream.of(
                FontUtil.getInstance().getFont("Arial-12"),
                FontUtil.getInstance().getFont("Times-17-bold").withColor(Color.DARKBLUE),
                FontUtil.getInstance().getFont("Arial-12-underline"),
                FontUtil.getInstance().getFont("Arial-12-strikethrough"),
                FontUtil.getInstance().getFont("Arial-12-italic"),
                FontUtil.getInstance().getFont("Helvetica-10-bold-underline-strikethrough-italic").withColor(Color.WHITE)
        );
    }

    @Test
    void testCreateWithFamilies() {
        List<String> families = List.of("Arial", "Helvetica", "SansSerif");
        FontDef fontDef = FontDef.families(families);
        assertEquals(families, fontDef.getFamilies());
    }

    @Test
    void testCreateWithItalic() {
        boolean italic = true;
        FontDef fontDef = FontDef.italic(italic);
        assertEquals(italic, fontDef.getItalic());
    }

    @Test
    void testCreateWithUnderline() {
        boolean underline = true;
        FontDef fontDef = FontDef.underline(underline);
        assertEquals(underline, fontDef.getUnderline());
    }

    @Test
    void testCreateWithStrikeThrough() {
        boolean strikeThrough = true;
        FontDef fontDef = FontDef.strikeThrough(strikeThrough);
        assertEquals(strikeThrough, fontDef.getStrikeThrough());
    }

    @Test
    void testParseFontFamiliesIndirectly() {
        // Test parseFontFamilies indirectly through family() method
        FontDef singleFamilyDef = FontDef.family("Arial");
        assertEquals(List.of("Arial"), singleFamilyDef.getFamilies());

        // Test with multiple families through setFamily
        FontDef multipleFamiliesDef = new FontDef();
        multipleFamiliesDef.setFamily("Arial, Helvetica, SansSerif");
        assertEquals(List.of("Arial", "Helvetica", "SansSerif"), multipleFamiliesDef.getFamilies());

        // Test with quoted family
        FontDef quotedFamilyDef = FontDef.family("\"Times New Roman\"");
        assertEquals(List.of("Times New Roman"), quotedFamilyDef.getFamilies());

        // Test with "inherit"
        FontDef inheritDef = FontDef.family("inherit");
        assertNull(inheritDef.getFamilies());

        // Test with null
        FontDef nullFamilyDef = FontDef.family(null);
        assertNull(nullFamilyDef.getFamilies());

        // Test with empty string
        FontDef emptyFamilyDef = FontDef.family("");
        assertNull(emptyFamilyDef.getFamilies());
    }

    @Test
    void testMatches() {
        // Create a FontDef with some properties set
        FontDef fontDef = new FontDef();
        fontDef.setBold(true);
        fontDef.setItalic(false);
        fontDef.setSize(12.0f);

        // Create a matching Font
        Font matchingFont = FontUtil.getInstance().getFont("Arial-12-bold");

        // Create a non-matching Font
        Font nonMatchingFont = FontUtil.getInstance().getFont("Arial-12-italic");

        // Test matching
        assertTrue(fontDef.matches(matchingFont));

        // Test non-matching
        assertFalse(fontDef.matches(nonMatchingFont));

        // Test with null properties (should match any font)
        FontDef emptyFontDef = new FontDef();
        assertTrue(emptyFontDef.matches(matchingFont));
        assertTrue(emptyFontDef.matches(nonMatchingFont));
    }

    @Test
    void testMerge() {
        // Create a base FontDef
        FontDef base = new FontDef();
        base.setBold(true);
        base.setSize(12.0f);

        // Create a delta FontDef
        FontDef delta = new FontDef();
        delta.setItalic(true);
        delta.setColor(Color.RED);

        // Merge delta into base
        base.merge(delta);

        // Check that base now has properties from both
        assertTrue(base.getBold());
        assertTrue(base.getItalic());
        assertEquals(12.0f, base.getSize());
        assertEquals(Color.RED, base.getColor());

        // Test that null properties in delta don't overwrite base
        FontDef base2 = new FontDef();
        base2.setBold(true);

        FontDef delta2 = new FontDef();
        // delta2 has all null properties

        base2.merge(delta2);
        assertTrue(base2.getBold()); // Should still be true
    }

    @Test
    void testIsEmpty() {
        // Test with empty FontDef
        FontDef emptyFontDef = new FontDef();
        assertTrue(emptyFontDef.isEmpty());

        // Test with non-empty FontDef
        FontDef nonEmptyFontDef = new FontDef();
        nonEmptyFontDef.setBold(true);
        assertFalse(nonEmptyFontDef.isEmpty());
    }

    @Test
    void testCopy() {
        // Create a FontDef with various properties set
        FontDef original = new FontDef();
        original.setBold(true);
        original.setItalic(false);
        original.setSize(12.0f);
        original.setColor(Color.RED);
        original.setFamilies(List.of("Arial", "Helvetica"));

        // Create a copy
        FontDef copy = original.copy();

        // Test that copy equals original
        assertEquals(original, copy);

        // Test that modifying the copy doesn't affect the original
        copy.setBold(false);
        assertNotEquals(original, copy);
        assertTrue(original.getBold());
        assertFalse(copy.getBold());
    }

    @Test
    void testConditionalMethods() {
        // Create a FontDef with various properties set
        FontDef fontDef = new FontDef();
        fontDef.setBold(true);
        fontDef.setItalic(false);
        fontDef.setSize(12.0f);
        fontDef.setColor(Color.RED);
        fontDef.setFamilies(List.of("Arial", "Helvetica"));
        fontDef.setUnderline(true);
        fontDef.setStrikeThrough(false);
        fontDef.setType(FontType.MONOSPACED);

        // Test ifColorDefined
        List<Color> colors = new ArrayList<>();
        fontDef.ifColorDefined(colors::add);
        assertEquals(List.of(Color.RED), colors);

        // Test ifSizeDefined
        List<Float> sizes = new ArrayList<>();
        boolean sizeRun = fontDef.ifSizeDefined(sizes::add);
        assertTrue(sizeRun);
        assertEquals(List.of(12.0f), sizes);

        // Test ifFamiliesDefined
        List<List<String>> familiesList = new ArrayList<>();
        boolean familiesRun = fontDef.ifFamiliesDefined(familiesList::add);
        assertTrue(familiesRun);
        assertEquals(List.of(List.of("Arial", "Helvetica")), familiesList);

        // Test ifBoldDefined
        List<Boolean> bolds = new ArrayList<>();
        fontDef.ifBoldDefined(bolds::add);
        assertEquals(List.of(true), bolds);

        // Test ifItalicDefined
        List<Boolean> italics = new ArrayList<>();
        fontDef.ifItalicDefined(italics::add);
        assertEquals(List.of(false), italics);

        // Test ifUnderlineDefined
        List<Boolean> underlines = new ArrayList<>();
        fontDef.ifUnderlineDefined(underlines::add);
        assertEquals(List.of(true), underlines);

        // Test ifStrikeThroughDefined
        List<Boolean> strikethroughs = new ArrayList<>();
        fontDef.ifStrikeThroughDefined(strikethroughs::add);
        assertEquals(List.of(false), strikethroughs);

        // Test ifTypeDefined
        List<FontType> types = new ArrayList<>();
        fontDef.ifTypeDefined(types::add);
        assertEquals(List.of(FontType.MONOSPACED), types);

        // Test with null properties
        FontDef emptyFontDef = new FontDef();

        List<Color> emptyColors = new ArrayList<>();
        emptyFontDef.ifColorDefined(emptyColors::add);
        assertTrue(emptyColors.isEmpty());

        List<Float> emptySizes = new ArrayList<>();
        boolean emptySizeRun = emptyFontDef.ifSizeDefined(emptySizes::add);
        assertFalse(emptySizeRun);
        assertTrue(emptySizes.isEmpty());
    }

    @Test
    void testIndirectParsingMethods() {
        // Test parseColor indirectly through parseCssFontDef
        FontDef colorDef = FontDef.parseCssFontDef("{ color: #FF0000; }");
        assertEquals(Color.RED, colorDef.getColor());

        FontDef inheritColorDef = FontDef.parseCssFontDef("{ color: inherit; }");
        assertNull(inheritColorDef.getColor());

        // Test parseFontWeight indirectly through parseCssFontDef
        FontDef boldDef = FontDef.parseCssFontDef("{ font-weight: bold; }");
        assertTrue(boldDef.getBold());

        FontDef normalWeightDef = FontDef.parseCssFontDef("{ font-weight: normal; }");
        assertFalse(normalWeightDef.getBold());

        FontDef inheritWeightDef = FontDef.parseCssFontDef("{ font-weight: inherit; }");
        assertNull(inheritWeightDef.getBold());

        // Test parseFontStyle indirectly through parseCssFontDef
        FontDef italicDef = FontDef.parseCssFontDef("{ font-style: italic; }");
        assertTrue(italicDef.getItalic());

        FontDef obliqueDef = FontDef.parseCssFontDef("{ font-style: oblique; }");
        assertTrue(obliqueDef.getItalic());

        FontDef normalStyleDef = FontDef.parseCssFontDef("{ font-style: normal; }");
        assertFalse(normalStyleDef.getItalic());

        FontDef inheritStyleDef = FontDef.parseCssFontDef("{ font-style: inherit; }");
        assertNull(inheritStyleDef.getItalic());

        // Test parseFontSize indirectly through parseCssFontDef
        FontDef sizeDef = FontDef.parseCssFontDef("{ font-size: 12pt; }");
        assertEquals(12.0f, sizeDef.getSize());

        FontDef sizeInPxDef = FontDef.parseCssFontDef("{ font-size: 16px; }");
        assertEquals(12.0f, sizeInPxDef.getSize()); // 16px = 12pt

        FontDef inheritSizeDef = FontDef.parseCssFontDef("{ font-size: inherit; }");
        assertNull(inheritSizeDef.getSize());

        // Test parseCssRule indirectly through parseCssFontDef
        FontDef ruleDef = FontDef.parseCssFontDef("font-size: 12pt; color: #FF0000;");
        assertEquals(12.0f, ruleDef.getSize());
        assertEquals(Color.RED, ruleDef.getColor());

        FontDef ruleWithWhitespaceDef = FontDef.parseCssFontDef("  font-family  :  Arial  ;  ");
        assertEquals(List.of("Arial"), ruleWithWhitespaceDef.getFamilies());

        // Test invalid rule
        assertThrows(IllegalArgumentException.class, () -> FontDef.parseCssFontDef("invalid-rule"));
    }
}
