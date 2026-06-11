package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxGraphics;
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
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Labeled;
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
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT = TextPane.class.getName() + ".inlineReferenceAscent";
    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_DESCENT = TextPane.class.getName() + ".inlineReferenceDescent";
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

    private Layout createLayout(double availableWidth) {
        RichText richText = getText();
        Font font = getFont();
        com.dua3.utility.text.FontUtil fontUtil = com.dua3.utility.text.FontUtil.getInstance();
        RichText layoutText = createLayoutText(richText, font, fontUtil);

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
                        placements.add(new InlineControlPlacement(node, fragment.x(), fragment.y(), fragment.w(), fragment.h(), baselineY, fragment.font(), vAnchor, refAscent, refDescent, descent));
                    }
                }
            }
        }

        Map<Float, Float> lineShiftByY = computeLineShifts(renderFragments, placements);
        List<InlineControlPlacement> shiftedPlacements = shiftPlacements(placements, lineShiftByY);
        List<List<FragmentedText.Fragment>> shiftedRenderLines = shiftRenderLines(renderFragments, lineShiftByY);
        float renderHeight = computeRenderedHeight(shiftedRenderLines, font);

        return new Layout(shiftedRenderLines, shiftedPlacements, renderWidth, renderHeight);
    }

    private static RichText createLayoutText(RichText source, Font baseFont, com.dua3.utility.text.FontUtil fontUtil) {
        RichTextBuilder builder = new RichTextBuilder(source.length());
        for (Run run : source) {
            if (hasInlineNode(run)) {
                Node node = createInlineNode(run);
                if (node != null) {
                    Font runFont = fontUtil.deriveFont(baseFont, run.getFontDef());
                    if (node instanceof Labeled labeled) {
                        labeled.setFont(FxFontUtil.getInstance().convert(runFont));
                    }
                    double controlWidth = measureNodeWidth(node);
                    double controlHeight = measureNodeHeight(node);
                    double textWidth = fontUtil.getTextDimension(run, runFont).width();
                    double textHeight = runFont.getFontData().height();
                    double extraWidth = controlWidth - textWidth;
                    String text = run.toString();
                    Style lineHeightStyle = null;
                    if (node instanceof ImageView && controlHeight > textHeight + 0.5 && textHeight > 0.1) {
                        VAnchor vAnchor = getInlineNodeVAnchor(run);
                        double inlineDescent = getInlineNodeDescent(run);
                        if (!Double.isFinite(inlineDescent)) {
                            inlineDescent = 0.0;
                        }
                        double scale = requiredLineMetricsScale(
                                controlHeight,
                                runFont.getAscent(),
                                runFont.getDescent(),
                                vAnchor,
                                inlineDescent
                        );
                        double referenceAscent = runFont.getAscent();
                        double referenceDescent = runFont.getDescent();
                        float scaledFontSize = (float) (runFont.getSizeInPoints() * scale);
                        lineHeightStyle = Style.create(
                                "text-pane-inline-height",
                                Map.entry(Style.FONT_SIZE, scaledFontSize),
                                Map.entry(STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT, referenceAscent),
                                Map.entry(STYLE_ATTRIBUTE_INLINE_REFERENCE_DESCENT, referenceDescent)
                        );
                    }
                    if (extraWidth > 0.5) {
                        double spaceWidth = Math.max(1.0, runFont.getFontData().spaceWidth());
                        int extraSpaces = (int) Math.ceil(extraWidth / spaceWidth);
                        if (extraSpaces > 0) {
                            builder.append(NO_BREAK_SPACE.repeat(extraSpaces));
                        }
                    }
                    List<Style> styles = run.getStyles();
                    styles.forEach(builder::push);
                    if (lineHeightStyle != null) {
                        builder.push(lineHeightStyle);
                    }
                    builder.append(text);
                    if (lineHeightStyle != null) {
                        builder.pop(lineHeightStyle);
                    }
                    for (int i = styles.size() - 1; i >= 0; i--) {
                        builder.pop(styles.get(i));
                    }
                } else {
                    List<Style> styles = run.getStyles();
                    styles.forEach(builder::push);
                    builder.append(run.toString());
                    for (int i = styles.size() - 1; i >= 0; i--) {
                        builder.pop(styles.get(i));
                    }
                }
            } else {
                List<Style> styles = run.getStyles();
                styles.forEach(builder::push);
                builder.append(run.toString());
                for (int i = styles.size() - 1; i >= 0; i--) {
                    builder.pop(styles.get(i));
                }
            }
        }
        return builder.toRichText();
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
        }
        return builder.toRichText();
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
        if (TextUtil.isBlank(run)) {
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

    private static double requiredLineMetricsScale(
            double nodeHeight,
            double referenceAscent,
            double referenceDescent,
            VAnchor vAnchor,
            double inlineDescent
    ) {
        double refHeight = referenceAscent + referenceDescent;
        if (refHeight <= 0.0) {
            return 1.0;
        }

        double safeInlineDescent = Double.isFinite(inlineDescent) ? Math.max(0.0, inlineDescent) : 0.0;
        double requiredBottom = switch (vAnchor) {
            case TOP -> nodeHeight;
            case BOTTOM -> refHeight;
            case MIDDLE -> 0.5 * (refHeight + nodeHeight);
            case BASELINE -> referenceAscent + safeInlineDescent;
        };
        return Math.max(1.0, requiredBottom / refHeight);
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

    private static Map<Float, Float> computeLineShifts(
            FragmentedText renderFragments,
            List<InlineControlPlacement> placements
    ) {
        Map<Float, Float> overflowByLineY = new java.util.HashMap<>();
        for (InlineControlPlacement placement : placements) {
            Node node = placement.node();
            node.applyCss();
            node.autosize();
            double prefH = Math.max(node.prefHeight(-1), node.getLayoutBounds().getHeight());
            double baselineOffset = node.getBaselineOffset();
            double nodeY = computeInlineNodeY(placement, prefH, baselineOffset);
            float overflowAbove = (float) Math.max(0.0, placement.y() - nodeY);
            if (overflowAbove > 0.0f) {
                overflowByLineY.merge(placement.y(), overflowAbove, Math::max);
            }
        }

        Map<Float, Float> lineShiftByY = new java.util.HashMap<>();
        float cumulativeShift = 0.0f;
        for (List<FragmentedText.Fragment> line : renderFragments.lines()) {
            if (line.isEmpty()) {
                continue;
            }
            float lineY = line.getFirst().y();
            cumulativeShift += overflowByLineY.getOrDefault(lineY, 0.0f);
            lineShiftByY.put(lineY, cumulativeShift);
        }
        return lineShiftByY;
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

    private static float computeRenderedHeight(List<List<FragmentedText.Fragment>> lines, Font fallbackFont) {
        float maxBottom = 0.0f;
        for (List<FragmentedText.Fragment> line : lines) {
            for (FragmentedText.Fragment fragment : line) {
                maxBottom = Math.max(maxBottom, fragment.y() + fragment.h());
            }
        }
        return (float) Math.max(fallbackFont.getFontData().height(), maxBottom);
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

    private record Layout(
            List<List<FragmentedText.Fragment>> renderLines,
            List<InlineControlPlacement> placements,
            float width,
            float height
    ) {}

    private static final class TextPaneSkin extends SkinBase<TextPane> {
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
        private final Timeline caretTimeline;

        private TextPaneSkin(TextPane control) {
            super(control);

            caretTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> setBlink(false)),
                    new KeyFrame(Duration.seconds(0.5), e -> setBlink(true)),
                    new KeyFrame(Duration.seconds(1.0))
            );
            caretTimeline.setCycleCount(Timeline.INDEFINITE);

            contentPane.getStyleClass().add("content");
            selectionLayer.setMouseTransparent(true);
            caretLayer.setMouseTransparent(true);
            contentPane.getChildren().setAll(selectionLayer, canvas, inlineLayer, caretLayer);

            scrollPane.setFitToWidth(control.isWrapText());
            scrollPane.setHbarPolicy(control.isWrapText() ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setFitToHeight(false);

            if (control instanceof TextEditorPane editor) {
                Button copyButton = Controls.button()
                        .tooltip("Copy")
                        .graphic(Controls.graphic(Feather.COPY.getDescription()))
                        .action(e -> {
                            editor.copy();
                            editor.requestFocus();
                        })
                        .build();
                Button cutButton = Controls.button()
                        .tooltip("Cut")
                        .graphic(Controls.graphic(Feather.SCISSORS.getDescription()))
                        .action(e -> {
                            editor.cut();
                            editor.requestFocus();
                        })
                        .build();
                Button pasteButton = Controls.button()
                        .tooltip("Paste")
                        .graphic(Controls.graphic(Feather.CLIPBOARD.getDescription()))
                        .action(e -> {
                            editor.paste();
                            editor.requestFocus();
                        })
                        .build();
                copyButton.setFocusTraversable(false);
                cutButton.setFocusTraversable(false);
                pasteButton.setFocusTraversable(false);

                ToolBar toolbar = new ToolBar(
                        copyButton,
                        cutButton,
                        pasteButton
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
                invalidate();
            });
            control.fontProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.widthProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.heightProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> invalidate());

            if (control instanceof TextEditorPane editor) {
                editor.selectionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    invalidate();
                });
                editor.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    invalidate();
                });
                editor.editableProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
                editor.toolbarVisibleProperty().addListener((obs, oldVal, newVal) -> invalidate());

                // Route interaction through the internal ScrollPane so input works regardless of focus owner.
                scrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, editor::processMousePressed);
                scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, editor::processMouseDragged);
                scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, editor::processKeyPressed);
                scrollPane.addEventFilter(KeyEvent.KEY_TYPED, editor::processKeyTyped);
                scrollPane.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            }

            updateCaretAnimationState();
        }

        private void invalidate() {
            dirty = true;
            getSkinnable().requestLayout();
        }

        @Override
        public void dispose() {
            caretTimeline.stop();
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
                double x = placement.x() - Math.max(0.0, prefW - placement.w());
                double y = computeInlineNodeY(placement, prefH, baselineOffset);
                node.resizeRelocate(x, y, prefW, prefH);
            }

            try (Graphics graphics = new FxGraphics(canvas)) {
                graphics.reset();
                graphics.setFont(control.getFont());
                for (List<FragmentedText.Fragment> line : layout.renderLines()) {
                    for (FragmentedText.Fragment fragment : line) {
                        graphics.setFont(fragment.font());
                        graphics.drawText(fragment.text().toString(), fragment.x(), fragment.y(), HAnchor.LEFT, VAnchor.TOP);
                    }
                }
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
                int selStart = selection.getStart();
                int selEnd = selection.getEnd();
                FontUtil fontUtil = FontUtil.getInstance();
                for (List<FragmentedText.Fragment> line : layout.renderLines()) {
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
                                fragment.y(),
                                Math.max(1.0, Math.abs(x2 - x1)),
                                fragment.h()
                        );
                        marker.setFill(javafx.scene.paint.Color.color(0.25, 0.45, 0.85, 0.35));
                        selectionLayer.getChildren().add(marker);
                    }
                }
            }

            if (editor.isEditable() && hasEditorFocus(control) && !blink) {
                CaretInfo caretInfo = findCaret(layout.renderLines(), editor.getCaretPosition());
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

        private static @Nullable CaretInfo findCaret(List<List<FragmentedText.Fragment>> lines, int caretPosition) {
            FontUtil fontUtil = FontUtil.getInstance();
            for (List<FragmentedText.Fragment> line : lines) {
                for (FragmentedText.Fragment fragment : line) {
                    if (!(fragment.text() instanceof Run run)) {
                        continue;
                    }

                    int start = run.getStart();
                    int end = run.getEnd();
                    if (caretPosition < start || caretPosition > end) {
                        continue;
                    }

                    int rel = caretPosition - start;
                    double x = fragment.x() + textWidth(fontUtil, run, rel, fragment.font());
                    return new CaretInfo(x, fragment.y(), fragment.h());
                }
            }
            return null;
        }

        private record CaretInfo(double x, double y, double height) {}
    }
}
