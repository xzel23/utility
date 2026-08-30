package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.data.Image;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxGraphics;
import com.dua3.utility.fx.FxImageUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.math.geometry.Dimension2f;
import com.dua3.utility.math.geometry.Vector2f;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.text.ToRichText;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.Graphics;
import com.dua3.utility.ui.HAnchor;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.IndexRange;
import com.dua3.utility.ui.RichTextPane;
import com.dua3.utility.ui.RichTextPaneLayoutHelper;
import com.dua3.utility.ui.RichTextTableHelper;
import com.dua3.utility.ui.RichTextTableRenderer;
import com.dua3.utility.ui.RichTextRenderer;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VAnchor;
import com.dua3.utility.ui.VisualLine;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Labeled;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.feather.Feather;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
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
public class TextPane extends Control implements RichTextPane {

    /**
     * A shared instance of {@link FxFontUtil} used for font-related utilities
     * within the {@code TextPane} class. This utility provides various
     * font-related operations and methods to support text layout, rendering,
     * and styling in JavaFX.
     * <p>
     * This variable is declared as {@code protected static final} to ensure
     * it is accessible within this class and its subclasses, but cannot
     * be modified after initialization.
     */
    protected static final FxFontUtil FONT_UTIL = FxFontUtil.getInstance();

    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT = TextPane.class.getName() + ".inlineReferenceAscent";
    private static final String STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH = TextPane.class.getName() + ".inlineLeadingWidth";
    private static final String IMAGE_VIEW_BASE_SETTINGS = TextPane.class.getName() + ".imageViewBaseSettings";
    private static final String DEFAULT_STYLE_CLASS = "text-pane";

    private final ObjectProperty<ToRichText> text = new SimpleObjectProperty<>(this, "text", RichText.emptyText());
    private final BooleanProperty wrapText = new SimpleBooleanProperty(this, "wrapText", false);
    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(this, "font", FONT_UTIL.getDefaultFont());
    private final DoubleProperty displayScale = new SimpleDoubleProperty(this, "displayScale", 1.0);
    private final ObjectProperty<Consumer<URI>> hyperlinkHandler = new SimpleObjectProperty<>(this, "hyperlinkHandler", TextPane::openUriUsingDesktop);

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
     * Returns the text property.
     *
     * @return text property
     */
    public final ObjectProperty<ToRichText> textProperty() {
        return text;
    }

    /**
     * Returns the rich text.
     *
     * @return text
     */
    @Override
    public RichText getText() {
        return text.get().toRichText();
    }

    /**
     * Set rich text.
     *
     * @param value text or {@code null} for empty text
     */
    @Override
    public final void setText(@Nullable CharSequence value) {
        text.set(value == null ? RichText.emptyText() : RichText.valueOf(value));
    }

    /**
     * Returns the hyperlink handler property used for inline hyperlinks.
     *
     * @return hyperlink handler property
     */
    public final ObjectProperty<Consumer<URI>> hyperlinkHandlerProperty() {
        return hyperlinkHandler;
    }

    /**
     * Returns the hyperlink handler used for inline hyperlinks.
     *
     * @return hyperlink handler
     */
    @Override
    public final Consumer<URI> getHyperlinkHandler() {
        return hyperlinkHandler.get();
    }

