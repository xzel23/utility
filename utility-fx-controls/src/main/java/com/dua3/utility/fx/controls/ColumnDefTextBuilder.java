package com.dua3.utility.fx.controls;

import javafx.util.StringConverter;
import org.jspecify.annotations.Nullable;

/**
 * Builder for {@link ColumnDefText}.
 *
 * @param <S> The type of the objects displayed in the rows of the table or grid.
 * @param <T> The type of the values contained within the cells of this column.
 */
public class ColumnDefTextBuilder<S, T> extends ColumnDefBuilder<S, T, ColumnDefTextBuilder<S, T>> {
    private final StringConverter<@Nullable T> converter;

    /**
     * Constructs a new instance of {@code ColumnDefTextBuilder} with the specified column name and value converter.
     *
     * @param name the text to be displayed as the header for the column
     * @param converter the {@code StringConverter} used to convert between cell values and their textual representation
     */
    ColumnDefTextBuilder(String name, StringConverter<@Nullable T> converter) {
        super(name);
        this.converter = converter;
    }

    @Override
    public ColumnDefText<S, T> build() {
        return new ColumnDefText<>(
                text,
                graphic,
                editable,
                minWidth,
                maxWidth,
                weight,
                resizable,
                sortable,
                valueGetter,
                valueSetter,
                converter
        );
    }
}
