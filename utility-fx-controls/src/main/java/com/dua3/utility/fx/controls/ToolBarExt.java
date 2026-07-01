package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.ui.DetachableNode;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A subclass of {@code ToolBar} that implements the {@code DetachableNode} interface, allowing it
 * to be dynamically moved or detached within an application's UI. It introduces support for managing
 * the toolbar's location using a {@code Property} object.
 * <p>
 * {@code ToolBarExt} enables flexibility in the UI layout by allowing the toolbar to exist in different
 * states such as hidden, embedded, part of the application, or in an independent floating window.
 */
public class ToolBarExt extends ToolBar implements DetachableNode<ToolBarExt, Parent, Parent> {
    private static final Logger LOG = LogManager.getLogger(ToolBarExt.class);

    private final Property<Location> locationProperty = new SimpleObjectProperty<>(this, "location", Location.HIDDEN);
    private final List<LocationListener> locationListeners = new ArrayList<>();

    record ParentData(
            @Nullable Parent parent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> addToParent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> removeFromParent
    ) {
        void setAsParentOf(ToolBarExt node) {
            PlatformHelper.checkApplicationThread();

            assert node.getParent() == null : "node already has a parent";
            assert node.getScene() == null : "node already is added to a scene graph";

            addToParent.accept(parent, node);
        }

        void removeAsParentOf(ToolBarExt node) {
            PlatformHelper.checkApplicationThread();

            removeFromParent.accept(parent, node);

            assert node.getParent() == null : "node parent should be null now";
            assert node.getScene() == null : "node should not be part of a scene graph now";
        }
    }

    private final ParentData floatingParent;
    private ParentData applicationParent;
    private ParentData embeddedParent;
    private ParentData hiddenParent;

    /**
     * Constructs a {@code ToolBarExt} instance with the provided items.
     * This constructor initializes the toolbar with the specified {@code Node} elements
     * and adds them as children to the toolbar.
     *
     * @param items one or more {@code Node} elements to be included in the toolbar.
     */
    public ToolBarExt(
            Node... items
    ) {
        super(items);

        this.floatingParent = createFloatingParent();
        this.hiddenParent = createHiddenParent();
        this.embeddedParent = createHiddenParent();
        this.applicationParent = createHiddenParent();

        locationProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != oldValue) {
                // remove from previous parent
                getParentData(oldValue).removeAsParentOf(this);

                // add to new parent
                getParentData(newValue).setAsParentOf(this);

                for (LocationListener listener : locationListeners) {
                    listener.locationChanged(this, oldValue, newValue);
                }
            }
        });
    }

    private ParentData createFloatingParent() {
        return new ParentData(
                this,
                // add to a new floating window
                (parent, node) -> {
                    assert this == node : "node is not this";
                    assert getParent() == null : "node already has a parent";
                    assert getScene() == null : "node already is added to a scene graph";
                    Stage stage = new Stage(StageStyle.UNDECORATED);
                    stage.setScene(new Scene(this));
                },
                // remove the floating window
                (parent, node) -> {
                    if (getScene() instanceof Scene scene && scene.getWindow() instanceof Stage stage) {
                        // close the window wrapper
                        assert stage.getStyle() == StageStyle.UNDECORATED;
                        stage.close();
                    }
                    assert getParent() == null : "node already has a parent";
                    assert getScene() == null : "node already is added to a scene graph";
                });
    }

    private ParentData createHiddenParent() {
        return new ParentData(
                null,
                (p, n) -> {},
                (q, n) -> {
                    Node parentNode = n.getParent();
                    if (n.getParent() != q && q != null) {
                        throw new IllegalStateException("unexpected partent: " + parentNode + ", expected " + q);
                    }
                    switch (parentNode) {
                        case Pane pane -> pane.getChildren().remove(n);
                        case null -> { /* ignore */ }
                        default -> LOG.warn("unsupported parent type: {}", q.getClass().getName());
                    }
                }
        );
    }

    private ParentData getParentData(Location location) {
        return switch (location) {
            case APPLICATION -> applicationParent;
            case EMBEDDED -> embeddedParent;
            case FLOATING -> floatingParent;
            case HIDDEN -> hiddenParent;
        };
    }

    @Override
    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
    }

    @Override
    public boolean removeLocationListener(LocationListener listener) {
        return locationListeners.remove(listener);
    }

    @Override
    public void setLocation(Location location) {
        locationProperty.setValue(location);
    }

    @Override
    public Location getLocation() {
        return locationProperty.getValue();
    }

    @Override
    public ToolBarExt getNode() {
        return this;
    }

    /**
     * Provides access to the property representing the location or visibility state of the toolbar.
     * This property allows observing and modifying the {@code Location} of the toolbar,
     * indicating whether it is hidden, embedded, part of the application, or in a floating window.
     *
     * @return a {@code Property<Location>} representing the location state of the toolbar.
     */
    public Property<Location> locationProperty() {
        return locationProperty;
    }

    @Override
    public Optional<Parent> getEmbeddedParent() {
        return Optional.ofNullable(embeddedParent.parent);
    }

    @Override
    public void setEmbeddedParent(
            @Nullable Parent parent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> addToParent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> removeFromParent
    ) {
        PlatformHelper.checkApplicationThread();

        // remove from previous parent if necessary
        embeddedParent.removeAsParentOf(this);

        // set new values for fields
        embeddedParent = new ParentData(parent, addToParent, removeFromParent);

        // add to new parent
        embeddedParent.setAsParentOf(this);
    }

    @Override
    public Optional<Parent> getApplicationParent() {
        return Optional.ofNullable(applicationParent.parent);
    }

    @Override
    public void setApplicationParent(
            @Nullable Parent parent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> addToParent,
            BiConsumer<? super @Nullable Parent, ? super ToolBarExt> removeFromParent
    ) {
        PlatformHelper.checkApplicationThread();

        // remove from previous parent if necessary
        applicationParent.removeAsParentOf(this);

        // set new values for fields
        applicationParent = new ParentData(parent, addToParent, removeFromParent);

        // add to new parent
        applicationParent.setAsParentOf(this);
    }
}
