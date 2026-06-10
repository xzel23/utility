package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.ToRichText;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.ui.HAnchor;
import com.dua3.utility.ui.RichTextBuilderExtBase;
import com.dua3.utility.ui.VAnchor;
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
import javafx.scene.control.Labeled;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import org.jspecify.annotations.Nullable;

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
    private final ObjectProperty<javafx.scene.text.Font> font = new SimpleObjectProperty<>(this, "font", javafx.scene.text.Font.getDefault());

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
    public TextPane(@Nullable RichText text) {
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
    public final void setText(@Nullable RichText value) {
        text.set(value == null ? RichText.emptyText() : value);
    }

    /**
     * Set text from {@link ToRichText}.
     *
     * @param value value or {@code null} for empty text
     */
    public final void setText(@Nullable ToRichText value) {
        setText(value == null ? null : value.toRichText());
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
    public final ObjectProperty<javafx.scene.text.Font> fontProperty() {
        return font;
    }

    /**
     * Returns the rendering font.
     *
     * @return font
     */
    public final javafx.scene.text.Font getFont() {
        return font.get();
    }

    /**
     * Set the rendering font.
     *
     * @param value font
     */
    public final void setFont(javafx.scene.text.Font value) {
        font.set(value);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextPaneSkin(this);
    }

    @Override
    protected double computePrefWidth(double height) {
        Font font = getUtilityFont();
        double textWidth = font.getFontData().spaceWidth() * 40.0;
        return snappedLeftInset() + Math.ceil(textWidth) + snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = width > 0
                ? Math.max(1.0, width - snappedLeftInset() - snappedRightInset())
                : Math.ceil(getUtilityFont().getFontData().spaceWidth() * 40.0f);
        Layout layout = createLayout(contentWidth);
        double pref = snappedTopInset() + Math.ceil(layout.height()) + snappedBottomInset();
        return clampToMaxHeight(pref);
    }

    @Override
    protected double computeMinHeight(double width) {
        Font font = getUtilityFont();
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

    private Font getUtilityFont() {
        return FxFontUtil.getInstance().convert(getFont() == null ? javafx.scene.text.Font.getDefault() : getFont());
    }

    private Layout createLayout(double availableWidth) {
        RichText richText = Objects.requireNonNullElse(getText(), RichText.emptyText());
        Font font = getUtilityFont();
        com.dua3.utility.text.FontUtil fontUtil = com.dua3.utility.text.FontUtil.getInstance();
        RichText layoutText = createLayoutText(richText, font, fontUtil);

        float width = (float) Math.max(1.0, availableWidth);
        float wrapWidth = isWrapText() ? width : FragmentedText.NO_WRAP;
        FragmentedText fragments = FragmentedText.generateFragments(
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
        float renderWidth = isWrapText() ? width : Math.max(width, fragments.actualWidth());
        float renderHeight = (float) Math.max(font.getFontData().height(), fragments.actualHeight());

        List<InlineControlPlacement> placements = new ArrayList<>();
        for (List<FragmentedText.Fragment> line : fragments.lines()) {
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

        return new Layout(renderedText, placements, renderWidth, renderHeight);
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
                        double referenceAscent = runFont.getAscent() * scale;
                        double referenceDescent = runFont.getDescent() * scale;
                        if (vAnchor == VAnchor.BASELINE) {
                            double descent = Math.max(0.0, inlineDescent);
                            double ascent = Math.max(0.0, controlHeight - descent);
                            referenceAscent = ascent;
                            referenceDescent = descent;
                        }
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
        return Math.max(1.0, nodeHeight / refHeight);
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

    private record Layout(RichText renderText, List<InlineControlPlacement> placements, float width, float height) {}

    private static final class TextPaneSkin extends SkinBase<TextPane> {
        private final Pane contentPane = new Pane();
        private final Canvas canvas = new Canvas();
        private final Pane inlineLayer = new Pane();
        private final ScrollPane scrollPane = new ScrollPane(contentPane);
        private boolean dirty = true;
        private double lastAvailableWidth = Double.NaN;
        private RichText lastText;
        private javafx.scene.text.Font lastFont;
        private boolean lastWrapText;

        private TextPaneSkin(TextPane control) {
            super(control);

            contentPane.getStyleClass().add("content");
            contentPane.getChildren().setAll(canvas, inlineLayer);

            scrollPane.setFitToWidth(control.isWrapText());
            scrollPane.setHbarPolicy(control.isWrapText() ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setFitToHeight(false);

            getChildren().setAll(scrollPane);

            control.textProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.wrapTextProperty().addListener((obs, oldVal, newVal) -> {
                scrollPane.setFitToWidth(newVal);
                scrollPane.setHbarPolicy(newVal ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
                invalidate();
            });
            control.fontProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.widthProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.heightProperty().addListener((obs, oldVal, newVal) -> invalidate());
            scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> invalidate());
        }

        private void invalidate() {
            dirty = true;
            getSkinnable().requestLayout();
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
            javafx.scene.text.Font font = control.getFont();
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
                double lineTop = placement.baselineY() - placement.referenceAscent();
                double lineBottom = placement.baselineY() + placement.referenceDescent();
                double inlineDescent = Double.isFinite(placement.descent())
                        ? placement.descent()
                        : (baselineOffset != Node.BASELINE_OFFSET_SAME_AS_HEIGHT && Double.isFinite(baselineOffset)
                                ? Math.max(0.0, prefH - baselineOffset)
                                : 0.0);
                double y = switch (placement.vAnchor()) {
                    case TOP -> lineTop;
                    case BOTTOM -> lineBottom - prefH;
                    case MIDDLE -> (lineTop + lineBottom - prefH) / 2.0;
                    case BASELINE -> placement.baselineY() - (prefH - inlineDescent);
                };
                node.resizeRelocate(x, y, prefW, prefH);
            }

            FxGraphics graphics = new FxGraphics(canvas);
            try {
                graphics.reset();
                graphics.setFont(control.getUtilityFont());
                graphics.renderText(
                        Vector2f.of(0.0f, 0.0f),
                        layout.renderText(),
                        HAnchor.LEFT,
                        VAnchor.TOP,
                        Alignment.LEFT,
                        VerticalAlignment.TOP,
                        Dimension2f.of((float) canvas.getWidth(), (float) canvas.getHeight()),
                        control.isWrapText() ? Graphics.TextWrapping.WRAP : Graphics.TextWrapping.NO_WRAP
                );
            } finally {
                graphics.close();
            }
        }
    }
}
