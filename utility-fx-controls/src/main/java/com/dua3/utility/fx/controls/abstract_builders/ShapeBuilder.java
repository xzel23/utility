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
 * @param <S> the type of node to be built
 * @param <B> the type of the concrete builder
 */
public abstract class ShapeBuilder<S extends Shape, B extends ShapeBuilder<S, B>> extends NodeBuilder<S, B> {
    private @Nullable ObservableValue<? extends Paint> fill = null;
    private @Nullable ObservableValue<? extends Paint> stroke = null;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected ShapeBuilder(Supplier<? extends S> factory) {
        super(factory);
    }

    /**
     * Build the Control.
     *
     * @return new Control instance
     */
    @Override
    public S build() {
        S node = super.build();
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
    public B fill(Paint fill) {
        this.fill = new SimpleObjectProperty<>(fill);
        return self();
    }

    /**
     * Sets the fill property for the shape being built.
     *
     * @param fill the {@link Paint} to be used as the fill for the shape
     * @return this builder instance
     */
    public B fill(Color fill) {
        this.fill = new SimpleObjectProperty<>(FxUtil.convert(fill));
        return self();
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     * @deprecated use {@link #fillFx(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindFillFx(ObservableValue<Paint> fill) {
        return fillFx(fill);
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     */
    public B fillFx(ObservableValue<Paint> fill) {
        this.fill = fill;
        return self();
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     * @deprecated use {@link #fill(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindFill(ObservableValue<Color> fill) {
        return fill(fill);
    }

    /**
     * Binds the given {@link ObservableValue} to the fill property of the node being built.
     * This allows the fill property of the node to dynamically reflect the value of the provided observable.
     *
     * @param fill the {@link ObservableValue} representing the fill value to be bound
     * @return this instance of the builder
     */
    public B fill(ObservableValue<Color> fill) {
        this.fill = PropertyConverter.convertReadOnly(fill, FxUtil.colorConverter());
        return self();
    }

    /**
     * Sets the stroke paint property for the shape being built.
     *
     * @param stroke the {@link Paint} to be applied as the stroke for the shape
     * @return this builder instance
     */
    public B stroke(Paint stroke) {
        this.stroke = new SimpleObjectProperty<>(stroke);
        return self();
    }

    /**
     * Sets the stroke paint property for the shape being built.
     *
     * @param stroke the {@link Paint} to be applied as the stroke for the shape
     * @return this builder instance
     */
    public B stroke(Color stroke) {
        this.stroke = new SimpleObjectProperty<>(FxUtil.convert(stroke));
        return self();
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     * @deprecated use {@link #strokeFx(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindStrokeFx(ObservableValue<Paint> stroke) {
        return strokeFx(stroke);
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     */
    public B strokeFx(ObservableValue<Paint> stroke) {
        this.stroke = stroke;
        return self();
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     * @deprecated use {@link #stroke(ObservableValue)} instead
     */
    @Deprecated(since = "20.0.4", forRemoval = true)
    public B bindStroke(ObservableValue<Color> stroke) {
        return stroke(stroke);
    }

    /**
     * Binds the stroke property of the node being built to the specified {@link ObservableValue}.
     * This ensures that the stroke property dynamically updates to reflect the value of the observable.
     *
     * @param stroke the {@link ObservableValue} to bind to the node's stroke property
     * @return the current instance of the builder
     */
    public B stroke(ObservableValue<Color> stroke) {
        this.stroke = PropertyConverter.convertReadOnly(stroke, FxUtil.colorConverter());
        return self();
    }

}
