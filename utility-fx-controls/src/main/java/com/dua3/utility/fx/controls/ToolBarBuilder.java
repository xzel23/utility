package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.controls.abstract_builders.ControlBuilder;
import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.ui.DetachableNode;
import javafx.beans.property.Property;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A builder for constructing instances of {@link ToolBarEx}, providing a fluent API to configure
 * properties and manage toolbar items.
 */
public class ToolBarBuilder extends ControlBuilder<ToolBarEx, ToolBarBuilder> {

    private final List<Node> items = new ArrayList<>();

    private @Nullable Parent applicationParent;
    private @Nullable Property<DetachableNode.Location> location;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     */
    protected ToolBarBuilder() {
        super(ToolBarEx::new);
    }

    /**
     * Adds the provided items to the toolbar being built.
     *
     * @param items the items to be added to the toolbar. Each item is represented as a {@link Node}.
     *              These will be stored as an unmodifiable list.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    public ToolBarBuilder items(Node... items) {
        return items(LangUtil.asUnmodifiableList(items));
    }

    /**
     * Adds the provided items to the toolbar being built.
     *
     * @param items the items to be added to the toolbar. Each item is represented as a {@link Node}.
     *              These will be stored as an unmodifiable list.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    public ToolBarBuilder items(Collection<? extends Node> items) {
        this.items.addAll(items);
        return self();
    }

    /**
     * Sets the application-level parent for the toolbar being built. This method allows
     * specifying how the toolbar should be added to and removed from the given parent.
     *
     * @param parent the parent component with which the toolbar will be associated.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    public ToolBarBuilder applicationParent(Parent parent) {
        this.applicationParent = parent;
        return self();
    }

    /**
     * Binds the location property of the toolbar being built. This method associates
     * the given {@link Property} representing the location with the toolbar, allowing
     * it to track and reflect changes in location.
     *
     * @param location the {@link Property} representing the location of the toolbar.
     *                 The property is of type {@link DetachableNode.Location}.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    public ToolBarBuilder bindLocation(Property<DetachableNode.Location> location) {
        this.location = location;
        return self();
    }

    @Override
    public ToolBarEx build() {
        ToolBarEx toolBarEx = super.build();
        toolBarEx.getItems().addAll(items);
        if (applicationParent != null) {
            toolBarEx.setApplicationParent(applicationParent);
        }
        if (location != null) {
            toolBarEx.locationProperty().bind(location);
        }
        return toolBarEx;
    }
}
