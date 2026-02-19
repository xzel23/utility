package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Abstract base class for {@link ColumnDef} implementations.
 *
 * @param <S> The type of the objects displayed in the rows of the table or grid.
 * @param <T> The type of the values contained within the cells of this column.
 */
public abstract class AbstractColumnDef<S, T> {
    /**
     * The display text associated with the column definition.
     */
    protected final String text;
    /**
     * The graphic representation associated with a column.
     */
    protected final @Nullable Node graphic;
    /**
     * Indicates whether the column is editable.
     */
    protected final boolean editable;
    /**
     * Specifies the minimum width of the column in pixels.
     */
    protected final double minWidth;
    /**
     * The maximum width constraint for a column.
     */
    protected final double maxWidth;
    /**
     * Represents the relative importance of this column during layout computations.
     * A higher weight value indicates that the column should take more space
     * relative to other columns with lower weights during layout distribution.
     */
    protected double weight;
    /**
     * Determines whether the column is resizable.
     */
    protected final boolean resizable;
    /**
     * Indicates whether the column is sortable.
     */
    protected final boolean sortable;
    /**
     * A function that retrieves the value of type {@code T} for a given row of type {@code S}.
     */
    protected final Function<S, T> valueGetter;
    /**
     * A {@link BiConsumer} responsible for updating the value.
     */
    protected final BiConsumer<S, T> valueSetter;

    /**
     * Constructs an instance of AbstractColumnDef with the specified properties.
     *
     * @param text        The header text of the column.
     * @param graphic     An optional graphic to be displayed alongside the header text. Can be null.
     * @param editable    Specifies whether the column can be edited.
     * @param minWidth    The minimum width of the column.
     * @param maxWidth    The maximum allowable width of the column.
     * @param weight      A weight value that determines the proportional size of the column relative to others.
     * @param resizable   Indicates whether the column can be resized by the user.
     * @param sortable    Indicates whether the column supports sorting.
     * @param valueGetter The function used to retrieve the column's value for a row object.
     * @param valueSetter The function used to set the column's value for a row object.
     */
    protected AbstractColumnDef(
            String text,
            @Nullable Node graphic,
            boolean editable,
            double minWidth,
            double maxWidth,
            double weight,
            boolean resizable,
            boolean sortable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter) {
        this.text = text;
        this.graphic = graphic;
        this.editable = editable;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.weight = weight;
        this.resizable = resizable;
        this.sortable = sortable;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    /**
     * Retrieves the value of a specified row using the column's value getter function.
     *
     * @param row The row from which the value is extracted.
     * @return The value associated with the provided row.
     */
    public T get(S row) {
        return valueGetter.apply(row);
    }

    /**
     * Sets the value of the specified row using the valueSetter function.
     *
     * @param row   The row object whose value is to be set.
     * @param value The value to set for the specified row.
     */
    public void set(S row, T value) {
        valueSetter.accept(row, value);
    }

    /**
     * Retrieves the header text for the column.
     *
     * @return the column's header text
     */
    public String text() {
        return text;
    }

    /**
     * Retrieves an optional graphic associated with the column.
     * If a graphic is present, it is typically displayed alongside the column header text.
     *
     * @return an Optional containing the graphic if it is present; otherwise, an empty Optional.
     */
    public Optional<Node> graphic() {
        return Optional.ofNullable(graphic);
    }

    /**
     * Indicates whether the column values can be edited.
     *
     * @return {@code true} if the column values are editable; {@code false} otherwise.
     */
    public boolean editable() {
        return editable;
    }

    /**
     * Retrieves the minimum width of the column.
     *
     * @return the minimum width of the column as a double.
     */
    public double minWidth() {
        return minWidth;
    }

    /**
     * Retrieves the maximum allowable width of the column.
     *
     * @return the maximum width of the column as a double value.
     */
    public double maxWidth() {
        return maxWidth;
    }

    /**
     * Returns the weight associated with this column.
     * The weight determines the proportional sizing of the column
     * relative to other columns.
     *
     * @return the weight of the column as a double value
     */
    public double weight() {
        return weight;
    }

    /**
     * Sets the weight of the column. The weight influences how much space
     * the column should occupy relative to other columns.
     *
     * @param weight the new weight to be assigned to the column. Must be a non-negative value.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    /**
     * Indicates whether the column can be resized by the user.
     *
     * @return true if the column is resizable, false otherwise.
     */
    public boolean resizable() {
        return resizable;
    }

    /**
     * Indicates whether the column can be sorted.
     *
     * @return true if the column is sortable; false otherwise.
     */
    public boolean sortable() {
        return sortable;
    }

    /**
     * Returns the function responsible for retrieving the value of the column
     * from a given row object.
     *
     * @return A function that accepts a row object of type S and returns a value of type T.
     */
    public Function<S, T> valueGetter() {
        return valueGetter;
    }

    /**
     * Retrieves the BiConsumer function responsible for setting the value of the column
     * for a given object of type S and value of type T.
     *
     * @return A BiConsumer that defines the value-setting logic for this column.
     */
    public BiConsumer<S, T> valueSetter() {
        return valueSetter;
    }
}