    /**
     * Sets the hyperlink handler used for inline hyperlinks.
     *
     * @param handler hyperlink handler
     */
    @Override
    public final void setHyperlinkHandler(Consumer<URI> handler) {
        hyperlinkHandler.set(handler);
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
    @Override
    public final boolean isWrapText() {
        return wrapText.get();
    }

    /**
     * Set wrapping mode.
     *
     * @param value true to wrap text to available width
     */
    @Override
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

    @Override
    public final void setTextFont(Font value) {
        setFont(value);
    }

    /**
     * Set the rendering font.
     *
     * @param value font
     */
    public final void setFxFont(javafx.scene.text.Font value) {
        font.set(FONT_UTIL.convert(value));
    }

    /**
     * Returns the display scale property used for preview rendering.
     *
     * @return display-scale property
     */
    public final DoubleProperty displayScaleProperty() {
        return displayScale;
    }

    /**
     * Returns the current display scale used for preview rendering.
     *
     * @return display scale
     */
    @Override
    public final double getDisplayScale() {
        return displayScale.get();
    }

    /**
     * Sets the display scale used for preview rendering.
     *
     * @param value display scale (&gt; 0)
     */
    @Override
    public final void setDisplayScale(double value) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException("displayScale must be > 0: " + value);
        }
        displayScale.set(value);
    }

    /**
     * Creates the default skin implementation.
     *
     * @return default skin for this control
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextPaneSkin(this);
    }

    /**
     * Computes preferred width.
     *
     * @param height available height hint
     * @return preferred width
     */
    @Override
    protected double computePrefWidth(double height) {
        double textWidth = getFont().getFontData().spaceWidth() * 40.0 * getDisplayScale();
        return snappedLeftInset() + Math.ceil(textWidth) + snappedRightInset();
    }

    /**
     * Computes preferred height.
     *
     * @param width available width hint
     * @return preferred height
     */
    @Override
    protected double computePrefHeight(double width) {
        double contentWidth = width > 0
                ? Math.max(1.0, width - snappedLeftInset() - snappedRightInset())
                : Math.ceil(getFont().getFontData().spaceWidth() * 40.0f * getDisplayScale());
        RichTextPaneLayoutHelper.Layout<InlineControlPlacement> layout = createLayout(contentWidth);
        double pref = snappedTopInset() + Math.ceil(layout.height()) + snappedBottomInset();
        pref = Math.max(pref, super.computePrefHeight(width));
        return clampToMaxHeight(pref);
    }

    /**
     * Computes minimum height.
     *
     * @param width available width hint
     * @return minimum height
     */
    @Override
    protected double computeMinHeight(double width) {
        Font font = getFont();
        double min = snappedTopInset() + Math.ceil(font.getFontData().height() * getDisplayScale()) + snappedBottomInset();
        min = Math.max(min, super.computeMinHeight(width));
        return clampToMaxHeight(min);
    }

    private double clampToMaxHeight(double value) {
        double max = getMaxHeight();
        if (!Double.isNaN(max) && max >= 0 && max < Double.MAX_VALUE && max != USE_COMPUTED_SIZE) {
            return Math.min(value, max);
        }
        return value;
    }

    /**
     * Returns the rendering font.
     *
     * @return current utility-font value
     */
    public Font getFont() {
        return font.get();
    }

    @Override
    public Font getTextFont() {
        return getFont();
    }

    RichTextPaneLayoutHelper.Layout<InlineControlPlacement> createLayout(double availableWidth) {
        return createLayout(getText(), availableWidth);
    }

    RichTextPaneLayoutHelper.Layout<InlineControlPlacement> createLayout(RichText richText, double availableWidth) {
        return createLayout(richText, getFont(), isWrapText(), availableWidth, getDisplayScale());
    }

    /**
     * Resolves a source position for a content-local point, including the original source range hidden by an inline
     * table placeholder. The fallback lines are used only for ordinary text.
     */
    int sourcePositionForPoint(Point2D point, double availableWidth, List<VisualLine> fallbackLines) {
        RichTextPaneLayoutHelper.Layout<InlineControlPlacement> layout = createLayout(availableWidth);
        for (InlineControlPlacement placement : layout.placements()) {
            if (!(placement.node() instanceof TableNode tableNode)) {
                continue;
            }
            tableNode.applyCss();
            tableNode.autosize();
            double prefHeight = tableNode.prefHeight(-1);
            double tableY = computeInlineNodeY(placement, prefHeight, tableNode.getBaselineOffset());
            var sourcePosition = RichTextTableHelper.sourcePositionForPoint(
                    tableNode.tableLayout(),
                    (float) (point.getX() - placement.x()),
                    (float) (point.getY() - tableY),
                    FONT_UTIL
            );
            if (sourcePosition.isPresent()) {
                return sourcePosition.getAsInt();
            }
        }
        return RichTextVisualLayoutHelper.indexForPoint(fallbackLines, point.getX(), point.getY());
    }

    private static RichTextPaneLayoutHelper.Layout<InlineControlPlacement> createLayout(
            RichText richText,
            Font font,
            boolean wrapText,
            double availableWidth,
            double displayScale
    ) {
        RichText renderText = RichTextTableHelper.replaceTablesWithInlineNodes(
                inheritParagraphIndentationForRendering(richText)
        );
        RichTextPaneLayoutHelper.LayoutPreparation prepared = RichTextPaneLayoutHelper.prepareLayout(
                renderText,
                font,
                wrapText,
                availableWidth,
                STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH,
                (run, runFont) -> {
                    Node node = createInlineNode(run, displayScale, availableWidth);
                    applyInlineNodeFont(node, runFont);
                    return node;
                },
                TextPane::measureNodeWidth,
                resolvedFont -> resolvedFont.scaled((float) displayScale)
        );

        FragmentedText renderFragments = applyParagraphIndentation(prepared.renderFragments(), displayScale);
        BlockSpacingData blockSpacingData = applyBlockVerticalSpacing(renderFragments.lines(), displayScale);
        renderFragments = new FragmentedText(
                blockSpacingData.lines(),
                renderFragments.width(),
                renderFragments.height(),
                renderFragments.baseLine(),
                renderFragments.actualWidth(),
                renderFragments.actualHeight()
        );

        List<InlineControlPlacement> placements = new ArrayList<>();
        for (List<FragmentedText.Fragment> line : renderFragments.lines()) {
            if (line.isEmpty()) {
                continue;
            }

            float lineTop = line.getFirst().y();
            float lineBottom = lineTop;
            double lineAscent = 0.0;
            for (FragmentedText.Fragment fragment : line) {
                lineBottom = Math.max(lineBottom, fragment.y() + fragment.h());
                double fragmentAscent = fragment.font().getAscent();
                if (fragment.text() instanceof Run run) {
                    fragmentAscent = getInlineReferenceAscent(run, fragment.font(), displayScale);
                }
                lineAscent = Math.max(lineAscent, fragmentAscent);
            }

            float lineHeight = Math.max(0.0f, lineBottom - lineTop);
            lineAscent = Math.clamp(lineAscent, 0.0, lineHeight);
            double lineDescent = Math.max(0.0, lineHeight - lineAscent);
            float baselineY = (float) (lineTop + lineAscent);

            for (FragmentedText.Fragment fragment : line) {
                if (fragment.text() instanceof Run run) {
                    Node node = createInlineNode(run, displayScale, availableWidth);
                    if (node != null) {
                        applyInlineNodeFont(node, fragment.font());
                        VAnchor vAnchor = getInlineNodeVAnchor(run);
                        double descent = getInlineNodeDescent(run, displayScale);
                        placements.add(new InlineControlPlacement(
                                node,
                                fragment.x(),
                                lineTop,
                                fragment.w(),
                                lineHeight,
                                baselineY,
                                fragment.font(),
                                vAnchor,
                                lineAscent,
                                lineDescent,
                                descent,
                                run.getStart(),
                                run.getEnd()
                        ));
                    }
                }
            }
        }

        LineShiftData lineShiftData = computeLineShifts(renderFragments, placements);
        Map<Float, Float> lineShiftByY = lineShiftData.lineShiftByY();
        List<InlineControlPlacement> shiftedPlacements = shiftPlacements(placements, lineShiftByY);
        List<List<FragmentedText.Fragment>> shiftedRenderLines = shiftRenderLines(renderFragments, lineShiftByY);
        float renderHeight = computeRenderedHeight(
                shiftedRenderLines,
                lineShiftData.tailOverflowBelow() + blockSpacingData.tailSpacing(),
                font.scaled((float) displayScale)
        );

        return new RichTextPaneLayoutHelper.Layout<>(
                shiftedRenderLines,
                shiftedPlacements,
                prepared.renderWidth(),
                renderHeight,
                prepared.layoutTextData()
        );
    }

    private static RichText inheritParagraphIndentationForRendering(RichText source) {
        RichText rendered = source;
        int start = 0;
        while (start < source.length()) {
            int end = start;
            while (end < source.length() && source.charAt(end) != '\n') {
                end++;
            }
            if (start < end) {
                Number indent = findLineDirectIndent(source, start, end);
                if (indent != null) {
                    rendered = rendered.apply(Map.of(Style.TEXT_INDENT_LEFT, indent), start, end);
                }
            }
            start = end + 1;
        }
        return rendered;
    }

    private static @Nullable Number findLineDirectIndent(RichText text, int start, int end) {
        for (int index = start; index < end; index++) {
            if (text.charAt(index) == RichText.SPLIT_MARKER) {
                continue;
            }
            Object indent = text.attributesAt(index).get(Style.TEXT_INDENT_LEFT);
            return indent instanceof Number number ? number : null;
        }
        return null;
    }

    private static FragmentedText applyParagraphIndentation(FragmentedText fragments, double scale) {
        List<List<FragmentedText.Fragment>> lines = new ArrayList<>();
        float maximumIndent = 0.0f;
        List<@Nullable Number> indents = fragments.lines().stream()
                .map(TextPane::findFragmentLineIndent)
                .toList();
        for (int i = 0; i < fragments.lines().size(); i++) {
            List<FragmentedText.Fragment> line = fragments.lines().get(i);
            Number value = indents.get(i);
            // Depending on the text source, an empty source line may be represented by no
            // fragments or by one zero-length run. In either case it belongs to the following
            // paragraph for indentation purposes.
            if (value == null && TextPaneSkin.isBlankVisualLine(line)) {
                value = nextIndent(indents, i + 1);
            }
            double indent = value == null ? 0.0 : value.doubleValue();
            float dx = (float) (indent * scale);
            maximumIndent = Math.max(maximumIndent, dx);
            lines.add(line.stream().map(frgmnt -> new FragmentedText.Fragment(frgmnt.x() + dx, frgmnt.y(), frgmnt.w(), frgmnt.h(), frgmnt.baseLine(), frgmnt.font(), frgmnt.text())).toList());
        }
        return new FragmentedText(lines, fragments.width() + maximumIndent, fragments.height(), fragments.baseLine(), fragments.actualWidth() + maximumIndent, fragments.actualHeight());
    }

    private static BlockSpacingData applyBlockVerticalSpacing(
            List<List<FragmentedText.Fragment>> lines,
            double displayScale
    ) {
        List<List<FragmentedText.Fragment>> adjustedLines = new ArrayList<>(lines.size());
        BlockSpacing activeSpacing = null;
        float yOffset = 0.0f;

        for (int i = 0; i < lines.size(); i++) {
            List<FragmentedText.Fragment> line = lines.get(i);
            BlockSpacing spacing = TextPaneSkin.blockSpacing(line);
            // FragmentedText represents a source-empty line either as an empty fragment list or a
            // zero-length run. Preserve a blank line *inside* one decorated block: it is content
            // of the block, not a boundary between two blocks. If it has no font metrics, use the
            // surrounding decorated line height for its visual height.
            float additionalBlankLineHeight = 0.0f;
            if (spacing == null && TextPaneSkin.isBlankVisualLine(line) && activeSpacing != null
                    && TextPaneSkin.nextNonEmptyLineHasSpacing(lines, i + 1, activeSpacing)) {
                spacing = activeSpacing;
                // An empty fragment list has no intrinsic line height. A zero-length run, on the
                // other hand, already reserves its font height, so only add what is missing.
                additionalBlankLineHeight = Math.max(0.0f,
                        TextPaneSkin.emptyLineHeight(lines, i, activeSpacing) - TextPaneSkin.lineHeight(line));
            }
            if (!Objects.equals(activeSpacing, spacing)) {
                if (activeSpacing != null) {
                    yOffset += activeSpacing.bottom() * displayScale;
                }
                if (spacing != null) {
                    yOffset += spacing.top() * displayScale;
                }
                activeSpacing = spacing;
            }

            float offset = yOffset;
            adjustedLines.add(line.stream()
                    .map(fragment -> new FragmentedText.Fragment(
                            fragment.x(),
                            fragment.y() + offset,
                            fragment.w(),
                            fragment.h(),
                            fragment.baseLine(),
                            fragment.font(),
                            fragment.text()
                    ))
                    .toList());
            yOffset += additionalBlankLineHeight;
        }

        float tailSpacing = activeSpacing == null ? 0.0f : (float) (activeSpacing.bottom() * displayScale);
        return new BlockSpacingData(adjustedLines, tailSpacing);
    }

    private static @Nullable Number findFragmentLineIndent(List<FragmentedText.Fragment> line) {
        for (FragmentedText.Fragment fragment : line) {
            if (!(fragment.text() instanceof Run run) || isStructuralMarker(run)) {
                continue;
            }
            Object directIndent = run.getAttributes().get(Style.TEXT_INDENT_LEFT);
            if (directIndent instanceof Number number) {
                return number;
            }
            for (int i = run.getStyles().size() - 1; i >= 0; i--) {
                Object styleIndent = run.getStyles().get(i).get(Style.TEXT_INDENT_LEFT);
                if (styleIndent instanceof Number number) {
                    return number;
                }
            }
        }
        return null;
    }

    private static boolean isStructuralMarker(Run run) {
        return run.isEmpty() && run.codePoints().allMatch(codePoint -> codePoint == RichText.SPLIT_MARKER);
    }

    private static double nextIndent(List<@Nullable Number> indents, int start) {
        for (int i = start; i < indents.size(); i++) {
            Number indent = indents.get(i);
            if (indent != null) {
                return indent.doubleValue();
            }
        }
        return 0.0;
    }

    private static double measureNodeWidth(Node node) {
        if (node.getScene() == null) {
            Pane tempRoot = new Pane(node);
            // Attach temporarily so CSS/skin-dependent preferred sizes are available.
            LangUtil.ignore(new Scene(tempRoot));
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

    private static void applyInlineNodeFont(@Nullable Node node, Font font) {
        switch (node) {
            case TableNode tableNode -> tableNode.setTableFont(font);
            case Labeled labeled -> labeled.setFont(FxFontUtil.getInstance().convert(font));
            case null, default -> {/* do nothing */}
        }
    }

    private static @Nullable Node createInlineNode(Run run, double displayScale, double availableWidth) {
        if (TextUtil.isWhitespaceOnly(run)) {
            return null;
        }
        String text = inlineText(run);
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);

            Object factory = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY);
            if (factory instanceof Function<?, ?> f) {
                @SuppressWarnings("unchecked")
                Function<String, ?> fn = (Function<String, ?>) f;
                Node node = toFxInlineNode(fn.apply(text), style, displayScale, availableWidth);
                if (node != null) {
                    return node;
                }
            }

            Node node = toFxInlineNode(
                    style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE), style, displayScale, availableWidth
            );
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private static String inlineText(Run run) {
        String text = run.toString();
        if (getInlineLeadingWidth(run) <= 0.0) {
            return text;
        }

        int index = 0;
        while (index < text.length() && text.charAt(index) == '\u00A0') {
            index++;
        }
        return text.substring(index);
    }

    private static @Nullable Node toFxInlineNode(
            @Nullable Object value,
            Style style,
            double displayScale,
            double availableWidth
    ) {
        double maxWidth = getPositiveStyleValue(style, RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH);
        double maxHeight = getPositiveStyleValue(style, RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT);

        Object wrapped = value;
        switch (wrapped) {
            case RichTextTableHelper.InlineTable inlineTable -> {
                return new TableNode(inlineTable.table(), tableAvailableWidth(availableWidth));
            }
            case InlineNode<?> inlineNode -> {
                if (RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_BUTTON.equals(inlineNode.getMimeType())) {
                    RichTextBuilderExtBase.ButtonData buttonData = RichTextBuilderExtBase.decodeInlineButtonData(inlineNode.getData());
                    String text = inlineNode.getWrapped() instanceof CharSequence cs && !cs.isEmpty()
                            ? cs.toString()
                            : (buttonData.text().isBlank() ? buttonData.target() : buttonData.text());
                    Button button = new Button(text);
                    button.setFocusTraversable(false);
                    toUri(buttonData.target()).ifPresent(button::setUserData);
                    return button;
                }
                if (RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_HYPERLINK.equals(inlineNode.getMimeType())) {
                    RichTextBuilderExtBase.HyperlinkData hyperlinkData = RichTextBuilderExtBase.decodeInlineHyperlinkData(inlineNode.getData());
                    String text = inlineNode.getWrapped() instanceof CharSequence cs && !cs.isEmpty()
                            ? cs.toString()
                            : (hyperlinkData.text().isBlank() ? hyperlinkData.target() : hyperlinkData.text());
                    Hyperlink hyperlink = new Hyperlink(text);
                    hyperlink.setFocusTraversable(false);
                    toUri(hyperlinkData.target()).ifPresent(hyperlink::setUserData);
                    return hyperlink;
                }
                wrapped = inlineNode.getWrapped();
            }
            case null, default -> {/* do nothing */}
        }

        return switch (wrapped) {
            case Node node -> {
                applyImageViewScaling(node, maxWidth, maxHeight, displayScale);
                yield node;
            }
            case Image image -> {
                ImageView imageView = new ImageView(FxImageUtil.getInstance().toImage(image).fxImage());
                applyImageViewScaling(imageView, maxWidth, maxHeight, displayScale);
                yield imageView;
            }
            case null, default -> null;
        };
    }

    private static float tableAvailableWidth(double availableWidth) {
        return Double.isFinite(availableWidth) && availableWidth > 0.0
                ? (float) Math.min(Float.MAX_VALUE, availableWidth)
                : Float.MAX_VALUE;
    }

    private static final class TableNode extends Region {
        private static final float CELL_PADDING = 4.0f;

        private final RichTextTableHelper.Table table;
        private final float availableWidth;
        private final Canvas canvas = new Canvas();
        private Font tableFont = FONT_UTIL.getDefaultFont();

        private TableNode(RichTextTableHelper.Table table, float availableWidth) {
            this.table = table;
            this.availableWidth = availableWidth;
            getChildren().add(canvas);
        }

        private void setTableFont(Font font) {
            tableFont = font;
            requestLayout();
        }

        @Override
        protected double computePrefWidth(double height) {
            return tableLayout().bounds().width();
        }

        @Override
        protected double computePrefHeight(double width) {
            return tableLayout().bounds().height();
        }

        @Override
        protected void layoutChildren() {
            RichTextTableHelper.TableLayout layout = tableLayout();
            canvas.setWidth(Math.max(1.0, layout.bounds().width()));
            canvas.setHeight(Math.max(1.0, layout.bounds().height()));
            canvas.relocate(0.0, 0.0);
            try (Graphics graphics = new FxGraphics(canvas)) {
                graphics.reset();
                RichTextTableRenderer.render(graphics, layout);
            }
        }

        private RichTextTableHelper.TableLayout tableLayout() {
            return RichTextTableHelper.layout(
                    table,
                    FONT_UTIL,
                    tableFont,
                    availableWidth,
                    availableWidth < Float.MAX_VALUE,
                    CELL_PADDING
            );
        }
    }

    private static double getPositiveStyleValue(Style style, String key) {
        Object value = style.get(key);
        if (value instanceof Number n) {
            double d = n.doubleValue();
            if (Double.isFinite(d) && d > 0.0) {
                return d;
            }
        }
        return Double.NaN;
    }

    private static void applyImageViewScaling(Node node, double maxWidth, double maxHeight, double displayScale) {
        if (!(node instanceof ImageView imageView)) {
            return;
        }

        boolean hasWidth = Double.isFinite(maxWidth) && maxWidth > 0.0;
        boolean hasHeight = Double.isFinite(maxHeight) && maxHeight > 0.0;
        ImageViewBaseSettings base = getImageViewBaseSettings(imageView);
        if (hasWidth || hasHeight) {
            imageView.setPreserveRatio(true);
            if (hasWidth) {
                imageView.setFitWidth(Math.max(1.0, maxWidth * displayScale));
            }
            if (hasHeight) {
                imageView.setFitHeight(Math.max(1.0, maxHeight * displayScale));
            }
            imageView.setSmooth(true);
            return;
        }

        if (displayScale == 1.0) {
            imageView.setPreserveRatio(base.preserveRatio());
            imageView.setFitWidth(base.fitWidth());
            imageView.setFitHeight(base.fitHeight());
            return;
        }

        double imageWidth = imageView.getImage() == null ? 0.0 : imageView.getImage().getWidth();
        double imageHeight = imageView.getImage() == null ? 0.0 : imageView.getImage().getHeight();
        double baseWidth = base.fitWidth() > 0.0 ? base.fitWidth() : imageWidth;
        double baseHeight = base.fitHeight() > 0.0 ? base.fitHeight() : imageHeight;
        if (baseWidth <= 0.0 && baseHeight <= 0.0) {
            return;
        }

        imageView.setPreserveRatio(true);
        if (baseWidth > 0.0) {
            imageView.setFitWidth(Math.max(1.0, baseWidth * displayScale));
        }
        if (baseHeight > 0.0) {
            imageView.setFitHeight(Math.max(1.0, baseHeight * displayScale));
        }
    }

    private static ImageViewBaseSettings getImageViewBaseSettings(ImageView imageView) {
        Object cached = imageView.getProperties().get(IMAGE_VIEW_BASE_SETTINGS);
        if (cached instanceof ImageViewBaseSettings settings) {
            return settings;
        }

        ImageViewBaseSettings settings = new ImageViewBaseSettings(
                imageView.getFitWidth(),
                imageView.getFitHeight(),
                imageView.isPreserveRatio()
        );
        imageView.getProperties().put(IMAGE_VIEW_BASE_SETTINGS, settings);
        return settings;
    }

    private static Optional<URI> toUri(@Nullable Object value) {
        return switch (value) {
            case URI uri -> Optional.of(uri);
            case CharSequence cs -> {
                String text = cs.toString().trim();
                if (text.isEmpty()) {
                    yield Optional.empty();
                }
                try {
                    yield Optional.of(new URI(text));
                } catch (URISyntaxException ex) {
                    yield Optional.empty();
                }
            }
            case null, default -> Optional.empty();
        };
    }

    private static void wireButtonAction(TextPane control, ButtonBase button) {
        if (button.getOnAction() != null) {
            return;
        }

        Optional<URI> target = toUri(button.getUserData());
        if (target.isEmpty()) {
            return;
        }

        button.setOnAction(evt -> control.getHyperlinkHandler().accept(target.get()));
    }

    private static void openUriUsingDesktop(URI uri) {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        try {
            String scheme = uri.getScheme();
            if ("mailto".equalsIgnoreCase(scheme) && desktop.isSupported(Desktop.Action.MAIL)) {
                desktop.mail(uri);
            } else if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // ignore failures from user-supplied or unsupported URI schemes
        }
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

    private static double getInlineReferenceValue(Run run, String styleName, DoubleSupplier fallbackValueSupplier) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);
            Object value = style.get(styleName);
            if (value instanceof Number n) {
                return n.doubleValue();
            }
        }
        return fallbackValueSupplier.getAsDouble();
    }

    private static double getInlineReferenceAscent(Run run, Font fallbackFont, double displayScale) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Object value = run.getStyles().get(i).get(STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT);
            if (value instanceof Number n) {
                return n.doubleValue() * displayScale;
            }
        }
        return fallbackFont.getAscent();
    }

    private static double getInlineNodeDescent(Run run, double displayScale) {
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Object value = run.getStyles().get(i).get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT);
            if (value instanceof Number n) {
                return n.doubleValue() * displayScale;
            }
        }
        return Double.NaN;
    }

    private static double getInlineLeadingWidth(Run run) {
        return getInlineReferenceValue(run, STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH, () -> 0.0);
    }

    private static double computeInlineDescent(InlineControlPlacement placement, double prefH, double baselineOffset) {
        return Double.isFinite(placement.descent())
                ? placement.descent()
                : (baselineOffset != BASELINE_OFFSET_SAME_AS_HEIGHT && Double.isFinite(baselineOffset)
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
                    placement.descent(),
                    placement.runStart(),
                    placement.runEnd()
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
            double descent,
            int runStart,
            int runEnd
    ) {}

    private record ImageViewBaseSettings(double fitWidth, double fitHeight, boolean preserveRatio) {}

    private record LineShiftData(Map<Float, Float> lineShiftByY, float tailOverflowBelow) {}

    private record BlockSpacingData(List<List<FragmentedText.Fragment>> lines, float tailSpacing) {}

    private record BlockDecoration(Color backgroundColor, Color borderColor, float borderWidth, float padding) {}

    private record BlockDecorationArea(BlockDecoration decoration, Object blockId, float left, float top, float bottom) {}

    private record BlockDecorationData(BlockDecoration decoration, Object blockId) {}

    private record BlockSpacing(Object blockId, float top, float bottom) {}

    private static final class TextPaneSkin extends SkinBase<TextPane> {
        private static final double CARET_AUTOSCROLL_MARGIN = 6.0;
        private static final double DRAG_AUTOSCROLL_EDGE = 18.0;
        private static final double DRAG_AUTOSCROLL_TICK_MS = 40.0;
        private static final SequencedCollection<String> AVAILABLE_FONTS = FxFontUtil.getInstance().getFamilies(FontUtil.FontTypes.ALL);
        private static final Float[] DEFAULT_FONT_SIZES = {8.0f, 9.0f, 10.0f, 11.0f, 12.0f, 14.0f, 16.0f, 18.0f, 20.0f, 24.0f, 28.0f, 32.0f, 36.0f, 40.0f, 48.0f, 56.0f, 64.0f};
        private static final Color[] DEFAULT_TEXT_COLORS = {
                Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
                Color.RED.darker(), Color.RED, Color.RED.brighter(),
                Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
                Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
                Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
                Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
                Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
        };
        private static final Color[] DEFAULT_BACKGROUND_COLORS = {
                Color.TRANSPARENT_WHITE,
                Color.BLACK, Color.DARKGRAY, Color.GRAY, Color.LIGHTGRAY, Color.WHITE,
                Color.RED.darker(), Color.RED, Color.RED.brighter(),
                Color.GREEN.darker(), Color.GREEN, Color.GREEN.brighter(),
                Color.BLUE.darker(), Color.BLUE, Color.BLUE.brighter(),
                Color.YELLOW.darker(), Color.YELLOW, Color.YELLOW.brighter(),
                Color.DARKCYAN, Color.DARKCYAN.brighter(), Color.LIGHTCYAN,
                Color.DARKMAGENTA, Color.DARKMAGENTA.brighter(), Color.DARKMAGENTA.brighter().brighter()
        };

        private final Pane contentPane = new Pane();
        private final Pane selectionLayer = new Pane();
        private final Canvas canvas = new Canvas();
        private final Pane inlineLayer = new Pane();
        private final Pane caretLayer = new Pane();
        private final Group scrollContent = new Group(contentPane);
        private final ScrollPane scrollPane = new ScrollPane(scrollContent);
        private final VBox editorRoot = new VBox();
        private volatile boolean dirty = true;
        private double lastAvailableWidth = Double.NaN;
        private double lastDisplayScale = Double.NaN;
        private RichText lastText = RichText.emptyText();
        private Font lastFont = FontUtil.getInstance().getDefaultFont();
        private boolean lastWrapText;
        private boolean blink = true;
        private @Nullable Rectangle caretNode;
        private final @Nullable TextEditorPane editor;
        private final Timeline caretTimeline;
        private final Timeline dragAutoscrollTimeline;

        private volatile boolean caretVisibilityRequested;
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
            caretTimeline.setCycleCount(Animation.INDEFINITE);

            dragAutoscrollTimeline = new Timeline(
                    new KeyFrame(Duration.millis(DRAG_AUTOSCROLL_TICK_MS), e -> autoScrollDuringSelectionDrag())
            );
            dragAutoscrollTimeline.setCycleCount(Animation.INDEFINITE);

            contentPane.getStyleClass().add("content");
            inlineLayer.setManaged(false);
            selectionLayer.setManaged(false);
            caretLayer.setManaged(false);
            selectionLayer.setMouseTransparent(true);
            caretLayer.setMouseTransparent(true);
            // Keep selection overlay above text and inline nodes so selection stays visible
            // even when text background colors or inline controls are present.
            contentPane.getChildren().setAll(canvas, inlineLayer, selectionLayer, caretLayer);
            contentPane.setMinSize(0.0, 0.0);
            contentPane.setPrefSize(0.0, 0.0);

            scrollPane.setFitToWidth(control.isWrapText());
            scrollPane.setHbarPolicy(control.isWrapText() ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setFitToHeight(false);
            scrollPane.setMinSize(0.0, 0.0);
            scrollPane.setPrefViewportWidth(0.0);
            scrollPane.setPrefViewportHeight(0.0);
            scrollPane.setMinViewportWidth(0.0);
            scrollPane.setMinViewportHeight(0.0);

            editorRoot.setMinSize(0.0, 0.0);

            if (control instanceof TextEditorPane tep) {
                Button copyButton = createButton("Copy", Controls.graphic(Feather.COPY.getDescription()), tep, TextEditorPane::copy);
                Button cutButton = createButton("Cut", Controls.graphic(Feather.SCISSORS.getDescription()), tep, TextEditorPane::cut);
                Button pasteButton = createButton("Paste", Controls.graphic(Feather.CLIPBOARD.getDescription()), tep, TextEditorPane::paste);

                Button undoButton = createButton("Undo", Controls.graphic(Feather.ROTATE_CCW.getDescription()), tep, TextEditorPane::undo);
                Button redoButton = createButton("Redo", Controls.graphic(Feather.ROTATE_CW.getDescription()), tep, TextEditorPane::redo);
                Button decreaseIndentButton = createButton("Decrease indentation", Controls.graphic(Feather.ARROW_LEFT.getDescription()), tep, TextEditorPane::decreaseIndentation);
                Button increaseIndentButton = createButton("Increase indentation", Controls.graphic(Feather.ARROW_RIGHT.getDescription()), tep, TextEditorPane::increaseIndentation);

                ToggleButton boldButton = createToggleButton("Bold", Controls.graphic(Feather.BOLD.getDescription()), tep, TextEditorPane::markBold);
                ToggleButton italicsButton = createToggleButton("Italic", Controls.graphic(Feather.ITALIC.getDescription()), tep, TextEditorPane::markItalic);
                ToggleButton underlineButton = createToggleButton("Underline", Controls.graphic(Feather.UNDERLINE.getDescription()), tep, TextEditorPane::markUnderline);
                ToggleButton strikeThroughButton = createToggleButton("Strike Through", Controls.graphic(Feather.MINUS.getDescription()), tep, TextEditorPane::markStrikeThrough);

                ComboBoxEx<String> fontList = Controls.comboBoxEx(AVAILABLE_FONTS).build();
                ComboBoxEx<Float> sizeList = Controls.comboBoxEx(DEFAULT_FONT_SIZES).build();
                ComboBoxEx<Color> textColorList = Controls.comboBoxEx(DEFAULT_TEXT_COLORS)
                        .defaultValue(() -> Color.BLACK)
                        .format(color -> LangUtil.mapNonNullOrElse(color, Color::toArgb, ""))
                        .graphic(color -> new Rectangle(16, 16, FxUtil.convert(color)))
                        .build();
                ComboBoxEx<Color> backgroundColorList = Controls.comboBoxEx(DEFAULT_BACKGROUND_COLORS)
                        .defaultValue(() -> Color.TRANSPARENT_WHITE)
                        .format(color -> LangUtil.mapNonNullOrElse(color, Color::toArgb, ""))
                        .graphic(color -> new Rectangle(16, 16, FxUtil.convert(color)))
                        .build();

                copyButton.setFocusTraversable(false);
                cutButton.setFocusTraversable(false);
                pasteButton.setFocusTraversable(false);
                undoButton.setFocusTraversable(false);
                redoButton.setFocusTraversable(false);
                boldButton.setFocusTraversable(false);
                italicsButton.setFocusTraversable(false);
                underlineButton.setFocusTraversable(false);
                strikeThroughButton.setFocusTraversable(false);
                fontList.setFocusTraversable(false);
                sizeList.setFocusTraversable(false);
                textColorList.setFocusTraversable(false);
                backgroundColorList.setFocusTraversable(false);

                boldButton.selectedProperty().bindBidirectional(tep.boldProperty());
                italicsButton.selectedProperty().bindBidirectional(tep.italicProperty());
                underlineButton.selectedProperty().bindBidirectional(tep.underlineProperty());
                strikeThroughButton.selectedProperty().bindBidirectional(tep.strikeThroughProperty());
                bindFontLists(tep, fontList, sizeList, textColorList, backgroundColorList);
                undoButton.disableProperty().bind(tep.undoableProperty().not());
                redoButton.disableProperty().bind(tep.redoableProperty().not());

                ToolBarEx toolbar = Controls.toolBar()
                        .items(
                                cutButton,
                                copyButton,
                                pasteButton,
                                new Separator(),
                                undoButton,
                                redoButton,
                                new Separator(),
                                decreaseIndentButton,
                                increaseIndentButton,
                                new Separator(),
                                fontList,
                                sizeList,
                                boldButton,
                                italicsButton,
                                underlineButton,
                                strikeThroughButton,
                                textColorList,
                                backgroundColorList
                        )
                        .focusTraversable(false)
                        .bindLocation(tep.toolbarLocationProperty())
                        .bindApplicationParent(tep.toolbarApplicationParentProperty())
                        .build();

                VBox.setVgrow(scrollPane, Priority.ALWAYS);
                editorRoot.getChildren().setAll(toolbar, scrollPane);
                getChildren().setAll(editorRoot);
            } else {
                getChildren().setAll(scrollPane);
            }

            control.textProperty().addListener((obs, oldVal, newVal) -> invalidate());
            if (editor != null) {
                // Some editor changes update textProperty after the model version. Listen to
                // both signals so a repaint always observes the current RichText snapshot.
                editor.documentVersionProperty().addListener((obs, oldVal, newVal) -> invalidate());
            }
            control.wrapTextProperty().addListener((obs, oldVal, newVal) -> {
                scrollPane.setFitToWidth(newVal);
                scrollPane.setHbarPolicy(newVal == Boolean.TRUE ? ScrollPane.ScrollBarPolicy.NEVER : ScrollPane.ScrollBarPolicy.AS_NEEDED);
                invalidate();
            });
            control.fontProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.displayScaleProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.widthProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.heightProperty().addListener((obs, oldVal, newVal) -> invalidate());
            control.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> invalidate());

            if (control instanceof TextEditorPane tep) {
                caretVisibilityRequested = true;
                tep.selectionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    invalidate();
                });
                tep.caretPositionProperty().addListener((obs, oldVal, newVal) -> {
                    restartCaretAnimation();
                    requestCaretVisibility();
                    invalidate();
                });
                tep.editableProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
                tep.toolbarLocationProperty().addListener((obs, oldVal, newVal) -> invalidate());

                // Route interaction through the internal ScrollPane so input works regardless of focus owner.
                scrollPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
                    tep.processMousePressed(evt);
                    stopSelectionDragAutoscroll();
                });
                scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
                    tep.processMouseDragged(evt);
                    updateSelectionDragAutoscroll(evt);
                });
                scrollPane.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> stopSelectionDragAutoscroll());
                scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, tep::processKeyPressed);
                scrollPane.addEventFilter(KeyEvent.KEY_TYPED, tep::processKeyTyped);
                scrollPane.focusedProperty().addListener((obs, oldVal, newVal) -> updateCaretAnimationState());
            }

            updateCaretAnimationState();
        }

        private static Button createButton(String text, Node graphic, TextEditorPane editor, Consumer<TextEditorPane> action) {
            Button button = Controls.button()
                    .tooltip(text)
                    .graphic(graphic)
                    .action(e -> {
                        action.accept(editor);
                        editor.requestFocus();
                    })
                    .build();
            button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> editor.requestFocus());
            return button;
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
                ComboBoxEx<Color> textColorList,
                ComboBoxEx<Color> backgroundColorList
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
                editor.requestFocus();
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
                editor.requestFocus();
            });

            editor.textColorProperty().addListener((obs, oldValue, newValue) ->
                    synchronizeFromEditor(synchronizing, () -> {
                        if (newValue == null) {
                            return;
                        }

                        ensureValuePresent(textColorList, newValue);
                        if (!Objects.equals(textColorList.valueProperty().getValue(), newValue)) {
                            textColorList.valueProperty().setValue(newValue);
                        }
                    }));

            textColorList.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (synchronizing.get() || newValue == null) {
                    return;
                }
                if (!Objects.equals(editor.getTextColor(), newValue)) {
                    editor.setTextColor(newValue);
                }
                editor.requestFocus();
            });

            editor.backgroundColorProperty().addListener((obs, oldValue, newValue) ->
                    synchronizeFromEditor(synchronizing, () -> {
                        if (newValue == null) {
                            return;
                        }

                        ensureValuePresent(backgroundColorList, newValue);
                        if (!Objects.equals(backgroundColorList.valueProperty().getValue(), newValue)) {
                            backgroundColorList.valueProperty().setValue(newValue);
                        }
                    }));

            backgroundColorList.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (synchronizing.get() || newValue == null) {
                    return;
                }
                if (!Objects.equals(editor.getBackgroundColor(), newValue)) {
                    editor.setBackgroundColor(newValue);
                }
                editor.requestFocus();
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
                    ensureValuePresent(textColorList, currentColor);
                    if (!Objects.equals(textColorList.valueProperty().getValue(), currentColor)) {
                        textColorList.valueProperty().setValue(currentColor);
                    }
                }

                Color currentBackgroundColor = editor.getBackgroundColor();
                if (currentBackgroundColor != null) {
                    ensureValuePresent(backgroundColorList, currentBackgroundColor);
                    if (!Objects.equals(backgroundColorList.valueProperty().getValue(), currentBackgroundColor)) {
                        backgroundColorList.valueProperty().setValue(currentBackgroundColor);
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

            List<VisualLine> lines = editor.buildVisualLines(getAvailableWidth());
            if (lines.isEmpty()) {
                return;
            }

            Bounds viewport = getViewportSceneBounds();
            double sceneX = dragSceneX;
            if (viewport != null) {
                sceneX = Math.clamp(sceneX, viewport.getMinX(), viewport.getMaxX());
            }

            Point2D point = contentPane.sceneToLocal(sceneX, dragSceneY);
            int caret = getSkinnable().sourcePositionForPoint(point, getAvailableWidth(), lines);
            editor.selectPositionCaret(caret);
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
            int caret = editor.getCaretPosition();
            for (Node node : inlineLayer.getChildren()) {
                if (!(node instanceof TableNode tableNode)) {
                    continue;
                }
                RichTextTableHelper.Table table = tableNode.tableLayout().table();
                if (caret < table.start() || caret >= table.end()) {
                    continue;
                }
                RichTextTableHelper.caretForSourcePosition(tableNode.tableLayout(), caret, FONT_UTIL)
                        .ifPresent(tableCaret -> {
                            Bounds bounds = tableNode.getBoundsInParent();
                            scrollHorizontallyToInclude(bounds.getMinX() + tableCaret.x(), 1.0);
                            scrollVerticallyToInclude(
                                    bounds.getMinY() + tableCaret.y(),
                                    bounds.getMinY() + tableCaret.y() + tableCaret.height()
                            );
                        });
                return;
            }
            List<VisualLine> lines = editor.buildVisualLines(availableWidth);
            if (lines.isEmpty()) {
                return;
            }

            int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caret);
            if (lineIndex < 0 || lineIndex >= lines.size()) {
                return;
            }

            VisualLine line = lines.get(lineIndex);
            double caretX = RichTextVisualLayoutHelper.xForIndex(line, caret);
            double caretWidth = Math.max(1.0, editor.getFont().getFontData().spaceWidth() * editor.getDisplayScale());

            scrollHorizontallyToInclude(caretX, caretWidth);
            scrollVerticallyToInclude(line.top(), line.top() + line.height());
        }

        private boolean scrollHorizontallyToInclude(double x, double width) {
            Bounds viewport = scrollPane.getViewportBounds();
            double viewportWidth = viewport.getWidth();
            double contentWidth = Math.max(contentPane.getBoundsInParent().getWidth(), canvas.getWidth());
            double maxOffset = Math.max(0.0, contentWidth - viewportWidth);
            if (viewportWidth <= 0.0 || maxOffset <= 0.0) {
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
            double contentHeight = Math.max(contentPane.getBoundsInParent().getHeight(), canvas.getHeight());
            double maxOffset = Math.max(0.0, contentHeight - viewportHeight);
            if (viewportHeight <= 0.0 || maxOffset <= 0.0) {
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
            double contentHeight = Math.max(contentPane.getBoundsInParent().getHeight(), canvas.getHeight());
            double maxOffset = Math.max(0.0, contentHeight - viewportHeight);
            if (viewportHeight <= 0.0 || maxOffset <= 0.0) {
                return false;
            }

            double currentOffset = scrollPane.getVvalue() * maxOffset;
            double targetOffset = Math.clamp(currentOffset + delta, 0.0, maxOffset);
            if (Math.abs(targetOffset - currentOffset) < 0.5) {
                return false;
            }

            scrollPane.setVvalue(targetOffset / maxOffset);
            return true;
        }

        private void invalidate() {
            dirty = true;
            getSkinnable().requestLayout();
        }

        /**
         * Releases animation resources owned by this skin.
         */
        @Override
        public void dispose() {
            caretTimeline.stop();
            dragAutoscrollTimeline.stop();
            super.dispose();
        }

        private void setBlink(boolean value) {
            if (blink != value) {
                blink = value;
                // Blinking only changes the overlay's paint state. Opacity (rather than
                // visibility) keeps the caret in the parent's bounds, so ScrollPane does
                // not recompute its content bounds on every blink.
                if (caretNode != null) {
                    caretNode.setOpacity(blink ? 0.0 : 1.0);
                }
            }
        }

        private boolean hasEditorFocus(TextPane control) {
            return control.isFocused() || scrollPane.isFocused();
        }

        private boolean shouldAnimateCaret() {
            TextPane control = getSkinnable();
            if (!(control instanceof TextEditorPane tep)) {
                return false;
            }
            return tep.isEditable() && hasEditorFocus(control);
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

        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
            prepareContentForPreferredHeight(width, rightInset, leftInset);
            return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        private void prepareContentForPreferredHeight(double width, double rightInset, double leftInset) {
            TextPane control = getSkinnable();
            double visualWidth = width > 0.0
                    ? Math.max(1.0, width - leftInset - rightInset)
                    : Math.max(1.0, control.computePrefWidth(-1));
            double availableWidth = control.isWrapText() ? visualWidth : 1.0;
            RichTextPaneLayoutHelper.Layout<InlineControlPlacement> layout = control.createLayout(availableWidth);
            double contentWidth = Math.max(1.0, Math.ceil(layout.width()));
            double contentHeight = Math.max(1.0, Math.ceil(layout.height()));

            if (Math.abs(contentPane.getWidth() - contentWidth) > 0.5
                    || Math.abs(contentPane.getHeight() - contentHeight) > 0.5) {
                contentPane.setMinSize(contentWidth, contentHeight);
                contentPane.setPrefSize(contentWidth, contentHeight);
                contentPane.resize(contentWidth, contentHeight);
                dirty = true;
            }
        }

        private void refreshIfNeeded() {
            TextPane control = getSkinnable();
            double availableWidth = getAvailableWidth();
            double displayScale = control.getDisplayScale();
            RichText text = control.getText();
            Font font = control.getFont();
            boolean wrapText = control.isWrapText();
            boolean widthChanged = !Double.isFinite(lastAvailableWidth) || Math.abs(lastAvailableWidth - availableWidth) > 0.5;
            boolean displayScaleChanged = !Double.isFinite(lastDisplayScale) || Math.abs(lastDisplayScale - displayScale) > 1.0e-6;
            boolean textChanged = !Objects.equals(lastText, text);
            boolean fontChanged = !Objects.equals(lastFont, font);
            boolean wrapChanged = lastWrapText != wrapText;
            boolean geometryChanged = widthChanged || displayScaleChanged || textChanged || fontChanged || wrapChanged;
            if (editor != null && textChanged && editor.getCaretPosition() == editor.getLength()) {
                caretVisibilityRequested = true;
            }
            if (!dirty
                    && !widthChanged
                    && !displayScaleChanged
                    && Objects.equals(lastText, text)
                    && Objects.equals(lastFont, font)
                    && lastWrapText == wrapText) {
                return;
            }

            refresh(availableWidth, !geometryChanged);
            dirty = false;
            lastAvailableWidth = availableWidth;
            lastDisplayScale = displayScale;
            lastText = text;
            lastFont = font;
            lastWrapText = wrapText;
        }

        private double getAvailableWidth() {
            TextPane control = getSkinnable();
            Bounds vp = scrollPane.getViewportBounds();
            double availableWidth = control.isWrapText() ? vp.getWidth() : 1.0;
            boolean viewportWidthUnavailable = !Double.isFinite(availableWidth) || availableWidth <= 0.0;
            if (control.isWrapText() && viewportWidthUnavailable) {
                availableWidth = control.getWidth() - control.snappedLeftInset() - control.snappedRightInset();
            }
            if (!Double.isFinite(availableWidth) || availableWidth <= 0.0) {
                availableWidth = Math.max(1.0, control.computePrefWidth(-1));
            }
            return availableWidth;
        }

        private void refresh(double availableWidth, boolean preserveContentHeight) {
            TextPane control = getSkinnable();
            RichTextPaneLayoutHelper.Layout<InlineControlPlacement> layout = control.createLayout(availableWidth);
            double previousCanvasHeight = canvas.getHeight();

            canvas.setWidth(Math.max(1.0, Math.ceil(layout.width())));
            double layoutHeight = Math.max(1.0, Math.ceil(layout.height()));
            if (preserveContentHeight) {
                layoutHeight = Math.max(layoutHeight, previousCanvasHeight);
            }
            canvas.setHeight(layoutHeight);
            contentPane.setMinSize(canvas.getWidth(), canvas.getHeight());
            contentPane.setPrefSize(canvas.getWidth(), canvas.getHeight());
            contentPane.resize(canvas.getWidth(), canvas.getHeight());

            ensureEditorContentHeight(control, availableWidth, previousCanvasHeight);

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
                if (node instanceof ButtonBase button) {
                    wireButtonAction(control, button);
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

            renderEditorOverlay(control, availableWidth, layout);
            if (editor != null && caretVisibilityRequested) {
                Bounds viewport = scrollPane.getViewportBounds();
                if (viewport.getWidth() > 1.0 && viewport.getHeight() > 1.0) {
                    ensureCaretVisible(editor, availableWidth);
                } else if (editor.getCaretPosition() == editor.getLength()) {
                    scrollPane.setVvalue(1.0);
                }
                caretVisibilityRequested = false;
            }

            try (Graphics graphics = new FxGraphics(canvas)) {
                graphics.reset();
                graphics.setFont(control.getFont().scaled((float) control.getDisplayScale()));
                renderBlockDecorations(graphics, layout.renderLines(), (float) layout.width(), control.getDisplayScale());
                RichTextRenderer.renderFragmentLines(graphics, layout.renderLines(), TextPaneSkin::isInvisibleInlinePlaceholder);
            }
        }

        private static void renderBlockDecorations(
                Graphics graphics,
                List<List<FragmentedText.Fragment>> lines,
                float contentWidth,
                double displayScale
        ) {
            for (BlockDecorationArea area : blockDecorationAreas(lines)) {
                renderBlockDecoration(graphics, area, contentWidth, displayScale);
            }
        }

        /**
         * Finds the contiguous painted areas for decorated blocks.
         *
         * <p>A blank visual line has no style-bearing content, so bridge it only when the next
         * actual line belongs to the same semantic block.</p>
         */
        private static List<BlockDecorationArea> blockDecorationAreas(List<List<FragmentedText.Fragment>> lines) {
            List<BlockDecorationArea> areas = new ArrayList<>();
            BlockDecorationArea area = null;
            for (int i = 0; i < lines.size(); i++) {
                List<FragmentedText.Fragment> line = lines.get(i);
                BlockDecorationData decorationData = blockDecorationData(line);
                if (decorationData == null) {
                    if (isBlankVisualLine(line) && area != null && nextNonEmptyLineHasDecoration(lines, i + 1, area)) {
                        continue;
                    }
                    if (area != null) {
                        areas.add(area);
                        area = null;
                    }
                    continue;
                }

                float lineTop = line.stream().map(FragmentedText.Fragment::y).min(Float::compare).orElse(0.0f);
                float lineBottom = line.stream()
                        .map(fragment -> fragment.y() + fragment.h())
                        .max(Float::compare)
                        .orElse(lineTop);
                float lineLeft = line.stream().map(FragmentedText.Fragment::x).min(Float::compare).orElse(0.0f);

                if (area == null
                        || !area.decoration().equals(decorationData.decoration())
                        || !Objects.equals(area.blockId(), decorationData.blockId())) {
                    if (area != null) {
                        areas.add(area);
                    }
                    area = new BlockDecorationArea(decorationData.decoration(), decorationData.blockId(), lineLeft, lineTop, lineBottom);
                } else {
                    area = new BlockDecorationArea(area.decoration(), area.blockId(), Math.min(area.left(), lineLeft), area.top(), lineBottom);
                }
            }
            if (area != null) {
                areas.add(area);
            }
            return areas;
        }

        private static boolean nextNonEmptyLineHasDecoration(
                List<List<FragmentedText.Fragment>> lines,
                int start,
                BlockDecorationArea area
        ) {
            for (int i = start; i < lines.size(); i++) {
                List<FragmentedText.Fragment> line = lines.get(i);
                if (isBlankVisualLine(line)) {
                    continue;
                }
                BlockDecorationData data = blockDecorationData(line);
                return data != null
                        && area.decoration().equals(data.decoration())
                        && Objects.equals(area.blockId(), data.blockId());
            }
            return false;
        }

        private static boolean nextNonEmptyLineHasSpacing(
                List<List<FragmentedText.Fragment>> lines,
                int start,
                BlockSpacing spacing
        ) {
            for (int i = start; i < lines.size(); i++) {
                List<FragmentedText.Fragment> line = lines.get(i);
                if (isBlankVisualLine(line)) {
                    continue;
                }
                return Objects.equals(spacing, blockSpacing(line));
            }
            return false;
        }

        private static float emptyLineHeight(
                List<List<FragmentedText.Fragment>> lines,
                int emptyLineIndex,
                BlockSpacing activeSpacing
        ) {
            for (int i = emptyLineIndex - 1; i >= 0; i--) {
                List<FragmentedText.Fragment> line = lines.get(i);
                if (!isBlankVisualLine(line) && Objects.equals(activeSpacing, blockSpacing(line))) {
                    return lineHeight(line);
                }
            }
            for (int i = emptyLineIndex + 1; i < lines.size(); i++) {
                List<FragmentedText.Fragment> line = lines.get(i);
                if (!isBlankVisualLine(line) && Objects.equals(activeSpacing, blockSpacing(line))) {
                    return lineHeight(line);
                }
            }
            return 0.0f;
        }

        private static float lineHeight(List<FragmentedText.Fragment> line) {
            float top = line.stream().map(FragmentedText.Fragment::y).min(Float::compare).orElse(0.0f);
            float bottom = line.stream().map(fragment -> fragment.y() + fragment.h()).max(Float::compare).orElse(top);
            return Math.max(0.0f, bottom - top);
        }

        private static boolean isBlankVisualLine(List<FragmentedText.Fragment> line) {
            return line.isEmpty() || line.stream().allMatch(fragment -> {
                if (!(fragment.text() instanceof Run run)) {
                    return false;
                }
                return run.isEmpty();
            });
        }

        private static void renderBlockDecoration(
                Graphics graphics,
                @Nullable BlockDecorationArea area,
                float contentWidth,
                double displayScale
        ) {
            if (area == null) {
                return;
            }
            BlockDecoration decoration = area.decoration();
            float padding = (float) (decoration.padding() * displayScale);
            float x = Math.max(0.0f, area.left() - padding);
            float y = Math.max(0.0f, area.top() - padding);
            float width = Math.max(1.0f, contentWidth - x);
            float height = Math.max(1.0f, area.bottom() - area.top() + 2.0f * padding);

            graphics.setFill(decoration.backgroundColor());
            graphics.fillRect(x, y, width, height);
            graphics.setStroke(decoration.borderColor(), (float) (decoration.borderWidth() * displayScale));
            graphics.strokeRect(x, y, width, height);
        }

        private static @Nullable BlockDecorationData blockDecorationData(List<FragmentedText.Fragment> line) {
            for (FragmentedText.Fragment fragment : line) {
                if (!(fragment.text() instanceof Run run)) {
                    continue;
                }
                if (isStructuralMarker(run)) {
                    continue;
                }
                BlockDecoration decoration = blockDecoration(run);
                if (decoration != null) {
                    return new BlockDecorationData(decoration, decoratedBlockId(run, decoration));
                }
            }
            return null;
        }

        private static @Nullable BlockSpacing blockSpacing(List<FragmentedText.Fragment> line) {
            for (FragmentedText.Fragment fragment : line) {
                if (!(fragment.text() instanceof Run run)) {
                    continue;
                }
                if (isStructuralMarker(run)) {
                    continue;
                }
                BlockSpacing spacing = blockSpacing(run);
                if (spacing != null) {
                    return spacing;
                }
            }
            return null;
        }

        private static @Nullable BlockSpacing blockSpacing(Run run) {
            Float marginTop = null;
            Float marginBottom = null;
            BlockDecoration decoration = blockDecoration(run);
            for (int i = run.getStyles().size() - 1; i >= 0; i--) {
                Style style = run.getStyles().get(i);
                if (marginTop == null && style.get(Style.BLOCK_MARGIN_TOP) instanceof Number value) {
                    marginTop = Math.max(0.0f, value.floatValue());
                }
                if (marginBottom == null && style.get(Style.BLOCK_MARGIN_BOTTOM) instanceof Number value) {
                    marginBottom = Math.max(0.0f, value.floatValue());
                }
            }
            float top = marginTop == null ? 0.0f : marginTop;
            float bottom = marginBottom == null ? 0.0f : marginBottom;
            if (decoration != null) {
                top += decoration.padding();
                bottom += decoration.padding();
            }
            if (top == 0.0f && bottom == 0.0f) {
                return null;
            }
            // A decorated block is an atomic visual unit. Its decoration is stable across its
            // styled child runs (including line-end runs), whereas their structural attributes
            // need not be. This also keeps wrapped visual lines inside the same block.
            return new BlockSpacing(decoration == null ? blockId(run) : decoratedBlockId(run, decoration), top, bottom);
        }

        private static Object decoratedBlockId(Run run, BlockDecoration decoration) {
            Object id = run.getAttributes().get(Style.BLOCK_ID);
            return id == null ? decoration : id;
        }

        private static Object blockId(Run run) {
            Object id = run.getAttributes().get(Style.BLOCK_ID);
            if (id != null) {
                return id;
            }
            Object blockStack = run.getAttributes().get("block-stack");
            if (blockStack instanceof String[] stack) {
                // Arrays compare by identity. The value is a structural type stack, so normalize it
                // to its contents before using it as a grouping key.
                return List.of(stack.clone());
            }
            return run.getAttributes();
        }

        private static @Nullable BlockDecoration blockDecoration(Run run) {
            Color backgroundColor = null;
            Color borderColor = null;
            float borderWidth = 1.0f;
            float padding = 0.0f;
            for (int i = run.getStyles().size() - 1; i >= 0; i--) {
                Style style = run.getStyles().get(i);
                if (backgroundColor == null && style.get(Style.BLOCK_BACKGROUND_COLOR) instanceof Color color) {
                    backgroundColor = color;
                }
                if (borderColor == null && style.get(Style.BLOCK_BORDER_COLOR) instanceof Color color) {
                    borderColor = color;
                }
                if (style.get(Style.BLOCK_BORDER_WIDTH) instanceof Number value) {
                    borderWidth = Math.max(0.0f, value.floatValue());
                }
                if (style.get(Style.BLOCK_PADDING) instanceof Number value) {
                    padding = Math.max(0.0f, value.floatValue());
                }
            }
            return backgroundColor == null ? null : new BlockDecoration(
                    backgroundColor,
                    borderColor == null ? backgroundColor : borderColor,
                    borderWidth,
                    padding
            );
        }

        private static boolean isInvisibleInlinePlaceholder(FragmentedText.Fragment fragment) {
            if (!(fragment.text() instanceof Run run)) {
                return false;
            }
            return run.getStyles().contains(RichTextPaneLayoutHelper.STYLE_INVISIBLE_TEXT);
        }

        private void ensureEditorContentHeight(TextPane control, double availableWidth, double previousCanvasHeight) {
            if (!(control instanceof TextEditorPane tep)) {
                return;
            }

            List<VisualLine> lines = tep.buildVisualLines(availableWidth);
            if (lines.isEmpty()) {
                return;
            }

            int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, tep.getCaretPosition());
            if (lineIndex < 0 || lineIndex >= lines.size()) {
                return;
            }

            VisualLine line = lines.get(lineIndex);
            double requiredHeight = Math.ceil(line.top() + line.height());
            if (requiredHeight > canvas.getHeight()) {
                canvas.setHeight(requiredHeight);
                contentPane.setMinHeight(requiredHeight);
                contentPane.setPrefHeight(requiredHeight);
                contentPane.resize(canvas.getWidth(), requiredHeight);
                if (requiredHeight > previousCanvasHeight + 0.5) {
                    caretVisibilityRequested = true;
                    if (lineIndex == lines.size() - 1 && tep.getCaretPosition() == tep.getLength()) {
                        scrollPane.setVvalue(1.0);
                    }
                }
            }
        }

        private void renderEditorOverlay(
                TextPane control,
                double availableWidth,
                RichTextPaneLayoutHelper.Layout<InlineControlPlacement> layout
        ) {
            selectionLayer.getChildren().clear();
            caretLayer.getChildren().clear();
            caretNode = null;

            if (!(control instanceof TextEditorPane tep)) {
                return;
            }

            IndexRange selection = tep.getSelection();
            if (selection.getLength() > 0) {
                int sourceSelStart = selection.getStart();
                int sourceSelEnd = selection.getEnd();
                int selStart = layout.layoutTextData().sourceToLayoutPosition(selection.getStart());
                int selEnd = layout.layoutTextData().sourceToLayoutPosition(selection.getEnd());
                FontUtil fontUtil = FontUtil.getInstance();

                // Draw full-node selection markers for inline nodes based on source-range overlap.
                for (InlineControlPlacement placement : layout.placements()) {
                    if (placement.node() instanceof TableNode tableNode) {
                        Bounds bounds = tableNode.getBoundsInParent();
                        for (com.dua3.utility.math.geometry.Rectangle2f cell : RichTextTableHelper.selectionBounds(
                                tableNode.tableLayout(), sourceSelStart, sourceSelEnd
                        )) {
                            Rectangle marker = new Rectangle(
                                    bounds.getMinX() + cell.x(),
                                    bounds.getMinY() + cell.y(),
                                    Math.max(1.0, cell.width()),
                                    Math.max(1.0, cell.height())
                            );
                            marker.setFill(javafx.scene.paint.Color.color(0.25, 0.45, 0.85, 0.35));
                            selectionLayer.getChildren().add(marker);
                        }
                        continue;
                    }
                    if (!isInlinePlacementSelected(layout.layoutTextData(), placement, sourceSelStart, sourceSelEnd)) {
                        continue;
                    }
                    Rectangle marker = createInlineSelectionMarker(placement);
                    marker.setFill(javafx.scene.paint.Color.color(0.25, 0.45, 0.85, 0.35));
                    selectionLayer.getChildren().add(marker);
                }

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

                        if (RichTextPaneLayoutHelper.hasInlineNode(run)) {
                            // Inline-node selections are handled above using placement bounds.
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

            if (tep.isEditable() && hasEditorFocus(control)) {
                CaretInfo caretInfo = null;
                int caretPosition = tep.getCaretPosition();
                for (InlineControlPlacement placement : layout.placements()) {
                    if (!(placement.node() instanceof TableNode tableNode)) {
                        continue;
                    }
                    RichTextTableHelper.Table table = tableNode.tableLayout().table();
                    if (caretPosition < table.start() || caretPosition >= table.end()) {
                        continue;
                    }
                    Bounds bounds = tableNode.getBoundsInParent();
                    caretInfo = RichTextTableHelper.caretForSourcePosition(
                                    tableNode.tableLayout(), caretPosition, FONT_UTIL)
                            .map(caret -> new CaretInfo(
                                    bounds.getMinX() + caret.x(),
                                    bounds.getMinY() + caret.y(),
                                    caret.height()
                            ))
                            .orElse(null);
                    if (caretInfo != null) {
                        break;
                    }
                }
                List<VisualLine> lines = tep.buildVisualLines(availableWidth);
                if (caretInfo == null && !lines.isEmpty()) {
                    int lineIndex = RichTextVisualLayoutHelper.lineIndexForCaret(lines, caretPosition);
                    if (lineIndex >= 0 && lineIndex < lines.size()) {
                        VisualLine line = lines.get(lineIndex);
                        caretInfo = new CaretInfo(
                                RichTextVisualLayoutHelper.xForIndex(line, caretPosition),
                                line.top(),
                                line.height()
                        );
                    }
                }
                if (caretInfo == null) {
                    int layoutCaretPosition = layout.layoutTextData().sourceToLayoutPosition(tep.getCaretPosition());
                    caretInfo = findCaret(layout.renderLines(), layoutCaretPosition);
                }
                if (caretInfo != null) {
                    caretNode = createCaretNode(caretInfo.x(), caretInfo.y(), caretInfo.height());
                    caretNode.setOpacity(blink ? 0.0 : 1.0);
                    caretLayer.getChildren().add(caretNode);
                }
            }
        }

        private static boolean isInlinePlacementSelected(
                RichTextPaneLayoutHelper.LayoutTextData layoutTextData,
                InlineControlPlacement placement,
                int sourceSelStart,
                int sourceSelEnd
        ) {
            int from = layoutTextData.layoutToSourcePosition(placement.runStart());
            int to = layoutTextData.layoutToSourcePosition(placement.runEnd());
            int sourceFrom = Math.min(from, to);
            int sourceTo = Math.max(from, to);
            return sourceSelStart < sourceTo && sourceFrom < sourceSelEnd;
        }

        private static Rectangle createInlineSelectionMarker(InlineControlPlacement placement) {
            Node node = placement.node();
            Bounds nodeBounds = node.getBoundsInParent();

            double x = nodeBounds.getMinX();
            double width = Math.max(1.0, nodeBounds.getWidth());
            double top = nodeBounds.getMinY();
            double bottom = nodeBounds.getMaxY();

            // Text-like inline controls (buttons/hyperlinks/etc.) should follow line selection height.
            // Images (ImageView) keep their full visual height.
            if (node instanceof Control) {
                double lineTop = placement.y();
                double lineBottom = placement.y() + placement.h();
                top = Math.max(top, lineTop);
                bottom = Math.min(bottom, lineBottom);
                if (bottom <= top) {
                    top = lineTop;
                    bottom = lineBottom;
                }
            }

            return new Rectangle(x, top, width, Math.max(1.0, bottom - top));
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

        private static Rectangle createCaretNode(double x, double y, double height) {
            // Use fill-only geometry for caret rendering so bounds never spill by half a stroke
            // pixel (which causes content-bound jitter when blink toggles).
            Rectangle caret = new Rectangle(x, y, 1.0, Math.max(1.0, height));
            caret.setFill(javafx.scene.paint.Color.BLACK);
            caret.setStroke(null);
            caret.setManaged(false);
            return caret;
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
