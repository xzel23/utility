package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Image;
import com.dua3.utility.fx.FxImageUtil;
import com.dua3.utility.text.RichTextBuilderExtBase;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;

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

    @Override
    protected Node createImage(Image image) {
        return new ImageView(FxImageUtil.getInstance().toImage(image).fxImage());
    }

    @Override
    protected Node createImage(Image image, float maxWidth, float maxHeight) {
        ImageView imageView = new ImageView(FxImageUtil.getInstance().toImage(image).fxImage());
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(Math.max(1.0, maxWidth));
        imageView.setFitHeight(Math.max(1.0, maxHeight));
        imageView.setSmooth(true);
        return imageView;
    }
}
