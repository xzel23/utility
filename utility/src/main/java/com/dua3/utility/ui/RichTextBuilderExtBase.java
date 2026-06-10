package com.dua3.utility.ui;

import com.dua3.utility.text.RichTextBuilder;
import com.dua3.utility.text.Style;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Base class for rich-text builders with support for inline nodes.
 *
 * @param <N> node type
 * @param <B> concrete builder type
 */
public abstract class RichTextBuilderExtBase<N, B extends RichTextBuilderExtBase<N, B>> extends RichTextBuilder {

    private static final char INLINE_NODE_MARKER = '\uFFFC';
    private static final AtomicLong STYLE_ID = new AtomicLong();

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
    protected final B self() {
        return (B) this;
    }

    protected abstract String getInlineNodeSupplierStyleAttributeName();

    protected abstract N createHyperlink(CharSequence text, Runnable action);

    protected abstract N createButton(CharSequence text, Runnable action);

    /**
     * Appends an inline node to the rich-text content using a {@link Supplier}.
     *
     * @param node a {@link Supplier} that provides the node to append
     * @return the builder instance for method chaining
     */
    public B appendInlineNode(Supplier<? extends N> node) {
        return appendInlineNodeWithStyle(Objects.requireNonNull(node, "node"));
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
        Objects.requireNonNull(action, "action");
        return appendInlineNodeWithStyle(() -> createHyperlink(linkText, action));
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
        Objects.requireNonNull(action, "action");
        return appendInlineNodeWithStyle(() -> createButton(buttonText, action));
    }

    protected B appendInlineNodeWithStyle(Supplier<? extends N> nodeSupplier) {
        Style style = Style.create(
                nextStyleName("inline-node"),
                Map.entry(getInlineNodeSupplierStyleAttributeName(), Objects.requireNonNull(nodeSupplier, "nodeSupplier"))
        );
        push(style);
        append(INLINE_NODE_MARKER);
        pop(style);
        return self();
    }

    private static String nextStyleName(String prefix) {
        return "rich-text-builder-ext-" + prefix + "-" + STYLE_ID.incrementAndGet();
    }

}
