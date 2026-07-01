package com.dua3.utility.fx.controls;

import com.dua3.utility.fx.PlatformHelper;
import com.dua3.utility.ui.DetachableNode;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A subclass of {@code ToolBar} that implements the {@code DetachableNode} interface, allowing it
 * to be dynamically moved or detached within an application's UI. It introduces support for managing
 * the toolbar's location using a {@code Property} object.
 * <p>
 * {@code ToolBarExt} enables flexibility in the UI layout by allowing the toolbar to exist in different
 * states such as hidden, embedded, part of the application, or in an independent floating window.
 */
public class ToolBarEx extends ToolBar implements DetachableNode<ToolBarEx, Parent> {
    private static final Logger LOG = LogManager.getLogger(ToolBarEx.class);

    private final Property<Location> locationProperty = new SimpleObjectProperty<>(this, "location", Location.HIDDEN);
    private final List<LocationListener> locationListeners = new ArrayList<>();

    private final Map<Location, @Nullable Parent> locationToParent = new EnumMap<>(Location.class);
    private @Nullable Scene mainScene;

    /**
     * Constructs a {@code ToolBarExt} instance with the provided items.
     * This constructor initializes the toolbar with the specified {@code Node} elements
     * and adds them as children to the toolbar.
     *
     * @param items one or more {@code Node} elements to be included in the toolbar.
     */
    public ToolBarEx(
            Node... items
    ) {
        super(items);

        // use the parent that the toolbar is added to as the embedded location
        parentProperty().addListener(new ChangeListener<>() {
            @Override
            @SuppressWarnings("java:S4274")
            public void changed(ObservableValue<? extends @Nullable Parent> observable, @Nullable Parent oldValue, @Nullable Parent newValue) {
                assert newValue != null : "expected non-null parent on first parent change";
                Parent previousParent = locationToParent.computeIfAbsent(Location.EMBEDDED, k -> newValue);
                assert previousParent == null : "expected previous parent to be null";

                // keep the owner up to date
                newValue.sceneProperty().addListener((obs, oldVScene, newScene) -> mainScene = newScene);

                if (getLocation() != Location.EMBEDDED) {
                    removeFromParent(getNode());
                    addToParent(locationToParent.get(getLocation()));
                }

                // listener should only run once
                parentProperty().removeListener(this);
            }
        });

        locationToParent.put(Location.HIDDEN, new StackPane());
        locationToParent.put(Location.FLOATING, new FloatingPane());

        locationProperty.addListener((observable, oldValue, newValue) ->
                PlatformHelper.runAndWait(() -> {
                    Parent oldParent = getParent();
                    Parent newParent = locationToParent.get(newValue);

                    if (newParent != oldParent) {
                        removeFromParent(oldParent);
                        addToParent(newParent);
                    }
                })
        );
    }

    private void removeFromParent(@Nullable Parent parent) {
        switch (parent) {
            case Pane pane -> pane.getChildren().remove(this);
            case null -> { /* nothing to do */ }
            default -> LOG.warn("cannot remove node from the unsupported parent type: {}", parent::getClass);
        }
    }

    private void addToParent(@Nullable Parent parent) {
        switch (parent) {
            case Pane pane -> pane.getChildren().addFirst(this);
            case null -> { /* nothing to do */ }
            default -> LOG.warn("cannot add node to unsupported parent type: {}", parent::getClass);
        }
    }

    final class FloatingPane extends StackPane {
        private @Nullable Stage stage;
        private final Scene scene = new Scene(this);

        private double xOffset = 0;
        private double yOffset = 0;

        FloatingPane() {
            // Intercept mouse events during the capturing phase (on the way down) to make the floating pane draggable
            addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (getBoundsInLocal().contains(event.getX(), event.getY()) && stage != null && isDraggableTarget(event.getTarget())) {
                    xOffset = event.getScreenX() - stage.getX();
                    yOffset = event.getScreenY() - stage.getY();
                    event.consume();
                    // We do NOT consume the press here, allowing the button
                    // to still receive its click event down the line.
                }
            });

            addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
                if (stage != null && isDraggableTarget(event.getTarget())) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                    event.consume(); // Consume the drag so inner controls don't fight it
                }
            });

            // automatically destroy the stage when the toolbar is removed
            getChildren().addListener((ListChangeListener<? super Node>) c -> {
                if (c.getList().isEmpty()) {
                    if (stage != null) {
                        stage.close();
                        stage = null;
                    }
                } else if (stage == null) {
                    stage = new Stage(StageStyle.UNDECORATED);
                    if (mainScene != null) {
                        stage.initOwner(mainScene.getWindow());
                    }
                    stage.setScene(scene);
                    stage.show();
                }
            });
        }

        private boolean isDraggableTarget(Object target) {
            if (!(target instanceof Node node)) {
                return false;
            }

            // Walk up the temporary tree to see if it's inside a Button or Control
            while (node != null && node != this) {
                if (node instanceof javafx.scene.control.Control && !(node instanceof javafx.scene.control.ToolBar)) {
                    return false; // It's an interactive control, don't drag the window
                }
                node = node.getParent();
            }

            return true;
        }
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
    public ToolBarEx getNode() {
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
    public void setApplicationParent(@Nullable Parent parent) {
        LOG.debug("setApplicationParent({})", parent);
        locationToParent.put(Location.APPLICATION, parent);
    }
}
