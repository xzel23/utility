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
    protected final String text;
    protected final @Nullable Node graphic;
    protected final boolean editable;
    protected final double minWidth;
    protected final double maxWidth;
    protected double weight;
    protected final boolean resizable;
    protected final Function<S, T> valueGetter;
    protected final BiConsumer<S, T> valueSetter;

    protected AbstractColumnDef(
            String text,
            @Nullable Node graphic,
            boolean editable,
            double minWidth,
            double maxWidth,
            double weight,
            boolean resizable,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter) {
        this.text = text;
        this.graphic = graphic;
        this.editable = editable;
        this.minWidth = minWidth;
        this.maxWidth = maxWidth;
        this.weight = weight;
        this.resizable = resizable;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    public T get(S row) {
        return valueGetter.apply(row);
    }

    public void set(S row, T value) {
        valueSetter.accept(row, value);
    }

    public String text() {
        return text;
    }

    public Optional<Node> graphic() {
        return Optional.ofNullable(graphic);
    }

    public boolean editable() {
        return editable;
    }

    public double minWidth() {
        return minWidth;
    }

    public double maxWidth() {
        return maxWidth;
    }

    public double weight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public boolean resizable() {
        return resizable;
    }

    public Function<S, T> valueGetter() {
        return valueGetter;
    }

    public BiConsumer<S, T> valueSetter() {
        return valueSetter;
    }
}
