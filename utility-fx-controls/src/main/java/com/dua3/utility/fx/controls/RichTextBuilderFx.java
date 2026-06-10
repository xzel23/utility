package com.dua3.utility.fx.controls;

import com.dua3.utility.text.Style;
import com.dua3.utility.ui.RichTextBuilderExtBase;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * JavaFX rich-text builder with support for inline JavaFX nodes.
 */
public class RichTextBuilderFx extends RichTextBuilderExtBase<Node, RichTextBuilderFx> {

    private static final char INLINE_NODE_MARKER = '\uFFFC';
    private static final AtomicLong STYLE_ID = new AtomicLong();

    /**
     * Constructs a new instance of RichTextBuilderFx with default settings.
     */
    public RichTextBuilderFx() {
        // nothing to do
    }

    /**
     * Constructs a {@code RichTextBuilderFx} instance with the specified initial capacity.
     *
     * @param capacity the initial capacity of the internal buffer
     */
    public RichTextBuilderFx(int capacity) {
        super(capacity);
    }

    @Override
    public RichTextBuilderFx appendInlineNode(Supplier<? extends Node> node) {
        return appendInlineNodeWithStyle(Objects.requireNonNull(node, "node"));
    }

    @Override
    public RichTextBuilderFx appendHyperlink(CharSequence text, Runnable action) {
        String linkText = String.valueOf(text);
        Objects.requireNonNull(action, "action");
        return appendInlineNodeWithStyle(() -> {
            Hyperlink hyperlink = new Hyperlink(linkText);
            hyperlink.setPadding(Insets.EMPTY);
            hyperlink.setOnAction(evt -> action.run());
            return hyperlink;
        });
    }

    @Override
    public RichTextBuilderFx appendButton(CharSequence text, Runnable action) {
        String buttonText = String.valueOf(text);
        Objects.requireNonNull(action, "action");
        return appendInlineNodeWithStyle(() -> {
            Button button = new Button(buttonText);
            button.setPadding(new Insets(0, 4, 0, 4));
            button.setOnAction(evt -> action.run());
            return button;
        });
    }

    private RichTextBuilderFx appendInlineNodeWithStyle(Supplier<? extends Node> nodeSupplier) {
        Style style = Style.create(
                nextStyleName("inline-node"),
                Map.entry(TextPane.STYLE_ATTRIBUTE_INLINE_NODE_SUPPLIER, nodeSupplier)
        );
        push(style);
        append(INLINE_NODE_MARKER);
        pop(style);
        return this;
    }

    private static String nextStyleName(String prefix) {
        return "rich-text-builder-fx-" + prefix + "-" + STYLE_ID.incrementAndGet();
    }
}
