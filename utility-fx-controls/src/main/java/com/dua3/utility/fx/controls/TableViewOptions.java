package com.dua3.utility.fx.controls;

/**
 * Represents configuration options for a table view. This class allows the
 * specification and retrieval of various behaviors associated with the
 * table view by using predefined options.
 *
 * <ul>
 *     <li>READ_ONLY: Makes the table view read-only, preventing any editing of its content.</li>
 *     <li>DRAGGABLE_ROWS: Enables row reordering within the table view through drag-and-drop.</li>
 * </ul>
 *
 * Instances of this class are immutable and can be created using the
 * {@code of} method by specifying the desired options.
 */
public final class TableViewOptions {
    /**
     * Defines the set of configurable options for a table view. Each option
     * represents a specific behavior or property that can be applied to the
     * table view.
     */
    public enum Option {
        /** Make the TableView editable. */
        EDITABLE,
        /** Make the TableView sortable by clicking on a column header. */
        SORTABLE,
        /** Allow dragging columns for reordering. */
        REORDERABLE_COLUMNS,
        /** Allow dragging rows for reordering. */
        REORDERABLE_ROWS;

        int bitmask() { return 1 << ordinal(); }
    }

    /** Make the TableView editable. */
    public static final Option EDITABLE = Option.EDITABLE;
    /** Make the TableView sortable by clicking on a column header. */
    public static final Option SORTABLE = Option.SORTABLE;
    /** Allow dragging columns for reordering. */
    public static final Option REORDERABLE_COLUMNS = Option.REORDERABLE_COLUMNS;
    /** Allow dragging rows for reordering. */
    public static final Option REORDERABLE_ROWS = Option.REORDERABLE_ROWS;

    private final int value;

    /**
     * Creates an instance of TableViewOptions with the specified configuration value.
     * This constructor is private and is used internally to initialize
     * the table view options with a bitmask value representing selected options.
     *
     * @param value an integer bitmask representing the combination of enabled options
     */
    private TableViewOptions(int value) {
        this.value = value;
    }

    /**
     * Creates an instance of TableViewOptions by combining the specified options.
     * Each option is represented by a bitmask, and the resulting object encapsulates
     * the bitwise combination of the options provided.
     *
     * @param options an array of {@code Option} enums specifying the desired table view behaviors.
     *                If no options are provided, the resulting TableViewOptions will have no behaviors enabled.
     * @return a new {@code TableViewOptions} instance configured with the specified options.
     */
    public static TableViewOptions of(Option... options) {
        int v = 0;
        for (Option opt : options) {
            v |= opt.bitmask();
        }
        return new TableViewOptions(v);
    }

    /**
     * Checks if the specified option is enabled for this table view configuration.
     * Each option is evaluated based on the internal bitmask representation.
     *
     * @param option the {@code Option} to check; must not be null
     * @return {@code true} if the specified option is enabled, {@code false} otherwise
     */
    public boolean isEnabled(Option option) {
        return (value & option.bitmask()) != 0;
    }

    /**
     * Creates a new {@code TableViewOptions} instance by combining the current options
     * with the specified additional options.
     *
     * @param options an array of {@code Option} enums specifying additional table view
     *                configurations to be combined with the current state.
     * @return a new {@code TableViewOptions} instance that combines the current options
     *         with the specified additional options.
     */
    public TableViewOptions with(Option... options) {
        int v = value;
        for (Option opt : options) {
            v |= opt.bitmask();
        }
        return new TableViewOptions(v);
    }

    /**
     * Creates a new {@code TableViewOptions} instance by disabling the specified options
     * in the current configuration.
     *
     * @param options an array of {@code Option} enums to be disabled.
     * @return a new {@code TableViewOptions} instance with the specified options disabled.
     */
    public TableViewOptions without(Option... options) {
        int v = value;
        for (Option opt : options) {
            v &= ~opt.bitmask();
        }
        return new TableViewOptions(v);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TableViewOptions o && value == o.value;
    }

    @Override
    public int hashCode() {
        return value;
    }
}
