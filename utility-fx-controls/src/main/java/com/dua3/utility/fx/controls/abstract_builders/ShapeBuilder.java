package com.dua3.utility.fx.controls.abstract_builders;

import com.dua3.utility.data.Color;
import com.dua3.utility.fx.FxUtil;
import com.dua3.utility.fx.PropertyConverter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An abstract base class for building nodes, providing a fluent API for configuring and creating instances
 * of the node type specified by the generic parameter {@code N}.
 *
 * @param <N>  the type of node to be built
 * @param <NN> the type of the concrete builder
 */
public abstract class ShapeBuilder<N extends Shape, NN extends ShapeBuilder<N, NN>> extends NodeBuilder<N, NN> {
    private @Nullable ObservableValue<? extends Paint> fill = null;
    private @Nullable ObservableValue<? extends Paint> stroke = null;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected ShapeBuilder(Supplier<? extends N> factory) {
        super(factory);
    }

    /**
     * Build the Control.
     *
     * @return new Control instance
     */
    @Override
    public N build() {
        N node = super.build();
        apply(fill, node.fillProperty());
        apply(stroke, node.strokeProperty());
        return node;
    }

    /**
     * Sets the fill property for the shape being built.
     *
     * @param fill the {@link Paint} to be used as the fill for the shape
     * @return this builder instance
     */
    public NN fill(Paint fill) {
        this.fill = new SimpleObjectProperty<>(fill);
        return self();
    }

    /**
     * Sets the fill property for the shape being built.
     *
     * @param fill the {@link Paint} to be used as the fill for the shape
     * @return this builder instance
     */
    public NN fill(Color fill) {
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
    public NN bindFillFx(ObservableValue<Paint> fill) {
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
    public NN bindFill(ObservableValue<Color> fill) {
        this.fill = PropertyConverter.convertReadOnly(fill, FxUtil.colorConverter());
        return self();
    }

    /**
     * Sets the stroke paint property for the shape being built.
     *
     * @param stroke the {@link Paint} to be applied as the stroke for the shape
     * @return this builder instance
     */
    public NN stroke(Paint stroke) {
        this.stroke = new SimpleObjectProperty<>(stroke);
        return self();
    }

    /**
     * Sets the stroke paint property for the shape being built.
     *
     * @param stroke the {@link Paint} to be applied as the stroke for the shape
     * @return this builder instance
     */
    public NN stroke(Color stroke) {
        this.stroke = new SimpleObjectProperty<>(FxUtil.convert(stroke));
        return self();
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     */
    public NN bindStrokeFx(ObservableValue<Paint> stroke) {
        this.stroke = stroke;
        return self();
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     */
    public NN bindStroke(ObservableValue<Color> stroke) {
        this.stroke = PropertyConverter.convertReadOnly(stroke, FxUtil.colorConverter());
        return self();
    }

}
