package com.dua3.utility.swing;

import com.dua3.utility.lang.LangUtil;
import org.jspecify.annotations.Nullable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A custom JPanel-based ComboBox component with additional functionality for editing, adding, and removing items.
 *
 * @param <T> the type of the items in the ComboBox
 */
public class ComboBoxEx<T> extends JPanel {
    private static final Logger LOG = LogManager.getLogger(ComboBoxEx.class);

    /**
     * A transient Comparator used to define the order of items in the ComboBoxEx.
     * If non-null, the items in the ComboBoxEx will be sorted using this Comparator.
     * If null, the natural order of the items will be used by default.
     */
    private transient @Nullable Comparator<? super T> comparator;
    /**
     * A function that provides editing capability for items in the ComboBoxEx.
     * The function takes an item as input and returns the modified item. If editing is disabled,
     * this function is null. If the function returns null for a given input, the item remains unchanged.
     * <p>
     * This variable allows customization of how items are edited. The intended use is to provide a
     * function that when alled displays an editor where the item value can be editid.
     * <p>
     * The type parameter {@code T} corresponds to the type of the items in the ComboBoxEx.
     */
    private final transient @Nullable Function<T, @Nullable T> edit;

    /**
     * A Supplier that provides new items to be added to the ComboBoxEx.
     * This supplier can be used to dynamically generate, fetch content,
     * or show a dialog where the user can add a new item value.
     * <p>
     * The value is optional and can be null, indicating that adding new
     * items is not enabled or supported. If non-null, the supplier
     * returns a possibly null item of type {@code T} when invoked.
     * <p>
     * If the function returns {@code }
     * The type parameter {@code T} corresponds to the type of the items in the ComboBoxEx.
     */
    private final transient @Nullable Supplier<? extends @Nullable T> add;
    /**
     * A BiPredicate that determines whether an item can be removed from the ComboBoxEx.
     * This predicate is used as part of the "removing" functionality in the ComboBoxEx component.
     * If it evaluates to true for a given ComboBoxEx instance and item, the removal is allowed.
     * Otherwise, the removal is disallowed.
     * <p>
     * The first parameter of the BiPredicate is the ComboBoxEx instance from which the item
     * might be removed, and the second parameter is the item to be potentially removed.
     * <p>
     * If null, removing functionality is disabled in the ComboBoxEx.
     */
    private final transient @Nullable BiPredicate<ComboBoxEx<T>, ? super @Nullable T> remove;
    /**
     * A Function used to format the items in the ComboBoxEx as strings.
     * This formatting is applied to each item displayed in the dropdown or selected in the ComboBoxEx.
     * It is passed as a parameter to the constructor of the ComboBoxEx.
     * <p>
     * The input to this function is an item of type T, and the output is the corresponding string representation.
     * This allows customization of how items are displayed in the ComboBoxEx.
     * <p>
     * Immutable and transient, meaning it cannot be modified after construction of ComboBoxEx
     * and will not be serialized if the ComboBoxEx object is serialized.
     */
    private final transient Function<T, String> format;
    /**
     * The internal data model holding the items for the ComboBoxEx.
     * This model provides the underlying storage and management of the items
     * displayed in the ComboBoxEx, supporting modifications such as adding,
     * removing, or updating items.
     */
    private final DefaultComboBoxModel<T> model;
    /**
     * A JComboBox instance that serves as the primary dropdown component for the ComboBoxEx class.
     * This component allows the selection of items of type {@code T}.
     * <p>
     * It is designed to work in conjunction with the additional features provided by the ComboBoxEx class,
     * such as editing, adding, and removing items, as well as custom item formatting and optional sorting.
     */
    private final JComboBox<T> comboBox;
    /**
     * Represents the "Edit" button within the ComboBoxEx component, which can be used to
     * modify an existing item in the ComboBoxEx. This button may be null if editing
     * functionality is not enabled.
     * <p>
     * The button's availability and behavior are influenced by the editing logic
     * provided during the construction of the ComboBoxEx. When enabled, this button
     * allows users to interact with a selected item in the ComboBoxEx to perform editing
     * operations.
     */
    private final @Nullable JButton buttonEdit;
    /**
     * A button component in the ComboBoxEx that facilitates the addition of new items.
     * <p>
     * This button's functionality is defined by the {@code add} parameter passed to the
     * ComboBoxEx constructor. If the {@code add} parameter is null, this button may be
     * disabled or entirely omitted in the UI.
     * <p>
     * This field is marked as {@code @Nullable} to reflect that the button may not
     * always be initialized, depending on the configuration of the ComboBoxEx instance.
     */
    private final @Nullable JButton buttonAdd;
    /**
     * A button that may be used to trigger the removal of selected items in a ComboBoxEx.
     * This field is optional and may be null, indicating that removal functionality is not enabled
     * or the button has not been initialized.
     */
    private final @Nullable JButton buttonRemove;

