package com.dua3.utility.fx.controls;

import com.dua3.utility.lang.LangUtil;
import com.dua3.utility.text.MessageFormatter;
import javafx.scene.control.Alert;
import org.jspecify.annotations.Nullable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A custom ComboBox control that supports additional features like editing, adding, and removing items.
 *
 * @param <T> the type of the items contained in the ComboBox
 */
@SuppressWarnings({"java:S4276", "java:S107"})
public class ComboBoxEx<T> extends CustomControl<HBox> {
    private static final Logger LOG = LogManager.getLogger(ComboBoxEx.class);

    private @Nullable Comparator<? super T> comparator = null;
    private final @Nullable Function<T, @Nullable T> edit;
    private final @Nullable Supplier<@Nullable T> add;
    private final @Nullable BiPredicate<ComboBoxEx<T>, @Nullable T> remove;
    private final Supplier<? extends @Nullable T> dflt;
    private final Function<? super @Nullable T, @Nullable String> format;
    private final Function<? super @Nullable T, ? extends @Nullable Node> graphic;
    private final Function<? super @Nullable T, ? extends javafx.scene.text.@Nullable Font> font;
    private final ObservableList<T> items;
    private final ComboBox<@Nullable T> comboBox;

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, and items.
     *
     * @param edit   the unary operator to perform editing on the selected item (nullable)
     * @param add    the supplier to provide a new item to add (nullable)
     * @param remove the bi-predicate to determine if an item should be removed (nullable)
     * @param dflt   the supplier for the default value
     * @param format the function to format the items as strings
     * @param graphic  a function to provide a graphic for the combo box item, or null if no graphic is required
     * @param items  the initial items to populate the ComboBox (variadic parameter)
     */
    @SafeVarargs
    public ComboBoxEx(
            @Nullable Function<T, @Nullable T> edit,
            @Nullable Supplier<@Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Supplier<? extends @Nullable T> dflt,
            Function<? super @Nullable T, @Nullable String> format,
            Function<? super @Nullable T, ? extends @Nullable Node> graphic,
            T... items
    ) {
        this(edit, add, remove, dflt, format, graphic, item -> null, LangUtil.asUnmodifiableList(items));
    }

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, and items.
     *
     * @param edit   the unary operator to perform editing on the selected item (nullable)
     * @param add    the supplier to provide a new item to add (nullable)
     * @param remove the bi-predicate to determine if an item should be removed (nullable)
     * @param dflt   the supplier for the default value
     * @param format the function to format the items as strings
     * @param graphic  a function to provide a graphic for the combo box item, or null if no graphic is required
     * @param items  the initial items to populate the ComboBox (variadic parameter)
     */
    public ComboBoxEx(
            @Nullable Function<T, @Nullable T> edit,
            @Nullable Supplier<@Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Supplier<? extends @Nullable T> dflt,
            Function<? super @Nullable T, @Nullable String> format,
            Function<? super @Nullable T, ? extends @Nullable Node> graphic,
            Collection<T> items
    ) {
        this(edit, add, remove, dflt, format, graphic, item -> null, items);
    }

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, font, and items.
     *
     * @param edit   the unary operator to perform editing on the selected item (nullable)
     * @param add    the supplier to provide a new item to add (nullable)
     * @param remove the bi-predicate to determine if an item should be removed (nullable)
     * @param dflt   the supplier for the default value
     * @param format the function to format the items as strings
     * @param graphic  a function to provide a graphic for the combo box item, or null if no graphic is required
     * @param font   a function to provide a font for the combo box item, or null if no font is required
     * @param items  the initial items to populate the ComboBox (variadic parameter)
     */
    @SafeVarargs
    public ComboBoxEx(
            @Nullable Function<T, @Nullable T> edit,
            @Nullable Supplier<@Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Supplier<? extends @Nullable T> dflt,
            Function<? super @Nullable T, @Nullable String> format,
            Function<? super @Nullable T, ? extends @Nullable Node> graphic,
            Function<? super @Nullable T, ? extends javafx.scene.text.@Nullable Font> font,
            T... items
    ) {
        this(edit, add, remove, dflt, format, graphic, font, LangUtil.asUnmodifiableList(items));
    }

