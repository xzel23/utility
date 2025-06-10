package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Pane;

/**
 * Base class for simple custom controls composed of different child controls.
 * @param <C> the container generic parameter
 */
public class CustomControl<C extends Pane> extends Control {

    /**
     * The container holding child controls for the custom control.
     * This is the root Node of the control hierarchy and provides layout capabilities
     * and other properties inherited from its type.
     */
    protected final C container;

    /**
     * Constructor.
     * @param container the container holding the child controls
     */
    protected CustomControl(C container) {
        this.container = container;
    }

    @Override
    protected double computePrefWidth(double height) {
        return container.prefWidth(height);
    }

    @Override
    protected double computePrefHeight(double width) {
        return container.prefHeight(width);
    }

    @Override
    protected double computeMinWidth(double height) {
        return container.minWidth(height);
    }

    @Override
    protected double computeMinHeight(double width) {
        return container.minHeight(width);
    }

    @Override
    protected double computeMaxWidth(double height) {
        return container.maxWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width) {
        return container.maxHeight(width);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new Skin<CustomControl<C>>() {
            @Override
            public CustomControl<C> getSkinnable() {
                return CustomControl.this;
            }

            @Override
            public Node getNode() {
                return container;
            }

            @Override
            public void dispose() { /* nothing to do */ }
        };
    }
}
