package com.dua3.utility.text;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.ui.InlineNode;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RtfConverterTest {

    @Test
    void testRoundTripRichText() {
        RtfConverter converter = RtfConverter.get().orElseThrow();

        RichTextBuilder builder = new RichTextBuilder();
        builder.push(Style.BOLD);
        builder.append("Bold");
        builder.pop(Style.BOLD);
        builder.append(" ");
        builder.push(Style.ITALIC);
        builder.push(Style.RED);
        builder.append("ItalicRed");
        builder.pop(Style.RED);
        builder.pop(Style.ITALIC);

        builder.append("\n");
        Style mono = Style.create(
                "mono-11",
                Map.entry(Style.FONT_FAMILIES, List.of("Courier New")),
                Map.entry(Style.FONT_SIZE, 11f)
        );
        builder.push(mono);
        builder.append("Mono {\\}\tX");
        builder.pop(mono);

        builder.append("\n");
        builder.push(Style.UNDERLINE);
        builder.push(Style.LINE_THROUGH);
        builder.append("UnderStrike");
        builder.pop(Style.LINE_THROUGH);
        builder.pop(Style.UNDERLINE);

        RichText expected = builder.toRichText();
        String rtf = converter.fromRichText(expected);
        RichText actual = converter.toRichText(rtf);

        assertEquals(expected.toString(), actual.toString());
        assertTrue(rtf.startsWith("{\\rtf1"));
        assertTrue(rtf.contains("{\\fonttbl"));
        assertTrue(rtf.contains("{\\colortbl"));

        int boldPos = actual.indexOf("Bold");
        assertEquals(Boolean.TRUE, actual.runAt(boldPos).getFontDef().getBold());

        int italicRedPos = actual.indexOf("ItalicRed");
        assertEquals(Boolean.TRUE, actual.runAt(italicRedPos).getFontDef().getItalic());
        assertEquals(Color.RED, actual.runAt(italicRedPos).getFontDef().getColor());

        int monoPos = actual.indexOf("Mono");
        assertEquals("Courier New", actual.runAt(monoPos).getFontDef().getFamily());
        assertEquals(11f, actual.runAt(monoPos).getFontDef().getSize());

        int underStrikePos = actual.indexOf("UnderStrike");
        assertEquals(Boolean.TRUE, actual.runAt(underStrikePos).getFontDef().getUnderline());
        assertEquals(Boolean.TRUE, actual.runAt(underStrikePos).getFontDef().getStrikeThrough());
    }

    @Test
    void testRoundTripInlineImage() {
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

        Image image = ImageUtil.getInstance().createImage(
                2,
                2,
                new int[]{
                        Color.RED.argb(), Color.GREEN.argb(),
                        Color.BLUE.argb(), Color.YELLOW.argb()
                }
        );

        InlineNode<Image> inlineNode = new InlineNode<>(
                image,
                image.mimeType(),
                InlineNode.encodeArgbImageData(image)
        );
        Style imageStyle = Style.create(
                "image-inline",
                Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE, inlineNode)
        );

        RichTextBuilder builder = new RichTextBuilder();
        builder.append("A");
        builder.push(imageStyle);
        builder.append(RichTextBuilderExtBase.INLINE_NODE_MARKER);
        builder.pop(imageStyle);
        builder.append("B");
        RichText expected = builder.toRichText();

        String rtf = rtfConverter.fromRichText(expected);

        RichText actual = rtfConverter.toRichText(rtf);

        assertEquals(expected.toString(), actual.toString());

        int inlinePos = actual.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER);
        Run run = actual.runAt(inlinePos);

        Object inlineAttribute = run.getStyles().stream()
                .map(style -> style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow();
        InlineNode<?> imported = assertInstanceOf(InlineNode.class, inlineAttribute);

        assertEquals("image/jpeg", imported.getMimeType());

        Image importedImage = InlineNode.decodeArgbImageData(imported.getData());
        assertEquals(image.width(), importedImage.width());
        assertEquals(image.height(), importedImage.height());
        assertTrue(Arrays.equals(image.getArgb(), importedImage.getArgb()));
    }

    @Test
    void testFromRichTextHandlesEmptyText() {
        RtfConverter converter = RtfConverter.get().orElseThrow();
        String rtf = converter.fromRichText(RichText.emptyText());
        RichText parsed = converter.toRichText(rtf);

        assertTrue(rtf.startsWith("{\\rtf1"));
        assertEquals("", parsed.toString());
    }

    @Test
    void testToRichTextParsesRtfText() {
        RtfConverter converter = RtfConverter.get().orElseThrow();
        RichText actual = converter.toRichText("{\\rtf1\\ansi\\deff0\\pard\\b Bold\\b0\\par Plain\\par}");

        assertEquals("Bold\nPlain\n", actual.toString());
    }

    @Test
    void testToRichTextParsesSupportedStyles() {
        RtfConverter converter = RtfConverter.get().orElseThrow();
        String rtf = "{\\rtf1\\ansi\\deff0"
                + "{\\fonttbl{\\f0\\fnil Arial;}{\\f1\\fmodern Courier New;}}"
                + "{\\colortbl ;\\red255\\green0\\blue0;}"
                + "\\pard\\fs24\\b Bold\\b0 "
                + "\\i\\cf1 ItalicRed\\i0\\cf0 "
                + "\\f1\\fs20 Mono "
                + "\\ul Under\\ulnone "
                + "\\strike Strike\\strike0\\par}";

        RichText actual = converter.toRichText(rtf);

        assertEquals("BoldItalicRedMono UnderStrike\n", actual.toString());

        int boldPos = actual.indexOf("Bold");
        assertEquals(Boolean.TRUE, actual.runAt(boldPos).getFontDef().getBold());
        assertEquals(12f, actual.runAt(boldPos).getFontDef().getSize());

        int italicRedPos = actual.indexOf("ItalicRed");
        assertEquals(Boolean.TRUE, actual.runAt(italicRedPos).getFontDef().getItalic());
        assertEquals(Color.RED, actual.runAt(italicRedPos).getFontDef().getColor());

        int monoPos = actual.indexOf("Mono");
        assertEquals("Courier New", actual.runAt(monoPos).getFontDef().getFamily());
        assertEquals(10f, actual.runAt(monoPos).getFontDef().getSize());

        int underPos = actual.indexOf("Under");
        assertEquals(Boolean.TRUE, actual.runAt(underPos).getFontDef().getUnderline());

        int strikePos = actual.indexOf("Strike");
        assertEquals(Boolean.TRUE, actual.runAt(strikePos).getFontDef().getStrikeThrough());
        assertNotNull(actual.runAt(strikePos).getFontDef());
    }

    @Test
    void testToRichTextParsesResourceFileWithAllSupportedStyles() throws IOException {
        RtfConverter converter = RtfConverter.get().orElseThrow();
        String rtf = readResource("test.rtf");

        RichText actual = converter.toRichText(rtf);

        String expected = """
                Testdocument

                row 1
                row 2

                normal bold italic underline strikethrough
                bold+italic bold+underline bold+strikethrough
                italic+underline italic+strikethrough
                underline+strikethrough

                bold+italic+underline bold+italic+strikethrough
                bold+underline+strikethrough
                italic+underline+strikethrough

                red blue green

                Times New Roman
                Arial
                Courier New

                8.0 9.0 10.0 11.0 12.0 14.0 18.0 24.0 32.0 44.0

                """;
        assertEquals(expected, stripTrailingImagePlaceholder(actual.toString()));

        assertStyle(actual, "Testdocument", true, false, false, false);
        assertFamilyContains(actual.runAt(actual.indexOf("Testdocument")).getFontDef().getFamily(), "Times", "Liberation");
        assertEquals(18f, actual.runAt(actual.indexOf("Testdocument")).getFontDef().getSize());

        assertStyle(actual, "normal", false, false, false, false);
        assertStyle(actual, "bold", true, false, false, false);
        assertStyle(actual, "italic", false, true, false, false);
        assertStyle(actual, "underline", false, false, true, false);
        assertStyle(actual, "strikethrough", false, false, false, true);

        assertStyle(actual, "bold+italic", true, true, false, false);
        assertStyle(actual, "bold+underline", true, false, true, false);
        assertStyle(actual, "bold+strikethrough", true, false, false, true);
        assertStyle(actual, "italic+underline", false, true, true, false);
        assertStyle(actual, "italic+strikethrough", false, true, false, true);
        assertStyle(actual, "underline+strikethrough", false, false, true, true);

        assertStyle(actual, "bold+italic+underline", true, true, true, false);
        assertStyle(actual, "bold+italic+strikethrough", true, true, false, true);
        assertStyle(actual, "bold+underline+strikethrough", true, false, false, true);
        assertStyle(actual, "italic+underline+strikethrough", false, true, true, true);

        assertEquals(Color.rgb(255, 0, 0), actual.runAt(actual.indexOf("red")).getFontDef().getColor());
        assertEquals(Color.rgb(42, 96, 153), actual.runAt(actual.indexOf("blue")).getFontDef().getColor());
        assertEquals(Color.rgb(129, 212, 26), actual.runAt(actual.indexOf("green")).getFontDef().getColor());

        assertFamilyContains(actual.runAt(actual.indexOf("Times New Roman")).getFontDef().getFamily(), "Times");
        assertFamilyContains(actual.runAt(actual.indexOf("Arial")).getFontDef().getFamily(), "Arial");
        assertFamilyContains(actual.runAt(actual.indexOf("Courier New")).getFontDef().getFamily(), "Courier");

        assertEquals(8f, actual.runAt(actual.indexOf("8.0")).getFontDef().getSize());
        assertEquals(9f, actual.runAt(actual.indexOf("9.0")).getFontDef().getSize());
        assertEquals(10f, actual.runAt(actual.indexOf("10.0")).getFontDef().getSize());
        assertEquals(11f, actual.runAt(actual.indexOf("11.0")).getFontDef().getSize());
        assertEquals(12f, actual.runAt(actual.indexOf("12.0")).getFontDef().getSize());
        assertEquals(14f, actual.runAt(actual.indexOf("14.0")).getFontDef().getSize());
        assertEquals(18f, actual.runAt(actual.indexOf("18.0")).getFontDef().getSize());
        assertEquals(24f, actual.runAt(actual.indexOf("24.0")).getFontDef().getSize());
        assertEquals(32f, actual.runAt(actual.indexOf("32.0")).getFontDef().getSize());
        assertEquals(44f, actual.runAt(actual.indexOf("44.0")).getFontDef().getSize());
    }

    private static void assertStyle(RichText text, String token, Boolean bold, Boolean italic, Boolean underline, Boolean strikeThrough) {
        int index = text.indexOf(token);
        assertNotNull(text.runAt(index).getFontDef());
        assertStyleFlag(text.runAt(index).getFontDef().getBold(), bold, "bold", token);
        assertStyleFlag(text.runAt(index).getFontDef().getItalic(), italic, "italic", token);
        assertStyleFlag(text.runAt(index).getFontDef().getUnderline(), underline, "underline", token);
        assertStyleFlag(text.runAt(index).getFontDef().getStrikeThrough(), strikeThrough, "strikethrough", token);
    }

    private static void assertStyleFlag(@Nullable Boolean actual, Boolean expected, String attributeName, String token) {
        if (Boolean.TRUE.equals(expected)) {
            assertEquals(Boolean.TRUE, actual, attributeName + " style mismatch for token: " + token);
        } else {
            assertNotEquals(Boolean.TRUE, actual, attributeName + " style mismatch for token: " + token);
        }
    }

    private static String readResource(String resourceName) throws IOException {
        try (InputStream in = RtfConverterTest.class.getResourceAsStream(resourceName)) {
            assertNotNull(in, "resource not found: " + resourceName);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String stripTrailingImagePlaceholder(String s) {
        String result = s.endsWith("\nImage: ") ? s.substring(0, s.length() - "Image: ".length()) : s;
        if (result.endsWith("\n")) {
            return result;
        }
        return result + "\n";
    }

    private static void assertFamilyContains(@Nullable String actualFamily, String... expectedParts) {
        assertNotNull(actualFamily, "font family is null");
        String lowerCaseFamily = actualFamily.toLowerCase();
        for (String expectedPart : expectedParts) {
            if (lowerCaseFamily.contains(expectedPart.toLowerCase())) {
                return;
            }
        }
        assertTrue(false, "unexpected font family: " + actualFamily);
    }
}
