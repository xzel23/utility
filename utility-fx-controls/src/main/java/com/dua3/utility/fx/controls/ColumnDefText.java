package com.dua3.utility.fx.controls;

import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents the definition of a column in a TableView.
 *
 * @param <S>         The type of the objects displayed in the TableView rows.
 * @param <T>         The type of the value displayed in the cells of this column.
 * @param header      The header text of the column.
 * @param editable    Specifies whether the column values are editable.
 * @param valueGetter A function that extracts the cell value from the row object.
 * @param valueSetter A consumer that sets the cell value in the row object.
 * @param converter   A StringConverter for converting between the cell value and its string representation.
 */
public record ColumnDefText<S, T>(
        String header,
        boolean editable,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        StringConverter<@Nullable T> converter) implements ColumnDef<S, T> {
    @Override
    public T get(S row) {
        return valueGetter.apply(row);
    }

    @Override
    public void set(S row, T value) {
        valueSetter.accept(row, value);
    }
}
