package com.dua3.utility.fx.controls;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PropertyConverter;
import com.dua3.utility.fx.controls.abstract_builders.ShapeBuilder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jspecify.annotations.Nullable;

/**
 * An abstract base class for building nodes, providing a fluent API for configuring and creating instances
 * of the node type specified by the generic parameter {@code N}.
 */
public class TextBuilder extends ShapeBuilder<Text, TextBuilder> {
    private final ObservableValue<String> text;
    private @Nullable ObservableValue<Font> font = null;
    private @Nullable ObservableValue<? extends Paint> fill = null;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     */
    protected TextBuilder(String text) {
        super(Text::new);
        this.text = new SimpleStringProperty(text);
    }

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     */
    protected TextBuilder(ObservableValue<String> text) {
        super(Text::new);
        this.text = text;
    }

    @Override
    public Text build() {
        Text node = super.build();
        apply(text, node.textProperty());
        apply(font, node.fontProperty());
        apply(fill, node.fillProperty());
        return node;
    }

    /**
     * Sets the fill property for the node being built.
     *
     * @param fill the {@link Paint} to be used as the fill for the shape
     * @return this builder instance
     */
    public TextBuilder fill(Paint fill) {
        this.fill = new SimpleObjectProperty<>(fill);
        return self();
    }

    /**
     * Sets the fill property for the node being built.
     *
     * @param fill the {@link Paint} to be used as the fill for the shape
     * @return this builder instance
     */
    public TextBuilder fill(Color fill) {
        this.fill = new SimpleObjectProperty<>(FxUtil.convert(fill));
        return self();
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     */
    public TextBuilder bindFillFx(ObservableValue<Paint> fill) {
        this.fill = fill;
        return self();
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     */
    public TextBuilder bindFill(ObservableValue<Color> fill) {
        this.fill = PropertyConverter.convertReadOnly(fill, FxUtil.colorConverter());
        return self();
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link com.dua3.utility.text.Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    public TextBuilder font(javafx.scene.text.Font font) {
        this.font = new SimpleObjectProperty<>(font);
        return self();
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link com.dua3.utility.text.Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    public TextBuilder font(com.dua3.utility.text.Font font) {
        this.font = new SimpleObjectProperty<>(FxFontUtil.getInstance().convert(font));
        this.fill = new SimpleObjectProperty<>(FxUtil.convert(font.getColor()));
        return self();
    }

    /**
     * Binds the {@link com.dua3.utility.text.Font} property of the {@link Text} node to the specified {@link ObservableValue}.
     *
     * <p>This allows the font property of the node to dynamically update whenever the value
     * in the provided observable changes.
     *
     * @param font the {@link ObservableValue} providing the font to bind to the node's font property
     * @return this {@code TextBuilder} instance for method chaining
     */
    public TextBuilder bindFontFx(ObservableValue<javafx.scene.text.Font> font) {
        this.font = font;
        return self();
    }

    /**
     * Binds the {@link com.dua3.utility.text.Font} property of the {@link Text} node to the specified {@link ObservableValue}.
     *
     * <p>This allows the font property of the node to dynamically update whenever the value
     * in the provided observable changes.
     *
     * @param font the {@link ObservableValue} providing the font to bind to the node's font property
     * @return this {@code TextBuilder} instance for method chaining
     */
    public TextBuilder bindFont(ObservableValue<com.dua3.utility.text.Font> font) {
        this.font = PropertyConverter.convertReadOnly(font, FxUtil.fontConverter());
        this.fill = PropertyConverter.convertReadOnly(font, f -> FxUtil.convert(f.getColor()));
        return self();
    }

}
