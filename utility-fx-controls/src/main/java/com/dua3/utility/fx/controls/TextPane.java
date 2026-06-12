package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.ui.HAnchor;
import com.dua3.utility.ui.RichTextBuilderExtBase;
import com.dua3.utility.ui.VAnchor;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A read-only JavaFX control for displaying {@link RichText}.
 *
 * <p>The control renders text using {@link FxGraphics#renderText(Vector2f, RichText, HAnchor, VAnchor, Alignment, VerticalAlignment, Dimension2f, Graphics.TextWrapping)}
 * so line breaking and wrapping match the Utility text layout implementation.
 *
 * <p>Inline controls can be embedded by assigning styles containing
 * {@link RichTextBuilderExtBase#STYLE_ATTRIBUTE_INLINE_NODE_FACTORY} to a run.
 */
public class TextPane extends Control {

    protected static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();

    private static final String NO_BREAK_SPACE = "\u00A0";
    private static final String STYLE_LIST_ATTRIBUTE = "__styles";
    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT = TextPane.class.getName() + ".inlineReferenceAscent";
    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_DESCENT = TextPane.class.getName() + ".inlineReferenceDescent";
    private static final String STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH = TextPane.class.getName() + ".inlineLeadingWidth";
    private static final String DEFAULT_STYLE_CLASS = "text-pane";
    private static final Style STYLE_INVISIBLE_TEXT = Style.create(
            "text-pane-invisible",
            Map.entry(Style.COLOR, Color.TRANSPARENT_BLACK)
    );

    private final ObjectProperty<RichText> text = new SimpleObjectProperty<>(this, "text", RichText.emptyText());
    private final BooleanProperty wrapText = new SimpleBooleanProperty(this, "wrapText", false);
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "font", FONT_UTIL.getDefaultFont());

    /**
     * Create an empty {@code TextPane}.
     */
    public TextPane() {
        getStyleClass().setAll("text-input", "text-area", DEFAULT_STYLE_CLASS);
    }

    /**
     * Create a {@code TextPane} with initial text.
     *
     * @param text the initial text
     */
    public TextPane(@Nullable CharSequence text) {
        this();
        setText(text);
    }

    /**
     * Create a style that renders the run as an inline JavaFX node.
     *
     * @param styleName style name
     * @param nodeFactory node factory receiving the run text
     * @return style carrying the inline node factory
     */
    public static Style inlineNodeStyle(String styleName, Function<String, ? extends Node> nodeFactory) {
        return Style.create(styleName, Map.entry(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY, nodeFactory));
    }

    /**
     * Create a style for an inline {@link Hyperlink}.
     *
     * @param styleName style name
     * @param action action handler
     * @return style carrying an inline hyperlink factory
     */
    public static Style hyperlinkStyle(String styleName, EventHandler<ActionEvent> action) {
        return inlineNodeStyle(styleName, text -> {
            Hyperlink hyperlink = new Hyperlink(text);
            hyperlink.setOnAction(action);
            return hyperlink;
        });
    }

    /**
     * Create a style for an inline {@link Button}.
     *
     * @param styleName style name
     * @param action action handler
     * @return style carrying an inline button factory
     */
    public static Style buttonStyle(String styleName, EventHandler<ActionEvent> action) {
        return inlineNodeStyle(styleName, text -> {
            Button button = new Button(text);
            button.setOnAction(action);
            return button;
        });
    }

    /**
     * Returns the text property.
     *
     * @return text property
     */
    public final ObjectProperty<RichText> textProperty() {
        return text;
    }

    /**
     * Returns the rich text.
     *
     * @return text
     */
    public final RichText getText() {
        return text.get();
    }

    /**
     * Set rich text.
     *
     * @param value text or {@code null} for empty text
     */
    public final void setText(@Nullable CharSequence value) {
        text.set(value == null ? RichText.emptyText() : RichText.valueOf(value));
    }

    /**
     * Returns the wrap-text property.
     *
     * @return wrap-text property
     */
    public final BooleanProperty wrapTextProperty() {
        return wrapText;
    }

    /**
     * Returns whether wrapping is enabled.
     *
     * @return true if wrapping is enabled
     */
    public final boolean isWrapText() {
        return wrapText.get();
    }

    /**
     * Set wrapping mode.
     *
     * @param value true to wrap text to available width
     */
    public final void setWrapText(boolean value) {
        wrapText.set(value);
    }

    /**
     * Returns the font property used for rendering.
     *
     * @return font property
     */
    public final ObjectProperty<Font> fontProperty() {
        return font;
    }

    /**
     * Returns the rendering font (JavaFX Font).
     *
     * @return font
     */
    public final javafx.scene.text.Font getFxFont() {
        return FONT_UTIL.convert(getFont());
    }

    /**
     * Set the rendering font.
     *
     * @param value font
     */
    public final void setFont(Font value) {
        font.set(value);
    }

    /**
     * Set the rendering font.
     *
     * @param value font
     */
    public final void setFxFont(javafx.scene.text.Font value) {
        font.set(FONT_UTIL.convert(value));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextPaneSkin(this);
    }

    @Override
    protected double computePrefWidth(double height) {
        double textWidth = getFont().getFontData().spaceWidth() * 40.0;
        return snappedLeftInset() + Math.ceil(textWidth) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = width > 0
                ? Math.max(1.0, width - snappedLeftInset() - snappedRightInset())
                : Math.ceil(getFont().getFontData().spaceWidth() * 40.0f);
        Layout layout = createLayout(contentWidth);
        double pref = snappedTopInset() + Math.ceil(layout.height()) + snappedBottomInset();
        return clampToMaxHeight(pref);
    }

    @Override
    protected double computeMinHeight(double width) {
        Font font = getFont();
        double min = snappedTopInset() + Math.ceil(font.getFontData().height()) + snappedBottomInset();
        return clampToMaxHeight(min);
    }

    private double clampToMaxHeight(double value) {
        double max = getMaxHeight();
        if (!Double.isNaN(max) && max >= 0 && max < Double.MAX_VALUE && max != USE_COMPUTED_SIZE) {
            return Math.min(value, max);
        }
        return value;
    }

    public Font getFont() {
        return font.get();
    }

    Layout createLayout(double availableWidth) {
        RichText richText = getText();
        Font font = getFont();
        com.dua3.utility.text.FontUtil fontUtil = com.dua3.utility.text.FontUtil.getInstance();
        LayoutTextData layoutTextData = createLayoutTextData(richText, font, fontUtil);
        RichText layoutText = layoutTextData.text();

        float width = (float) Math.max(1.0, availableWidth);
        float wrapWidth = isWrapText() ? width : FragmentedText.NO_WRAP;
        FragmentedText layoutFragments = FragmentedText.generateFragments(
                layoutText,
                fontUtil,
                font,
                width,
                Float.MAX_VALUE,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                HAnchor.LEFT,
                VAnchor.TOP,
                wrapWidth
                );

        RichText renderedText = createRenderedText(layoutText);
        FragmentedText renderFragments = FragmentedText.generateFragments(
                renderedText,
                fontUtil,
                font,
                width,
                Float.MAX_VALUE,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                HAnchor.LEFT,
                VAnchor.TOP,
                wrapWidth
        );
        float renderWidth = isWrapText() ? width : Math.max(width, renderFragments.actualWidth());

        List<InlineControlPlacement> placements = new ArrayList<>();
        for (List<FragmentedText.Fragment> line : layoutFragments.lines()) {
            for (FragmentedText.Fragment fragment : line) {
                if (fragment.text() instanceof Run run) {
                    Node node = createInlineNode(run);
                    if (node != null) {
                        VAnchor vAnchor = getInlineNodeVAnchor(run);
                        double refAscent = getInlineReferenceAscent(run, fragment.font());
                        double refDescent = getInlineReferenceDescent(run, fragment.font());
                        float baselineY = (float) (fragment.y() + refAscent);
                        double descent = getInlineNodeDescent(run);
                        double leadingWidth = getInlineLeadingWidth(run);
                        placements.add(new InlineControlPlacement(
                                node,
                                (float) (fragment.x() - leadingWidth),
                                fragment.y(),
                                fragment.w(),
                                fragment.h(),
                                baselineY,
                                fragment.font(),
                                vAnchor,
                                refAscent,
                                refDescent,
                                descent
                        ));
                    }
                }
            }
        }

        LineShiftData lineShiftData = computeLineShifts(renderFragments, placements);
        Map<Float, Float> lineShiftByY = lineShiftData.lineShiftByY();
        List<InlineControlPlacement> shiftedPlacements = shiftPlacements(placements, lineShiftByY);
        List<List<FragmentedText.Fragment>> shiftedRenderLines = shiftRenderLines(renderFragments, lineShiftByY);
        float renderHeight = computeRenderedHeight(shiftedRenderLines, lineShiftData.tailOverflowBelow(), font);

        return new Layout(shiftedRenderLines, shiftedPlacements, renderWidth, renderHeight, layoutTextData);
    }

    private static LayoutTextData createLayoutTextData(RichText source, Font baseFont, com.dua3.utility.text.FontUtil fontUtil) {
        RichTextBuilder builder = new RichTextBuilder(source.length());
        List<Integer> layoutToSourceBoundaries = new ArrayList<>(source.length() + 1);
        layoutToSourceBoundaries.add(0);
        int sourcePosition = 0;

        for (Run run : source) {
            List<String> pushedAttributes = pushNonStyleAttributes(builder, run);
            List<Style> styles = run.getStyles();
            styles.forEach(builder::push);
            if (hasInlineNode(run)) {
                Node node = createInlineNode(run);
                if (node != null) {
                    Font runFont = fontUtil.deriveFont(baseFont, run.getFontDef());
                    if (node instanceof Labeled labeled) {
                        labeled.setFont(FxFontUtil.getInstance().convert(runFont));
                    }
                    double controlWidth = measureNodeWidth(node);
                    String text = run.toString();
                    double markerWidth = fontUtil.getTextDimension(run, runFont).width();
                    double extraWidth = controlWidth - markerWidth;
                    Style leadingWidthStyle = null;
                    if (extraWidth > 0.5) {
                        double spaceWidth = Math.max(1.0, runFont.getFontData().spaceWidth());
                        int extraSpaces = (int) Math.ceil(extraWidth / spaceWidth);
                        if (extraSpaces > 0) {
                            String leadingPadding = NO_BREAK_SPACE.repeat(extraSpaces);
                            builder.append(leadingPadding);
                            for (int i = 0; i < leadingPadding.length(); i++) {
                                layoutToSourceBoundaries.add(sourcePosition);
                            }
                            double leadingWidth = fontUtil.getTextWidth(leadingPadding, runFont);
                            leadingWidthStyle = Style.create(
                                    "text-pane-inline-leading-width",
                                    Map.entry(STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH, leadingWidth)
                            );
                        }
                    }
                    if (leadingWidthStyle != null) {
                        builder.push(leadingWidthStyle);
                    }
                    builder.append(text);
                    for (int i = 0; i < text.length(); i++) {
                        layoutToSourceBoundaries.add(++sourcePosition);
                    }
                    if (leadingWidthStyle != null) {
                        builder.pop(leadingWidthStyle);
                    }
                } else {
                    String text = run.toString();
                    builder.append(text);
                    for (int i = 0; i < text.length(); i++) {
                        layoutToSourceBoundaries.add(++sourcePosition);
                    }
                }
            } else {
                String text = run.toString();
                builder.append(text);
                for (int i = 0; i < text.length(); i++) {
                    layoutToSourceBoundaries.add(++sourcePosition);
                }
            }

            for (int i = styles.size() - 1; i >= 0; i--) {
                builder.pop(styles.get(i));
            }
            for (int i = pushedAttributes.size() - 1; i >= 0; i--) {
                builder.pop(pushedAttributes.get(i));
            }
        }

        RichText layoutText = builder.toRichText();
        int layoutLength = layoutText.length();
        int[] layoutToSourceMap = new int[layoutLength + 1];
        int count = Math.min(layoutToSourceBoundaries.size(), layoutLength + 1);
        for (int i = 0; i < count; i++) {
            layoutToSourceMap[i] = Math.clamp(layoutToSourceBoundaries.get(i), 0, source.length());
        }
        for (int i = count; i < layoutToSourceMap.length; i++) {
            layoutToSourceMap[i] = source.length();
        }

        int[] sourceToLayoutMap = buildSourceToLayoutMap(layoutToSourceMap, source.length());
        return new LayoutTextData(layoutText, layoutToSourceMap, sourceToLayoutMap);
    }

    private static int[] buildSourceToLayoutMap(int[] layoutToSourceMap, int sourceLength) {
        int[] sourceToLayout = new int[sourceLength + 1];
        Arrays.fill(sourceToLayout, -1);

        for (int layoutPos = 0; layoutPos < layoutToSourceMap.length; layoutPos++) {
            int sourcePos = Math.clamp(layoutToSourceMap[layoutPos], 0, sourceLength);
            if (sourceToLayout[sourcePos] < 0) {
                sourceToLayout[sourcePos] = layoutPos;
            }
        }

        int lastLayoutPos = 0;
        for (int sourcePos = 0; sourcePos < sourceToLayout.length; sourcePos++) {
            if (sourceToLayout[sourcePos] < 0) {
                sourceToLayout[sourcePos] = lastLayoutPos;
            } else {
                lastLayoutPos = sourceToLayout[sourcePos];
            }
        }

        return sourceToLayout;
    }

    private static double measureNodeWidth(Node node) {
        if (node.getScene() == null) {
            Pane tempRoot = new Pane(node);
            // Attach temporarily so CSS/skin-dependent preferred sizes are available.
            new Scene(tempRoot);
            tempRoot.applyCss();
            node.applyCss();
            node.autosize();
            double width = Math.max(node.prefWidth(-1), node.getLayoutBounds().getWidth());
            tempRoot.getChildren().clear();
            return width;
        }
        node.applyCss();
        node.autosize();
        return Math.max(node.prefWidth(-1), node.getLayoutBounds().getWidth());
    }

    private static double measureNodeHeight(Node node) {
        if (node.getScene() == null) {
            Pane tempRoot = new Pane(node);
            new Scene(tempRoot);
            tempRoot.applyCss();
            node.applyCss();
            node.autosize();
            double height = Math.max(node.prefHeight(-1), node.getLayoutBounds().getHeight());
            tempRoot.getChildren().clear();
            return height;
        }
        node.applyCss();
        node.autosize();
        return Math.max(node.prefHeight(-1), node.getLayoutBounds().getHeight());
    }

    private static RichText createRenderedText(RichText source) {
        RichTextBuilder builder = new RichTextBuilder(source.length());
        for (Run run : source) {
            List<String> pushedAttributes = pushNonStyleAttributes(builder, run);
            List<Style> styles = run.getStyles();
            styles.forEach(builder::push);
            if (hasInlineNode(run)) {
                builder.push(STYLE_INVISIBLE_TEXT);
                builder.append(run.toString());
                builder.pop(STYLE_INVISIBLE_TEXT);
            } else {
                builder.append(run.toString());
            }
            for (int i = styles.size() - 1; i >= 0; i--) {
                builder.pop(styles.get(i));
            }
            for (int i = pushedAttributes.size() - 1; i >= 0; i--) {
                builder.pop(pushedAttributes.get(i));
            }
        }
        return builder.toRichText();
    }

    private static List<String> pushNonStyleAttributes(RichTextBuilder builder, Run run) {
        List<String> pushedAttributes = new ArrayList<>();
        for (Map.Entry<String, @Nullable Object> entry : run.attributes().entrySet()) {
            if (STYLE_LIST_ATTRIBUTE.equals(entry.getKey()) || entry.getValue() == null) {
                continue;
            }
            builder.push(entry.getKey(), entry.getValue());
            pushedAttributes.add(entry.getKey());
        }
        return pushedAttributes;
    }

    private static boolean hasInlineNode(Run run) {
        for (Style style : run.getStyles()) {
            if (style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY) != null) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable Node createInlineNode(Run run) {
        if (TextUtil.isWhitespaceOnly(run)) {
            return null;
        }
        String text = run.toString();
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);

            Object factory = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY);
            if (factory instanceof Function<?, ?> f) {
                @SuppressWarnings("unchecked")
                Function<String, ? extends Node> fn = (Function<String, ? extends Node>) f;
                return fn.apply(text);
            }
        }
        return null;
    }

    private static VAnchor getInlineNodeVAnchor(Run run) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object anchor = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR);
            if (anchor instanceof VAnchor vAnchor) {
                return vAnchor;
            }
        }
        return VAnchor.BASELINE;
    }

    private static double getInlineReferenceAscent(Run run, Font fallbackFont) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object value = style.get(STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT);
            if (value instanceof Number n) {
                return n.doubleValue();
            }
        }
        return fallbackFont.getAscent();
    }

    private static double getInlineReferenceDescent(Run run, Font fallbackFont) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object value = style.get(STYLE_ATTRIBUTE_INLINE_REFERENCE_DESCENT);
            if (value instanceof Number n) {
                return n.doubleValue();
            }
        }
        return fallbackFont.getDescent();
    }

    private static double getInlineNodeDescent(Run run) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object value = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT);
            if (value instanceof Number n) {
                return n.doubleValue();
            }
        }
        return Double.NaN;
    }

    private static double getInlineLeadingWidth(Run run) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object value = style.get(STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH);
            if (value instanceof Number n) {
                return Math.max(0.0, n.doubleValue());
            }
        }
        return 0.0;
    }

    private static double computeInlineDescent(InlineControlPlacement placement, double prefH, double baselineOffset) {
        return Double.isFinite(placement.descent())
                ? placement.descent()
                : (baselineOffset != Node.BASELINE_OFFSET_SAME_AS_HEIGHT && Double.isFinite(baselineOffset)
                        ? Math.max(0.0, prefH - baselineOffset)
                        : 0.0);
    }

    private static double computeInlineNodeY(InlineControlPlacement placement, double prefH, double baselineOffset) {
        double lineTop = placement.y();
        double lineBottom = placement.y() + placement.h();
        double inlineDescent = computeInlineDescent(placement, prefH, baselineOffset);
        return switch (placement.vAnchor()) {
            case TOP -> lineTop;
            case BOTTOM -> lineBottom - prefH;
            case MIDDLE -> {
                double textCenterY = placement.baselineY() + (placement.referenceDescent() - placement.referenceAscent()) / 2.0;
                yield textCenterY - prefH / 2.0;
            }
            case BASELINE -> placement.baselineY() - (prefH - inlineDescent);
        };
    }

    private static LineShiftData computeLineShifts(
            FragmentedText renderFragments,
            List<InlineControlPlacement> placements
    ) {
        Map<Float, Float> overflowAboveByLineY = new java.util.HashMap<>();
        Map<Float, Float> overflowBelowByLineY = new java.util.HashMap<>();
        for (InlineControlPlacement placement : placements) {
            Node node = placement.node();
            node.applyCss();
            node.autosize();
            double prefH = Math.max(node.prefHeight(-1), node.getLayoutBounds().getHeight());
            double baselineOffset = node.getBaselineOffset();
            double nodeY = computeInlineNodeY(placement, prefH, baselineOffset);
            double nodeBottom = nodeY + prefH;
            float overflowAbove = (float) Math.max(0.0, placement.y() - nodeY);
            float overflowBelow = (float) Math.max(0.0, nodeBottom - (placement.y() + placement.h()));
            if (overflowAbove > 0.0f) {
                overflowAboveByLineY.merge(placement.y(), overflowAbove, Math::max);
            }
            if (overflowBelow > 0.0f) {
                overflowBelowByLineY.merge(placement.y(), overflowBelow, Math::max);
            }
        }

        Map<Float, Float> lineShiftByY = new java.util.HashMap<>();
        float cumulativeShift = 0.0f;
        float lastLineShift = 0.0f;
        for (List<FragmentedText.Fragment> line : renderFragments.lines()) {
            if (line.isEmpty()) {
                continue;
            }
            float lineY = line.getFirst().y();
            cumulativeShift += overflowAboveByLineY.getOrDefault(lineY, 0.0f);
            lineShiftByY.put(lineY, cumulativeShift);
            lastLineShift = cumulativeShift;
            cumulativeShift += overflowBelowByLineY.getOrDefault(lineY, 0.0f);
        }
        float tailOverflowBelow = Math.max(0.0f, cumulativeShift - lastLineShift);
        return new LineShiftData(lineShiftByY, tailOverflowBelow);
    }

    private static List<InlineControlPlacement> shiftPlacements(
            List<InlineControlPlacement> placements,
            Map<Float, Float> lineShiftByY
    ) {
        if (lineShiftByY.isEmpty()) {
            return placements;
        }

        List<InlineControlPlacement> shifted = new ArrayList<>(placements.size());
        for (InlineControlPlacement placement : placements) {
            float dy = lineShiftByY.getOrDefault(placement.y(), 0.0f);
            shifted.add(new InlineControlPlacement(
                    placement.node(),
                    placement.x(),
                    placement.y() + dy,
                    placement.w(),
                    placement.h(),
                    placement.baselineY() + dy,
                    placement.font(),
                    placement.vAnchor(),
                    placement.referenceAscent(),
                    placement.referenceDescent(),
                    placement.descent()
            ));
        }
        return shifted;
    }

    private static List<List<FragmentedText.Fragment>> shiftRenderLines(
            FragmentedText fragments,
            Map<Float, Float> lineShiftByY
    ) {
        if (lineShiftByY.isEmpty()) {
            return fragments.lines();
        }

        List<List<FragmentedText.Fragment>> shiftedLines = new ArrayList<>(fragments.lines().size());
        for (List<FragmentedText.Fragment> line : fragments.lines()) {
            if (line.isEmpty()) {
                shiftedLines.add(List.of());
                continue;
            }
            float lineY = line.getFirst().y();
            float dy = lineShiftByY.getOrDefault(lineY, 0.0f);
            if (dy == 0.0f) {
                shiftedLines.add(line);
                continue;
            }
            List<FragmentedText.Fragment> shiftedLine = new ArrayList<>(line.size());
            for (FragmentedText.Fragment fragment : line) {
                shiftedLine.add(new FragmentedText.Fragment(
                        fragment.x(),
                        fragment.y() + dy,
                        fragment.w(),
                        fragment.h(),
                        fragment.baseLine(),
                        fragment.font(),
                        fragment.text()
                ));
            }
            shiftedLines.add(shiftedLine);
        }
        return shiftedLines;
    }

    private static float computeRenderedHeight(List<List<FragmentedText.Fragment>> lines, float tailOverflowBelow, Font fallbackFont) {
        float maxBottom = 0.0f;
        for (List<FragmentedText.Fragment> line : lines) {
            for (FragmentedText.Fragment fragment : line) {
                maxBottom = Math.max(maxBottom, fragment.y() + fragment.h());
            }
        }
        return (float) Math.max(fallbackFont.getFontData().height(), maxBottom + Math.max(0.0f, tailOverflowBelow));
    }

    private record InlineControlPlacement(
            Node node,
            float x,
            float y,
            float w,
            float h,
            float baselineY,
            Font font,
            VAnchor vAnchor,
            double referenceAscent,
            double referenceDescent,
            double descent
    ) {}

    record LayoutTextData(
            RichText text,
            int[] layoutToSourceMap,
            int[] sourceToLayoutMap
    ) {
        int layoutToSourcePosition(int layoutPosition) {
            int pos = Math.clamp(layoutPosition, 0, layoutToSourceMap.length - 1);
            return layoutToSourceMap[pos];
        }

        int sourceToLayoutPosition(int sourcePosition) {
            int pos = Math.clamp(sourcePosition, 0, sourceToLayoutMap.length - 1);
            return sourceToLayoutMap[pos];
        }
    }

    record Layout(
            List<List<FragmentedText.Fragment>> renderLines,
            List<InlineControlPlacement> placements,
            float width,
            float height,
            LayoutTextData layoutTextData
    ) {}

    private record LineShiftData(Map<Float, Float> lineShiftByY, float tailOverflowBelow) {}

    private static final class TextPaneSkin extends SkinBase<TextPane> {
        private static final double CARET_AUTOSCROLL_MARGIN = 6.0;
        private static final double DRAG_AUTOSCROLL_EDGE = 18.0;
        private static final double DRAG_AUTOSCROLL_TICK_MS = 40.0;
        private static final SequencedCollection<String> AVAILABLE_FONTS = FxFontUtil.getInstance().getFamilies(FontUtil.FontTypes.ALL);
        private static final Float[] DEFAULT_FONT_SIZES = {8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 14.0f, 16.0f, 18.0f, 20.0f, 24.0f, 28.0f, 32.0f, 36.0f, 40.0f, 48.0f, 56.0f, 64.0f};
        private static final Color[] DEFAULT_COLORS = {
                Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
                Color.DARKRED, Color.RED, Color.RED.brighter(),
                Color.DARKGREEN, Color.GREEN, Color.GREEN.brighter(),
                Color.DARKBLUE, Color.BLUE, Color.BLUE.brighter(),
                Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
                Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
                Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
        };

        private final Pane contentPane = new Pane();
        private final Pane selectionLayer = new Pane();
        private final Canvas canvas = new Canvas();
        private final Pane inlineLayer = new Pane();
        private final Pane caretLayer = new Pane();
        private final ScrollPane scrollPane = new ScrollPane(contentPane);
        private final VBox editorRoot = new VBox();
        private boolean dirty = true;
        private double lastAvailableWidth = Double.NaN;
        private RichText lastText;
        private Font lastFont;
        private boolean lastWrapText;
        private boolean blink = true;
        private final @Nullable TextEditorPane editor;
        private final Timeline caretTimeline;
        private final Timeline dragAutoscrollTimeline;
        private boolean caretVisibilityRequested;
        private boolean draggingSelection;
        private double dragSceneX;
        private double dragSceneY;

        private TextPaneSkin(TextPane control) {
            super(control);
            this.editor = control instanceof TextEditorPane e ? e : null;

            caretTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> setBlink(false)),
                    new KeyFrame(Duration.seconds(0.5), e -> setBlink(true)),
                    new KeyFrame(Duration.seconds(1.0))
            );
            caretTimeline.setCycleCount(Timeline.INDEFINITE);

            dragAutoscrollTimeline = new Timeline(
                    new KeyFrame(Duration.millis(DRAG_AUTOSCROLL_TICK_MS), e -> autoScrollDuringSelectionDrag())
            );
            dragAutoscrollTimeline.setCycleCount(Timeline.INDEFINITE);

            contentPane.getStyleClass().add("content");
            selectionLayer.setMouseTransparent(true);
            caretLayer.setMouseTransparent(true);
            contentPane.getChildren().setAll(selectionLayer, canvas, inlineLayer, caretLayer);

            scrollPane.setFitToWidth(control.isWrapText());
            scrollPane.setHbarPolicy(control.isWrapText() ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setFitToHeight(false);

            if (control instanceof TextEditorPane editor) {
                Button copyButton = createButton("Copy", Controls.graphic(Feather.COPY.getDescription()), editor, TextEditorPane::copy);
                Button cutButton = createButton("Cut", Controls.graphic(Feather.SCISSORS.getDescription()), editor, TextEditorPane::cut);
                Button pasteButton = createButton("Paste", Controls.graphic(Feather.CLIPBOARD.getDescription()), editor, TextEditorPane::paste);

                Button undoButton = createButton("Undo", Controls.graphic(Feather.ROTATE_CCW.getDescription()), editor, TextEditorPane::undo);
                Button redoButton = createButton("Redo", Controls.graphic(Feather.ROTATE_CW.getDescription()), editor, TextEditorPane::redo);

                ToggleButton boldButton = createToggleButton("Bold", Controls.graphic(Feather.BOLD.getDescription()), editor, TextEditorPane::markBold);
                ToggleButton italicsButton = createToggleButton("Italic", Controls.graphic(Feather.ITALIC.getDescription()), editor, TextEditorPane::markItalic);
                ToggleButton underlineButton = createToggleButton("Underline", Controls.graphic(Feather.UNDERLINE.getDescription()), editor, TextEditorPane::markUnderline);
                ToggleButton strikeThroughButton = createToggleButton("Strike Through", Controls.graphic(Feather.MINUS.getDescription()), editor, TextEditorPane::markStrikeThrough);

                ComboBoxEx<String> fontList = Controls.comboBoxEx(AVAILABLE_FONTS).build();
                ComboBoxEx<Float> sizeList = Controls.comboBoxEx(DEFAULT_FONT_SIZES).build();
                ComboBoxEx<Color> colorList = Controls.comboBoxEx(DEFAULT_COLORS)
                        .defaultValue(() -> Color.BLACK)
                        .format(color -> LangUtil.mapNonNullOrElse(color, Color::toArgb, ""))
                        .graphic(color -> new Rectangle(16, 16, FxUtil.convert(color)))
                        .build();

                boldButton.selectedProperty().bindBidirectional(editor.boldProperty());
                italicsButton.selectedProperty().bindBidirectional(editor.italicProperty());
                underlineButton.selectedProperty().bindBidirectional(editor.underlineProperty());
                strikeThroughButton.selectedProperty().bindBidirectional(editor.strikeThroughProperty());
                bindFontLists(editor, fontList, sizeList, colorList);
                undoButton.disableProperty().bind(editor.undoableProperty().not());
                redoButton.disableProperty().bind(editor.redoableProperty().not());
                copyButton.setFocusTraversable(false);
                cutButton.setFocusTraversable(false);
                pasteButton.setFocusTraversable(false);

                ToolBar toolbar = new ToolBar(
                        copyButton,
                        cutButton,
                        pasteButton,
                        new Separator(),
                        undoButton,
                        redoButton,
                        new Separator(),
                        fontList,
                        sizeList,
                        colorList,
                        new Separator(),
                        boldButton,
                        italicsButton,
                        underlineButton,
                        strikeThroughButton
                );

                toolbar.setFocusTraversable(false);
                toolbar.visibleProperty().bind(editor.toolbarVisibleProperty());
                toolbar.managedProperty().bind(editor.toolbarVisibleProperty());

                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                editorRoot.getChildren().setAll(toolbar, scrollPane);
                getChildren().setAll(editorRoot);
            } else {
                getChildren().setAll(scrollPane);
            }

            control.textProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.wrapTextProperty().addListener((obs, oldVal, newVal) -> {
                scrollPane.setFitToWidth(newVal);
                scrollPane.setHbarPolicy(newVal ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
                requestCaretVisibility();
                invalidate();
            });
            control.fontProperty().addListener((obs, oldVal, newVal) -> {
                requestCaretVisibility();
                invalidate();
            });
            control.widthProperty().addListener((obs, oldVal, newVal) -> {
                requestCaretVisibility();
                invalidate();
            });
            control.heightProperty().addListener((obs, oldVal, newVal) -> {
                requestCaretVisibility();
                invalidate();
            });
            control.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
                requestCaretVisibility();
                invalidate();
            });

            if (control instanceof TextEditorPane editor) {
                editor.selectionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    requestCaretVisibility();
                    invalidate();
                });
                editor.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    requestCaretVisibility();
                    invalidate();
                });
                editor.editableProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
                editor.toolbarVisibleProperty().addListener((obs, oldVal, newVal) -> invalidate());

                // Route interaction through the internal ScrollPane so input works regardless of focus owner.
                scrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
                    editor.processMousePressed(evt);
                    stopSelectionDragAutoscroll();
                });
                scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
                    editor.processMouseDragged(evt);
                    updateSelectionDragAutoscroll(evt);
                });
                scrollPane.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> stopSelectionDragAutoscroll());
                scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, editor::processKeyPressed);
                scrollPane.addEventFilter(KeyEvent.KEY_TYPED, editor::processKeyTyped);
                scrollPane.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            }

            updateCaretAnimationState();
        }

        private static Button createButton(String text, Node graphic, TextEditorPane editor, Consumer<TextEditorPane> action) {
            return Controls.button()
                    .tooltip(text)
                    .graphic(graphic)
                    .action(e -> {
                        action.accept(editor);
                        editor.requestFocus();
                    })
                    .build();
        }

    private static ToggleButton createToggleButton(String text, Node graphic, TextEditorPane editor, BiConsumer<TextEditorPane, Boolean> action) {
        return Controls.toggleButton()
                .tooltip(text)
                .graphic(graphic)
                .action(e -> {
                    if (!(e.getSource() instanceof ToggleButton tb)) {
                        throw new IllegalStateException("Unexpected source");
                    }
                    action.accept(editor, tb.isSelected());
                    editor.requestFocus();
                })
                .build();
    }

        private static void bindFontLists(
                TextEditorPane editor,
                ComboBoxEx<String> fontList,
                ComboBoxEx<Float> sizeList,
                ComboBoxEx<Color> colorList
        ) {
            AtomicBoolean synchronizing = new AtomicBoolean(false);

            editor.fontFamilyProperty().addListener((obs, oldValue, newValue) ->
                    synchronizeFromEditor(synchronizing, () -> {
                        if (newValue == null || newValue.isBlank()) {
                            return;
                        }

                        ensureValuePresent(fontList, newValue);
                        if (!Objects.equals(fontList.valueProperty().getValue(), newValue)) {
                            fontList.valueProperty().setValue(newValue);
                        }
                    }));

            fontList.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (synchronizing.get() || newValue == null || newValue.isBlank()) {
                    return;
                }
                if (!Objects.equals(editor.getFontFamily(), newValue)) {
                    editor.setFontFamily(newValue);
                }
            });

            editor.fontSizeProperty().addListener((obs, oldValue, newValue) ->
                    synchronizeFromEditor(synchronizing, () -> {
                        double size = newValue.doubleValue();
                        if (!Double.isFinite(size) || size <= 0.0) {
                            return;
                        }

                        float comboValue = (float) size;
                        ensureValuePresent(sizeList, comboValue);
                        if (!Objects.equals(sizeList.valueProperty().getValue(), comboValue)) {
                            sizeList.valueProperty().setValue(comboValue);
                        }
                    }));

            sizeList.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (synchronizing.get() || newValue == null) {
                    return;
                }
                double size = newValue.doubleValue();
                if (Double.compare(editor.getFontSize(), size) != 0) {
                    editor.setFontSize(size);
                }
            });

            editor.textColorProperty().addListener((obs, oldValue, newValue) ->
                    synchronizeFromEditor(synchronizing, () -> {
                        if (newValue == null) {
                            return;
                        }

                        ensureValuePresent(colorList, newValue);
                        if (!Objects.equals(colorList.valueProperty().getValue(), newValue)) {
                            colorList.valueProperty().setValue(newValue);
                        }
                    }));

            colorList.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (synchronizing.get() || newValue == null) {
                    return;
                }
                if (!Objects.equals(editor.getTextColor(), newValue)) {
                    editor.setTextColor(newValue);
                }
            });

            synchronizeFromEditor(synchronizing, () -> {
                String currentFamily = editor.getFontFamily();
                if (currentFamily != null && !currentFamily.isBlank()) {
                    ensureValuePresent(fontList, currentFamily);
                    if (!Objects.equals(fontList.valueProperty().getValue(), currentFamily)) {
                        fontList.valueProperty().setValue(currentFamily);
                    }
                }

                double currentSize = editor.getFontSize();
                if (Double.isFinite(currentSize) && currentSize > 0.0) {
                    float comboSize = (float) currentSize;
                    ensureValuePresent(sizeList, comboSize);
                    if (!Objects.equals(sizeList.valueProperty().getValue(), comboSize)) {
                        sizeList.valueProperty().setValue(comboSize);
                    }
                }

                Color currentColor = editor.getTextColor();
                if (currentColor != null) {
                    ensureValuePresent(colorList, currentColor);
                    if (!Objects.equals(colorList.valueProperty().getValue(), currentColor)) {
                        colorList.valueProperty().setValue(currentColor);
                    }
                }
            });
        }

        private static void synchronizeFromEditor(AtomicBoolean synchronizing, Runnable action) {
            synchronizing.set(true);
            try {
                action.run();
            } finally {
                synchronizing.set(false);
            }
        }

        private static <T> void ensureValuePresent(ComboBoxEx<T> comboBoxEx, T value) {
            if (!comboBoxEx.getItems().contains(value)) {
                comboBoxEx.addValue(value);
            }
        }

        private void requestCaretVisibility() {
            if (editor != null) {
                caretVisibilityRequested = true;
            }
        }

        private void updateSelectionDragAutoscroll(MouseEvent evt) {
            if (editor == null || !editor.isEditable() || !evt.isPrimaryButtonDown()) {
                stopSelectionDragAutoscroll();
                return;
            }

            draggingSelection = true;
            dragSceneX = evt.getSceneX();
            dragSceneY = evt.getSceneY();

            Bounds viewport = getViewportSceneBounds();
            if (viewport == null) {
                stopSelectionDragAutoscroll();
                return;
            }

            boolean nearTop = dragSceneY < viewport.getMinY() + DRAG_AUTOSCROLL_EDGE;
            boolean nearBottom = dragSceneY > viewport.getMaxY() - DRAG_AUTOSCROLL_EDGE;
            if (nearTop || nearBottom) {
                if (dragAutoscrollTimeline.getStatus() != Animation.Status.RUNNING) {
                    dragAutoscrollTimeline.play();
                }
            } else if (dragAutoscrollTimeline.getStatus() == Animation.Status.RUNNING) {
                dragAutoscrollTimeline.stop();
            }
        }

        private void autoScrollDuringSelectionDrag() {
            if (!draggingSelection || editor == null || !editor.isEditable()) {
                stopSelectionDragAutoscroll();
                return;
            }

            Bounds viewport = getViewportSceneBounds();
            if (viewport == null) {
                stopSelectionDragAutoscroll();
                return;
            }

            double deltaY = 0.0;
            if (dragSceneY < viewport.getMinY() + DRAG_AUTOSCROLL_EDGE) {
                double over = viewport.getMinY() + DRAG_AUTOSCROLL_EDGE - dragSceneY;
                deltaY = -dragAutoscrollPixels(over);
            } else if (dragSceneY > viewport.getMaxY() - DRAG_AUTOSCROLL_EDGE) {
                double over = dragSceneY - (viewport.getMaxY() - DRAG_AUTOSCROLL_EDGE);
                deltaY = dragAutoscrollPixels(over);
            }

            if (deltaY == 0.0) {
                dragAutoscrollTimeline.stop();
                return;
            }

            scrollByPixelsVertical(deltaY);
            extendSelectionToDragPoint();
        }

        private static double dragAutoscrollPixels(double over) {
            double factor = Math.clamp(over / DRAG_AUTOSCROLL_EDGE, 0.0, 1.0);
            return 4.0 + factor * 16.0;
        }

        private void extendSelectionToDragPoint() {
            if (editor == null) {
                return;
            }

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(getAvailableWidth());
            if (lines.isEmpty()) {
                return;
            }

            Bounds viewport = getViewportSceneBounds();
            double sceneX = dragSceneX;
            if (viewport != null) {
                sceneX = Math.clamp(sceneX, viewport.getMinX(), viewport.getMaxX());
            }

            Point2D point = contentPane.sceneToLocal(sceneX, dragSceneY);
            int caret = TextEditorPane.indexForPoint(lines, point.getX(), point.getY());
            editor.selectPositionCaret(caret);
            requestCaretVisibility();
        }

        private void stopSelectionDragAutoscroll() {
            draggingSelection = false;
            if (dragAutoscrollTimeline.getStatus() == Animation.Status.RUNNING) {
                dragAutoscrollTimeline.stop();
            }
        }

        private @Nullable Bounds getViewportSceneBounds() {
            Node viewport = scrollPane.lookup(".viewport");
            if (viewport != null) {
                return viewport.localToScene(viewport.getBoundsInLocal());
            }
            return scrollPane.localToScene(scrollPane.getBoundsInLocal());
        }

        private void ensureCaretVisible(TextEditorPane editor, double availableWidth) {
            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(availableWidth);
            if (lines.isEmpty()) {
                return;
            }

            int caret = editor.getCaretPosition();
            int lineIndex = TextEditorPane.lineIndexForCaret(lines, caret);
            if (lineIndex < 0 || lineIndex >= lines.size()) {
                return;
            }

            TextEditorPane.VisualLine line = lines.get(lineIndex);
            double caretX = TextEditorPane.xForIndex(line, caret);
            double caretWidth = Math.max(1.0, editor.getFont().getFontData().spaceWidth());

            scrollHorizontallyToInclude(caretX, caretWidth);
            scrollVerticallyToInclude(line.top(), line.top() + line.height());
        }

        private boolean scrollHorizontallyToInclude(double x, double width) {
            Bounds viewport = scrollPane.getViewportBounds();
            double viewportWidth = viewport.getWidth();
            double contentWidth = Math.max(contentPane.getLayoutBounds().getWidth(), canvas.getWidth());
            double maxOffset = Math.max(0.0, contentWidth - viewportWidth);
            if (!(viewportWidth > 0.0) || maxOffset <= 0.0) {
                return false;
            }

            double currentOffset = scrollPane.getHvalue() * maxOffset;
            double targetOffset = currentOffset;
            double left = currentOffset + CARET_AUTOSCROLL_MARGIN;
            double right = currentOffset + viewportWidth - CARET_AUTOSCROLL_MARGIN;

            if (x < left) {
                targetOffset = x - CARET_AUTOSCROLL_MARGIN;
            } else if (x + width > right) {
                targetOffset = x + width - viewportWidth + CARET_AUTOSCROLL_MARGIN;
            }

            targetOffset = Math.clamp(targetOffset, 0.0, maxOffset);
            if (Math.abs(targetOffset - currentOffset) < 0.5) {
                return false;
            }

            scrollPane.setHvalue(maxOffset <= 0.0 ? 0.0 : targetOffset / maxOffset);
            return true;
        }

        private boolean scrollVerticallyToInclude(double top, double bottom) {
            Bounds viewport = scrollPane.getViewportBounds();
            double viewportHeight = viewport.getHeight();
            double contentHeight = Math.max(contentPane.getLayoutBounds().getHeight(), canvas.getHeight());
            double maxOffset = Math.max(0.0, contentHeight - viewportHeight);
            if (!(viewportHeight > 0.0) || maxOffset <= 0.0) {
                return false;
            }

            double currentOffset = scrollPane.getVvalue() * maxOffset;
            double targetOffset = currentOffset;
            double visibleTop = currentOffset + CARET_AUTOSCROLL_MARGIN;
            double visibleBottom = currentOffset + viewportHeight - CARET_AUTOSCROLL_MARGIN;

            if (top < visibleTop) {
                targetOffset = top - CARET_AUTOSCROLL_MARGIN;
            } else if (bottom > visibleBottom) {
                targetOffset = bottom - viewportHeight + CARET_AUTOSCROLL_MARGIN;
            }

            targetOffset = Math.clamp(targetOffset, 0.0, maxOffset);
            if (Math.abs(targetOffset - currentOffset) < 0.5) {
                return false;
            }

            scrollPane.setVvalue(maxOffset <= 0.0 ? 0.0 : targetOffset / maxOffset);
            return true;
        }

        private boolean scrollByPixelsVertical(double delta) {
            if (delta == 0.0) {
                return false;
            }

            Bounds viewport = scrollPane.getViewportBounds();
            double viewportHeight = viewport.getHeight();
            double contentHeight = Math.max(contentPane.getLayoutBounds().getHeight(), canvas.getHeight());
            double maxOffset = Math.max(0.0, contentHeight - viewportHeight);
            if (!(viewportHeight > 0.0) || maxOffset <= 0.0) {
                return false;
            }

            double currentOffset = scrollPane.getVvalue() * maxOffset;
            double targetOffset = Math.clamp(currentOffset + delta, 0.0, maxOffset);
            if (Math.abs(targetOffset - currentOffset) < 0.5) {
                return false;
            }

            scrollPane.setVvalue(maxOffset <= 0.0 ? 0.0 : targetOffset / maxOffset);
            return true;
        }

    private void invalidate() {
            dirty = true;
            getSkinnable().requestLayout();
        }

        @Override
        public void dispose() {
            caretTimeline.stop();
            dragAutoscrollTimeline.stop();
            super.dispose();
        }

        private void setBlink(boolean value) {
            if (blink != value) {
                blink = value;
                invalidate();
            }
        }

        private boolean hasEditorFocus(TextPane control) {
            return control.isFocused() || scrollPane.isFocused();
        }

        private boolean shouldAnimateCaret() {
            TextPane control = getSkinnable();
            if (!(control instanceof TextEditorPane editor)) {
                return false;
            }
            return editor.isEditable() && hasEditorFocus(control);
        }

        private void restartCaretAnimation() {
            if (!shouldAnimateCaret()) {
                return;
            }
            setBlink(false);
            caretTimeline.playFromStart();
        }

        private void updateCaretAnimationState() {
            if (shouldAnimateCaret()) {
                restartCaretAnimation();
            } else {
                stopSelectionDragAutoscroll();
                if (caretTimeline.getStatus() == Animation.Status.RUNNING) {
                    caretTimeline.stop();
                }
                setBlink(true);
            }
            invalidate();
        }

        @Override
        protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
            super.layoutChildren(contentX, contentY, contentWidth, contentHeight);
            refreshIfNeeded();
        }

        private void refreshIfNeeded() {
            TextPane control = getSkinnable();
            double availableWidth = getAvailableWidth();
            RichText text = control.getText();
            Font font = control.getFont();
            boolean wrapText = control.isWrapText();
            boolean widthChanged = !Double.isFinite(lastAvailableWidth) || Math.abs(lastAvailableWidth - availableWidth) > 0.5;
            if (!dirty
                    && !widthChanged
                    && Objects.equals(lastText, text)
                    && Objects.equals(lastFont, font)
                    && lastWrapText == wrapText) {
                return;
            }

            refresh(availableWidth);
            dirty = false;
            lastAvailableWidth = availableWidth;
            lastText = text;
            lastFont = font;
            lastWrapText = wrapText;
        }

        private double getAvailableWidth() {
            TextPane control = getSkinnable();
            Bounds vp = scrollPane.getViewportBounds();
            double availableWidth = control.isWrapText()
                    ? Math.max(1.0, vp.getWidth())
                    : Math.max(1.0, control.getWidth());
            if (!Double.isFinite(availableWidth) || availableWidth <= 0.0) {
                availableWidth = Math.max(1.0, control.computePrefWidth(-1));
            }
            return availableWidth;
        }

        private void refresh(double availableWidth) {
            TextPane control = getSkinnable();
            Layout layout = control.createLayout(availableWidth);

            canvas.setWidth(Math.max(1.0, Math.ceil(layout.width())));
            canvas.setHeight(Math.max(1.0, Math.ceil(layout.height())));
            contentPane.setMinSize(canvas.getWidth(), canvas.getHeight());
            contentPane.setPrefSize(canvas.getWidth(), canvas.getHeight());

            renderEditorOverlay(control, availableWidth, layout);
            ensureEditorContentHeight(control, availableWidth);
            if (editor != null && caretVisibilityRequested) {
                ensureCaretVisible(editor, availableWidth);
                caretVisibilityRequested = false;
            }

            inlineLayer.getChildren().clear();
            Set<Node> added = Collections.newSetFromMap(new IdentityHashMap<>());
            for (InlineControlPlacement placement : layout.placements()) {
                Node node = placement.node();
                if (!added.add(node)) {
                    continue;
                }
                inlineLayer.getChildren().add(node);
                if (node instanceof Labeled labeled) {
                    labeled.setFont(FxFontUtil.getInstance().convert(placement.font()));
                }
                node.setManaged(false);
                node.applyCss();
                node.autosize();
                double prefW = node.prefWidth(-1);
                double prefH = node.prefHeight(-1);
                double baselineOffset = node.getBaselineOffset();
                double x = placement.x();
                double y = computeInlineNodeY(placement, prefH, baselineOffset);
                node.resizeRelocate(x, y, prefW, prefH);
            }

            try (Graphics graphics = new FxGraphics(canvas)) {
                graphics.reset();
                graphics.setFont(control.getFont());
                for (List<FragmentedText.Fragment> line : layout.renderLines()) {
                    double lineBaseline = line.stream()
                            .filter(fragment -> !isInvisibleInlinePlaceholder(fragment))
                            .mapToDouble(fragment -> fragment.font().getAscent())
                            .max()
                            .orElseGet(() -> line.stream()
                                    .mapToDouble(fragment -> fragment.font().getAscent())
                                    .max()
                                    .orElse(0.0));
                    for (FragmentedText.Fragment fragment : line) {
                        graphics.setFont(fragment.font());
                        graphics.drawText(
                                fragment.text().toString(),
                                fragment.x(),
                                (float) (fragment.y() + lineBaseline),
                                HAnchor.LEFT,
                                VAnchor.BASELINE
                        );
                    }
                }
            }
        }

        private static boolean isInvisibleInlinePlaceholder(FragmentedText.Fragment fragment) {
            if (!(fragment.text() instanceof Run run)) {
                return false;
            }
            return run.getStyles().contains(STYLE_INVISIBLE_TEXT);
        }

        private void ensureEditorContentHeight(TextPane control, double availableWidth) {
            if (!(control instanceof TextEditorPane editor)) {
                return;
            }

            List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(availableWidth);
            if (lines.isEmpty()) {
                return;
            }

            int lineIndex = TextEditorPane.lineIndexForCaret(lines, editor.getCaretPosition());
            if (lineIndex < 0 || lineIndex >= lines.size()) {
                return;
            }

            TextEditorPane.VisualLine line = lines.get(lineIndex);
            double requiredHeight = Math.ceil(line.top() + line.height());
            if (requiredHeight > canvas.getHeight()) {
                canvas.setHeight(requiredHeight);
                contentPane.setMinHeight(requiredHeight);
                contentPane.setPrefHeight(requiredHeight);
            }
        }

        private void renderEditorOverlay(TextPane control, double availableWidth, Layout layout) {
            selectionLayer.getChildren().clear();
            caretLayer.getChildren().clear();

            if (!(control instanceof TextEditorPane editor)) {
                return;
            }

            IndexRange selection = editor.getSelection();
            if (selection.getLength() > 0) {
                int selStart = layout.layoutTextData().sourceToLayoutPosition(selection.getStart());
                int selEnd = layout.layoutTextData().sourceToLayoutPosition(selection.getEnd());
                FontUtil fontUtil = FontUtil.getInstance();
                for (List<FragmentedText.Fragment> line : layout.renderLines()) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    double lineTop = line.getFirst().y();
                    double lineHeight = line.stream().mapToDouble(FragmentedText.Fragment::h).max().orElse(0.0);
                    for (FragmentedText.Fragment fragment : line) {
                        if (!(fragment.text() instanceof Run run)) {
                            continue;
                        }
                        int fragStart = run.getStart();
                        int fragEnd = run.getEnd();
                        int from = Math.max(selStart, fragStart);
                        int to = Math.min(selEnd, fragEnd);
                        if (from >= to) {
                            continue;
                        }

                        int relStart = from - fragStart;
                        int relEnd = to - fragStart;
                        double x1 = fragment.x() + textWidth(fontUtil, run, relStart, fragment.font());
                        double x2 = fragment.x() + textWidth(fontUtil, run, relEnd, fragment.font());
                        Rectangle marker = new Rectangle(
                                Math.min(x1, x2),
                                lineTop,
                                Math.max(1.0, Math.abs(x2 - x1)),
                                lineHeight
                        );
                        marker.setFill(javafx.scene.paint.Color.color(0.25, 0.45, 0.85, 0.35));
                        selectionLayer.getChildren().add(marker);
                    }
                }
            }

            if (editor.isEditable() && hasEditorFocus(control) && !blink) {
                int layoutCaretPosition = layout.layoutTextData().sourceToLayoutPosition(editor.getCaretPosition());
                CaretInfo caretInfo = findCaret(layout.renderLines(), layoutCaretPosition);
                if (caretInfo == null) {
                    List<TextEditorPane.VisualLine> lines = editor.buildVisualLines(availableWidth);
                    if (!lines.isEmpty()) {
                        int caretPosition = editor.getCaretPosition();
                        int lineIndex = TextEditorPane.lineIndexForCaret(lines, caretPosition);
                        if (lineIndex >= 0 && lineIndex < lines.size()) {
                            TextEditorPane.VisualLine line = lines.get(lineIndex);
                            double x = TextEditorPane.xForIndex(line, caretPosition);
                            caretInfo = new CaretInfo(x, line.top(), line.height());
                        }
                    }
                }
                if (caretInfo != null) {
                    Line caret = new Line(caretInfo.x(), caretInfo.y(), caretInfo.x(), caretInfo.y() + caretInfo.height());
                    caret.setStroke(javafx.scene.paint.Color.BLACK);
                    caret.setStrokeWidth(1.0);
                    caretLayer.getChildren().add(caret);
                }
            }
        }

        private static double textWidth(FontUtil fontUtil, Run run, int length, Font font) {
            if (length <= 0) {
                return 0.0;
            }
            if (length >= run.length()) {
                return fontUtil.getTextWidth(run, font);
            }
            return fontUtil.getTextWidth(run.subSequence(0, length), font);
        }

        private static @Nullable CaretInfo findCaret(List<List<FragmentedText.Fragment>> lines, int layoutCaretPosition) {
            FontUtil fontUtil = FontUtil.getInstance();
            for (List<FragmentedText.Fragment> line : lines) {
                if (line.isEmpty()) {
                    continue;
                }
                double lineTop = line.getFirst().y();
                double lineHeight = line.stream().mapToDouble(FragmentedText.Fragment::h).max().orElse(0.0);
                for (FragmentedText.Fragment fragment : line) {
                    if (!(fragment.text() instanceof Run run)) {
                        continue;
                    }

                    int start = run.getStart();
                    int end = run.getEnd();
                    if (layoutCaretPosition < start || layoutCaretPosition > end) {
                        continue;
                    }

                    int rel = layoutCaretPosition - start;
                    double x = fragment.x() + textWidth(fontUtil, run, rel, fragment.font());
                    return new CaretInfo(x, lineTop, lineHeight);
                }
            }
            return null;
        }

        private record CaretInfo(double x, double y, double height) {}
    }
}
