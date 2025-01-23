package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.fx.FxFontUtil;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PropertyConverter;
import com.dua3.utility.fx.controls.LabelBuilder;
import com.dua3.utility.text.Font;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.text.Text;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * LabeledBuilder is an abstract base class for building instances of Labeled nodes.
 * It provides a fluent API for configuring properties such as text and graphics,
 * allowing subclasses to create customized Labeled instances.
 *
 * @param <N>  the type of Labeled node to be built
 * @param <NN> the type of the concrete builder
 */
public abstract class LabeledBuilder<N extends Labeled, NN extends LabeledBuilder<N, NN>> extends ControlBuilder<N, NN> {
    private @Nullable ObservableValue<String> text;
    private @Nullable ObservableValue<javafx.scene.text.Font> font = null;
    private @Nullable ObservableValue<Node> graphic;

    /**
     * Constructor.
     *
     * @param factory the factory method for Labeled instances
     */
    protected LabeledBuilder(Supplier<? extends N> factory) {
        super(factory);
    }

    /**
     * Build the Labeled.
     *
     * @return the new Labeled instance
     */
    public N build() {
        N node = super.build();
        apply(text, node.textProperty());
        apply(graphic, node.graphicProperty());
        apply(font, node.fontProperty());
        return node;
    }

    /**
     * Set text for the Labeled.
     *
     * @param text the text
     * @return this LabeledBuilder instance
     */
    public NN text(String text) {
        this.text = new SimpleStringProperty(text);
        return self();
    }

    /**
     * Set text for the Labeled.
     *
     * @param text the text
     * @return this LabeledBuilder instance
     */
    public NN bindText(ObservableValue<String> text) {
        this.text = text;
        return self();
    }

    /**
     * Set the graphic for the Labeled.
     *
     * @param graphic the graphic to use
     * @return this LabeledBuilder instance
     */
    public NN graphic(Node graphic) {
        this.graphic = new SimpleObjectProperty<>(graphic);
        return self();
    }

    /**
     * Set the graphic for the Labeled.
     *
     * @param graphic the graphic to use
     * @return this LabeledBuilder instance
     */
    public NN graphic(ObservableValue<Node> graphic) {
        this.graphic = graphic;
        return self();
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    public NN font(javafx.scene.text.Font font) {
        this.font = new SimpleObjectProperty<>(font);
        return self();
    }

    /**
     * Sets the font for the text node being built.
     *
     * @param font the {@link Font} to set for the text node
     * @return this TextBuilder instance for fluent method chaining
     */
    public NN font(Font font) {
        this.font = new SimpleObjectProperty<>(FxFontUtil.getInstance().convert(font));
        return self();
    }

    /**
     * Binds the {@link Font} property of the {@link Text} node to the specified {@link ObservableValue}.
     *
     * This allows the font property of the node to dynamically update whenever the value
     * in the provided observable changes.
     *
     * @param font the {@link ObservableValue} providing the font to bind to the node's font property
     * @return this {@link LabelBuilder} instance for method chaining
     */
    public NN bindFxFont(ObservableValue<javafx.scene.text.Font> font) {
        this.font = font;
        return self();
    }

    /**
     * Binds the {@link Font} property of the {@link Text} node to the specified {@link ObservableValue}.
     *
     * <p>This allows the font property of the node to dynamically update whenever the value
     * in the provided observable changes.
     *
     * @param font the {@link ObservableValue} providing the font to bind to the node's font property
     * @return this {@link LabelBuilder} instance for method chaining
     */
    public NN bindFont(ObservableValue<Font> font) {
        this.font = PropertyConverter.convertReadOnly(font, FxUtil.fontConverter());
        return self();
    }
}
