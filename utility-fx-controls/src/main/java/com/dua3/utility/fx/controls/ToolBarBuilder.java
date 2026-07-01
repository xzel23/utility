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
import java.util.function.BiConsumer;

/**
 * A builder for constructing instances of {@link ToolBarExt}, providing a fluent API to configure
 * properties and manage toolbar items.
 */
public class ToolBarBuilder extends ControlBuilder<ToolBarExt, ToolBarBuilder> {

    private final List<Node> items = new ArrayList<>();

    private @Nullable Parent applicationParent;
    private @Nullable BiConsumer<? super @Nullable Parent, ? super ToolBarExt> addToApplicationParent;
    private @Nullable BiConsumer<? super @Nullable Parent, ? super ToolBarExt> removeFromApplicationParent;

    private @Nullable Parent embeddedParent;
    private @Nullable BiConsumer<? super @Nullable Parent, ? super ToolBarExt> addToEmbeddedParent;
    private @Nullable BiConsumer<? super @Nullable Parent, ? super ToolBarExt> removeFromEmbeddedParent;
    private @Nullable Property<DetachableNode.Location> location;

    /**
     * Constructs a new instance of the ControlBuilder class using the specified factory.
     *
     */
    protected ToolBarBuilder() {
        super(ToolBarExt::new);
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
     * @param <P> the generic type of the parent component.
     * @param parent the parent component to which the toolbar will be associated. It must be a
     *               subclass of {@link Parent}.
     * @param addToParent a {@link BiConsumer} defining the operation to add the toolbar to the
     *                    specified parent.
     * @param removeFromParent a {@link BiConsumer} defining the operation to remove the toolbar
     *                         from the specified parent.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    @SuppressWarnings("unchecked")
    public <P extends Parent> ToolBarBuilder setApplicationParent(
            P parent,
            BiConsumer<? super @Nullable P, ? super ToolBarExt> addToParent,
            BiConsumer<? super @Nullable P, ? super ToolBarExt> removeFromParent
    ) {
        this.applicationParent = parent;
        this.addToApplicationParent = (BiConsumer<? super Parent, ? super ToolBarExt>) addToParent;
        this.removeFromApplicationParent = (BiConsumer<? super Parent, ? super ToolBarExt>) removeFromParent;
        return self();
    }

    /**
     * Sets the embedded-level parent for the toolbar being built. This method allows
     * specifying how the toolbar should be added to and removed from the given parent.
     *
     * @param <P> the generic type of the parent component.
     * @param parent the parent component to which the toolbar will be associated. It must be a
     *               subclass of {@link Parent}.
     * @param addToParent a {@link BiConsumer} defining the operation to add the toolbar to the
     *                    specified parent.
     * @param removeFromParent a {@link BiConsumer} defining the operation to remove the toolbar
     *                         from the specified parent.
     * @return the current {@link ToolBarBuilder} instance, allowing method chaining.
     */
    @SuppressWarnings("unchecked")
    public <P extends Parent> ToolBarBuilder setEmbeddedParent(
            P parent,
            BiConsumer<? super @Nullable P, ? super ToolBarExt> addToParent,
            BiConsumer<? super @Nullable P, ? super ToolBarExt> removeFromParent
    ) {
        this.embeddedParent = parent;
        this.addToEmbeddedParent = (BiConsumer<? super Parent, ? super ToolBarExt>) addToParent;
        this.removeFromEmbeddedParent = (BiConsumer<? super Parent, ? super ToolBarExt>) removeFromParent;
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
    public ToolBarExt build() {
        ToolBarExt toolBarExt = super.build();
        toolBarExt.getItems().addAll(items);
        if (applicationParent != null) {
            assert addToApplicationParent != null && removeFromApplicationParent != null;
            toolBarExt.setEmbeddedParent(applicationParent, addToApplicationParent, removeFromApplicationParent);
        }
        if (embeddedParent != null) {
            assert addToEmbeddedParent != null && removeFromEmbeddedParent != null;
            toolBarExt.setEmbeddedParent(embeddedParent, addToEmbeddedParent, removeFromEmbeddedParent);
        }
        if (location != null) {
            toolBarExt.locationProperty().bind(location);
        }
        return toolBarExt;
    }
}
