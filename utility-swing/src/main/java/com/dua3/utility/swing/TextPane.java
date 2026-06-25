package com.dua3.utility.swing;

import com.dua3.utility.awt.AwtFontUtil;
import com.dua3.utility.text.Alignment;
import com.dua3.utility.text.Font;
import com.dua3.utility.text.FontUtil;
import com.dua3.utility.text.FragmentedText;
import com.dua3.utility.text.RichText;
import com.dua3.utility.ui.HAnchor;
import com.dua3.utility.ui.RichTextEditorModel;
import com.dua3.utility.ui.RichTextPane;
import com.dua3.utility.ui.RichTextRenderer;
import com.dua3.utility.ui.RichTextVisualLayoutHelper;
import com.dua3.utility.ui.VAnchor;
import com.dua3.utility.text.VerticalAlignment;
import com.dua3.utility.ui.VisualLine;
import org.jspecify.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Swing rich-text viewer.
 */
public class TextPane extends JScrollPane implements RichTextPane {

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
        setViewportView(textComponent);
        setWrapText(false);
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
        textComponent.revalidate();
        textComponent.repaint();
    }

    private RenderLayout layoutForWidth(double widthHint) {
        double width = Math.max(1.0, widthHint);
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

    private RenderLayout createLayout(RichText richText, double availableWidth) {
        FontUtil fontUtil = FontUtil.getInstance();
        float width = (float) Math.max(1.0, availableWidth);
        float wrapWidth = wrapText ? width : FragmentedText.NO_WRAP;

        FragmentedText renderFragments = FragmentedText.generateFragments(
                richText,
                fontUtil,
                textFont,
                width,
                Float.MAX_VALUE,
                Alignment.LEFT,
                VerticalAlignment.TOP,
                HAnchor.LEFT,
                VAnchor.TOP,
                wrapWidth
        );

        double defaultLineHeight = Math.max(1.0, textFont.getFontData().height());
        List<VisualLine> visualLines = model.buildVisualLines(
                availableWidth,
                wrapText,
                textFont,
                blockText -> {
                    FragmentedText blockFragments = FragmentedText.generateFragments(
                            blockText,
                            fontUtil,
                            textFont,
                            width,
                            Float.MAX_VALUE,
                            Alignment.LEFT,
                            VerticalAlignment.TOP,
                            HAnchor.LEFT,
                            VAnchor.TOP,
                            wrapWidth
                    );
                    return new RichTextVisualLayoutHelper.BlockLayout(
                            blockFragments.lines(),
                            Math.max(defaultLineHeight, blockFragments.actualHeight()),
                            layoutPosition -> layoutPosition
                    );
                }
        );

        double renderWidth = wrapText ? width : Math.max(width, renderFragments.actualWidth());
        double renderHeight = Math.max(defaultLineHeight, renderFragments.actualHeight());
        return new RenderLayout(renderFragments.lines(), visualLines, renderWidth, renderHeight);
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
            List<List<FragmentedText.Fragment>> renderLines,
            List<VisualLine> visualLines,
            double width,
            double height
    ) {}

    private record RenderLayoutCache(
            double widthKey,
            boolean wrapText,
            RichText text,
            Font font,
            RenderLayout layout
    ) {}

    private final class RichTextCanvas extends JComponent implements Scrollable {

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
