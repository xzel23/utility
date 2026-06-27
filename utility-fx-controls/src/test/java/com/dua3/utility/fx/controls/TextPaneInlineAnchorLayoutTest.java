package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Image;
import com.dua3.utility.data.ImageUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.ui.RichTextPaneLayoutHelper;
import com.dua3.utility.ui.VAnchor;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TextPaneInlineAnchorLayoutTest extends FxTestBase {

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testTextPaneInlineAnchorsUseLineMetricsOnMixedFontLines() throws Exception {
        runOnFxThreadAndWait(() -> assertInlineAnchorsUseLineMetrics(new TextPane(createMixedFontInlineAnchorText())));
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testTextEditorPaneInlineAnchorsUseLineMetricsOnMixedFontLines() throws Exception {
        runOnFxThreadAndWait(() -> assertInlineAnchorsUseLineMetrics(new TextEditorPane(createMixedFontInlineAnchorText())));
    }

    private static void assertInlineAnchorsUseLineMetrics(TextPane control) {
        control.setWrapText(false);
        control.setPrefWidth(640);
        control.setMinWidth(640);
        control.setMaxWidth(640);

        addToScene(control);

        RichTextPaneLayoutHelper.Layout<?> layout = control.createLayout(control.getWidth());
        List<LineMetrics> lineMetrics = computeLineMetrics(layout.renderLines());
        List<?> placements = layout.placements();

        assertEquals(VAnchor.values().length, placements.size(), "expected one inline node placement per vAnchor");

        EnumSet<VAnchor> anchorsSeen = EnumSet.noneOf(VAnchor.class);
        for (Object placement : placements) {
            Node node = (Node) placementValue(placement, "node");
            VAnchor anchor = (VAnchor) placementValue(placement, "vAnchor");
            anchorsSeen.add(anchor);

            double lineTop = asDouble(placementValue(placement, "y"));
            LineMetrics metrics = findLineMetrics(lineMetrics, lineTop);
            assertNotNull(metrics, "unable to resolve line metrics for inline node");

            node.applyCss();
            node.autosize();
            double nodeHeight = Math.max(node.prefHeight(-1), node.getLayoutBounds().getHeight());
            double baselineOffset = node.getBaselineOffset();
            double actualY = computeInlineNodeY(placement, nodeHeight, baselineOffset);

            double configuredDescent = asDouble(placementValue(placement, "descent"));
            double inlineDescent = Double.isFinite(configuredDescent) ? Math.max(0.0, configuredDescent) : 0.0;
            double expectedY = switch (anchor) {
                case TOP -> metrics.top();
                case BOTTOM -> metrics.bottom() - nodeHeight;
                case MIDDLE -> metrics.center() - nodeHeight / 2.0;
                case BASELINE -> metrics.baseline() - (nodeHeight - inlineDescent);
            };

            assertEquals(expectedY, actualY, 0.75, "incorrect inline node y for vAnchor=" + anchor);
        }

        assertEquals(EnumSet.allOf(VAnchor.class), anchorsSeen, "missing inline node anchor(s)");
    }

    private static RichText createMixedFontInlineAnchorText() {
        RichTextBuilderFx builder = new RichTextBuilderFx();
        Style big = Style.create("big-inline-anchor-test", Map.entry(Style.FONT_SIZE, 30f));
        Image image = createTestImage(32, 18);

        VAnchor[] anchors = VAnchor.values();
        for (int i = 0; i < anchors.length; i++) {
            VAnchor anchor = anchors[i];
            builder.append("line ").append(anchor.name()).append(": ");
            builder.push(big).append("BIG").pop(big);
            builder.append(" inline ");
            builder.appendImage(image, 28f, 14f, anchor);
            builder.append(" tail");
            if (i < anchors.length - 1) {
                builder.append('\n');
            }
        }

        return builder.toRichText();
    }

    private static Image createTestImage(int width, int height) {
        int[] argb = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean upperLeft = x < width / 2 && y < height / 2;
                argb[y * width + x] = upperLeft ? 0xFF1E88E5 : 0xFF43A047;
            }
        }
        return ImageUtil.getInstance().createImage(width, height, argb);
    }

    private static List<LineMetrics> computeLineMetrics(List<List<FragmentedText.Fragment>> lines) {
        List<LineMetrics> metrics = new ArrayList<>();
        for (List<FragmentedText.Fragment> line : lines) {
            if (line.isEmpty()) {
                continue;
            }

            double top = line.getFirst().y();
            double bottom = line.stream()
                    .mapToDouble(fragment -> fragment.y() + fragment.h())
                    .max()
                    .orElse(top);

            double ascent = line.stream()
                    .filter(fragment -> !isInvisibleInlinePlaceholder(fragment))
                    .mapToDouble(fragment -> fragment.font().getAscent())
                    .max()
                    .orElseGet(() -> line.stream()
                            .mapToDouble(fragment -> fragment.font().getAscent())
                            .max()
                            .orElse(0.0));

            metrics.add(new LineMetrics(top, bottom, top + ascent));
        }
        return metrics;
    }

    private static @Nullable LineMetrics findLineMetrics(List<LineMetrics> lines, double top) {
        LineMetrics best = null;
        double bestDelta = Double.MAX_VALUE;
        for (LineMetrics metrics : lines) {
            double delta = Math.abs(metrics.top() - top);
            if (delta < bestDelta) {
                bestDelta = delta;
                best = metrics;
            }
        }
        return bestDelta <= 0.5 ? best : null;
    }

    private static boolean isInvisibleInlinePlaceholder(FragmentedText.Fragment fragment) {
        if (!(fragment.text() instanceof Run run)) {
            return false;
        }
        if (run.toString().indexOf(RichTextBuilderExtBase.INLINE_NODE_MARKER) < 0) {
            return false;
        }
        for (Style style : run.getStyles()) {
            if (style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY) != null
                    || style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE) != null) {
                return true;
            }
        }
        return false;
    }

    private static Object placementValue(Object placement, String accessorName) {
        try {
            Method method = placement.getClass().getDeclaredMethod(accessorName);
            method.setAccessible(true);
            return method.invoke(placement);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("failed to read inline placement accessor: " + accessorName, ex);
        }
    }

    private static double computeInlineNodeY(Object placement, double prefH, double baselineOffset) {
        try {
            Method method = TextPane.class.getDeclaredMethod("computeInlineNodeY", placement.getClass(), double.class, double.class);
            method.setAccessible(true);
            return (double) method.invoke(null, placement, prefH, baselineOffset);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError("failed to invoke TextPane.computeInlineNodeY", ex);
        }
    }

    private static double asDouble(Object value) {
        return ((Number) value).doubleValue();
    }

    private record LineMetrics(double top, double bottom, double baseline) {
        double center() {
            return (top + bottom) / 2.0;
        }
    }
}
