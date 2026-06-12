package com.dua3.utility.ui;

import com.dua3.utility.data.Image;
import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Base class for rich-text builders with support for inline nodes.
 *
 * @param <N> node type
 * @param <B> concrete builder type
 */
public abstract class RichTextBuilderExtBase<N, B extends RichTextBuilderExtBase<N, B>> extends RichTextBuilder {

    /**
     * Marker character used for inline nodes.
     */
    public static final char INLINE_NODE_MARKER = '\uFFFC';
    private static final AtomicLong STYLE_ID = new AtomicLong();
    /**
     * Style attribute key for a function creating an inline node from run text.
     */
    public static final String STYLE_ATTRIBUTE_INLINE_NODE_FACTORY = RichTextBuilderExtBase.class.getName() + ".inlineNodeFactory";
    /**
     * Style attribute key for inline-node metadata.
     */
    public static final String STYLE_ATTRIBUTE_INLINE_NODE = RichTextBuilderExtBase.class.getName() + ".inlineNode";
    /**
     * Style attribute key for vertical anchor of inline nodes.
     */
    public static final String STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR = RichTextBuilderExtBase.class.getName() + ".inlineNodeVAnchor";
    /**
     * Style attribute key for inline-node descent (part below baseline).
     */
    public static final String STYLE_ATTRIBUTE_INLINE_NODE_DESCENT = RichTextBuilderExtBase.class.getName() + ".inlineNodeDescent";

    /**
     * Protected constructor for the {@code RichTextBuilderExtBase} class.
     */
    protected RichTextBuilderExtBase() {
        // nothing to do
    }

