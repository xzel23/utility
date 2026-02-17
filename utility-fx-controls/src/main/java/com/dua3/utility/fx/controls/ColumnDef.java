package com.dua3.utility.fx.controls;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents the definition of a column in a tabular data structure.
 * This interface is intended to be implemented by specific column definitions,
 * defining behaviors for retrieving and setting cell values, as well as additional properties
 * such as the column's header and editability status.
 *
 * @param <S> The type of the objects displayed in the rows of the table or grid.
 * @param <T> The type of the values contained within the cells of this column.
 */
public sealed interface ColumnDef<S, T> permits ColumnDefText, ColumnDefGeneric {
    /**
     * A constant defining the default weight of a column.
     * The weight determines the relative allocation of space for the column
     * in comparison to other columns within a table.
     * <p>
     * The default value of 1.0 indicates an equal proportional distribution
     * among columns with the same weight.
     */
    double DEFAULT_WEIGHT = 1.0;

    /**
     * Retrieves the value of the cell in this column for the specified row object.
     *
     * @param row The row object from which the value should be extracted.
     * @return The value contained within the cell corresponding to this column for the given row.
     */
    T get(S row);

    /**
     * Sets the value for a specific cell in the column, corresponding to the given row.
     *
     * @param row The row object representing the data entry whose cell value is being updated.
     * @param value The new value to be set in the cell of the column for the specified row.
     */
    void set(S row, T value);

    /**
     * Retrieves the header text of the column.
     *
     * @return A string representing the header text of this column.
     */
    String header();

    /**
     * Determines whether the cells in this column are editable.
     *
     * @return true if the cells in the column can be edited; false otherwise.
     */
    boolean editable();

    /**
     * Retrieves the minimum width of the column.
     *
     * @return The minimum width of the column as a double value.
     */
    double minWidth();

    /**
     * Retrieves the maximum width of the column.
     * This value defines the upper limit for the column's width
     * when rendering or resizing within a table or grid.
     *
     * @return The maximum width of the column as a double.
     */
    double maxWidth();

    /**
     * Retrieves the weight of the column, which represents its relative proportion
     * or allocation of space compared to other columns.
     *
     * @return The weight of this column as a double value.
     */
    double weight();

    /**
     * Set a new weight value.
     *
     * @param weight The new weight value.
     */
    void setWeight(double weight);

    /**
     * Indicates whether the column can be resized.
     *
     * @return true if the column is resizable; false otherwise.
     */
    boolean resizable();

    /**
     * Retrieves the function used to extract the cell value from the row object.
     *
     * @return A function that takes a row object of type S and returns the cell value of type T.
     */
    Function<S, T> valueGetter();

    /**
     * Retrieves the value setter function for this column.
     * The value setter is a {@link BiConsumer} that updates the cell value of a row object.
     *
     * @return a BiConsumer that sets the cell value in a row object, accepting the row object
     *         and the new value as parameters.
     */
    BiConsumer<S, T> valueSetter();

}
