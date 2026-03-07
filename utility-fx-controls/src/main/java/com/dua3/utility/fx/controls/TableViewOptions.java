package com.dua3.utility.fx.controls;

import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

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

    static final String ITEM_FACTORY = "itemFactory";

    /**
     * Defines the set of configurable options for a table view. Each option
     * represents a specific behavior or property that can be applied to the
     * table view.
     */
    public static final class Option {
        private final String name;
        private final Map<String, Object> params;

        private Option(String name) {
            this(name, Collections.emptyMap());
        }

        private Option(String name, Map<String, Object> params) {
            this.name = name;
            this.params = params;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static final String EDITABLE_OPTION = "EDITABLE";
    static final String SORTABLE_OPTION = "SORTABLE";
    static final String REORDERABLE_COLUMNS_OPTION = "REORDERABLE_COLUMNS";
    static final String REORDERABLE_ROWS_OPTION = "REORDERABLE_ROWS";
    static final String MULTIPLE_ROWS_SELECTABLE_OPTION = "MULTIPLE_ROWS_SELECTABLE";
    static final String ALLOW_DELETING_ROWS_OPTION = "ALLOW_DELETING_ROWS";
    static final String ALLOW_INSERTING_ROWS_OPTION = "ALLOW_INSERTING_ROWS";

    /** Make the TableView editable. */
    public static final Option EDITABLE = new Option(EDITABLE_OPTION);
    /** Make the TableView sortable by clicking on a column header. */
    public static final Option SORTABLE = new Option(SORTABLE_OPTION);
    /** Allow dragging columns for reordering. */
    public static final Option REORDERABLE_COLUMNS = new Option(REORDERABLE_COLUMNS_OPTION);
    /** Allow dragging rows for reordering. */
    public static final Option REORDERABLE_ROWS = new Option(REORDERABLE_ROWS_OPTION);
    /** Allow selecting multiple rows at once. */
    public static final Option MULTIPLE_ROWS_SELECTABLE = new Option(MULTIPLE_ROWS_SELECTABLE_OPTION);
    /** Allow deleting rows. */
    public static final Option ALLOW_DELETING_ROWS = new Option(ALLOW_DELETING_ROWS_OPTION);

    /**
     * Allow inserting rows.
     * <p>
     * Note: The unconventional method name is intentional to match the other options
     * that are simple constants.
     *
     * @param <S> the generic item type
     * @param itemFactory the item factory used to create new rows
     * @return the option to pass
     */
    public static <S> Option ALLOW_INSERTING_ROWS(Supplier<S> itemFactory) {
        return new Option(ALLOW_INSERTING_ROWS_OPTION, Map.of(ITEM_FACTORY, itemFactory));
    }

    private final Map<String, Option> options;

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
        Map<String, Option> map = new java.util.HashMap<>();
        for (Option opt : options) {
            map.put(opt.name, opt);
        }
        return new TableViewOptions(map);
    }

    private TableViewOptions(Map<String, Option> options) {
        this.options = Map.copyOf(options);
    }

    /**
     * Checks if the specified option is enabled for this table view configuration.
     * Each option is evaluated based on the internal bitmask representation.
     *
     * @param option the {@code Option} to check; must not be null
     * @return {@code true} if the specified option is enabled, {@code false} otherwise
     */
    public boolean isEnabled(Option option) {
        return isEnabled(option.name);
    }

    /**
     * Checks if the specified option is enabled for this table view configuration.
     *
     * @param option the name of the option to check
     * @return {@code true} if the specified option is enabled, otherwise {@code false}
     */
    public boolean isEnabled(String option) {
        return options.containsKey(option);
    }

    /**
     * Retrieves the parameters associated with the specified option.
     *
     * @param option the name of the option for which parameters are to be retrieved; must not be null
     * @return a map of parameter keys and values associated with the specified option,
     *         or an empty map if the option is not found
     */
    Map<String, Object> getParams(String option) {
        Option opt = options.get(option);
        return opt != null ? opt.params : Collections.emptyMap();
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
        Map<String, Option> map = new java.util.HashMap<>(this.options);
        for (Option opt : options) {
            map.put(opt.name, opt);
        }
        return new TableViewOptions(map);
    }

    /**
     * Creates a new {@code TableViewOptions} instance by disabling the specified options
     * in the current configuration.
     *
     * @param options an array of {@code Option} enums to be disabled.
     * @return a new {@code TableViewOptions} instance with the specified options disabled.
     */
    public TableViewOptions without(Option... options) {
        Map<String, Option> map = new java.util.HashMap<>(this.options);
        for (Option opt : options) {
            map.remove(opt.name);
        }
        return new TableViewOptions(map);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof TableViewOptions o && options.equals(o.options);
    }

    @Override
    public int hashCode() {
        return options.hashCode();
    }
}
