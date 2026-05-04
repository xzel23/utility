package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents the definition of a column in a TableView.
 *
 * @param <S>         The type of the objects displayed in the TableView rows.
 * @param <T>         The type of the value displayed in the cells of this column.
 */
public final class ColumnDefText<S, T> extends AbstractColumnDef<S, T> implements ColumnDef<S, T> {
    private final StringConverter<@Nullable T> converter;

    /**
     * Creates a new instance.
     *
     * @param text        The header text of the column.
     * @param graphic     The node to display in the column header.
     * @param editable    Specifies whether the column values are editable.
     * @param minWidth    The minimum width of the column.
     * @param maxWidth    The maximum width of the column.
     * @param weight      The column weight used to distribute the available width when resizing columns.
     * @param resizable   Specifies whether the column can be resized by the user.
     * @param reorderable Specifies whether the column can be reordered by the user.
     * @param sortable    Specifies whether the column is sortable.
     * @param valueGetter A function that extracts the cell value from the row object.
     * @param valueSetter A consumer that sets the cell value in the row object.
     * @param converter   A StringConverter for converting between the cell value and its string representation.
     */
    ColumnDefText(
            String text,
            @Nullable Node graphic,
            boolean editable,
            double minWidth,
            double maxWidth,
            double weight,
            boolean resizable,
            boolean reorderable,
            boolean sortable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            StringConverter<@Nullable T> converter) {
        super(text, graphic, editable, minWidth, maxWidth, weight, resizable, reorderable, sortable, valueGetter, valueSetter);
        this.converter = converter;
    }

    /**
     * Retrieves the StringConverter associated with the column. The converter is
     * used to convert between the string representation and the actual value type
     * of the column cells.
     *
     * @return the StringConverter used for converting cell values between their
     *         string representation and their actual type.
     */
    public StringConverter<@Nullable T> converter() {return converter;}

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        var that = (ColumnDefText) obj;
        return Objects.equals(text, that.text) &&
                editable == that.editable &&
                Double.doubleToLongBits(minWidth) == Double.doubleToLongBits(that.minWidth) &&
                Double.doubleToLongBits(maxWidth) == Double.doubleToLongBits(that.maxWidth) &&
                Double.doubleToLongBits(weight) == Double.doubleToLongBits(that.weight) &&
                resizable == that.resizable &&
                reorderable == that.reorderable &&
                sortable == that.sortable &&
                Objects.equals(valueGetter, that.valueGetter) &&
                Objects.equals(valueSetter, that.valueSetter) &&
                Objects.equals(converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, editable, minWidth, maxWidth, weight, resizable, reorderable, sortable, valueGetter, valueSetter, converter);
    }

    @Override
    public String toString() {
        return "ColumnDefText[" +
                "header=" + text + ", " +
                "editable=" + editable + ", " +
                "minWidth=" + minWidth + ", " +
                "maxWidth=" + maxWidth + ", " +
                "weight=" + weight + ", " +
                "resizable=" + resizable + ", " +
                "reorderable=" + reorderable + ", " +
                "sortable=" + sortable + ", " +
                "valueGetter=" + valueGetter + ", " +
                "valueSetter=" + valueSetter + ", " +
                "converter=" + converter + ']';
    }

}
