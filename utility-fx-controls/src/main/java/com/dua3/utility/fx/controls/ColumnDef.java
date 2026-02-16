package com.dua3.utility.fx.controls;

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
}
