package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a generic column definition intended for use in tabular data structures or grids.
 * The column supports customizable behavior for value retrieval, value updates, 
 * and rendering using provided functional interfaces.
 *
 * @param <S> The type of the row object from which the column value is derived and to which it is set.
 * @param <T> The type of the value held by this column.
 * @param header      The header text of the column.
 * @param editable    Specifies whether the column values are editable.
 * @param valueGetter A function that extracts the cell value from the row object.
 * @param valueSetter A consumer that sets the cell value in the row object.
 * @param nodeFactory A function that creates a Node representation of the cell content.
 * @param startEdit   A function that starts editing the cell content.
 * @param cancelEdit  A function that cancels the editing process.
 */
public record ColumnDefGeneric<S, T>(
        String header,
        boolean editable,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory,
        BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit,
        Function<@Nullable Node, @Nullable Node> cancelEdit) implements ColumnDef<S, T> {
    @Override
    public T get(S row) {
        return valueGetter.apply(row);
    }

    @Override
    public void set(S row, T value) {
        valueSetter.accept(row, value);
    }
}
