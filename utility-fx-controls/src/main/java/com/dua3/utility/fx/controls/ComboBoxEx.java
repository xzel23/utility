package com.dua3.utility.fx.controls;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.jspecify.annotations.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A custom ComboBox control that supports additional features like editing, adding, and removing items.
 *
 * @param <T> the type of the items contained in the ComboBox
 */
public class ComboBoxEx<T extends @Nullable Object> extends CustomControl<HBox> implements InputControl<T> {
    private static final Logger LOG = LogManager.getLogger(ComboBoxEx.class);

    private @Nullable Comparator<? super T> comparator = null;
    private final @Nullable UnaryOperator<T> edit;
    private final @Nullable Supplier<T> add;
    private final @Nullable BiPredicate<ComboBoxEx<T>, T> remove;
    private final Function<T, String> format;
    private final ObservableList<T> items;
    private final ComboBox<T> comboBox;

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, and items.
     *
     * @param edit    the unary operator to perform editing on the selected item (nullable)
     * @param add     the supplier to provide a new item to add (nullable)
     * @param remove  the bi-predicate to determine if an item should be removed (nullable)
     * @param format  the function to format the items as strings
     * @param items   the initial items to populate the ComboBox (variadic parameter)
     */
    @SafeVarargs
    public ComboBoxEx(@Nullable UnaryOperator<T> edit, @Nullable Supplier<T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, T... items) {
        this(edit, add, remove, format, Arrays.asList(items));
    }

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, and items.
     *
     * @param edit    the unary operator to perform editing on the selected item (nullable)
     * @param add     the supplier to provide a new item to add (nullable)
     * @param remove  the bi-predicate to determine if an item should be removed (nullable)
     * @param format  the function to format the items as strings
     * @param items   the initial items to populate the ComboBox (variadic parameter)
     */
    public ComboBoxEx(@Nullable UnaryOperator<T> edit, @Nullable Supplier<T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, Collection<T> items) {
        super(new HBox());
        container.setAlignment(Pos.CENTER_LEFT);

        getStyleClass().setAll("comboboxex");

        this.format = format;
        this.items = FXCollections.observableArrayList(List.copyOf(items));

        this.comboBox = new ComboBox<>(this.items);
        ObservableList<Node> children = container.getChildren();
        children.setAll(comboBox);

        if (edit != null) {
            this.edit = edit;
            Button buttonEdit = Controls.button().text("✎").action(this::editItem).build();
            children.add(buttonEdit);
            buttonEdit.disableProperty().bind(comboBox.selectionModelProperty().isNull());
        } else {
            this.edit = null;
        }

        if (add != null) {
            this.add = add;
            Button buttonAdd = Controls.button().text("+").action(this::addItem).build();
            children.add(buttonAdd);
        } else {
            this.add = null;
        }

        if (remove != null) {
            this.remove = remove;
            Button buttonRemove = Controls.button().text("-").action(this::removeItem).build();
            children.add(buttonRemove);
            buttonRemove.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> comboBox.getSelectionModel().getSelectedItem() != null && this.items.size() > 1,
                    comboBox.selectionModelProperty(), this.items)
            );
            buttonRemove.disableProperty().bind(comboBox.selectionModelProperty().isNull().or(comboBox.valueProperty().isNull()));
        } else {
            this.remove = null;
        }

        Callback<ListView<T>, @Nullable ListCell<T>> cellFactory = new Callback<>() {

            @Override
            public ListCell<T> call(@Nullable ListView<T> lv) {
                return new ListCell<>() {

                    @Override
                    protected void updateItem(@Nullable T item, boolean empty) {
                        super.updateItem(item, empty);

                        String text = "";
                        if (!empty) {
                            try {
                                text = format.apply(item);
                            } catch (Exception e) {
                                LOG.warn("error during formatting", e);
                                text = String.valueOf(item);
                            }
                        }
                        setText(text);
                    }
                };
            }
        };

        comboBox.setButtonCell(cellFactory.call(null));
        comboBox.setCellFactory(cellFactory);
    }

    private void editItem() {
        if (edit == null) {
            LOG.warn("editing not supported");
            return;
        }

        int idx = comboBox.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            T item = items.get(idx);
            item = edit.apply(item);
            if (item != null) {
                items.remove(idx);
                items.add(idx, item);
                comboBox.getSelectionModel().select(idx);
                sortItems();
            }
        }
    }

    private void addItem() {
        Optional.ofNullable(add).map(Supplier::get).ifPresent(item -> {
            items.add(item);
            comboBox.getSelectionModel().select(item);
            sortItems();
        });
    }

    private void removeItem() {
        T item = comboBox.getSelectionModel().getSelectedItem();
        if (Optional.ofNullable(remove).orElse(ComboBoxEx::alwaysRemoveSelectedItem).test(this, item)) {
            int idx = items.indexOf(item);
            items.remove(idx);
            idx = Math.min(idx, items.size() - 1);
            if (idx >= 0) {
                set(items.get(idx));
            }
        }
    }

    /**
     * Prompts the user with a confirmation dialogue to verify whether to remove the selected item.
     *
     * <p>Pass this as the {@code remove} parameter to the constructor to show a confirmation dialog
     * when the user wants to remove an item.
     *
     * @param item the item to be removed
     * @return true if the user confirms the removal, false otherwise
     */
    public boolean askBeforeRemoveSelectedItem(T item) {
        return Dialogs.confirmation(Optional.ofNullable(getScene()).map(Scene::getWindow).orElse(null))
                .header("Remove %s?", format.apply(item))
                .defaultButton(ButtonType.YES)
                .build()
                .showAndWait()
                .map(bt -> bt == ButtonType.YES || bt == ButtonType.OK)
                .orElse(false);
    }

    /**
     * Remove the selected item without showing a confirmation dialog.
     *
     * <p>Pass this as the {@code remove} parameter to the constructor to remove items without showing
     * a confirmation dialog.
     *
     * @param <T> the type of items contained in the {@code ComboBoxEx}
     * @param cb the {@code ComboBoxEx} to remove the item from
     * @param item the item to be removed
     * @return true if the user confirms the removal, false otherwise
     */
    public static <T> boolean alwaysRemoveSelectedItem(ComboBoxEx<T> cb, T item) {
        return true;
    }

    /**
     * Retrieves the currently selected item from the ComboBoxEx.
     *
     * @return an Optional containing the selected item if one is selected, or an empty Optional if no item is selected
     */
    public Optional<T> getSelectedItem() {
        return Optional.ofNullable(comboBox.getSelectionModel().getSelectedItem());
    }

    /**
     * Retrieves a copy of the items in the ComboBoxEx.
     *
     * @return an immutable list containing the current items in the ComboBoxEx
     */
    public List<T> getItems() {
        return List.copyOf(items);
    }

    /**
     * Returns the property containing the selected item in the ComboBoxEx.
     *
     * @return the ReadOnlyObjectProperty representing the selected item property
     */
    public ReadOnlyObjectProperty<T> selectedItemProperty() {
        return comboBox.selectionModelProperty().get().selectedItemProperty();
    }

    /**
     * Sets the comparator for the ComboBoxEx and sorts the items accordingly.
     * If the comparator is null, the items will not be sorted.
     *
     * @param comparator the comparator to set, which is used for sorting the items
     */
    public void setComparator(Comparator<? super T> comparator) {
        this.comparator = comparator;
        sortItems();
    }

    /**
     * Sorts the items in the ComboBoxEx using the defined comparator.
     * If the comparator is null, the method returns without performing any action.
     * The currently selected item, if any, will remain selected after sorting.
     */
    public void sortItems() {
        if (comparator == null) {
            return;
        }

        Optional<T> selectedItem = getSelectedItem();
        items.sort(comparator);
        selectedItem.ifPresent(item -> comboBox.selectionModelProperty().get().select(item));
    }

    @Override
    public Node node() {
        return this;
    }

    @Override
    public Property<T> valueProperty() {
        return comboBox.valueProperty();
    }

    @Override
    public void reset() {

    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return new SimpleBooleanProperty(true);
    }

    @Override
    public ReadOnlyStringProperty errorProperty() {
        return new SimpleStringProperty("");
    }
}
