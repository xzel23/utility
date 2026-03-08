package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.ContentDisplay;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A generic TableCell implementation.
 *
 * @param <S> The row type of the TableView.
 * @param <T> The type of the item contained within the Cell.
 */
public class GenericTableCell<S, T> extends TableCell<S, T> {
    private final BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory;
    private final BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit;
    private final Function<@Nullable Node, @Nullable Node> cancelEdit;

    private @Nullable Node node;
    private @Nullable Node editNode;

    /**
     * Constructs a new GenericTableCell.
     * * @param nodeFactory builds/updates the viewing node. Takes (oldNode, item) and returns a Node.
     * @param nodeFactory the factory for creating the view node.
     * @param startEdit   builds the editing node. Takes (viewNode, commitCallback) and returns a Node.
     * @param cancelEdit  performs cleanup on the editing node when editing is aborted.
     */
    public GenericTableCell(
            BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory,
            BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit,
            Function<@Nullable Node, @Nullable Node> cancelEdit
    ) {
        this.nodeFactory = nodeFactory;
        this.startEdit = startEdit;
        this.cancelEdit = cancelEdit;
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(@Nullable T item, boolean empty) {
        super.updateItem(item, empty);
        node = empty ? null : nodeFactory.apply(node, item);
        editNode = null;
        setGraphic(node);
    }

    @Override
    public void startEdit() {
        assert editNode == null : "internal error: already in edit mode";

        // Create the editor node and inject the commit callback
        //noinspection DataFlowIssue - Qodana insists "this" might be null
        editNode = Objects.requireNonNull(startEdit.apply(node, this::commitEdit), "startEdit.apply() returned null");

        if (editNode != node) {
            setGraphic(editNode);
        }

        super.startEdit();

        // Ensure the editor gets focus immediately
        editNode.requestFocus();
    }

    @Override
    public void cancelEdit() {
        // Guard against double-calls which can happen in some FX versions
        if (editNode == null) return;

        cancelEdit.apply(editNode);
        if (editNode != node) {
            editNode = null;
            setGraphic(node);
        }

        super.cancelEdit();
    }
}