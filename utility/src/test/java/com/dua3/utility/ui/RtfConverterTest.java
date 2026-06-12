package com.dua3.utility.ui;

import com.dua3.utility.text.RichText;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RtfConverterTest {

    @Test
    @Disabled("Pending implementation of hand-written RTF writer and rtfparserkit parser")
    void testRoundTripRichText() {
        /*
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

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
        builder.append("\nSecond line");

        RichText expected = builder.toRichText();
        String rtf = rtfConverter.fromRichText(expected);
        RichText actual = rtfConverter.toRichText(rtf);

        assertEquals(expected.toString(), actual.toString());

        int boldPos = actual.indexOf("Bold");
        assertEquals(Boolean.TRUE, actual.runAt(boldPos).getFontDef().getBold());

        int italicRedPos = actual.indexOf("ItalicRed");
        assertEquals(Boolean.TRUE, actual.runAt(italicRedPos).getFontDef().getItalic());
        assertEquals(Color.RED, actual.runAt(italicRedPos).getFontDef().getColor());
 */
    }

    @Test
    @Disabled("Pending implementation of hand-written RTF writer and rtfparserkit parser")
    void testRoundTripInlineImage() {
        /*
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

        Image image = ImageUtil.getInstance().create(
                2,
                2,
                new int[]{
                        Color.RED.argb(), Color.GREEN.argb(),
                        Color.BLUE.argb(), Color.YELLOW.argb()
                }
        );

        InlineNode<Image> inlineNode = new InlineNode<>(
                image,
                InlineNode.MIME_TYPE_ARGB_IMAGE,
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

        assertEquals(InlineNode.MIME_TYPE_ARGB_IMAGE, imported.getMimeType());

        Image importedImage = InlineNode.decodeArgbImageData(imported.getData());
        assertEquals(image.width(), importedImage.width());
        assertEquals(image.height(), importedImage.height());
        assertTrue(Arrays.equals(image.getArgb(), importedImage.getArgb()));
         */
    }

    @Test
    void testToRtfNotImplementedYet() {
        /*
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

        RichText text = new RichTextBuilder().append("x").toRichText();
        assertThrows(UnsupportedOperationException.class, () -> rtfConverter.toRtf(text));
         */
    }

    @Test
    void testFromRtfNotImplementedYet() {
        /*
        RtfConverter rtfConverter = RtfConverter.get().orElse(null);
        Assumptions.assumeTrue(rtfConverter != null, "RtfConverter not available");

        assertThrows(UnsupportedOperationException.class, () -> rtfConverter.fromRtf("{\\rtf1\\ansi}"));
         */
    }

    @Test
    void testToRichTextParsesRtfText() {
        RtfConverter converter = RtfConverter.get().orElseThrow();
        RichText actual = converter.toRichText("{\\rtf1\\ansi\\deff0\\pard\\b Bold\\b0\\par Plain\\par}");

        assertEquals("Bold\nPlain\n", actual.toString());
    }
}
