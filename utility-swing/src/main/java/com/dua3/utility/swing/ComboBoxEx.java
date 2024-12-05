package com.dua3.utility.swing;

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
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * A custom JPanel-based ComboBox component with additional functionality for editing, adding, and removing items.
 *
 * @param <T> the type of the items in the ComboBox
 */
public class ComboBoxEx<T> extends JPanel {
    private static final Logger LOG = LogManager.getLogger(ComboBoxEx.class);

    private @Nullable Comparator<? super T> comparator;
    private final @Nullable UnaryOperator<T> edit;
    private final @Nullable Supplier<? extends T> add;
    private final @Nullable BiPredicate<ComboBoxEx<T>, @Nullable T> remove;
    private final Function<T, String> format;
    private final DefaultComboBoxModel<T> model;
    private final JComboBox<T> comboBox;
    private final @Nullable JButton buttonEdit;
    private final @Nullable JButton buttonAdd;
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
    public ComboBoxEx(@Nullable UnaryOperator<T> edit, @Nullable Supplier<? extends T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, T... items) {
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
            if (item != null) {
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

    private void removeItem() {
        //noinspection unchecked
        T item = (T) model.getSelectedItem();
        if (Optional.ofNullable(remove).orElse(ComboBoxEx::alwaysRemoveSelectedItem).test(this, item)) {
            model.removeElement(item);
        }
    }

    /**
     * Method to be passed as a method reference in the {@code remove} parameter to the ComboBoxEx construvtor.
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
                "",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return rc == JOptionPane.YES_OPTION;
    }

    /**
     * Method to be passed as a method reference in the {@code remove} parameter to the ComboBoxEx construvtor.
     * Always remove selected item from the given ComboBoxEx without asking for user confirmation.
     *
     * @param <T> the type of the items in the ComboBox
     * @param cb the ComboBoxEx from which the item should be removed
     * @param item the item to be removed
     * @return always returns true to indicate removal is allowed
     */
    @SuppressWarnings("SameReturnValue")
    public static <T> boolean alwaysRemoveSelectedItem(ComboBoxEx<T> cb, @Nullable T item) {
        return true;
    }

    /**
     * Retrieves the currently selected item from the ComboBoxEx.
     *
     * @return an Optional containing the selected item, or an empty Optional if no item is selected
     */
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
        return items;
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
     * If no Comparator is set, the method does nothing.
     * The selected item is preserved after the sorting.
     */
    public void sortItems() {
        if (comparator == null) {
            return;
        }

        Optional<T> selectedItem = getSelectedItem();
        int n = model.getSize();
        List<T> elements = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            elements.add(model.getElementAt(i));
        }
        elements.sort(comparator);
        model.removeAllElements();
        model.addAll(elements);

        selectedItem.ifPresent(model::setSelectedItem);
    }
}
