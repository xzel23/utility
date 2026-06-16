package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.RtfConverter;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.VAnchor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RichTextBuilderFxRtfRoundTripTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRoundTripInlineImageViaRtf() throws Exception {
        runOnFxThreadAndWait(() -> {
            RtfConverter converter = RtfConverter.get().orElse(null);
            Assumptions.assumeTrue(converter != null, "RtfConverter not available");

            Image image = ImageUtil.getInstance().createImage(
                    2,
                    2,
                    new int[]{
                            Color.RED.argb(), Color.GREEN.argb(),
                            Color.BLUE.argb(), Color.YELLOW.argb()
                    }
            );

            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("A");
            builder.appendImage(image);
            builder.append("B");
            RichText expected = builder.toRichText();

            String rtf = converter.fromRichText(expected);
            RichText actual = converter.toRichText(rtf);

            assertEquals(expected.toString(), actual.toString());
            assertTrue(rtf.contains("\\pict"));

            int inlinePos = actual.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER);
            assertTrue(inlinePos >= 0, "missing inline node marker after RTF round trip");
            Run run = actual.runAt(inlinePos);

            Object inlineAttribute = run.getStyles().stream()
                    .map(style -> style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow();

            InlineNode<?> imported = assertInstanceOf(InlineNode.class, inlineAttribute);
            Image importedImage = InlineNode.decodeArgbImageData(imported.getData());

            assertEquals(image.width(), importedImage.width());
            assertEquals(image.height(), importedImage.height());
            assertTrue(Arrays.equals(image.getArgb(), importedImage.getArgb()));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRoundTripScaledInlineImageViaRtf() throws Exception {
        runOnFxThreadAndWait(() -> {
            RtfConverter converter = RtfConverter.get().orElse(null);
            Assumptions.assumeTrue(converter != null, "RtfConverter not available");

            Image image = ImageUtil.getInstance().createImage(
                    4,
                    2,
                    new int[]{
                            Color.RED.argb(), Color.GREEN.argb(), Color.BLUE.argb(), Color.YELLOW.argb(),
                            Color.YELLOW.argb(), Color.BLUE.argb(), Color.GREEN.argb(), Color.RED.argb()
                    }
            );

            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("A");
            builder.appendImage(image, 6f, 6f, VAnchor.TOP);
            builder.append("B");
            RichText expected = builder.toRichText();

            String rtf = converter.fromRichText(expected);
            RichText actual = converter.toRichText(rtf);

            assertEquals(expected.toString(), actual.toString());
            assertTrue(rtf.contains("\\pict"));
            assertTrue(rtf.contains("\\picscalex"));
            assertTrue(rtf.contains("\\picscaley"));
            assertTrue(rtf.contains("\\up") || rtf.contains("\\dn"));
            assertEquals(4, controlWordValue(rtf, "picw"));
            assertEquals(2, controlWordValue(rtf, "pich"));
            assertEquals(60, controlWordValue(rtf, "picwgoal"));
            assertEquals(30, controlWordValue(rtf, "pichgoal"));
            assertEquals(150, controlWordValue(rtf, "picscalex"));
            assertEquals(150, controlWordValue(rtf, "picscaley"));

            int inlinePos = actual.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER);
            assertTrue(inlinePos >= 0, "missing inline node marker after RTF round trip");
            Run run = actual.runAt(inlinePos);

            Style style = run.getStyles().stream()
                    .filter(s -> s.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE) != null)
                    .findFirst()
                    .orElseThrow();

            Object inlineAttribute = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE);
            InlineNode<?> imported = assertInstanceOf(InlineNode.class, inlineAttribute);
            Image importedImage = InlineNode.decodeArgbImageData(imported.getData());
            assertEquals(image.width(), importedImage.width());
            assertEquals(image.height(), importedImage.height());
            assertTrue(Arrays.equals(image.getArgb(), importedImage.getArgb()));

            Object vAnchor = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR);
            assertEquals(VAnchor.TOP, vAnchor);

            Number maxWidth = assertInstanceOf(Number.class, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH));
            Number maxHeight = assertInstanceOf(Number.class, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT));
            assertNotNull(maxWidth);
            assertNotNull(maxHeight);
            assertEquals(6.0, maxWidth.doubleValue(), 0.25);
            assertEquals(3.0, maxHeight.doubleValue(), 0.25);
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testImportedInlineImageScalingAppliedByTextPane() throws Exception {
        runOnFxThreadAndWait(() -> {
            RtfConverter converter = RtfConverter.get().orElse(null);
            Assumptions.assumeTrue(converter != null, "RtfConverter not available");

            Image image = ImageUtil.getInstance().createImage(
                    4,
                    2,
                    new int[]{
                            Color.RED.argb(), Color.GREEN.argb(), Color.BLUE.argb(), Color.YELLOW.argb(),
                            Color.YELLOW.argb(), Color.BLUE.argb(), Color.GREEN.argb(), Color.RED.argb()
                    }
            );

            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("A");
            builder.appendImage(image, 6f, 6f, VAnchor.TOP);
            builder.append("B");

            RichText imported = converter.toRichText(converter.fromRichText(builder.toRichText()));
            int inlinePos = imported.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER);
            assertTrue(inlinePos >= 0, "missing inline node marker after RTF round trip");
            Run run = imported.runAt(inlinePos);

            Style style = run.getStyles().stream()
                    .filter(s -> s.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE) != null)
                    .findFirst()
                    .orElseThrow();

            double expectedWidth = assertInstanceOf(Number.class, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH)).doubleValue();
            double expectedHeight = assertInstanceOf(Number.class, style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT)).doubleValue();

            ImageView imageView = assertInstanceOf(ImageView.class, createInlineNode(run));
            assertTrue(imageView.isPreserveRatio());
            assertEquals(expectedWidth, imageView.getFitWidth(), 0.25);
            assertEquals(expectedHeight, imageView.getFitHeight(), 0.25);
        });
    }

    private static int controlWordValue(String rtf, String controlWord) {
        Pattern pattern = Pattern.compile("\\\\" + controlWord + "(-?\\d+)");
        Matcher matcher = pattern.matcher(rtf);
        assertTrue(matcher.find(), "missing control word: \\" + controlWord);
        return Integer.parseInt(matcher.group(1));
    }

    private static Node createInlineNode(Run run) {
        try {
            Method method = TextPane.class.getDeclaredMethod("createInlineNode", Run.class);
            method.setAccessible(true);
            Object value = method.invoke(null, run);
            assertNotNull(value);
            return assertInstanceOf(Node.class, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("failed to create inline node", ex);
        }
    }
}
