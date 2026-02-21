package com.dua3.utility.fx.controls;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A container class for managing and organizing {@link InputControl} components that are grouped together
 * and share a single error decorator.
 */
public class InputControlContainer implements InputControl<Void> {

    private final Pane pane;
    private final List<InputControl<?>> controls;
    private final InputControlState<Void> state;

    /**
     * Constructs an InputControlContainer with the specified orientation. This container is responsible
     * for managing a group of {@link InputControl} components and their validation state.
     *
     * @param orientation the orientation of the container; must be either {@link Orientation#HORIZONTAL}
     *                    or {@link Orientation#VERTICAL}. Determines whether the controls are arranged
     *                    horizontally or vertically.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public InputControlContainer(Orientation orientation) {
        switch (orientation) {
            case HORIZONTAL -> {
                HBox box = new HBox();
                box.setAlignment(Pos.BASELINE_LEFT);
                box.setStyle("-fx-spacing: 1em;");
                this.pane = box;
            }
            case VERTICAL -> {
                VBox box = new VBox();
                box.setAlignment(Pos.CENTER);
                this.pane = box;
            }
            default -> throw new IllegalArgumentException("invalid orientation: " + orientation);
        }

        Property<Void> value = new SimpleObjectProperty<>();

        this.controls = new ArrayList<>();

        this.state = new ObjectInputControlState<>(value, () -> null, this::validateContent);
    }

    private Optional<String> validateContent(@Nullable Void v) {
        List<InputControl<?>> icList = List.copyOf(controls);

        if (icList.isEmpty()) {
            return Optional.empty();
        }

        // collect and return error messages from invalid controls
        return Optional.of(
                icList.stream()
                        .map(ic -> ic.errorProperty().get())
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining("\n\n"))
        );
    }

    /**
     * Adds the specified InputControl to the container.
     *
     * @param ic the {@link InputControl} instance to be added; must not be null
     */
    public void add(InputControl<?> ic) {
        add(ic.node());
        controls.add(ic);
        ic.valueProperty().addListener((v, o, n) -> state.reset());
        state.reset();
    }

    /**
     * Adds the specified node to the container.
     *
     * @param node the node to be added; must not be null
     */
    public void add(Node node) {
        pane.getChildren().add(node);
    }

    @Override
    public InputControlState<Void> state() {
        return state;
    }

    @Override
    public Node node() {
        return pane;
    }

    @Nullable Node getLast() {
        ObservableList<Node> children = pane.getChildren();
        return children.isEmpty() ? null : children.getLast();
    }
}