    /**
     * Constructs a ComboBoxEx with the specified edit, add, remove, format, font, and items.
     *
     * @param edit   the unary operator to perform editing on the selected item (nullable)
     * @param add    the supplier to provide a new item to add (nullable)
     * @param remove the bi-predicate to determine if an item should be removed (nullable)
     * @param dflt   the supplier for the default value
     * @param format the function to format the items as strings
     * @param graphic  a function to provide a graphic for the combo box item, or null if no graphic is required
     * @param font   a function to provide a font for the combo box item, or null if no font is required
     * @param items  the initial items to populate the ComboBox (variadic parameter)
     */
    public ComboBoxEx(
            @Nullable Function<T, @Nullable T> edit,
            @Nullable Supplier<@Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, T> remove,
            Supplier<? extends @Nullable T> dflt,
            Function<? super @Nullable T, @Nullable String> format,
            Function<? super @Nullable T, ? extends @Nullable Node> graphic,
            Function<? super @Nullable T, ? extends javafx.scene.text.@Nullable Font> font,
            Collection<T> items
    ) {
        super(new HBox());
        container.setAlignment(Pos.BASELINE_LEFT);

        getStyleClass().setAll("comboboxex");

        this.format = format;
        this.graphic = graphic;
        this.font = font;
        this.items = FXCollections.observableArrayList(List.copyOf(items));
        this.dflt = dflt;
        container.setFillHeight(false);

        this.comboBox = new ComboBox<>(this.items);
        ObservableList<Node> children = container.getChildren();
        children.setAll(comboBox);

        this.edit = edit;
        if (edit != null) {
            Button buttonEdit = Controls.button().text(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.edit")).action(this::editItem).build();
            children.add(buttonEdit);
            buttonEdit.disableProperty().bind(comboBox.selectionModelProperty().isNull());
        }

        this.add = add;
        if (add != null) {
            Button buttonAdd = Controls.button().text(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.add")).action(this::addItem).build();
            children.add(buttonAdd);
        }

        this.remove = remove;
        if (remove != null) {
            Button buttonRemove = Controls.button().text(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.remove")).action(this::removeItem).build();
            children.add(buttonRemove);
            buttonRemove.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> comboBox.getSelectionModel().getSelectedItem() != null && this.items.size() > 1,
                    comboBox.selectionModelProperty(), this.items)
            );
            buttonRemove.disableProperty().bind(comboBox.selectionModelProperty().isNull().or(comboBox.valueProperty().isNull()));
        }

        comboBox.setButtonCell(createCell(true));
        comboBox.setCellFactory(lv -> createCell(false));

        comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (comboBox.isEditable()) {
                String expected = newValue != null ? (comboBox.getConverter() != null ? comboBox.getConverter().toString(newValue) : String.valueOf(newValue)) : "";
                if (!java.util.Objects.equals(comboBox.getEditor().getText(), expected)) {
                    comboBox.getEditor().setText(expected);
                }
            }
        });

