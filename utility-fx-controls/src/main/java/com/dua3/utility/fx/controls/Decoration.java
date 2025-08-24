package com.dua3.utility.fx.controls;

import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;

/**
 * The Decoration class provides static methods to manage decorations for JavaFX Nodes.
 * Decorations can be added, removed, and their positions updated relative to the owner node.
 */
public final class Decoration {

    private static final String DECORATION_LIST = Decoration.class.getName() + ".decoration_list";
    private static final String OWNER = Decoration.class.getName() + ":owner";
    private static final String POSITION = Decoration.class.getName() + ":position";
    private static final String PREFIX = Decoration.class.getName() + ":";

    private Decoration() {
    }

    /**
     * Retrieves the list of decorations associated with a given Node. If no decorations are present,
     * a new list is created and stored in the node's properties.
     *
     * @param node the {@link Node} from which to retrieve or to which to add decorations
     * @return an observable list of decorations associated with the specified node
     */
    public static ObservableList<Decoration> getDecorations(Node node) {
        @SuppressWarnings("unchecked")
        ObservableList<Decoration> decorations = (ObservableList<Decoration>) node.getProperties().get(DECORATION_LIST);
        if (decorations == null) {
            decorations = FXCollections.observableArrayList();
            node.getProperties().put(DECORATION_LIST, decorations);
        }
        return decorations;
    }

    /**
     * Adds a decoration to a specified {@link Node} at a given position with an identifiable ID.
     *
     * @param node the node to which the decoration will be added
     * @param position the position where the decoration will be placed relative to the node
     * @param decoration the decoration node to be added
     * @param id the unique identifier for the decoration
     */
    public static void addDecoration(Node node, Pos position, Node decoration, String id) {
        DecorationPane decorationPane = DecorationPane.getDecorationPane(node);

        decoration.getProperties().put(OWNER, node);
        decoration.getProperties().put(POSITION, position);
        updateDecorationPosition(decoration);
        Object oldDecoration = node.getProperties().put(getDecorationId(id), decoration);

        if (oldDecoration != null) {
            decorationPane.removeDecoration(oldDecoration);
        }

        decorationPane.getChildren().add(decoration);
    }

    static void updateDecorationPosition(Node decoration) {
        Node node = (Node) decoration.getProperties().get(OWNER);

        if (node == null) {
            return;
        }

        Pos position = (Pos) decoration.getProperties().get(POSITION);

        Bounds bounds = node.getLayoutBounds();
        Bounds decorationBounds = decoration.getLayoutBounds();

        double x = switch (position.getHpos()) {
            case LEFT -> bounds.getMinX() - decorationBounds.getWidth() / 2.0;
            case CENTER -> bounds.getCenterX() - decorationBounds.getWidth() / 2.0;
            case RIGHT -> bounds.getMaxX() - decorationBounds.getWidth() / 2.0;
        };

        double y = switch (position.getVpos()) {
            case TOP -> bounds.getMinY() - decorationBounds.getHeight() / 2.0;
            case CENTER -> bounds.getCenterY() - decorationBounds.getHeight() / 2.0;
            case BOTTOM -> bounds.getMaxY() - decorationBounds.getHeight() / 2.0;
            case BASELINE -> bounds.getMaxY() - decorationBounds.getHeight() / 2.0;
        };

        decoration.setLayoutX(x + node.getLayoutX());
        decoration.setLayoutY(y + node.getLayoutY());
    }

    private static String getDecorationId(String id) {
        return PREFIX + id;
    }

    /**
     * Removes a decoration from the given Node as identified by the specified ID.
     * If the decoration exists, it is removed from the decoration pane associated with the node.
     *
     * @param node the Node from which to remove the decoration
     * @param id the unique identifier of the decoration to be removed
     */
    public static void removeDecoration(Node node, String id) {
        Object oldDecoration = node.getProperties().remove(getDecorationId(id));
        if (oldDecoration != null) {
            DecorationPane decorationPane = DecorationPane.getDecorationPane(node);
            decorationPane.removeDecoration(oldDecoration);
        }
    }

}

class DecorationPane extends AnchorPane {

    static final String DECORATION_PANE = "com.dua3.decoration_pane";

    DecorationPane(Parent sceneRoot) {
        setBackground(null);
        getChildren().setAll(sceneRoot);
    }

    static DecorationPane getDecorationPane(Node node) {
        DecorationPane decorationPane = (DecorationPane) node.getProperties().get(DECORATION_PANE);
        if (decorationPane == null) {
            decorationPane = getDecorationPane(node.getScene());
            node.getProperties().put(DECORATION_PANE, decorationPane);
        }
        return decorationPane;
    }

    /**
     * Get DecorationPane scene. If no DecorationPane has been set up, inject a new DecorationPane
     * between the scene and its root.
     *
     * @param scene the Scene
     * @return the scene's DecorationPane
     */
    private static DecorationPane getDecorationPane(Scene scene) {
        Parent sceneRoot = Objects.requireNonNull(scene.getRoot(), "scene has no root");

        if (sceneRoot instanceof DecorationPane dp) {
            return dp;
        }

        DecorationPane decorationPane = new DecorationPane(sceneRoot);
        scene.setRoot(decorationPane);
        scene.addPostLayoutPulseListener(decorationPane::updateDecorationLayout);
        return decorationPane;
    }

    private void updateDecorationLayout() {
        getChildren().forEach(Decoration::updateDecorationPosition);
    }

    void removeDecoration(Object oldDecoration) {
        //noinspection SuspiciousMethodCalls
        getChildren().remove(oldDecoration);
    }
}