    /**
     * Constructs a ComboBoxEx with optional editing, adding, and removing functionality.
     *
     * @param edit a UnaryOperator that allows editing of items in the ComboBoxEx, or null if editing is not enabled
     * @param add a Supplier that supplies new items to add to the ComboBoxEx, or null if adding is not enabled
     * @param remove a BiPredicate that determines if an item can be removed from the ComboBoxEx, or null if removing is not enabled
     * @param format a Function that formats each item in the ComboBoxEx as a string
     * @param items the initial items to populate the ComboBoxEx with
     */
    @SafeVarargs
    public ComboBoxEx(
            @Nullable Function<T, @Nullable T> edit,
            @Nullable Supplier<? extends @Nullable T> add,
            @Nullable BiPredicate<ComboBoxEx<T>, ? super @Nullable T> remove,
            Function<T, String> format, T... items
    ) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        this.format = format;
        this.model = new DefaultComboBoxModel<>(items);

        this.comboBox = new JComboBox<>(model);
        add(comboBox);

        if (edit != null) {
            this.edit = edit;
            this.buttonEdit = new JButton(SwingUtil.createAction("✎", this::editItem));
            add(buttonEdit);
            comboBox.addItemListener(item -> buttonEdit.setEnabled(item != null));
        } else {
            this.edit = null;
            this.buttonEdit = null;
        }

        if (add != null) {
            this.add = add;
            this.buttonAdd = new JButton(SwingUtil.createAction("+", this::addItem));
            add(buttonAdd);
        } else {
            this.add = null;
            this.buttonAdd = null;
        }

