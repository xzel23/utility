package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.awt.AwtImageUtil;
import com.dua3.utility.data.Image;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.text.RichTextBuilderExtBase;
import com.dua3.utility.text.Run;
import com.dua3.utility.text.Style;
import com.dua3.utility.text.TextUtil;
import com.dua3.utility.ui.InlineNode;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextPane;
import com.dua3.utility.ui.RichTextPaneLayoutHelper;
import com.dua3.utility.ui.RichTextRenderer;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VAnchor;
import com.dua3.utility.ui.VisualLine;
import org.jspecify.annotations.Nullable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * Swing rich-text viewer.
 */
public class TextPane extends JScrollPane implements RichTextPane {

    private static final String STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT = TextPane.class.getName() + ".inlineReferenceAscent";
    private static final String STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH = TextPane.class.getName() + ".inlineLeadingWidth";
    private static final String CLIENT_PROPERTY_INLINE_TARGET_URI = TextPane.class.getName() + ".inlineTargetUri";

    protected final transient RichTextEditorModel model;
    private final RichTextCanvas textComponent = new RichTextCanvas();
    private boolean wrapText;
    private transient Font textFont = FontUtil.getInstance().getDefaultFont();
    private transient Consumer<URI> hyperlinkHandler = TextPane::openUriUsingDesktop;
    private transient @Nullable RenderLayoutCache renderLayoutCache;

    /**
     * Creates an empty text pane.
     */
    public TextPane() {
        this(null);
    }

