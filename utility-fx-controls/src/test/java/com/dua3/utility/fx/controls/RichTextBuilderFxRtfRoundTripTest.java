package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.RtfConverter;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.VAnchor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
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
            assertTrue(containsShiftedPictGroup(rtf));
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

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testTextPaneHyperlinkDispatchesToCustomHandler() throws Exception {
        runOnFxThreadAndWait(() -> {
            URI uri = URI.create("testapp://setStatus?text=Button%201%20clicked");

            RichTextBuilderFx builder = new RichTextBuilderFx();
            builder.append("Before ");
            builder.appendHyperlink("Button 1", uri);
            builder.append(" After");

            AtomicReference<URI> clickedUri = new AtomicReference<>();
            TextPane pane = new TextPane(builder.toRichText());
            pane.setWrapText(true);
            pane.setHyperlinkHandler(clickedUri::set);

            Scene scene = addToScene(pane);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            pane.applyCss();
            pane.layout();

            Node node = pane.lookup(".hyperlink");
            Hyperlink hyperlink = assertInstanceOf(Hyperlink.class, node);
            hyperlink.fire();

            assertEquals(uri, clickedUri.get());
        });
    }

    private static int controlWordValue(String rtf, String controlWord) {
        Pattern pattern = Pattern.compile("\\\\" + controlWord + "(-?\\d+)");
        Matcher matcher = pattern.matcher(rtf);
        assertTrue(matcher.find(), "missing control word: \\" + controlWord);
        return Integer.parseInt(matcher.group(1));
    }

    private static boolean containsShiftedPictGroup(String rtf) {
        Pattern pattern = Pattern.compile("\\{\\\\(?:up|dn)-?\\d+\\s+\\{\\\\pict");
        return pattern.matcher(rtf).find();
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

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testClipboardRoundTripPreservesInlineControlsAndImageScaling() throws Exception {
        runOnFxThreadAndWait(() -> {
            RichText expected = createSampleText(new Label());

            FxUtil.copyToClipboard(expected);
            RichText actual = FxUtil.getTextFromClipboard().orElseThrow();

            assertEquals(expected, actual);
            assertTrue(countInlineNodeFactoryRuns(actual) >= 3, "inline controls were not preserved on clipboard round trip");
            assertEquals(extractRawScaledInlineImageInfo(expected), extractRawScaledInlineImageInfo(actual));
        });
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRoundTripAllFeatures() throws Exception {
        runOnFxThreadAndWait(() -> {
            RtfConverter converter = RtfConverter.get().orElse(null);
            Assumptions.assumeTrue(converter != null, "RtfConverter not available");

            RichText expected = createSampleText(new Label());
            String rtf = converter.fromRichText(expected);
            RichText actual = converter.toRichText(rtf);

            assertEquals(expected.toString(), actual.toString());
            assertTrue(rtf.contains("\\pict"));

            List<ScaledInlineImageInfo> expectedScaledImages = extractExpectedScaledInlineImageInfo(expected);
            List<ScaledInlineImageInfo> actualScaledImages = extractActualScaledInlineImageInfo(actual);
            assertEquals(expectedScaledImages.size(), actualScaledImages.size(), "scaled image count mismatch");
            assertEquals(5, actualScaledImages.size(), "unexpected number of scaled images in sample text");

            for (int i = 0; i < expectedScaledImages.size(); i++) {
                ScaledInlineImageInfo expectedInfo = expectedScaledImages.get(i);
                ScaledInlineImageInfo actualInfo = actualScaledImages.get(i);

                assertEquals(expectedInfo.vAnchor(), actualInfo.vAnchor(), "vAnchor mismatch for scaled image #" + i);

                // RTF picscale values are integer percentages, so small size drift is expected.
                assertEquals(expectedInfo.maxWidth(), actualInfo.maxWidth(), 1.0, "width drift too large for scaled image #" + i);
                assertEquals(expectedInfo.maxHeight(), actualInfo.maxHeight(), 1.0, "height drift too large for scaled image #" + i);
            }
        });
    }

    private static List<ScaledInlineImageInfo> extractExpectedScaledInlineImageInfo(RichText text) {
        List<ScaledInlineImageInfo> info = new ArrayList<>();
        for (Run run : text) {
            if (run.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER) < 0) {
                continue;
            }

            for (Style style : run.getStyles()) {
                Object width = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH);
                Object height = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT);
                if (width instanceof Number w && height instanceof Number h) {
                    InlineNode<?> inlineNode = resolveInlineNode(run.toString(), style);
                    Image image = inlineNode == null ? null : decodeInlineImage(inlineNode);
                    if (image == null) {
                        continue;
                    }

                    VAnchor vAnchor = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR) instanceof VAnchor v
                            ? v
                            : VAnchor.BASELINE;
                    double[] fittedSize = computeFittedSize(image.width(), image.height(), w.doubleValue(), h.doubleValue());
                    info.add(new ScaledInlineImageInfo(fittedSize[0], fittedSize[1], vAnchor));
                    break;
                }
            }
        }
        return info;
    }

    private static List<ScaledInlineImageInfo> extractActualScaledInlineImageInfo(RichText text) {
        List<ScaledInlineImageInfo> info = new ArrayList<>();
        for (Run run : text) {
            if (run.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER) < 0) {
                continue;
            }

            for (Style style : run.getStyles()) {
                Object width = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH);
                Object height = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT);
                if (width instanceof Number w && height instanceof Number h) {
                    VAnchor vAnchor = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR) instanceof VAnchor v
                            ? v
                            : VAnchor.BASELINE;
                    info.add(new ScaledInlineImageInfo(w.doubleValue(), h.doubleValue(), vAnchor));
                    break;
                }
            }
        }
        return info;
    }

    private static List<ScaledInlineImageInfo> extractRawScaledInlineImageInfo(RichText text) {
        List<ScaledInlineImageInfo> info = new ArrayList<>();
        for (Run run : text) {
            if (run.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER) < 0) {
                continue;
            }

            for (Style style : run.getStyles()) {
                Object width = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH);
                Object height = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT);
                if (width instanceof Number w && height instanceof Number h) {
                    VAnchor vAnchor = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR) instanceof VAnchor v
                            ? v
                            : VAnchor.BASELINE;
                    info.add(new ScaledInlineImageInfo(w.doubleValue(), h.doubleValue(), vAnchor));
                    break;
                }
            }
        }
        return info;
    }

    private static int countInlineNodeFactoryRuns(RichText text) {
        int count = 0;
        for (Run run : text) {
            for (Style style : run.getStyles()) {
                if (style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY) instanceof Function<?, ?>) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static InlineNode<?> resolveInlineNode(String runText, Style style) {
        Object inline = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE);
        if (inline instanceof InlineNode<?> inlineNode) {
            return inlineNode;
        }

        Object factory = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY);
        if (factory instanceof Function<?, ?> f) {
            @SuppressWarnings("unchecked")
            Function<String, ?> fn = (Function<String, ?>) f;
            Object value = fn.apply(runText);
            if (value instanceof InlineNode<?> inlineNode) {
                return inlineNode;
            }
        }

        return null;
    }

    private static Image decodeInlineImage(InlineNode<?> inlineNode) {
        try {
            return InlineNode.decodeArgbImageData(inlineNode.getData());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static double[] computeFittedSize(double nativeWidth, double nativeHeight, double maxWidth, double maxHeight) {
        double safeWidth = Math.max(1.0, nativeWidth);
        double safeHeight = Math.max(1.0, nativeHeight);
        double scaleX = maxWidth > 0.0 ? maxWidth / safeWidth : Double.POSITIVE_INFINITY;
        double scaleY = maxHeight > 0.0 ? maxHeight / safeHeight : Double.POSITIVE_INFINITY;
        double scale = Math.min(scaleX, scaleY);
        if (!Double.isFinite(scale) || scale <= 0.0) {
            scale = 1.0;
        }
        return new double[]{safeWidth * scale, safeHeight * scale};
    }

    private record ScaledInlineImageInfo(double maxWidth, double maxHeight, VAnchor vAnchor) {}

    private static RichText createSampleText(Label status) {
        RichTextBuilderFx b = new RichTextBuilderFx();
        b.push(Style.BOLD).append("Combined TextEditorPane/TextPane demo").pop(Style.BOLD).append('\n');
        b.append("Edit in the upper pane, then press Apply to commit to InputControl state.\n");
        b.append("Reset restores the default InputControl value.\n\n");

        b.append("Inline controls: ");
        b.appendHyperlink("Hyperlink with space", URI.create("testapp://setStatus?text=Hyperlink%20clicked"));
        b.append(" followed by text, and ");
        b.appendButton("Button 1", () -> status.setText("Inline button 1 clicked"));
        b.append(" followed by text.\n\n");

        Image imageOriginal = createDemoImage(96, 48, 0xFF147BDA, 0xFF13BFA7);
        Image imageScaled = createDemoImage(240, 140, 0xFFE38C22, 0xFFE34F6A);
        float maxWidth = 120.0f;
        float maxHeight = 20.0f;

        String separator = "---------------------------------------------\n";

        b.append(separator);
        b.append("Images (default vAnchor): original ");
        b.appendImage(imageOriginal);
        b.append(" and scaled ");
        b.appendImage(imageScaled, maxWidth, maxHeight);
        b.append("\n");

        for (VAnchor vAnchor : VAnchor.values()) {
            b.append(separator);
            b.append("Images (vAnchor=").append(vAnchor.name()).append("): original ");
            b.appendImage(imageOriginal, vAnchor);
            b.append(" and scaled ");
            b.appendImage(imageScaled, maxWidth, maxHeight, vAnchor);
            b.append('\n');
        }
        b.append(separator);

        b.append("Wrap test: this sentence is intentionally long so that the inline ");
        b.appendButton("Button 2", () -> status.setText("Inline button 2 clicked"));
        b.append(" is likely moved to a new line while text before and after keeps normal spacing.\n\n");

        b.append("Long paragraph: ");
        b.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ");
        b.append("ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco ");
        b.append("laboris nisi ut aliquip ex ea commodo consequat.");
        return b.toRichText();
    }

    private static Image createDemoImage(int width, int height, int argbA, int argbB) {
        int[] data = new int[width * height];
        for (int y = 0; y < height; y++) {
            float fy = height > 1 ? (float) y / (height - 1) : 0.0f;
            for (int x = 0; x < width; x++) {
                float fx = width > 1 ? (float) x / (width - 1) : 0.0f;
                float f = 0.65f * fx + 0.35f * fy;
                data[y * width + x] = blendArgb(argbA, argbB, f);
            }
        }
        return ImageUtil.getInstance().createImage(width, height, data);
    }

    private static int blendArgb(int argbA, int argbB, float factor) {
        float f = Math.clamp(factor, 0.0f, 1.0f);
        int a = blendChannel((argbA >>> 24) & 0xFF, (argbB >>> 24) & 0xFF, f);
        int r = blendChannel((argbA >>> 16) & 0xFF, (argbB >>> 16) & 0xFF, f);
        int g = blendChannel((argbA >>> 8) & 0xFF, (argbB >>> 8) & 0xFF, f);
        int b = blendChannel(argbA & 0xFF, argbB & 0xFF, f);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int blendChannel(int a, int b, float factor) {
        return Math.clamp(Math.round(a + (b - a) * factor), 0, 255);
    }
}