        if (remove != null) {
            this.remove = remove;
            this.buttonRemove = new JButton(SwingUtil.createAction("-", this::removeItem));
            add(buttonRemove);
        } else {
            this.remove = null;
            this.buttonRemove = null;
        }

        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                updateButtonStates();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                updateButtonStates();
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                updateButtonStates();
            }
        });

        updateButtonStates();

        ListCellRenderer<? super @Nullable T> renderer = new BasicComboBoxRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, @Nullable Object value, int index, boolean isSelected, boolean cellHasFocus) {
                String text = "";
                if (value != null) {
                    try {
                        //noinspection unchecked
                        text = format.apply((T) value);
                    } catch (Exception e) {
                        LOG.warn("error during formatting", e);
                        text = String.valueOf(value);
                    }
                }

                setText(text);
                if (isSelected) {
                    setBackground(UIManager.getColor("ComboBox.selectionBackground"));
                    setForeground(UIManager.getColor("ComboBox.selectionForeground"));
                } else {
                    setBackground(UIManager.getColor("ComboBox.background"));
                    setForeground(UIManager.getColor("ComboBox.foreground"));
                }
                return this;
            }
        };
        comboBox.setRenderer(renderer);
    }

    private void updateButtonStates() {
        if (buttonEdit != null) {
            buttonEdit.setEnabled(model.getSelectedItem() != null);
        }
        if (buttonAdd != null) {
            buttonAdd.setEnabled(true);
        }
        if (buttonRemove != null) {
            buttonRemove.setEnabled(model.getSize() > 1 && model.getSelectedItem() != null);
        }
    }

    private void editItem() {
        if (edit == null) {
            LOG.warn("editing not supported");
            return;
        }

        int idx = comboBox.getSelectedIndex();
        if (idx >= 0) {
            T item = model.getElementAt(idx);
            item = edit.apply(item);

            // check for duplicates
            if (item != null) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (i != idx && item.equals(model.getElementAt(i))) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Duplicate item: " + format.apply(item),
                                "Edit Values",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }

                // replace element
                model.removeElementAt(idx);
                model.insertElementAt(item, idx);
                model.setSelectedItem(item);
                sortItems();
            }
        }
    }

    private void addItem() {
        Optional.ofNullable(add).map(Supplier::get).ifPresent(item -> {
            model.addElement(item);
            model.setSelectedItem(item);
            sortItems();
        });
    }

    @SuppressWarnings("unchecked")
    private void removeItem() {
        //noinspection unchecked
        T item = (T) model.getSelectedItem();
        if (Objects.requireNonNullElse(remove, ComboBoxEx::alwaysRemoveSelectedItem).test(this, item)) {
            model.removeElement(item);
        }
    }

    /**
     * Method to be passed as a method reference in the {@code remove} parameter to the ComboBoxEx constructor.
     * Asks the user to confirm the removal of a selected item from the given ComboBoxEx.
     *
     * @param <T> the type of the items in the ComboBox
     * @param cb the ComboBoxEx from which the item should be removed
     * @param item the item to be removed
     * @return true if the user confirms the removal, false otherwise
     */
    public static <T> boolean askBeforeRemoveSelectedItem(ComboBoxEx<T> cb, T item) {
        int rc = JOptionPane.showConfirmDialog(
                cb,
                "Remove " + cb.format.apply(item) + "?",
                "Edit Values",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return rc == JOptionPane.YES_OPTION;
    }

    /**
     * Method to be passed as a method reference in the {@code remove} parameter to the ComboBoxEx constructor.
     * Always remove selected item from the given ComboBoxEx without asking for user confirmation.
     *
     * @param <T> the type of the items in the ComboBox
     * @param cb the ComboBoxEx from which the item should be removed
     * @param item the item to be removed
     * @return always returns true to indicate removal is allowed
     */
    @SuppressWarnings("SameReturnValue")
    public static <T> boolean alwaysRemoveSelectedItem(ComboBoxEx<T> cb, @Nullable Object item) {
        return true;
    }

    /**
     * Retrieves the currently selected item from the ComboBoxEx.
     *
     * @return an Optional containing the selected item, or an empty Optional if no item is selected
     */
    @SuppressWarnings("unchecked")
    public Optional<T> getSelectedItem() {
        //noinspection unchecked
        return Optional.ofNullable((@Nullable T) comboBox.getSelectedItem());
    }

    /**
     * Retrieves the list of items in the ComboBoxEx.
     *
     * @return a List containing all the items in the ComboBoxEx
     */
    public List<T> getItems() {
        int n = model.getSize();
        ArrayList<T> items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(model.getElementAt(i));
        }
        return List.copyOf(items);
    }

    /**
     * Sets the selected item in the ComboBoxEx to the specified item.
     *
     * @param item the item to be selected in the ComboBoxEx
     */
    public void setSelectedItem(T item) {
        comboBox.setSelectedItem(item);
    }

    /**
     * Inserts the specified item at the specified position in the ComboBoxEx.
     *
     * @param item  the item to be inserted in the ComboBoxEx
     * @param index the index at which the item is to be inserted
     */
    public void insertItemAt(T item, int index) {
        comboBox.insertItemAt(item, index);
    }

    /**
     * Adds an ActionListener to the ComboBoxEx.
     *
     * @param listener the ActionListener to be added to the ComboBoxEx
     */
    public void addActionListener(ActionListener listener) {
        comboBox.addActionListener(listener);
    }

    /**
     * Adds an ItemListener to the ComboBoxEx.
     *
     * @param listener the ItemListener to be added to the ComboBoxEx
     */
    public void addItemListener(ItemListener listener) {
        comboBox.addItemListener(listener);
    }

    /**
     * Adds a PopupMenuListener to the ComboBoxEx.
     *
     * @param listener the PopupMenuListener to be added to the ComboBoxEx
     */
    public void addPopupMenuListener(PopupMenuListener listener) {
        comboBox.addPopupMenuListener(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <L extends EventListener> L[] getListeners(Class<L> listenerType) {
        if (listenerType == ActionListener.class) {
            return (L[]) comboBox.getActionListeners();
        }
        if (listenerType == ItemListener.class) {
            return (L[]) comboBox.getItemListeners();
        }
        if (listenerType == PopupMenuListener.class) {
            return (L[]) comboBox.getPopupMenuListeners();
        }
        return super.getListeners(listenerType);
    }

    /**
     * Removes an ActionListener from the ComboBoxEx.
     *
     * @param listener the ActionListener to be removed from the ComboBoxEx
     */
    public void removeActionListener(ActionListener listener) {
        comboBox.removeActionListener(listener);
    }

    /**
     * Removes an ItemListener from the ComboBoxEx.
     *
     * @param listener the ItemListener to be removed from the ComboBoxEx
     */
    public void removeItemListener(ItemListener listener) {
        comboBox.removeItemListener(listener);
    }

    /**
     * Removes a PopupMenuListener from the ComboBoxEx.
     *
     * @param listener the PopupMenuListener to be removed from the ComboBoxEx
     */
    public void removePopupMenuListener(PopupMenuListener listener) {
        comboBox.removePopupMenuListener(listener);
    }

    /**
     * Sets the Comparator used for sorting items in the ComboBoxEx.
     *
     * @param comparator the Comparator to be set for sorting items in the ComboBoxEx
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
        Optional<T> selectedItem = getSelectedItem();
        int n = model.getSize();
        List<T> elements = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            elements.add(model.getElementAt(i));
        }
        elements.sort(LangUtil.orNaturalOrder(comparator));
        model.removeAllElements();
        model.addAll(elements);

        selectedItem.ifPresent(model::setSelectedItem);
    }
}