    /**
     * Creates a text pane with initial text.
     *
     * @param text initial text
     */
    public TextPane(@Nullable CharSequence text) {
        model = new RichTextEditorModel(text, AwtFontUtil.getInstance());
        model.setPageWidthProvider(m -> resolvePageWidthFromView());
        model.setPageHeightProvider(m -> resolvePageHeightFromView());
        model.setWrapWidthProvider(m -> resolveCurrentWrapWidthFromView());
        setViewportView(textComponent);
        setWrapText(false);
        textComponent.setLayout(null);
        textComponent.setFocusable(false);
        textComponent.setFont(com.dua3.utility.awt.AwtFontUtil.getInstance().convert(textFont));

        // keep layout cache in sync with viewport geometry changes
        getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                invalidateRenderLayout();
            }
        });

        // viewport changes after reparenting should also invalidate layout
        addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
            @Override
            public void ancestorResized(HierarchyEvent e) {
                invalidateRenderLayout();
            }
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
                invalidateRenderLayout();
            }
        });
    }

    /**
     * Returns the underlying text component.
     *
     * @return underlying text component
     */
    public final JComponent getTextComponent() {
        return textComponent;
    }

    @Override
    public RichText getText() {
        return model.getText();
    }

    @Override
    public final void setText(@Nullable CharSequence value) {
        model.setText(value == null ? RichText.emptyText() : RichText.valueOf(value));
        onModelChanged();
    }

    @Override
    public boolean isWrapText() {
        return wrapText;
    }

    @Override
    public final void setWrapText(boolean value) {
        if (wrapText == value) {
            return;
        }
        wrapText = value;
        setHorizontalScrollBarPolicy(value ? HORIZONTAL_SCROLLBAR_NEVER : HORIZONTAL_SCROLLBAR_AS_NEEDED);
        invalidateRenderLayout();
        revalidate();
        repaint();
    }

    @Override
    public Font getTextFont() {
        return textFont;
    }

    @Override
    public final void setTextFont(Font value) {
        textFont = Objects.requireNonNull(value);
        textComponent.setFont(com.dua3.utility.awt.AwtFontUtil.getInstance().convert(textFont));
        invalidateRenderLayout();
    }

    @Override
    public Consumer<URI> getHyperlinkHandler() {
        return hyperlinkHandler;
    }

    @Override
    public void setHyperlinkHandler(Consumer<URI> handler) {
        hyperlinkHandler = Objects.requireNonNull(handler);
    }

    /**
     * Indicates whether this pane supports editing.
     *
     * @return false for read-only viewer
     */
    public boolean isEditable() {
        return false;
    }

    /**
     * Hook called after text/model updates.
     */
    protected void onModelChanged() {
        invalidateRenderLayout();
    }

    /**
     * Hook for subclasses to paint overlays such as selection/caret.
     *
     * @param g2 graphics
     * @param layout current layout
     */
    protected void paintOverlay(Graphics2D g2, RenderLayout layout) {
        // default no-op
    }

    /**
     * Returns the current render layout used by the view component.
     *
     * @return current layout
     */
    protected final RenderLayout getRenderLayout() {
        return layoutForWidth(textComponent.getLayoutWidthHint(false));
    }

    /**
     * Maps a point in text-component coordinates to the nearest source index.
     *
     * @param point point in component coordinates
     * @return nearest source index
     */
    protected final int indexForPoint(Point point) {
        RenderLayout layout = getRenderLayout();
        return RichTextVisualLayoutHelper.indexForPoint(layout.visualLines(), point.getX(), point.getY());
    }

    /**
     * Explicitly invalidates cached layout and repaints.
     */
    protected final void invalidateRenderLayout() {
        renderLayoutCache = null;
        textComponent.invalidateInlineLayout();
        textComponent.revalidate();
        textComponent.repaint();
    }

    private RenderLayout layoutForWidth(double widthHint) {
        double width = model.resolveAvailableWidth(widthHint);
        double widthKey = wrapText ? width : Double.POSITIVE_INFINITY;
        RichText text = model.getText();
        RenderLayoutCache cache = renderLayoutCache;
        if (cache != null
                && Double.compare(cache.widthKey(), widthKey) == 0
                && cache.wrapText() == wrapText
                && Objects.equals(cache.text(), text)
                && Objects.equals(cache.font(), textFont)) {
            return cache.layout();
        }

        RenderLayout layout = createLayout(text, width);
        renderLayoutCache = new RenderLayoutCache(widthKey, wrapText, text, textFont, layout);
        return layout;
    }

    private double resolveCurrentWrapWidthFromView() {
        if (!wrapText) {
            return Double.POSITIVE_INFINITY;
        }
        return resolvePageWidthFromView();
    }

    private double resolvePageWidthFromView() {
        JViewport viewport = getViewport();
        if (viewport != null) {
            double viewportWidth = viewport.getExtentSize().getWidth();
            if (Double.isFinite(viewportWidth) && viewportWidth > 1.0) {
                return viewportWidth;
            }
        }

        double componentWidth = textComponent.getWidth();
        if (Double.isFinite(componentWidth) && componentWidth > 1.0) {
            return componentWidth;
        }

        double fallback = (double) getWidth() - getInsets().left - getInsets().right;
        return Double.isFinite(fallback) && fallback > 1.0 ? fallback : 1.0;
    }

    private double resolvePageHeightFromView() {
        JViewport viewport = getViewport();
        if (viewport != null) {
            double viewportHeight = viewport.getExtentSize().getHeight();
            if (Double.isFinite(viewportHeight) && viewportHeight > 1.0) {
                return viewportHeight;
            }
        }

        double componentHeight = textComponent.getHeight();
        if (Double.isFinite(componentHeight) && componentHeight > 1.0) {
            return componentHeight;
        }

        double fallback = (double) getHeight() - getInsets().top - getInsets().bottom;
        return Double.isFinite(fallback) && fallback > 1.0 ? fallback : 0.0;
    }

    private RenderLayout createLayout(RichText richText, double availableWidth) {
        RichTextPaneLayoutHelper.LayoutPreparation prepared = RichTextPaneLayoutHelper.prepareLayout(
                richText,
                textFont,
                wrapText,
                availableWidth,
                STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH,
                this::createInlineComponent,
                TextPane::measureComponentWidth
        );

        FragmentedText layoutFragments = prepared.layoutFragments();
        FragmentedText renderFragments = prepared.renderFragments();

        List<InlineComponentPlacement> placements = collectInlinePlacements(layoutFragments);
        LineShiftData lineShiftData = computeLineShifts(renderFragments, placements);
        Map<Float, Float> lineShiftByY = lineShiftData.lineShiftByY();
        List<InlineComponentPlacement> shiftedPlacements = shiftPlacements(placements, lineShiftByY);
        List<List<FragmentedText.Fragment>> shiftedRenderLines = shiftRenderLines(renderFragments, lineShiftByY);

        double defaultLineHeight = Math.max(1.0, textFont.getFontData().height());
        List<VisualLine> visualLines = model.buildVisualLines(
                availableWidth,
                wrapText,
                textFont,
                blockText -> createVisualBlockLayout(blockText, availableWidth, defaultLineHeight)
        );

        double renderHeight = Math.max(defaultLineHeight, computeRenderedHeight(shiftedRenderLines, lineShiftData.tailOverflowBelow(), textFont));
        RichTextPaneLayoutHelper.Layout<InlineComponentPlacement> layout = new RichTextPaneLayoutHelper.Layout<>(
                shiftedRenderLines,
                shiftedPlacements,
                prepared.renderWidth(),
                renderHeight,
                prepared.layoutTextData()
        );
        return new RenderLayout(layout, visualLines);
    }

    private RichTextVisualLayoutHelper.BlockLayout createVisualBlockLayout(
            RichText blockText,
            double availableWidth,
            double defaultLineHeight
    ) {
        RichTextPaneLayoutHelper.LayoutPreparation prepared = RichTextPaneLayoutHelper.prepareLayout(
                blockText,
                textFont,
                wrapText,
                availableWidth,
                STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH,
                this::createInlineComponent,
                TextPane::measureComponentWidth
        );

        FragmentedText blockLayoutFragments = prepared.layoutFragments();
        FragmentedText blockRenderFragments = prepared.renderFragments();
        List<InlineComponentPlacement> blockPlacements = collectInlinePlacements(blockLayoutFragments);

        LineShiftData lineShiftData = computeLineShifts(blockRenderFragments, blockPlacements);
        List<List<FragmentedText.Fragment>> shiftedLines = shiftRenderLines(blockRenderFragments, lineShiftData.lineShiftByY());
        double height = Math.max(defaultLineHeight, computeRenderedHeight(shiftedLines, lineShiftData.tailOverflowBelow(), textFont));

        return new RichTextVisualLayoutHelper.BlockLayout(
                shiftedLines,
                height,
                prepared.layoutTextData()::layoutToSourcePosition
        );
    }

    private List<InlineComponentPlacement> collectInlinePlacements(FragmentedText layoutFragments) {
        List<InlineComponentPlacement> placements = new ArrayList<>();

        for (List<FragmentedText.Fragment> line : layoutFragments.lines()) {
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
                    fragmentAscent = getInlineReferenceAscent(run, fragment.font());
                }
                lineAscent = Math.max(lineAscent, fragmentAscent);
            }

            float lineHeight = Math.max(0.0f, lineBottom - lineTop);
            lineAscent = Math.clamp(lineAscent, 0.0, lineHeight);
            double lineDescent = Math.max(0.0, lineHeight - lineAscent);
            float baselineY = (float) (lineTop + lineAscent);

            for (FragmentedText.Fragment fragment : line) {
                if (!(fragment.text() instanceof Run run)) {
                    continue;
                }

                Component component = createInlineComponent(run, fragment.font());
                if (component == null) {
                    continue;
                }

                VAnchor vAnchor = getInlineNodeVAnchor(run);
                double descent = getInlineNodeDescent(run);
                double leadingWidth = getInlineLeadingWidth(run);
                placements.add(new InlineComponentPlacement(
                        component,
                        (float) (fragment.x() - leadingWidth),
                        lineTop,
                        fragment.w(),
                        lineHeight,
                        baselineY,
                        fragment.font(),
                        vAnchor,
                        lineAscent,
                        lineDescent,
                        descent
                ));
            }
        }

        return placements;
    }
    private @Nullable Component createInlineComponent(Run run, Font runFont) {
        if (TextUtil.isWhitespaceOnly(run)) {
            return null;
        }

        String text = run.toString();
        for (int i = run.getStyles().size() - 1; i >= 0; i--) {
            Style style = run.getStyles().get(i);

            Object factory = style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_FACTORY);
            if (factory instanceof Function<?, ?> f) {
                @SuppressWarnings("unchecked")
                Function<String, ?> fn = (Function<String, ?>) f;
                Component component = toSwingInlineComponent(fn.apply(text), style);
                if (component != null) {
                    initializeInlineComponent(component, runFont);
                    return component;
                }
            }

            Component component = toSwingInlineComponent(style.get(RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE), style);
            if (component != null) {
                initializeInlineComponent(component, runFont);
                return component;
            }
        }
        return null;
    }

    private void initializeInlineComponent(Component component, Font runFont) {
        setInlineComponentFont(component, runFont);
        if (component instanceof JComponent jc) {
            jc.setFocusable(false);
        }
        if (component instanceof AbstractButton button) {
            wireButtonAction(this, button);
        }
    }

    private static @Nullable Component toSwingInlineComponent(@Nullable Object value, Style style) {
        double maxWidth = getPositiveStyleValue(style, RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_WIDTH);
        double maxHeight = getPositiveStyleValue(style, RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_MAX_HEIGHT);

        Object wrapped = value;
        if (wrapped instanceof InlineNode<?> inlineNode) {
            if (RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_BUTTON.equals(inlineNode.getMimeType())) {
                RichTextBuilderExtBase.ButtonData buttonData = RichTextBuilderExtBase.decodeInlineButtonData(inlineNode.getData());
                String text = inlineNode.getWrapped() instanceof CharSequence cs && !cs.isEmpty()
                        ? cs.toString()
                        : (buttonData.text().isBlank() ? buttonData.target() : buttonData.text());
                JButton button = new JButton(text);
                button.setFocusable(false);
                toUri(buttonData.target()).ifPresent(uri -> button.putClientProperty(CLIENT_PROPERTY_INLINE_TARGET_URI, uri));
                return button;
            }

            if (RichTextBuilderExtBase.INLINE_NODE_MIME_TYPE_HYPERLINK.equals(inlineNode.getMimeType())) {
                RichTextBuilderExtBase.HyperlinkData hyperlinkData = RichTextBuilderExtBase.decodeInlineHyperlinkData(inlineNode.getData());
                String text = inlineNode.getWrapped() instanceof CharSequence cs && !cs.isEmpty()
                        ? cs.toString()
                        : (hyperlinkData.text().isBlank() ? hyperlinkData.target() : hyperlinkData.text());
                JButton hyperlink = createHyperlinkButton(text);
                hyperlink.setFocusable(false);
                toUri(hyperlinkData.target()).ifPresent(uri -> hyperlink.putClientProperty(CLIENT_PROPERTY_INLINE_TARGET_URI, uri));
                return hyperlink;
            }

            Image decodedImage = decodeInlineImage(inlineNode);
            wrapped = decodedImage != null ? decodedImage : inlineNode.getWrapped();
        }

        if (wrapped instanceof JComponent jc) {
            applyImageScaling(jc, maxWidth, maxHeight);
            return jc;
        }
        if (wrapped instanceof Component component) {
            return component;
        }
        if (wrapped instanceof Image image) {
            return createImageLabel(new ImageIcon(AwtImageUtil.getInstance().toImage(image)), maxWidth, maxHeight);
        }
        if (wrapped instanceof java.awt.Image awtImage) {
            return createImageLabel(new ImageIcon(awtImage), maxWidth, maxHeight);
        }
        if (wrapped instanceof Icon icon) {
            return createImageLabel(icon, maxWidth, maxHeight);
        }
        return null;
    }

    private static JButton createHyperlinkButton(String text) {
        JButton hyperlink = new JButton(text);
        hyperlink.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        hyperlink.setBorderPainted(false);
        hyperlink.setContentAreaFilled(false);
        hyperlink.setOpaque(false);
        hyperlink.setHorizontalAlignment(SwingConstants.LEFT);
        hyperlink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hyperlink.setForeground(new java.awt.Color(0x1D, 0x4E, 0x89));
        hyperlink.setMargin(new Insets(0, 0, 0, 0));
        return hyperlink;
    }

    private static @Nullable Image decodeInlineImage(InlineNode<?> inlineNode) {
        try {
            return InlineNode.decodeArgbImageData(inlineNode.getData());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static JLabel createImageLabel(Icon icon, double maxWidth, double maxHeight) {
        JLabel label = new JLabel(icon);
        label.setFocusable(false);
        applyImageScaling(label, maxWidth, maxHeight);
        return label;
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

    private static void applyImageScaling(Component component, double maxWidth, double maxHeight) {
        if (!(component instanceof JLabel label)) {
            return;
        }
        if (!(label.getIcon() instanceof ImageIcon imageIcon)) {
            return;
        }

        ImageIcon scaled = scaleImageIcon(imageIcon, maxWidth, maxHeight);
        if (scaled != imageIcon) {
            label.setIcon(scaled);
        }
    }

    private static ImageIcon scaleImageIcon(ImageIcon icon, double maxWidth, double maxHeight) {
        boolean hasWidth = Double.isFinite(maxWidth) && maxWidth > 0.0;
        boolean hasHeight = Double.isFinite(maxHeight) && maxHeight > 0.0;
        if (!hasWidth && !hasHeight) {
            return icon;
        }

        int sourceWidth = Math.max(1, icon.getIconWidth());
        int sourceHeight = Math.max(1, icon.getIconHeight());
        double scale = 1.0;
        if (hasWidth) {
            scale = Math.min(scale, maxWidth / sourceWidth);
        }
        if (hasHeight) {
            scale = Math.min(scale, maxHeight / sourceHeight);
        }
        if (!Double.isFinite(scale) || scale <= 0.0 || Math.abs(scale - 1.0) < 0.01) {
            return icon;
        }

        int targetWidth = Math.max(1, (int) Math.round(sourceWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(sourceHeight * scale));
        java.awt.Image scaledImage = icon.getImage().getScaledInstance(targetWidth, targetHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImage);
    }

    private static void setInlineComponentFont(Component component, Font runFont) {
        if (!(component instanceof JLabel) && !(component instanceof AbstractButton)) {
            return;
        }
        component.setFont(AwtFontUtil.getInstance().convert(runFont));
    }

    private static double measureComponentWidth(Component component) {
        return Math.max(1.0, component.getPreferredSize().getWidth());
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

    private static void wireButtonAction(TextPane control, AbstractButton button) {
        if (button.getActionListeners().length > 0) {
            return;
        }

        Object targetValue = button.getClientProperty(CLIENT_PROPERTY_INLINE_TARGET_URI);
        Optional<URI> target = toUri(targetValue);
        if (target.isEmpty()) {
            return;
        }

        button.addActionListener(evt -> control.getHyperlinkHandler().accept(target.get()));
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

    private static double getInlineReferenceAscent(Run run, Font fallbackFont) {
        return getInlineReferenceValue(run, STYLE_ATTRIBUTE_INLINE_REFERENCE_ASCENT, fallbackFont::getAscent);
    }

    private static double getInlineNodeDescent(Run run) {
        return getInlineReferenceValue(run, RichTextBuilderExtBase.STYLE_ATTRIBUTE_INLINE_NODE_DESCENT, () -> Double.NaN);
    }

    private static double getInlineLeadingWidth(Run run) {
        return getInlineReferenceValue(run, STYLE_ATTRIBUTE_INLINE_LEADING_WIDTH, () -> 0.0);
    }

    private static double computeInlineDescent(InlineComponentPlacement placement, double prefH, int baselineOffset) {
        return Double.isFinite(placement.descent())
                ? Math.max(0.0, placement.descent())
                : (baselineOffset >= 0 ? Math.max(0.0, prefH - baselineOffset) : 0.0);
    }

    private static double computeInlineComponentY(InlineComponentPlacement placement, double prefH, int baselineOffset) {
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

    private static LineShiftData computeLineShifts(FragmentedText renderFragments, List<InlineComponentPlacement> placements) {
        Map<Float, Float> overflowAboveByLineY = new java.util.HashMap<>();
        Map<Float, Float> overflowBelowByLineY = new java.util.HashMap<>();

        for (InlineComponentPlacement placement : placements) {
            Component component = placement.component();
            Dimension prefSize = component.getPreferredSize();
            double prefW = Math.max(1.0, prefSize.getWidth());
            double prefH = Math.max(1.0, prefSize.getHeight());
            int baselineOffset = component.getBaseline((int) Math.ceil(prefW), (int) Math.ceil(prefH));
            double componentY = computeInlineComponentY(placement, prefH, baselineOffset);
            double componentBottom = componentY + prefH;
            float overflowAbove = (float) Math.max(0.0, placement.y() - componentY);
            float overflowBelow = (float) Math.max(0.0, componentBottom - (placement.y() + placement.h()));
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

    private static List<InlineComponentPlacement> shiftPlacements(
            List<InlineComponentPlacement> placements,
            Map<Float, Float> lineShiftByY
    ) {
        if (lineShiftByY.isEmpty()) {
            return placements;
        }

        List<InlineComponentPlacement> shifted = new ArrayList<>(placements.size());
        for (InlineComponentPlacement placement : placements) {
            float dy = lineShiftByY.getOrDefault(placement.y(), 0.0f);
            shifted.add(new InlineComponentPlacement(
                    placement.component(),
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

    private static void openUriUsingDesktop(URI uri) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            return;
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {
            String scheme = uri.getScheme();
            if ("mailto".equalsIgnoreCase(scheme) && desktop.isSupported(java.awt.Desktop.Action.MAIL)) {
                desktop.mail(uri);
            } else if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        } catch (IOException | UnsupportedOperationException ignored) {
            // ignore failures from user-supplied or unsupported URI schemes
        }
    }

    protected record RenderLayout(
            RichTextPaneLayoutHelper.Layout<InlineComponentPlacement> layout,
            List<VisualLine> visualLines
    ) {
        public List<List<FragmentedText.Fragment>> renderLines() {
            return layout.renderLines();
        }

        public List<InlineComponentPlacement> placements() {
            return layout.placements();
        }

        public double width() {
            return layout.width();
        }

        public double height() {
            return layout.height();
        }
    }

    protected record InlineComponentPlacement(
            Component component,
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

    private record LineShiftData(Map<Float, Float> lineShiftByY, float tailOverflowBelow) {}

    private record RenderLayoutCache(
            double widthKey,
            boolean wrapText,
            RichText text,
            Font font,
            RenderLayout layout
    ) {}

    private final class RichTextCanvas extends JComponent implements Scrollable {

        private transient @Nullable RenderLayout appliedInlineLayout;

        private void invalidateInlineLayout() {
            appliedInlineLayout = null;
            if (getComponentCount() > 0) {
                removeAll();
            }
        }

        private double getLayoutWidthHint(boolean forPreferredSize) {
            if (wrapText) {
                JViewport viewport = TextPane.this.getViewport();
                if (viewport != null) {
                    double viewportWidth = viewport.getExtentSize().getWidth();
                    if (viewportWidth > 1.0) {
                        return viewportWidth;
                    }
                }

                if (!forPreferredSize && getWidth() > 1.0) {
                    return getWidth();
                }
            }

            if (getWidth() > 1.0) {
                return getWidth();
            }

            JViewport viewport = TextPane.this.getViewport();
            if (viewport != null && viewport.getExtentSize().width > 1) {
                return viewport.getExtentSize().width;
            }

            return Math.max(1.0, (double) TextPane.this.getWidth() - TextPane.this.getInsets().left - TextPane.this.getInsets().right);
        }

        @Override
        public Dimension getPreferredSize() {
            double widthHint = getLayoutWidthHint(true);
            RenderLayout layout = layoutForWidth(widthHint);
            int prefWidth = (int) Math.ceil(Math.max(1.0, wrapText ? widthHint : layout.width()));
            int prefHeight = (int) Math.ceil(Math.max(1.0, layout.height()));
            return new Dimension(prefWidth, prefHeight);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            RenderLayout layout = layoutForWidth(getLayoutWidthHint(false));
            if (appliedInlineLayout != layout) {
                syncInlineComponents(layout);
            }

            Graphics2D g2 = (Graphics2D) g.create();
            try {
                try (SwingGraphics graphics = new SwingGraphics(g2, new Rectangle(0, 0, getWidth(), getHeight()))) {
                    graphics.reset();
                    graphics.setFont(textFont);
                    RichTextRenderer.renderFragmentLines(graphics, layout.renderLines());
                }
                paintOverlay(g2, layout);
            } finally {
                g2.dispose();
            }
        }

        private void syncInlineComponents(RenderLayout layout) {
            if (appliedInlineLayout == layout) {
                return;
            }

            Set<Component> used = Collections.newSetFromMap(new IdentityHashMap<>());

            for (InlineComponentPlacement placement : layout.placements()) {
                Component component = placement.component();
                if (!used.add(component)) {
                    continue;
                }

                if (component.getParent() != this) {
                    add(component);
                }
                if (component instanceof AbstractButton button) {
                    wireButtonAction(TextPane.this, button);
                }

                Dimension pref = component.getPreferredSize();
                int prefW = Math.max(1, (int) Math.ceil(pref.getWidth()));
                int prefH = Math.max(1, (int) Math.ceil(pref.getHeight()));
                int baselineOffset = component.getBaseline(prefW, prefH);
                double y = computeInlineComponentY(placement, prefH, baselineOffset);
                int x = (int) Math.floor(placement.x());
                component.setBounds(x, (int) Math.floor(y), prefW, prefH);
                component.setVisible(true);
            }

            for (Component child : getComponents()) {
                if (!used.contains(child)) {
                    remove(child);
                }
            }

            appliedInlineLayout = layout;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(1, (int) Math.ceil(textFont.getFontData().height()));
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            int lineHeight = getScrollableUnitIncrement(visibleRect, orientation, direction);
            return Math.max(lineHeight, visibleRect.height - lineHeight);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return wrapText;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
