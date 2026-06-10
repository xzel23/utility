package com.dua3.utility.fx.controls;

import com.dua3.utility.ui.RichTextBuilderExtBase;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;

/**
 * JavaFX rich-text builder with support for inline JavaFX nodes.
 */
public class RichTextBuilderFx extends RichTextBuilderExtBase<Node, RichTextBuilderFx> {

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
    protected String getInlineNodeSupplierStyleAttributeName() {
        return TextPane.STYLE_ATTRIBUTE_INLINE_NODE_SUPPLIER;
    }

    @Override
    protected Node createHyperlink(CharSequence text, Runnable action) {
        Hyperlink hyperlink = new Hyperlink(String.valueOf(text));
        hyperlink.setPadding(Insets.EMPTY);
        hyperlink.setOnAction(evt -> action.run());
        return hyperlink;
    }

    @Override
    protected Node createButton(CharSequence text, Runnable action) {
        Button button = new Button(String.valueOf(text));
        button.setPadding(new Insets(0, 4, 0, 4));
        button.setOnAction(evt -> action.run());
        return button;
    }
}
