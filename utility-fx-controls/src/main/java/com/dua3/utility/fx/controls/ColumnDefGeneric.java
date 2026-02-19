package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a generic column definition intended for use in tabular data structures or grids.
 * The column supports customizable behavior for value retrieval, value updates,
 * and rendering using provided functional interfaces.
 *
 * @param <S>         The type of the row object from which the column value is derived and to which it is set.
 * @param <T>         The type of the value held by this column.
 */
public final class ColumnDefGeneric<S, T> extends AbstractColumnDef<S, T> implements ColumnDef<S, T> {
    private final BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory;
    private final BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit;
    private final Function<@Nullable Node, @Nullable Node> cancelEdit;

    /**
     * Create a new instance.
     *
     * @param text        The header text of the column.
     * @param graphic     The node to display in the column header.
     * @param editable    Specifies whether the column values are editable.
     * @param minWidth    The minimum width of the column.
     * @param maxWidth    The maximum width of the column.
     * @param weight      The column weight used to distribute the available width when resizing columns.
     * @param resizable   Specifies whether the column can be resized by the user.
     * @param sortable    Specifies whether the column is sortable.
     * @param valueGetter A function that extracts the cell value from the row object.
     * @param valueSetter A consumer that sets the cell value in the row object.
     * @param nodeFactory A function that creates a Node representation of the cell content.
     * @param startEdit   A function that starts editing the cell content.
     * @param cancelEdit  A function that cancels the editing process.
     */
    ColumnDefGeneric(
            String text,
            @Nullable Node graphic,
            boolean editable,
            double minWidth,
            double maxWidth,
            double weight,
            boolean resizable,
            boolean sortable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory,
            BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit,
            Function<@Nullable Node, @Nullable Node> cancelEdit) {
        super(text, graphic, editable, minWidth, maxWidth, weight, resizable, sortable, valueGetter, valueSetter);
        this.nodeFactory = nodeFactory;
        this.startEdit = startEdit;
        this.cancelEdit = cancelEdit;
    }

    /**
     * Provides a function that creates or modifies a Node representation of the cell content
     * based on the current value.
     * <p>
     * The method returns a BiFunction that takes an optional existing Node and the cell value
     * as input, and outputs a new or updated Node. This function can be used to define how
     * the cell content is rendered or updated dynamically.
     *
     * @return a BiFunction that accepts an optional Node and a cell value, and returns an optional Node.
     */
    public BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory() {return nodeFactory;}

    /**
     * Provides a function to start editing a cell's content by returning a {@link BiFunction}.
     * The function takes a Node (the current cell representation) and a Consumer of the updated value.
     * It may return a new Node representation for the cell during the editing process.
     *
     * @return A {@link BiFunction} that takes a {@code Node} and a {@code Consumer<T>} for processing
     *         the edited value, and returns an updated {@code Node}, or {@code null} if no Node is
     *         applicable or required.
     */
    public BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit() {return startEdit;}

    /**
     * Provides a function to cancel the editing process of a cell.
     *
     * @return A {@code Function} that takes the currently edited {@code Node} as input
     *         and returns the {@code Node} to be used after the cancellation process, or
     *         {@code null} if no specific {@code Node} is required.
     */
    public Function<@Nullable Node, @Nullable Node> cancelEdit() {return cancelEdit;}

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ColumnDefGeneric) obj;
        return Objects.equals(this.text, that.text) &&
                this.editable == that.editable &&
                Double.doubleToLongBits(this.minWidth) == Double.doubleToLongBits(that.minWidth) &&
                Double.doubleToLongBits(this.maxWidth) == Double.doubleToLongBits(that.maxWidth) &&
                Double.doubleToLongBits(this.weight) == Double.doubleToLongBits(that.weight) &&
                this.resizable == that.resizable &&
                this.sortable == that.sortable &&
                Objects.equals(this.valueGetter, that.valueGetter) &&
                Objects.equals(this.valueSetter, that.valueSetter) &&
                Objects.equals(this.nodeFactory, that.nodeFactory) &&
                Objects.equals(this.startEdit, that.startEdit) &&
                Objects.equals(this.cancelEdit, that.cancelEdit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, editable, minWidth, maxWidth, weight, resizable, sortable, valueGetter, valueSetter, nodeFactory, startEdit, cancelEdit);
    }

    @Override
    public String toString() {
        return "ColumnDefGeneric[" +
                "header=" + text + ", " +
                "editable=" + editable + ", " +
                "minWidth=" + minWidth + ", " +
                "maxWidth=" + maxWidth + ", " +
                "weight=" + weight + ", " +
                "resizable=" + resizable + ", " +
                "sortable=" + sortable + ", " +
                "valueGetter=" + valueGetter + ", " +
                "valueSetter=" + valueSetter + ", " +
                "nodeFactory=" + nodeFactory + ", " +
                "startEdit=" + startEdit + ", " +
                "cancelEdit=" + cancelEdit + ']';
    }

}
