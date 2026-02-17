package com.dua3.utility.fx.controls;

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
public final class ColumnDefText<S, T> implements ColumnDef<S, T> {
    private final String header;
    private final boolean editable;
    private final double minWidth;
    private final double maxWidth;
    private double weight;
    private final boolean resizable;
    private final Function<S, T> valueGetter;
    private final BiConsumer<S, T> valueSetter;
    private final StringConverter<@Nullable T> converter;

    /**
     * Creates a new instance.
     *
     * @param header      The header text of the column.
     * @param editable    Specifies whether the column values are editable.
     * @param minWidth    The minimum width of the column.
     * @param maxWidth    The maximum width of the column.
     * @param weight      The column weight used to distribute the available width when resizing columns.
     * @param resizable   Specifies whether the column can be resized by the user.
     * @param valueGetter A function that extracts the cell value from the row object.
     * @param valueSetter A consumer that sets the cell value in the row object.
     * @param converter   A StringConverter for converting between the cell value and its string representation.
     */
    public ColumnDefText(
            String header,
            boolean editable,
            double minWidth,
            double maxWidth,
            double weight,
            boolean resizable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            StringConverter<@Nullable T> converter) {
        this.header = header;
        this.editable = editable;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.weight = weight;
        this.resizable = resizable;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
        this.converter = converter;
    }

    /**
     * Constructs a new instance using default values for width and resizable properties.
     *
     * @param header      The header text of the column.
     * @param editable    Specifies whether the column values are editable.
     * @param valueGetter A function that extracts the cell value from the row object.
     * @param valueSetter A consumer that sets the cell value in the row object.
     * @param converter   A StringConverter for converting between the cell value and its string representation.
     */
    public ColumnDefText(
            String header,
            boolean editable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            StringConverter<@Nullable T> converter
    ) {
        this(
                header,
                editable,
                0.0,
                Double.MAX_VALUE,
                DEFAULT_WEIGHT,
                true,
                valueGetter,
                valueSetter,
                converter
        );
    }

    @Override
    public T get(S row) {
        return valueGetter.apply(row);
    }

    @Override
    public void set(S row, T value) {
        valueSetter.accept(row, value);
    }

    @Override
    public String header() {return header;}

    @Override
    public boolean editable() {return editable;}

    @Override
    public double minWidth() {return minWidth;}

    @Override
    public double maxWidth() {return maxWidth;}

    @Override
    public double weight() {return weight;}

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public boolean resizable() {return resizable;}

    @Override
    public Function<S, T> valueGetter() {return valueGetter;}

    @Override
    public BiConsumer<S, T> valueSetter() {return valueSetter;}

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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ColumnDefText) obj;
        return Objects.equals(this.header, that.header) &&
                this.editable == that.editable &&
                Double.doubleToLongBits(this.minWidth) == Double.doubleToLongBits(that.minWidth) &&
                Double.doubleToLongBits(this.maxWidth) == Double.doubleToLongBits(that.maxWidth) &&
                Double.doubleToLongBits(this.weight) == Double.doubleToLongBits(that.weight) &&
                this.resizable == that.resizable &&
                Objects.equals(this.valueGetter, that.valueGetter) &&
                Objects.equals(this.valueSetter, that.valueSetter) &&
                Objects.equals(this.converter, that.converter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, editable, minWidth, maxWidth, weight, resizable, valueGetter, valueSetter, converter);
    }

    @Override
    public String toString() {
        return "ColumnDefText[" +
                "header=" + header + ", " +
                "editable=" + editable + ", " +
                "minWidth=" + minWidth + ", " +
                "maxWidth=" + maxWidth + ", " +
                "weight=" + weight + ", " +
                "resizable=" + resizable + ", " +
                "valueGetter=" + valueGetter + ", " +
                "valueSetter=" + valueSetter + ", " +
                "converter=" + converter + ']';
    }

}
