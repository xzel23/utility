package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A factory for creating {@link GenericTableCell} instances.
 *
 * @param <S> The type of the TableView generic type (the row object).
 * @param <T> The type of the item contained within the Cell.
 */
public class GenericTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    private final BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory;
    private final BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit;
    private final Function<@Nullable Node, @Nullable Node> cancelEdit;

    /**
     * Constructs a new GenericTableCellFactory.
     *
     * @param nodeFactory builds/updates the viewing node.
     * @param startEdit   builds the editing node and receives a commit callback.
     * @param cancelEdit  performs cleanup on the editing node.
     */
    public GenericTableCellFactory(
            BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory,
            BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit,
            Function<@Nullable Node, @Nullable Node> cancelEdit
    ) {
        this.nodeFactory = nodeFactory;
        this.startEdit = startEdit;
        this.cancelEdit = cancelEdit;
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> column) {
        return new GenericTableCell<>(nodeFactory, startEdit, cancelEdit);
    }
}
