package com.dua3.utility.ui;

import com.dua3.utility.text.RichTextBuilder;

import java.util.function.Supplier;

/**
 * Base class for rich-text builders with support for inline nodes.
 *
 * @param <N> node type
 * @param <B> concrete builder type
 */
public abstract class RichTextBuilderExtBase<N, B extends RichTextBuilderExtBase<N, B>> extends RichTextBuilder {

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

    /**
     * Appends an inline node to the rich-text content using a {@link Supplier}.
     *
     * @param node a {@link Supplier} that provides the node to append
     * @return the builder instance for method chaining
     */
    public abstract B appendInlineNode(Supplier<? extends N> node);

    /**
     * Appends a hyperlink to the rich-text content. The hyperlink is displayed with the specified text
     * and executes the provided action when activated.
     *
     * @param text the text to display for the hyperlink
     * @param action the action to execute when the hyperlink is activated
     * @return the builder instance for method chaining
     */
    public abstract B appendHyperlink(CharSequence text, Runnable action);

    /**
     * Appends a button to the rich-text content. The button is displayed with the specified text
     * and executes the provided action when activated.
     *
     * @param text the text to display on the button
     * @param action the action to execute when the button is activated
     * @return the builder instance for method chaining
     */
    public abstract B appendButton(CharSequence text, Runnable action);

}