    /**
     * Constructs a {@code RichTextBuilderExtBase} instance with the specified initial capacity.
     *
     * @param capacity the initial capacity of the internal buffer
     */
    protected RichTextBuilderExtBase(int capacity) {
        super(capacity);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final B self() {
        return (B) this;
    }

    /**
     * Creates a hyperlink node with the specified text and action. The hyperlink
     * triggers the provided action when interacted with.
     *
     * @param text the text to be displayed for the hyperlink
     * @param action the action to be executed when the hyperlink is activated
     * @return the created hyperlink node
     */
    protected abstract N createHyperlink(CharSequence text, Runnable action);

    /**
     * Creates a button node with the specified text and action.
     *
     * @param text the text to display on the button
     * @param action the action to execute when the button is activated
     * @return the newly created button node
     */
    protected abstract N createButton(CharSequence text, Runnable action);

    /**
     * Creates an inline node representation for the provided image.
     *
     * @param image the image to be added as an inline node
     * @return the newly created inline node representing the image
     */
    protected abstract N createImage(Image image);

    /**
     * Creates an inline image node from the given image, scaled to fit within the specified maximum width and height.
     *
     * @param image the image to create an inline node for
     * @param maxWidth the maximum width to scale the image to
     * @param maxHeight the maximum height to scale the image to
     * @return the created image node
     */
    protected abstract N createImage(Image image, float maxWidth, float maxHeight);

    /**
     * Appends an inline node to the rich-text content using a {@link Supplier}.
     *
     * @param node a {@link Supplier} that provides the node to append
     * @return the builder instance for method chaining
     */
    public B appendInlineNode(Supplier<? extends N> node) {
        return appendInlineNodeWithStyle(() ->
                new InlineNode<>(node.get(), "application/octet-stream", new byte[0]));
    }

    /**
     * Appends a hyperlink to the rich-text content. The hyperlink is displayed with the specified text
     * and executes the provided action when activated.
     *
     * @param text the text to display for the hyperlink
     * @param action the action to execute when the hyperlink is activated
     * @return the builder instance for method chaining
     */
    public B appendHyperlink(CharSequence text, Runnable action) {
        String linkText = String.valueOf(text);
        byte[] data = linkText.getBytes(StandardCharsets.UTF_8);
        return appendInlineNodeWithStyle(() ->
                new InlineNode<>(createHyperlink(linkText, action), "text/plain", data));
    }

    /**
     * Appends a button to the rich-text content. The button is displayed with the specified text
     * and executes the provided action when activated.
     *
     * @param text the text to display on the button
     * @param action the action to execute when the button is activated
     * @return the builder instance for method chaining
     */
    public B appendButton(CharSequence text, Runnable action) {
        String buttonText = String.valueOf(text);
        byte[] data = buttonText.getBytes(StandardCharsets.UTF_8);
        return appendInlineNodeWithStyle(() ->
                new InlineNode<>(createButton(buttonText, action), "text/plain", data));
    }

    /**
     * Appends an image as inline node using the image's original size.
     *
     * @param image image
     * @return the builder instance for method chaining
     */
    public B appendImage(Image image) {
        return appendImage(image, VAnchor.BASELINE);
    }

    /**
     * Appends an image as inline node using the image's original size.
     *
     * @param image image
     * @param vAnchor vertical anchor for image alignment in the text line
     * @return the builder instance for method chaining
     */
    public B appendImage(Image image, VAnchor vAnchor) {
        byte[] data = InlineNode.encodeArgbImageData(image);
        return appendInlineNodeWithStyle(() ->
                new InlineNode<>(createImage(image), InlineNode.MIME_TYPE_ARGB_IMAGE, data), vAnchor, 0.0);
    }

    /**
     * Appends an image as inline node scaled to fit inside the given maximum width and height.
     *
     * @param image image
     * @param maxWidth maximum width
     * @param maxHeight maximum height
     * @return the builder instance for method chaining
     */
    public B appendImage(Image image, float maxWidth, float maxHeight) {
        return appendImage(image, maxWidth, maxHeight, VAnchor.BASELINE);
    }

    /**
     * Appends an image as inline node scaled to fit inside the given maximum width and height.
     *
     * @param image image
     * @param maxWidth maximum width
     * @param maxHeight maximum height
     * @param vAnchor vertical anchor for image alignment in the text line
     * @return the builder instance for method chaining
     */
    public B appendImage(Image image, float maxWidth, float maxHeight, VAnchor vAnchor) {
        byte[] data = InlineNode.encodeArgbImageData(image);
        return appendInlineNodeWithStyle(() ->
                new InlineNode<>(createImage(image, maxWidth, maxHeight), InlineNode.MIME_TYPE_ARGB_IMAGE, data), vAnchor, 0.0);
    }

    /**
     * Appends an inline node to the rich-text content with a specific style.
     * This method creates a temporary style to apply while appending the inline node.
     *
     * @param inlineNodeSupplier a {@link Supplier} that provides the inline node to append
     * @return the builder instance for method chaining
     */
    protected B appendInlineNodeWithStyle(Supplier<? extends InlineNode<? extends N>> inlineNodeSupplier) {
        Function<String, InlineNode<? extends N>> nodeFactory = ignoredText -> inlineNodeSupplier.get();
        Style style = Style.create(
                nextStyleName("inline-node"),
                Map.entry(STYLE_ATTRIBUTE_INLINE_NODE_FACTORY, nodeFactory)
        );
        push(style);
        append(INLINE_NODE_MARKER);
        pop(style);
        return self();
    }

    /**
     * Appends an inline node to the rich-text content with a specific style
     * that includes vertical alignment and descent properties.
     * This method creates a temporary style to apply while appending the inline node.
     *
     * @param inlineNodeSupplier a {@link Supplier} that provides the inline node to append
     * @param vAnchor the vertical anchor point for aligning the node in the text line
     * @param descent the descent value to adjust the vertical alignment for the node
     * @return the builder instance for method chaining
     */
    protected B appendInlineNodeWithStyle(Supplier<? extends InlineNode<? extends N>> inlineNodeSupplier, VAnchor vAnchor, double descent) {
        Function<String, InlineNode<? extends N>> nodeFactory = ignoredText -> inlineNodeSupplier.get();
        Style style = Style.create(
                nextStyleName("inline-node"),
                Map.entry(STYLE_ATTRIBUTE_INLINE_NODE_FACTORY, nodeFactory),
                Map.entry(STYLE_ATTRIBUTE_INLINE_NODE_V_ANCHOR, vAnchor),
                Map.entry(STYLE_ATTRIBUTE_INLINE_NODE_DESCENT, descent)
        );
        push(style);
        append(INLINE_NODE_MARKER);
        pop(style);
        return self();
    }

    /**
     * Generates a unique style name prefixed with the specified string.
     *
     * @param prefix the prefix to use for the style name
     * @return a unique style name constructed using the prefix and an incrementing identifier
     */
    private static String nextStyleName(String prefix) {
        return "rich-text-builder-ext-" + prefix + "-" + STYLE_ID.incrementAndGet();
    }

}
