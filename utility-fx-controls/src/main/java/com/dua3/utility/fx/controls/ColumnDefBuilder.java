package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Base class for {@link ColumnDef} builders.
 *
 * @param <S> The type of the objects displayed in the rows of the table or grid.
 * @param <T> The type of the values contained within the cells of this column.
 * @param <B> The type of the builder itself.
 */
public abstract class ColumnDefBuilder<S, T, B extends ColumnDefBuilder<S, T, B>> {
    protected final String text;
    protected @Nullable Node graphic = null;
    protected boolean editable = false;
    protected double minWidth = 0.0;
    protected double maxWidth = Double.MAX_VALUE;
    protected double weight = ColumnDef.DEFAULT_WEIGHT;
    protected boolean resizable = true;
    protected Function<S, T> valueGetter = s -> null;
    protected BiConsumer<S, T> valueSetter = (s, t) -> {};

    /**
     * Constructs a new instance of the {@code ColumnDefBuilder} class with the specified column name.
     *
     * @param name the text to be displayed as the header for the column
     */
    protected ColumnDefBuilder(String name) {
        this.text = name;
    }

    /**
     * Returns the builder instance cast to the appropriate subclass type.
     * This method enables method chaining in the builder pattern by ensuring
     * that the methods in the subclass return the correct type.
     *
     * @return The builder instance cast to the type parameter {@code B}.
     */
    @SuppressWarnings("unchecked")
    protected final B self() {
        return (B) this;
    }

    /**
     * Sets the graphic to be displayed alongside the column's text.
     *
     * @param graphic the Node to be used as the graphic, or {@code null} if no graphic is to be displayed
     * @return this builder instance for chaining
     */
    public B graphic(@Nullable Node graphic) {
        this.graphic = graphic;
        return self();
    }

    /**
     * Sets whether the column is editable.
     *
     * @param editable true if the column should be editable, false otherwise
     * @return the builder instance itself for method chaining
     */
    public B editable(boolean editable) {
        this.editable = editable;
        return self();
    }

    /**
     * Sets the minimum width for the column.
     *
     * @param minWidth the minimum width to set, in pixels
     * @return the builder instance for method chaining
     */
    public B minWidth(double minWidth) {
        this.minWidth = minWidth;
        return self();
    }

    /**
     * Sets the maximum width for the column.
     *
     * @param maxWidth The maximum width of the column in pixels. Must be a non-negative value.
     * @return The builder instance for method chaining.
     */
    public B maxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
        return self();
    }

    /**
     * Sets the weight of the column. The weight is used to determine how much
     * space this column should take relative to other columns in layouts that
     * support flexible column widths.
     *
     * @param weight the relative weight of the column; must be a non-negative value.
     * @return the builder instance for chaining additional configuration methods.
     */
    public B weight(double weight) {
        this.weight = weight;
        return self();
    }

    /**
     * Sets whether the column is resizable by the user.
     *
     * @param resizable true if the column should be resizable, false otherwise
     * @return the builder instance for method chaining
     */
    public B resizable(boolean resizable) {
        this.resizable = resizable;
        return self();
    }

    /**
     * Sets the function used to extract the value from an object of type {@code S}
     * and assigns it to the cells of this column.
     *
     * @param valueGetter A {@link Function} that maps an object of type {@code S}
     *                    to a value of type {@code T}.
     * @return The builder instance for chaining further configuration.
     */
    public B valueGetter(Function<S, T> valueGetter) {
        this.valueGetter = valueGetter;
        return self();
    }

    /**
     * Sets the value setter function for this column. The value setter is responsible
     * for assigning a new value to the corresponding property of the row's backing object.
     *
     * @param valueSetter a {@code BiConsumer} that accepts the object representing a row
     *                    and the new value to be set in the column
     * @return the builder instance for method chaining
     */
    public B valueSetter(BiConsumer<S, T> valueSetter) {
        this.valueSetter = valueSetter;
        return self();
    }

    /**
     * Constructs and returns a new instance of {@link ColumnDef} based on the configuration
     * of the builder. This method finalizes the building process and encapsulates
     * the column definition settings into a concrete implementation of {@link ColumnDef}.
     *
     * @return A new {@link ColumnDef} instance encapsulating the configured settings.
     */
    public abstract ColumnDef<S, T> build();
}
