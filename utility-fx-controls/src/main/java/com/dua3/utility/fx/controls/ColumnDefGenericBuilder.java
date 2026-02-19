package com.dua3.utility.fx.controls;

import javafx.scene.Node;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for {@link ColumnDefGeneric}.
 *
 * @param <S> The type of the objects displayed in the rows of the table or grid.
 * @param <T> The type of the values contained within the cells of this column.
 */
public class ColumnDefGenericBuilder<S, T> extends ColumnDefBuilder<S, T, ColumnDefGenericBuilder<S, T>> {
    private final BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory;
    private BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit = (node, consumer) -> null;
    private Function<@Nullable Node, @Nullable Node> cancelEdit = node -> null;

    /**
     * Constructs a new instance of the {@code ColumnDefGenericBuilder} class with the specified column name
     * and a factory function for creating nodes.
     *
     * @param name the text to be displayed as the header for the column
     * @param nodeFactory a {@code BiFunction} that takes an optional {@code Node} and a cell value of type {@code T}
     *                    and produces an optional {@code Node} to be used for rendering
     */
    ColumnDefGenericBuilder(String name, BiFunction<@Nullable Node, T, @Nullable Node> nodeFactory) {
        super(name);
        this.nodeFactory = nodeFactory;
    }

    /**
     * Configures the behavior for starting the edit mode of a cell in the column.
     * The specified {@code startEdit} function is invoked when editing is initiated.
     *
     * @param startEdit A {@link BiFunction} that takes the currently rendered {@link Node}
     *                  (or {@code null} if there is no rendered node) and a
     *                  {@link Consumer} accepting the edited value. The function
     *                  returns a {@link Node} that represents the editor or {@code null}.
     * @return The current {@code ColumnDefGenericBuilder} instance for method chaining.
     */
    public ColumnDefGenericBuilder<S, T> startEdit(BiFunction<@Nullable Node, Consumer<@Nullable T>, @Nullable Node> startEdit) {
        this.startEdit = startEdit;
        return self();
    }

    /**
     * Sets the function to be invoked when the editing process is canceled for a cell in the column.
     * The function takes the current {@link Node} of the cell as input and returns the {@link Node}
     * to be displayed after the editing process is canceled.
     *
     * @param cancelEdit A {@link Function} that accepts the current {@link Node} of the cell
     *                   and returns the {@link Node} to be displayed when editing is canceled.
     *                   May accept and return {@code null}.
     * @return The current {@code ColumnDefGenericBuilder} instance for method chaining.
     */
    public ColumnDefGenericBuilder<S, T> cancelEdit(Function<@Nullable Node, @Nullable Node> cancelEdit) {
        this.cancelEdit = cancelEdit;
        return self();
    }

    @Override
    public ColumnDefGeneric<S, T> build() {
        return new ColumnDefGeneric<>(
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
                nodeFactory,
                startEdit,
                cancelEdit
        );
    }
}
