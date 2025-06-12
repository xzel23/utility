package com.dua3.utility.swing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ComboBoxEx class.
 * <p>
 * These tests focus on the component logic without requiring a full GUI setup.
 * They run in headless mode and test the core functionality of the ComboBoxEx component.
 */
class ComboBoxExTest {

    private ComboBoxEx<String> comboBox;
    private final String[] initialItems = {"Item 1", "Item 2", "Item 3"};

    @BeforeEach
    void setUp() {
        // Create a ComboBoxEx with String items
        // Use identity function for edit (no change)
        UnaryOperator<String> edit = UnaryOperator.identity();
        // Add function returns a new item
        // Remove function always returns true (allow removal)
        // Format function returns the item itself
        comboBox = new ComboBoxEx<>(
                edit,
                () -> "New Item",
                ComboBoxEx::alwaysRemoveSelectedItem,
                String::toString,
                initialItems
        );
    }

    /**
     * Test that the ComboBoxEx component can be created with initial items.
     */
    @Test
    void testCreation() {
        // Verify the component was created
        assertNotNull(comboBox, "ComboBoxEx should not be null");

        // Verify the initial items were added
        List<String> items = comboBox.getItems();
        assertEquals(initialItems.length, items.size(), "ComboBoxEx should have the correct number of items");
        for (String item : initialItems) {
            assertTrue(items.contains(item), "ComboBoxEx should contain item: " + item);
        }
    }

    /**
     * Test getting and setting the selected item.
     */
    @Test
    void testGetSetSelectedItem() {
        // Set the selected item
        comboBox.setSelectedItem(initialItems[1]);

        // Verify the selected item
        Optional<String> selectedItem = comboBox.getSelectedItem();
        assertTrue(selectedItem.isPresent(), "Selected item should be present");
        assertEquals(initialItems[1], selectedItem.get(), "Selected item should match what was set");

        // Set a different selected item
        comboBox.setSelectedItem(initialItems[2]);

        // Verify the new selected item
        selectedItem = comboBox.getSelectedItem();
        assertTrue(selectedItem.isPresent(), "Selected item should be present");
        assertEquals(initialItems[2], selectedItem.get(), "Selected item should match what was set");
    }

    /**
     * Test adding and removing items.
     */
    @Test
    void testAddRemoveItems() {
        // Get the initial number of items
        int initialCount = comboBox.getItems().size();

        // Insert a new item at a specific position
        String newItem = "Inserted Item";
        comboBox.insertItemAt(newItem, 1);

        // Verify the item was inserted
        List<String> items = comboBox.getItems();
        assertEquals(initialCount + 1, items.size(), "ComboBoxEx should have one more item");
        assertEquals(newItem, items.get(1), "Inserted item should be at the correct position");

        // Set the inserted item as selected
        comboBox.setSelectedItem(newItem);

        // Verify the selected item
        Optional<String> selectedItem = comboBox.getSelectedItem();
        assertTrue(selectedItem.isPresent(), "Selected item should be present");
        assertEquals(newItem, selectedItem.get(), "Selected item should match what was set");
    }

    /**
     * Test sorting items.
     */
    @Test
    void testSortItems() {
        // Create a ComboBoxEx with unsorted items
        String[] unsortedItems = {"C", "A", "B"};
        ComboBoxEx<String> unsortedComboBox = new ComboBoxEx<>(
                UnaryOperator.identity(),
                () -> "New Item",
                ComboBoxEx::alwaysRemoveSelectedItem,
                String::toString,
                unsortedItems
        );

        // Set a comparator and sort the items
        unsortedComboBox.setComparator(Comparator.naturalOrder());
        unsortedComboBox.sortItems();

        // Verify the items are sorted
        List<String> sortedItems = unsortedComboBox.getItems();
        assertEquals("A", sortedItems.get(0), "First item should be 'A'");
        assertEquals("B", sortedItems.get(1), "Second item should be 'B'");
        assertEquals("C", sortedItems.get(2), "Third item should be 'C'");
    }

    /**
     * Test adding and removing listeners.
     */
    @Test
    void testListeners() {
        // Create listeners
        ActionListener actionListener = e -> { /* Do nothing */ };
        ItemListener itemListener = e -> { /* Do nothing */ };
        PopupMenuListener popupMenuListenerListener = new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                // do nothing
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // do nothing
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // do nothing
            }
        };

        int nActionListeners = comboBox.getListeners(ActionListener.class).length;
        int nItemListeners = comboBox.getListeners(ItemListener.class).length;
        int nPopupMenuListeners = comboBox.getListeners(PopupMenuListener.class).length;

        // Add listeners
        comboBox.addActionListener(actionListener);
        assertEquals(nActionListeners + 1, comboBox.getListeners(ActionListener.class).length);

        comboBox.addItemListener(itemListener);
        assertEquals(nItemListeners + 1, comboBox.getListeners(ItemListener.class).length);

        comboBox.addPopupMenuListener(popupMenuListenerListener);
        assertEquals(nPopupMenuListeners + 1, comboBox.getListeners(PopupMenuListener.class).length);

        // Remove listeners
        comboBox.removeActionListener(actionListener);
        assertEquals(nActionListeners, comboBox.getListeners(ActionListener.class).length);

        comboBox.removeItemListener(itemListener);
        assertEquals(nItemListeners, comboBox.getListeners(ItemListener.class).length);

        comboBox.removePopupMenuListener(popupMenuListenerListener);
        assertEquals(nPopupMenuListeners, comboBox.getListeners(PopupMenuListener.class).length);
    }

    /**
     * Test getting all items.
     */
    @Test
    void testGetItems() {
        // Get all items
        List<String> items = comboBox.getItems();

        // Verify the items
        assertEquals(initialItems.length, items.size(), "ComboBoxEx should have the correct number of items");
        for (String item : initialItems) {
            assertTrue(items.contains(item), "ComboBoxEx should contain item: " + item);
        }
    }
}