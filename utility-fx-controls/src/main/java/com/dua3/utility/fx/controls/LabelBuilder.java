package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PropertyConverter;
import com.dua3.utility.fx.controls.abstract_builders.LabeledBuilder;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jspecify.annotations.Nullable;

/**
 * An abstract base class for building nodes, providing a fluent API for configuring and creating instances
 * of the node type specified by the generic parameter {@code N}.
 */
public class LabelBuilder extends LabeledBuilder<Label, LabelBuilder> {
    private final ObservableValue<String> text;
    private @Nullable ObservableValue<Font> font = null;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param text the label text
     */
    protected LabelBuilder(String text) {
        super(Label::new);
        this.text = new SimpleStringProperty(text);
    }

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param text the label text
     */
    protected LabelBuilder(ObservableValue<String> text) {
        super(Label::new);
        this.text = text;
    }

    @Override
    public Label build() {
        Label node = super.build();
        apply(text, node.textProperty());
        apply(font, node.fontProperty());
        return node;
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link com.dua3.utility.text.Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    @Override
    public LabelBuilder font(javafx.scene.text.Font font) {
        this.font = new SimpleObjectProperty<>(font);
        return self();
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link com.dua3.utility.text.Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    @Override
    public LabelBuilder font(com.dua3.utility.text.Font font) {
        this.font = new SimpleObjectProperty<>(FxFontUtil.getInstance().convert(font));
        return self();
    }

    /**
     * Binds the {@link com.dua3.utility.text.Font} property of the {@link Text} node to the specified {@link ObservableValue}.
     *
     * <p>This allows the font property of the node to dynamically update whenever the value
     * in the provided observable changes.
     *
     * @param font the {@link ObservableValue} providing the font to bind to the node's font property
     * @return this {@link TextBuilder} instance for method chaining
     */
    public LabelBuilder bindFontFx(ObservableValue<javafx.scene.text.Font> font) {
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
     * @return this {@link TextBuilder} instance for method chaining
     */
    @Override
    public LabelBuilder bindFont(ObservableValue<com.dua3.utility.text.Font> font) {
        this.font = PropertyConverter.convertReadOnly(font, FxUtil.fontConverter());
        return self();
    }

}
