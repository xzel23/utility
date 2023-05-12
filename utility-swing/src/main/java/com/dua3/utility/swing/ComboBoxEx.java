package com.dua3.utility.swing;

import com.dua3.cabe.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ComboBoxEx<T> extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(ComboBoxEx.class);

    private Comparator<? super T> comparator = null;
    private final UnaryOperator<T> edit;
    private final Supplier<T> add;
    private final BiPredicate<ComboBoxEx<T>, T> remove;
    private final Function<T, String> format;
    private final DefaultComboBoxModel<T> model;
    private final JComboBox<T> comboBox;
    private final JButton buttonEdit;
    private final JButton buttonAdd;
    private final JButton buttonRemove;

    /**
     * Constructor.
     */
    @SafeVarargs
    public ComboBoxEx(@Nullable UnaryOperator<T> edit, @Nullable Supplier<T> add, @Nullable BiPredicate<ComboBoxEx<T>, T> remove, Function<T, String> format, T... items) {
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

        ListCellRenderer<? super T> renderer = new BasicComboBoxRenderer() {
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
        buttonEdit.setEnabled(model.getSelectedItem() != null);
        buttonAdd.setEnabled(true);
        buttonRemove.setEnabled(model.getSize() > 1 && model.getSelectedItem() != null);
    }

    private void editItem() {
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
        Optional.ofNullable(add.get()).ifPresent(item -> {
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

    public static <T> boolean alwaysRemoveSelectedItem(ComboBoxEx<T> cb, T item) {
        return true;
    }

    public Optional<T> getSelectedItem() {
        //noinspection unchecked
        return Optional.ofNullable((T) comboBox.getSelectedItem());
    }

    public List<T> getItems() {
        int n = model.getSize();
        ArrayList<T> items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            items.add(model.getElementAt(i));
        }
        return items;
    }

    public void setSelectedItem(T item) {
        comboBox.setSelectedItem(item);
    }

    public void insertItemAt(T item, int index) {
        comboBox.insertItemAt(item, index);
    }

    public void addActionListener(ActionListener listener) {
        comboBox.addActionListener(listener);
    }

    public void addItemListener(ItemListener listener) {
        comboBox.addItemListener(listener);
    }

    public void addPopupMenuListener(PopupMenuListener listener) {
        comboBox.addPopupMenuListener(listener);
    }

    public void removeActionListener(ActionListener listener) {
        comboBox.removeActionListener(listener);
    }

    public void removeItemListener(ItemListener listener) {
        comboBox.removeItemListener(listener);
    }

    public void removePopupMenuListener(PopupMenuListener listener) {
        comboBox.removePopupMenuListener(listener);
    }

    public void setComparator(Comparator<? super T> comparator) {
        this.comparator = comparator;
        sortItems();
    }

    public void sortItems() {
        if (comparator==null) {
            return;
        }

        Optional<T> selectedItem = getSelectedItem();
        int n = model.getSize();
        List<T> elements = new ArrayList<>(n);
        for (int i = 0; i<n; i++) {
            elements.add(model.getElementAt(i));
        }
        elements.sort(comparator);
        model.removeAllElements();
        model.addAll(elements);

        selectedItem.ifPresent(model::setSelectedItem);
    }
}
