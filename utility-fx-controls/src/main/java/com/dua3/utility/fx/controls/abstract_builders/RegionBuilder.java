package com.dua3.utility.fx.controls.abstract_builders;

import javafx.scene.layout.Region;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * An abstract base class for building Regions, providing a fluent API for configuring and creating instances
 * of the Region type specified by the generic parameter {@code R}.
 *
 * @param <R> the type of Region to be built
 * @param <B> the type of the concrete builder
 */
public abstract class RegionBuilder<R extends Region, B extends RegionBuilder<R, B>> extends NodeBuilder<R, B> {
    private @Nullable Double prefWidth = null;
    private @Nullable Double prefHeight = null;
    private @Nullable Double minWidth = null;
    private @Nullable Double minHeight = null;
    private @Nullable Double maxWidth = null;
    private @Nullable Double maxHeight = null;

    /**
     * Constructs a new instance of the RegionBuilder class using the specified factory.
     *
     * @param factory the supplier that provides a new instance of the node type to be built
     */
    protected RegionBuilder(Supplier<? extends R> factory) {
        super(factory);
    }

    /**
     * Build the Region.
     *
     * @return new Region instance
     */
    @Override
    public R build() {
        R node = super.build();
        apply(minWidth, node::setMinWidth);
        apply(minHeight, node::setMinHeight);
        apply(prefWidth, node::setPrefWidth);
        apply(prefHeight, node::setPrefHeight);
        apply(maxWidth, node::setMaxWidth);
        apply(maxHeight, node::setMaxHeight);
        return node;
    }

    /**
     * Sets the minimum width for the node being built.
     *
     * @param width the minimum width to set for the node
     * @return this instance of the builder
     */
    public B minWidth(double width) {
        this.minWidth = width;
        return self();
    }

    /**
     * Sets the minimum height of the node being built.
     *
     * @param height the minimum height to set
     * @return this instance of the builder
     */
    public B minHeight(double height) {
        this.minHeight = height;
        return self();
    }

    /**
     * Sets the minimum width and height for the node being built.
     *
     * @param width  the minimum width to set for the node
     * @param height the minimum height to set for the node
     * @return this instance of the builder
     */
    public B minSize(double width, double height) {
        this.minWidth = width;
        this.minHeight = height;
        return self();
    }

    /**
     * Sets the preferred width for the node being built.
     *
     * @param width the preferred width to set for the node
     * @return this instance of the builder
     */
    public B prefWidth(double width) {
        this.prefWidth = width;
        return self();
    }

    /**
     * Sets the preferred height of the node being built.
     *
     * @param height the preferred height to set
     * @return this instance of the builder
     */
    public B prefHeight(double height) {
        this.prefHeight = height;
        return self();
    }

    /**
     * Sets the preferred width and height for the node being built.
     *
     * @param width  the preferred width to set for the node
     * @param height the preferred height to set for the node
     * @return this instance of the builder
     */
    public B prefSize(double width, double height) {
        this.prefWidth = width;
        this.prefHeight = height;
        return self();
    }

    /**
     * Sets the maximum width for the node being built.
     *
     * @param width the maximum width to set for the node
     * @return this instance of the builder
     */
    public B maxWidth(double width) {
        this.maxWidth = width;
        return self();
    }

    /**
     * Sets the maximum height of the node being built.
     *
     * @param height the maximum height to set
     * @return this instance of the builder
     */
    public B maxHeight(double height) {
        this.maxHeight = height;
        return self();
    }

    /**
     * Sets the maximum width and height for the node being built.
     *
     * @param width  the maximum width to set for the node
     * @param height the maximum height to set for the node
     * @return this instance of the builder
     */
    public B maxSize(double width, double height) {
        this.maxWidth = width;
        this.maxHeight = height;
        return self();
    }
}