        comboBox.getEditor().focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused && comboBox.isEditable()) {
                String text = comboBox.getEditor().getText();
                T current = comboBox.getValue();
                String currentText = current != null ? (comboBox.getConverter() != null ? comboBox.getConverter().toString(current) : String.valueOf(current)) : "";
                if (!java.util.Objects.equals(text, currentText)) {
                    try {
                        commitValue();
                    } catch (Exception e) {
                        LOG.warn("error committing value on focus loss", e);
                        comboBox.getEditor().setText(currentText);
                    }
                }
            }
        });

        comboBox.setValue(this.dflt.get());
    }

    private ListCell<@Nullable T> createCell(boolean isButtonCell) {
        return new ListCell<>() {
            private final javafx.scene.text.Font defaultFont = getFont();

            @Override
            protected void updateItem(@Nullable T item, boolean empty) {
                super.updateItem(item, empty);

                String text = "";
                Node node = null;
                javafx.scene.text.Font itemFont = null;
                if (!empty) {
                    try {
                        text = format.apply(item);
                    } catch (Exception e) {
                        LOG.warn("error during formatting", e);
                        text = String.valueOf(item);
                    }
                    try {
                        node = graphic.apply(item);
                    } catch (Exception e) {
                        LOG.warn("error during formatting", e);
                        text = String.valueOf(item);
                    }
                    if (!isButtonCell) {
                        try {
                            itemFont = font.apply(item);
                        } catch (Exception e) {
                            LOG.warn("error during formatting", e);
                        }
                    }
                }
                setText(text);
                setGraphic(node);
                setFont(itemFont != null ? itemFont : defaultFont);
            }
        };
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
                // check for duplicates
                int idxExisting = items.indexOf(item);
                if (idxExisting >= 0 && idx != idxExisting) {
                    Dialogs.alert(getScene().getWindow(), Alert.AlertType.CONFIRMATION, MessageFormatter.standard())
                            .title(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.title"))
                            .header(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.header"))
                            .text(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.remove.text"))
                            .buttons(ButtonType.YES, ButtonType.NO)
                            .defaultButton(ButtonType.NO)
                            .showAndWait()
                            .ifPresent(btn -> {
                                if (btn == ButtonType.OK || btn == ButtonType.YES) {
                                    comboBox.getSelectionModel().select(idxExisting);
                                    items.remove(idx);
                                }
                            });
                    return;
                }

                // replace item
                items.remove(idx);
                items.add(idx, item);
                comboBox.getSelectionModel().select(idx);
                sortItems();
            }
        }
    }

    private void addItem() {
        Optional.ofNullable(add).map(Supplier::get).ifPresent((T item) -> {
            int idxExisting = items.indexOf(item);
            if (idxExisting >= 0) {
                Dialogs.alert(getScene().getWindow(), Alert.AlertType.INFORMATION, MessageFormatter.standard())
                        .title(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.title"))
                        .header(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.header"))
                        .text(I18NInstance.get().get("dua3.utility.fx.controls.combobox.ex.duplicate.item.select.text"))
                        .showAndWait();
                comboBox.getSelectionModel().select(idxExisting);
                return;
            }
            items.add(item);
            comboBox.getSelectionModel().select(item);
            sortItems();
        });
    }

    private void removeItem() {
        T item = comboBox.getSelectionModel().getSelectedItem();
        //noinspection DataFlowIssue
        if (Optional.ofNullable(remove).orElse(ComboBoxEx::alwaysRemoveSelectedItem).test(this, item)) {
            int idx = items.indexOf(item);
            items.remove(idx);
            idx = Math.min(idx, items.size() - 1);
            if (idx >= 0) {
                comboBox.setValue(items.get(idx));
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
        return Dialogs.alert(Optional.ofNullable(getScene()).map(Scene::getWindow).orElse(null), Alert.AlertType.CONFIRMATION, MessageFormatter.standard())
                .header(I18NInstance.get().format("dua3.utility.fx.controls.combobox.ex.remove.item.header", format.apply(item)))
                .buttons(ButtonType.YES, ButtonType.NO)
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
    @SuppressWarnings("java:S3400")
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
     * Adds an item to the ComboBoxEx values if it is not already present.
     *
     * @param item item to add
     * @return {@code true} if the item was added, {@code false} if it was already present
     */
    public boolean addValue(T item) {
        if (items.contains(item)) {
            return false;
        }
        items.add(item);
        if (comparator != null) {
            sortItems();
        }
        return true;
    }

    /**
     * Returns whether the combo box is editable.
     *
     * @return true if editable, false otherwise
     */
    public boolean isEditable() {
        return comboBox.isEditable();
    }

    /**
     * Sets whether the combo box is editable.
     *
     * @param editable true to make the combo box editable, false otherwise
     */
    public void setEditable(boolean editable) {
        comboBox.setEditable(editable);
        if (editable && comboBox.getValue() != null) {
            T val = comboBox.getValue();
            String text = comboBox.getConverter() != null ? comboBox.getConverter().toString(val) : String.valueOf(val);
            comboBox.getEditor().setText(text);
        }
    }

    /**
     * Returns the editable property of the combo box.
     *
     * @return the editable property
     */
    public BooleanProperty editableProperty() {
        return comboBox.editableProperty();
    }

    /**
     * Retrieves the StringConverter for the combo box.
     *
     * @return the StringConverter
     */
    public @Nullable StringConverter<@Nullable T> getConverter() {
        return comboBox.getConverter();
    }

    /**
     * Sets the StringConverter for the combo box.
     *
     * @param converter the StringConverter to set
     */
    public void setConverter(@Nullable StringConverter<@Nullable T> converter) {
        comboBox.setConverter(converter);
        if (comboBox.isEditable() && comboBox.getValue() != null) {
            T val = comboBox.getValue();
            String text = converter != null ? converter.toString(val) : String.valueOf(val);
            comboBox.getEditor().setText(text);
        }
    }

    /**
     * Returns the StringConverter property of the combo box.
     *
     * @return the converter property
     */
    public ObjectProperty<@Nullable StringConverter<@Nullable T>> converterProperty() {
        return comboBox.converterProperty();
    }

    /**
     * Returns the editor TextField when the combo box is editable.
     *
     * @return the editor TextField
     */
    public TextField getEditor() {
        return comboBox.getEditor();
    }

    /**
     * Returns the editor property of the combo box.
     *
     * @return the editor property
     */
    public ReadOnlyObjectProperty<TextField> editorProperty() {
        return comboBox.editorProperty();
    }

    /**
     * Commits the current value from the editor if editable.
     */
    public void commitValue() {
        comboBox.commitValue();
    }

    /**
     * Cancels editing and reverts the editor text to the current value.
     */
    public void cancelEdit() {
        comboBox.cancelEdit();
    }

    /**
     * Sets the preferred number of columns for the editor in the combo box.
     *
     * @param columns the preferred number of columns
     */
    public void setColumns(int columns) {
        comboBox.getEditor().setPrefColumnCount(columns);
    }

    /**
     * Returns the preferred number of columns for the editor in the combo box.
     *
     * @return the preferred number of columns
     */
    public int getColumns() {
        return comboBox.getEditor().getPrefColumnCount();
    }

    /**
     * Returns the property representing the preferred number of text columns in the editor.
     *
     * @return the preferred column count property
     */
    public IntegerProperty columnsProperty() {
        return comboBox.getEditor().prefColumnCountProperty();
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
     * Sorts the items in the ComboBoxEx using the current Comparator.
     * If the current comparator is null, natural order is used.
     * The selected item is preserved after the sorting.
     */
    public void sortItems() {
        T currentVal = comboBox.getValue();
        items.sort(LangUtil.orNaturalOrder(comparator));
        if (currentVal != null) {
            comboBox.selectionModelProperty().get().select(currentVal);
        }
    }

    /**
     * Returns the property containing the value of the ComboBoxEx.
     *
     * @return the Property representing the value of the ComboBoxEx
     */
    public Property<@Nullable T> valueProperty() {
        return comboBox.valueProperty();
    }
}
